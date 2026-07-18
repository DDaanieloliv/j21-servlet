package io.ddaaniel.listener;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.ddaaniel.core.Handler;
import io.ddaaniel.core.httpStatus.HttpStatus;
import io.ddaaniel.internal.parser.reader.DefaultHttpServletRequest;
import io.ddaaniel.internal.parser.reader.ServletReader;
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

	public DefaultServletContainer() {
		try {
			this.listener = ServerSocketChannel.open();
			this.executor = Executors.newVirtualThreadPerTaskExecutor();
		} catch (Exception e) {
			throw new RuntimeException(" -> Error when create ServletContainer: ", e);
		}
	}

	@Override
	public DefaultServletContainer hookUp(int port) throws Exception {
		var router = new Router();
		this.attach(port, (message, writer) -> {
			try {

				String target = message.uri();
				String method = message.method();
				log.info(String.format(" -> Routing Request: %s %s", method, target));

				router.dispatch(method, target).ifPresentOrElse(
						(response) -> {
							try {
								writer.writeResponse(response);
							} catch (Exception e) {
								log.log(Level.WARNING, " -> Error when writing response ", e);
							}
						},
						() -> writer.writeErrorResponse(HttpStatus.NOT_FOUND));

			} catch (Exception e) {
				log.log(Level.SEVERE, " -> Error when routing: " + e.getMessage(), e);
				try {
					writer.writeErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
				} catch (Exception ignored) {
					log.log(Level.SEVERE, " -> Error when writing error-response: " + e.getMessage(), e);
				}
			}
			return;
		});
		return this;
	}

	@Override
	public DefaultServletContainer attach(int port, Handler handler) throws Exception {
		this.closed.set(false);
		this.handler = handler;
		this.listener.bind(new InetSocketAddress(port));
		log.info(" -> Server boundary socket bound successfully to port: " + port);
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

			DefaultHttpServletRequest message;		
			try {
				message = reader.processMessage();
			} catch (Exception err) { 
				log.log(Level.FINE, " -> Bad request payload received from client: ", err);
				writer.writeErrorResponse(HttpStatus.BAD_REQUEST);
				return;
			}
			handler.get(message, writer);
			log.info(" -> forwarding message to connection");
		} catch (Exception e) {
			if (!closed.get()) { 
				log.log(Level.SEVERE, " -> Error handling client connection lifecycle: " + e.getMessage(), e); 
			}
		}
	}

	@Override
	public void close() {
		try {
			closed.set(true);
			if (listener != null && listener.isOpen()) listener.close();
			executor.shutdown();
			log.info("ServletContainer shutdown executed cleanly.");
		} catch (Exception e) { 
			log.log(Level.SEVERE, " -> Error when closing server: ", e);
		}
	}
}
