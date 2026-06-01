package io.ddaaniel.listener;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.ddaaniel.internal.parser.request.Requests;
import io.ddaaniel.internal.parser.request.mapper.Request;
import io.ddaaniel.internal.parser.request.response.Response;
import io.ddaaniel.internal.parser.request.response.status.ResponseStatusCode;
import io.ddaaniel.listener.pipe.Handler;
import io.ddaaniel.listener.pipe.Server;


/**
 * Servers
 */
public class Servers {

	private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
	private ServerSocketChannel listener;
	public Server s = new Server();

	public static void handleConnection() {
		try {

			var server = ServerSocketChannel.open();
			server.bind(new InetSocketAddress(42069));
			var request = new Request();
			var headers = new HashMap<String, String>();

			while (true) {
				var socket = server.accept();
				request = new Requests().RequestFromReader(socket);
				headers = request.Headers.h.map();
				break;
			}

			System.out.println("Request Line:");
			System.out.println(" - Method: " + request.RequestLine.Method); 
			System.out.println(" - Target: " + request.RequestLine.RequestTarget);
			System.out.println(" - Version: " + request.RequestLine.HttpVersion);
			System.out.println("Headers:");
			headers.forEach((key, value) -> System.out.println(" - " + key + ": " + value));
			System.out.println("Body:");
			System.out.println(" - " + request.Body + "\n");

		} catch (Exception e) { e.printStackTrace(); }
	}

	public void runConnection(Server server, SocketChannel conn) {
		try (conn) {
			var response = new Response(conn);
			var headers = response.GetDefaultHeaders(0);

			var r = new Request();
			try {
				r = new Requests().RequestFromReader(conn);
			} catch (Exception err) { 
				response.WriteStatusLine(ResponseStatusCode.STATUS_BAD_REQUEST);
				response.WriteHeaders(headers.h);
				return;
			}

			server.handler.handle(response, r);

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
