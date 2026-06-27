package io.ddaaniel.listener;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.ddaaniel.internal.httpEntity.entities.ResponseEntity;
import io.ddaaniel.internal.httpStatus.HttpStatus;
import io.ddaaniel.internal.parser.reader.ServletReader;
import io.ddaaniel.internal.parser.writer.ServletWriter;
import io.ddaaniel.internal.parser.util.HttpFun.FunHttp;
import io.ddaaniel.listener.mapper.Server;
import io.ddaaniel.listener.pipe.Handler;
import io.ddaaniel.listener.pipe.routing.Router;


/**
 * Servlet
 */
public class Servlet {

	private ServerSocketChannel listener;

	public Server s = new Server();

	private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

	public void Close() {
		try {
			s.closed = true;
			if (listener != null && listener.isOpen()) listener.close();
			executor.shutdown();
		} catch (Exception e) { 
			System.err.println(" -> Error when closing server: " + e.getMessage());
		}
	}

	public Servlet bind(int port) throws Exception {
		var referenceRouter = new Router();
		var s = new Servlet().dispatch(port, (reader, writer) -> {
			try {
				String target = reader.uriWrap;
				ResponseEntity<?> response = referenceRouter.dispatch(target);

				if (response != null) {
					String body = response.getBody() != null ? response.getBody().toString() : "";
					var headersMap = writer.DefaultHeaders(body.getBytes().length);
					writer.WriteStatusLine(response.getStatusCode());
					if (response.getHeaders() != null) headersMap.putAll(response.getHeaders());
					writer.WriteHeaders(headersMap);
					writer.WriteBody(body.getBytes());
				} else {
					writer.WriteStatusLine(HttpStatus.NOT_FOUND);
					writer.WriteHeaders(writer.DefaultHeaders(0));
					writer.WriteBody(FunHttp.respond404().getBytes());
				}
			} catch (Exception e) {
				System.err.println(" -> Router Error: " + e.getMessage());
				try {
					writer.WriteStatusLine(HttpStatus.INTERNAL_SERVER_ERROR);
					writer.WriteHeaders(writer.DefaultHeaders(0));
				} catch (Exception ignored) {}
			}

			return;
		});
		return s;
	}

	public Servlet dispatch(int port, Handler handler) throws Exception {
		listener = ServerSocketChannel.open();
		listener.bind(new InetSocketAddress(port));
		s.closed = false;
		s.handler = handler;
		executor.submit(() -> { runServer(listener); } );
		return this;
	}

	public void handleConnection(Server server, SocketChannel conn) {
		try (conn) {
			var reader = new ServletReader(conn);
			var writer = new ServletWriter(conn);
			var headers = writer.DefaultHeaders(0);
			try {
				reader.RequestFromReader();
			} catch (Exception err) { 
				writer.WriteStatusLine(HttpStatus.BAD_REQUEST);
				writer.WriteHeaders(headers);
				return;
			}
			server.handler.handle(reader, writer);
		} catch (Exception e) {
			if (!server.closed) { 
				System.err.println(" -> Error in connection: " + e.getMessage()); 
			}
		}
	}

	public void runServer(ServerSocketChannel listener) {
		try {
			while (listener.isOpen() && !s.closed) {
				var socketChannel = listener.accept();
				if (s.closed) {
					if (socketChannel != null) socketChannel.close();
					return;
				}
				executor.submit(() -> { handleConnection(s, socketChannel); });
			}
		} catch (Exception e) { if (!s.closed) throw new RuntimeException(e); }
	}
}
