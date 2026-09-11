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
import io.ddaaniel.core.httpEntity.ResponseEntity;
import io.ddaaniel.core.httpEntity.httpHeaders.HttpHeaders;
import io.ddaaniel.core.httpStatus.HttpStatus;
import io.ddaaniel.listener.internal.codec.reader.DefaultHttpServletRequest;
import io.ddaaniel.listener.internal.codec.reader.ServletReader;
import io.ddaaniel.listener.internal.codec.writer.DefaultHttpServletResponse;
import io.ddaaniel.listener.internal.codec.writer.ServletWriter;
import io.ddaaniel.routing.CommonRequestRouter;



/**
 * DefaultServletContainer
 */
public class DefaultServletContainer implements ServletContainer {

	private Handler handler;

	private ServerSocketChannel listener;

	private final ExecutorService executor;

	private final List<Filter> filterChain = new ArrayList<>();

	private final AtomicBoolean closed = new AtomicBoolean(false);

	private static final Logger log = Logger.getLogger(DefaultServletContainer.class.getName());



	public DefaultServletContainer() {
		try {
			this.executor = Executors.newVirtualThreadPerTaskExecutor();
		} catch (Exception e) {
			throw new RuntimeException(" -> Error when create ServletContainer: ", e);
		}
	}

	@Override
	public DefaultServletContainer loadContainer(int port) throws Exception {
		var router = new CommonRequestRouter();
		return this.loadServletContainer(port, (req, res) -> {
			if (log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, " -> Dispatching request [{0} {1}]", 
						new Object[]{ req.method(), req.uri() });
			}
			var responseEntity = router.dispatch(req);
			if (responseEntity.isPresent()) {
				ResponseEntity<?> r = responseEntity.get();
				res.setResponse(r.getBody(), r.getHeaders(), r.getStatusCode());
			} else {
				res.sendError(HttpStatus.NOT_FOUND, "Route not found");
			}
		});
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



	public DefaultServletContainer loadServletContainer(int port, Handler handler) throws Exception {
		this.closed.set(false);
		this.handler = handler;
		this.listener = ServerSocketChannel.open();
		this.listener.bind(new InetSocketAddress(port));

		log.log(Level.INFO, " -> Server boundary socket bound successfully to port: ", port);

		executor.execute(() -> { runServer(listener); });
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
				executor.execute(() -> { 
					handleConnection(socketChannel); 
				});
			}
		} catch (Exception e) { 
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

				Optional<DefaultHttpServletRequest> optionalServletRequest = reader.readFromConnection();
				if (optionalServletRequest.isEmpty()) break;

				DefaultHttpServletRequest requestWrapper = optionalServletRequest.get();
				DefaultHttpServletResponse responseWrapper = new DefaultHttpServletResponse();

				try {
					FilterChain chain = new DefaultHttpFilterChain(this.filterChain, (req, res) -> {
						handler.get(req, res);
					});
					chain.doFilter(requestWrapper, responseWrapper);
				} catch (Exception error) {
					handleGlobalError(error, responseWrapper);
				}

				keepAlive = shouldKeepAlive(requestWrapper, responseWrapper);
				writer.writeResponse(responseWrapper);
			}

		} catch (Exception e) {
			if (!closed.get()) {
				log.log(Level.FINE, " -> Connection closed or network reset: " + e.getMessage());
			}
		}
	}


	private boolean shouldKeepAlive(DefaultHttpServletRequest request, DefaultHttpServletResponse response) {
		String reqConnection = request.headers().getFirst(HttpHeaders.HttpHeadersNames.CONNECTION);
		String resConnection = response.getHeaders().getFirst(HttpHeaders.HttpHeadersNames.CONNECTION);

		boolean clientWantsClose = "close".equalsIgnoreCase(reqConnection);
		boolean appWantsClose = "close".equalsIgnoreCase(resConnection);
		boolean shouldClose = response.getStatus().isError();

		if (clientWantsClose || appWantsClose || shouldClose) {
			response.setHeader(HttpHeaders.HttpHeadersNames.CONNECTION, "close");
			return false;
		}

		response.setHeader(HttpHeaders.HttpHeadersNames.CONNECTION, "keep-alive");

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
}
