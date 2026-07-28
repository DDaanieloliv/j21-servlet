package io.ddaaniel.listener;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.ddaaniel.core.Handler;
import io.ddaaniel.core.filter.DefaultHttpFilterChain;
import io.ddaaniel.core.filter.Filter;
import io.ddaaniel.core.filter.FilterChain;
import io.ddaaniel.core.httpStatus.HttpStatus;
import io.ddaaniel.internal.parser.reader.DefaultHttpServletRequest;
import io.ddaaniel.internal.parser.reader.ServletReader;
import io.ddaaniel.internal.parser.writer.DefaultHttpServletResponse;
import io.ddaaniel.internal.parser.writer.ServletWriter;



/**
 * DefaultServletContainer
 */
public class DefaultServletContainer implements ServletContainer {

	private Handler handler;

	private final ExecutorService executor;

	private final ServerSocketChannel listener;

	private final AtomicBoolean closed = new AtomicBoolean(false);

	private static final Logger log = Logger.getLogger(DefaultServletContainer.class.getName());

	private final List<Filter> filterChain = new ArrayList<>();



	public DefaultServletContainer() {
		try {
			this.listener = ServerSocketChannel.open();
			this.executor = Executors.newVirtualThreadPerTaskExecutor();
		} catch (Throwable e) {
			throw new RuntimeException(" -> Error when create ServletContainer: ", e);
		}
	}

	public DefaultServletContainer addFilter(Filter filter) {
		this.filterChain.add(filter);
		return this;
	}


	@Override
	public DefaultServletContainer hookUp(int port) throws Throwable {
		var router = new CommonRequestRouter();
		return this.attach(port, (req, res) -> {
			if (log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, " -> Dispatching request [{0} {1}]", 
						new Object[]{ req.method(), req.uri() });
			}

			var responseEntity = router.dispatch(req);

			if (responseEntity.isPresent()) {
				res.writeResponse(responseEntity.get());
			} else {
				res.sendError(HttpStatus.NOT_FOUND, "Route not found");
			}
		});
	}

	@Override
	public DefaultServletContainer attach(int port, Handler handler) throws Throwable {
		this.closed.set(false);
		this.handler = handler;
		this.listener.bind(new InetSocketAddress(port));
		log.log(Level.INFO, " -> Server boundary socket bound successfully to port: ", port);
		executor.submit(() -> { runServer(listener); });
		return this;
	}

	public void runServer(ServerSocketChannel listener) {
		try {
			while (listener.isOpen() && !closed.get()) {
				var socketChannel = listener.accept();
				if (closed.get()) {
					if (socketChannel != null) socketChannel.close();
					return;
				}
				executor.submit(() -> { handleConnection(socketChannel); });
			}
		} catch (Throwable e) { 
			if (!closed.get()) {
				log.log(Level.SEVERE, " -> Fatal crash in main TCP accept loop! Server stopped accepting connections. ", e);
			}
		}
	}

	public void handleConnection(SocketChannel conn) {
		try (conn) {
			var reader = new ServletReader(conn);
			var writer = new ServletWriter(conn);

			boolean keepAlive = true;

			while (keepAlive && conn.isOpen() && !closed.get()) {

				Optional<DefaultHttpServletRequest> requestOpt = reader.readConnection();
				if (requestOpt.isEmpty()) break;

				DefaultHttpServletRequest requestWrapper = requestOpt.get();
				DefaultHttpServletResponse responseWrapper = new DefaultHttpServletResponse(writer);

				try {
					FilterChain chain = new DefaultHttpFilterChain(this.filterChain, (req, res) -> {
						handler.get(req, res);
					});
					chain.doFilter(requestWrapper, responseWrapper);

				} catch (Throwable error) {
					handleGlobalError(error, responseWrapper);
				}

				keepAlive = configureConnection(requestWrapper, responseWrapper);

				responseWrapper.flushToSocket();
			}

		} catch (Throwable e) {
			if (!closed.get()) {
				log.log(Level.FINE, " -> Connection closed or network reset: " + e.getMessage());
			}
		}
	}

	private boolean configureConnection(DefaultHttpServletRequest request, DefaultHttpServletResponse response) {
		String reqConnection = request.headers().getFirst("Connection");
		String resConnection = response.getHeaders().getFirst("Connection");

		boolean clientWantsClose = "close".equalsIgnoreCase(reqConnection);
		boolean appWantsClose = "close".equalsIgnoreCase(resConnection) ;

		if (clientWantsClose || appWantsClose) {
			response.setHeader("Connection", "close");
			return false;
		}

		response.setHeader("Connection", "keep-alive");
		response.setHeader("Keep-Alive", "timeout=5, max=1000");

		return true;
	}

	private void handleGlobalError(Throwable error, DefaultHttpServletResponse response) {
		log.log(Level.SEVERE, " -> Unhandled exception during HTTP request processing: ", error);
		try {
			response.sendError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error: " + error.getMessage());
		} catch (Throwable fatal) {
			log.log(Level.SEVERE, " -> Fatal: Failed to format 500 error response", fatal);
		}
	}

	@Override
	public void close() {
		try {
			closed.set(true);
			if (listener != null && listener.isOpen()) listener.close();
			executor.shutdown();
			log.info("ServletContainer shutdown executed cleanly.");
		} catch (Throwable e) { 
			log.log(Level.SEVERE, " -> Error when closing server: ", e);
		}
	}
}
