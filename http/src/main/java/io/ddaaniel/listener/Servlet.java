package io.ddaaniel.listener;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.ddaaniel.internal.httpEntity.entities.ResponseEntity;
import io.ddaaniel.internal.httpStatus.HttpStatus;
import io.ddaaniel.internal.parser.request.Request;
import io.ddaaniel.internal.parser.response.Response;
import io.ddaaniel.internal.parser.util.HttpFun.FunHttp;
import io.ddaaniel.listener.mapper.Server;
import io.ddaaniel.listener.pipe.Handler;
import io.ddaaniel.listener.pipe.routing.Router;


/**
 * Servlet
 */
public class Servlet {

	private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
	private ServerSocketChannel listener;
	public Server s = new Server();

	public Servlet handleConnection(int port) throws Exception {
		var referenceRouter = new Router();
		var s = new Servlet().dispatch(port, (req, res) -> {
			try {
				String target = req.uriWrap;
				ResponseEntity<?> response = referenceRouter.dispatch(target);

				if (response != null) {
					String body = response.getBody() != null ? response.getBody().toString() : "";
					var headersMap = res.DefaultHeaders(body.getBytes().length);
					res.WriteStatusLine(response.getStatusCode());
					if (response.getHeaders() != null)  headersMap.putAll(response.getHeaders());
					res.WriteHeaders(headersMap);
					res.WriteBody(body.getBytes());
				} else {
					res.WriteStatusLine(HttpStatus.NOT_FOUND);
					res.WriteHeaders(res.DefaultHeaders(0));
					res.WriteBody(FunHttp.respond404().getBytes());
				}
			} catch (Exception e) {
				System.err.println(" -> Reflection Router Error: " + e.getMessage());
				try {
					res.WriteStatusLine(HttpStatus.INTERNAL_SERVER_ERROR);
					res.WriteHeaders(res.DefaultHeaders(0));
				} catch (Exception ignored) {}
			}

			return;
		});
		return s;
	}


	public void runConnection(Server server, SocketChannel conn) {
		try (conn) {
			var req = new Request(conn);
			var res = new Response(conn);
			var headers = res.DefaultHeaders(0);
			try {
				req.RequestFromReader();
			} catch (Exception err) { 
				res.WriteStatusLine(HttpStatus.BAD_REQUEST);
				res.WriteHeaders(headers);
				return;
			}
			server.handler.handle(req, res);
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
		} catch (Exception e) { if (!s.closed) throw new RuntimeException(e); }
	}

	public Servlet dispatch(int port, Handler handler) throws Exception {
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
			if (listener != null && listener.isOpen()) listener.close();
			executor.shutdown();
		} catch (Exception e) { 
			System.err.println(" -> Error when closing server: " + e.getMessage());
		}
	}



}
