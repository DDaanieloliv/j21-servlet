package io.ddaaniel.listener;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.ddaaniel.core.Handler;
import io.ddaaniel.core.httpStatus.HttpStatus;
import io.ddaaniel.internal.parser.reader.DefaultHttpServletRequest;
import io.ddaaniel.internal.parser.reader.DefaultServletReader;
import io.ddaaniel.internal.parser.writer.DefaultServletWriter;



/**
 * DefaultServletContainer
 */
public class DefaultServletContainer {

	private final ServerSocketChannel listener;

	private final ExecutorService executor;

	private final DefaultServletEntity server;

	public DefaultServletContainer() {
		try {
			this.server = new DefaultServletEntity();
			this.listener = ServerSocketChannel.open();
			this.executor = Executors.newVirtualThreadPerTaskExecutor();
		} catch (Exception e) {
			throw new RuntimeException(" -> Error when create ServletContainer: ", e);
		}
	}

	public DefaultServletContainer hookUp(int port) throws Exception {
		var router = new Router();
		this.attach(port, (message, writer) -> {
			try {

				String target = message.uri();
				router.dispatch(target).ifPresentOrElse(
						(response) -> {
							if (response.getBody() instanceof InputStream bin) {
								if (response.getHeaders().get("Content-Length") == null) {
									writer.writeChunkedStream(bin, response.getHeaders());
								} else {
									writer.writeRegularStream(bin, response.getHeaders());
								}
							} else {
								writer.WriteResponse(response);
							}
						}, 
						() -> writer.WriteErrorResponse());

			} catch (Exception e) {
				System.err.println(" -> Router Error: " + e.getMessage());
				try {
					writer.WriteStatusLine(HttpStatus.INTERNAL_SERVER_ERROR);
					writer.WriteHeaders(writer.DefaultHeaders(0));
				} catch (Exception ignored) {}
			}
			return;
		});
		return this;
	}

	public DefaultServletContainer attach(int port, Handler handler) throws Exception {
		server.closed = false;
		server.forward = handler;
		this.listener.bind(new InetSocketAddress(port));
		executor.submit(() -> { runServer(listener); });
		return this;
	}

	public void runServer(ServerSocketChannel listener) {
		try {
			while (listener.isOpen() && !server.closed) {
				var socketChannel = listener.accept();
				if (server.closed) {
					if (socketChannel != null) socketChannel.close();
					return;
				}
				executor.submit(() -> { handleConnection(server, socketChannel); });
			}
		} catch (Exception e) { if (!server.closed) throw new RuntimeException(e); }
	}

	public void handleConnection(DefaultServletEntity server, SocketChannel conn) {
		try (conn) {
			var reader = new DefaultServletReader(conn);
			var writer = new DefaultServletWriter(conn);

			DefaultHttpServletRequest message;		
			try {
				message = reader.processMessage();
			} catch (Exception err) { 
				var badRequestHeaders = writer.DefaultHeaders(0);
				writer.WriteStatusLine(HttpStatus.BAD_REQUEST);
				writer.WriteHeaders(badRequestHeaders);
				return;
			}
			server.forward.get(message, writer);
		} catch (Exception e) {
			if (!server.closed) { 
				System.err.println(" -> Error in connection: " + e.getMessage()); 
			}
		}
	}

	public void Close() {
		try {
			server.closed = true;
			if (listener != null && listener.isOpen()) listener.close();
			executor.shutdown();
		} catch (Exception e) { 
			System.err.println(" -> Error when closing server: " + e.getMessage());
		}
	}
}
