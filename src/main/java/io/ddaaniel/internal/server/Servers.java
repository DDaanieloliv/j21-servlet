package io.ddaaniel.internal.server;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.ddaaniel.internal.parser.request.Requests;
import io.ddaaniel.internal.parser.request.message.Request;
import io.ddaaniel.internal.parser.response.Responses;
import io.ddaaniel.internal.parser.response.message.enumns.StatusCode;
import io.ddaaniel.internal.server.message.Server;


/**
 * Servers
 */
public class Servers {

	private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
	private ServerSocketChannel listener;
	public Server s = new Server();

	public void runConnection(Server server, SocketChannel conn) {
		try (conn) {
			var response = new Responses();
			var headers = response.GetDefaultHeaders(0);

			var r = new Request();
			try {
				r = new Requests().RequestFromReader(conn);
			} catch (Exception err) { 
				if (err != null) {
					response.WriteStatusLine(conn, StatusCode.STATUS_BAD_REQUEST);
					response.WriteHeaders(conn, headers.h);
					return;
				}
			}

			var writer = new ByteArrayOutputStream();
			var handlerError = server.handler.handle(writer, r);
			var status = StatusCode.STATUS_OK;
			var body = writer.toByteArray();

			if (handlerError != null) {
				status = handlerError.code;
				body = handlerError.Message.getBytes();
			} else {
				body = writer.toByteArray();
			}

			headers.Replace("Content-Length", String.valueOf(body.length));

			response.WriteStatusLine(conn, status);
			response.WriteHeaders(conn, headers.h);

			var bodyBuffer = ByteBuffer.wrap(body);
			while (bodyBuffer.hasRemaining()) {
				conn.write(bodyBuffer);
			}

		} catch (Exception e) {
			if (!server.closed) { System.err.println(" -> Error in connection: " + e.getMessage()); }
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
