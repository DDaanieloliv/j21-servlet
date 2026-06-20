package io.ddaaniel.listener;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.ddaaniel.internal.parser.request.Requests;
import io.ddaaniel.internal.parser.request.mapper.Request;
import io.ddaaniel.internal.parser.request.response.Response;
import io.ddaaniel.internal.parser.request.response.status.ResponseStatusCode;
import io.ddaaniel.internal.parser.request.response.util.HttpMsg;
import io.ddaaniel.internal.routing.ReflectionRouter;
import io.ddaaniel.internal.routing.response.ResponseEntity;
import io.ddaaniel.listener.mapper.Server;
import io.ddaaniel.listener.pipe.Handler;


/**
 * Servers
 */
public class Servers {

	private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
	private ServerSocketChannel listener;
	public Server s = new Server();

	public Servers handleConnection(int port) throws Exception {
		var reflectionRouter = new ReflectionRouter();
		var s = new Servers().Serve(port, (req, res) -> {
			try {
				String target = req.RequestLine.RequestTarget;
				ResponseEntity<?> response = reflectionRouter.dispatch(target);

				if (response != null) {
					res.WriteStatusLine(response.getStatus());
					String body = response.getBody() != null ? response.getBody().toString() : "";
					var headersMap = res.DefaultHeaders(body.length()).h;
					if (response.getHeaders() != null)  headersMap.map().putAll(response.getHeaders());
					res.WriteHeaders(headersMap);
					res.WriteBody(body.getBytes());
				} else {
					res.WriteStatusLine(ResponseStatusCode.STATUS_NOT_FOUND);
					res.WriteHeaders(res.DefaultHeaders(0).h);
					res.WriteBody(HttpMsg.respond404().getBytes());
				}
			} catch (Exception e) {
				System.err.println(" -> Reflection Router Error: " + e.getMessage());
				try {
					res.WriteStatusLine(ResponseStatusCode.STATUS_INTERNAL_SERVER_ERROR);
					res.WriteHeaders(res.DefaultHeaders(0).h);
				} catch (Exception ignored) {}
			}

			return;
		});
		return s;
	}


	public void runConnection(Server server, SocketChannel conn) {
		try (conn) {
			var response = new Response(conn);
			var headers = response.DefaultHeaders(0);

			var r = new Request();
			try {
				r = new Requests().RequestFromReader(conn);
			} catch (Exception err) { 
				response.WriteStatusLine(ResponseStatusCode.STATUS_BAD_REQUEST);
				response.WriteHeaders(headers.h);
				return;
			}

			server.handler.handle(r, response);

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
				executor.submit(() -> { runConnection(s, socketChannel); });
			}
		} catch (Exception e) { 
			if (!s.closed) {
				throw new RuntimeException(e); 
			}
		}
	}

	public Servers Serve(int port, Handler handler) throws Exception {
		listener = ServerSocketChannel.open();
		listener.bind(new InetSocketAddress(port));
		s.closed = false;
		s.handler = handler;
		executor.submit(() -> { runServer(listener); } );
		return this;
	}

	public void Close() {
		try {
			s.closed = true;
			if (listener != null && listener.isOpen()) {
				listener.close();
			}
			executor.shutdown();
		} catch (Exception e) { 
			System.err.println(" -> Error when closing server: " + e.getMessage());
		}
	}
}
