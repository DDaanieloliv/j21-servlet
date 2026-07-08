package io.ddaaniel.listener;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.ddaaniel.internal.parser.reader.HttpServletRequest;
import io.ddaaniel.internal.parser.reader.ServletReader;
import io.ddaaniel.internal.parser.writer.ServletWriter;
import io.ddaaniel.internal.support.httpStatus.HttpStatus;
import io.ddaaniel.listener.mapper.Server;
import io.ddaaniel.listener.pipe.Handler;
import io.ddaaniel.listener.pipe.routing.Router;


/**
 * Servlet
 */
public class Servlet {

	private final ServerSocketChannel listener;

	private final ExecutorService executor;

	private final Server server;

	public Servlet() {
		try {
			this.server = new Server();
			this.listener = ServerSocketChannel.open();
			this.executor = Executors.newVirtualThreadPerTaskExecutor();
		} catch (Exception e) {
			throw new RuntimeException(" -> Error when create ServletContainer: ", e);
		}
	}

	public Servlet hookUp(int port) throws Exception {
		var router = new Router();
		this.attach(port, (message, writer) -> {
			try {

				String target = message.uri();
				router.dispatch(target).ifPresentOrElse(
						(response) -> writer.WriteResponse(response), 
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

	public Servlet attach(int port, Handler handler) throws Exception {
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

	public void handleConnection(Server server, SocketChannel conn) {
		try (conn) {
			var reader = new ServletReader(conn);
			var writer = new ServletWriter(conn);

			HttpServletRequest message;		
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
