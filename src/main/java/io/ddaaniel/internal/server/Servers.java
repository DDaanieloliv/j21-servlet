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
		try {
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

			ByteArrayOutputStream writer = new ByteArrayOutputStream();
			HandlerError handlerError = server.handler.handle(writer, r);
			if (handlerError != null) {
				var errorHeaders = response.GetDefaultHeaders(0); 
				response.WriteStatusLine(conn, handlerError.code);
				response.WriteHeaders(conn, errorHeaders.h);
				conn.write(ByteBuffer.wrap(handlerError.Message.getBytes()));
				return;
			}

			var body = writer.toByteArray();
			headers.Replace("Content-Length", String.valueOf(body.length));

			response.WriteStatusLine(conn, response.r.code = StatusCode.STATUS_OK);
			response.WriteHeaders(conn, headers.h);

			var bodyBuffer = ByteBuffer.wrap(body);
			while (bodyBuffer.hasRemaining()) {
				conn.write(bodyBuffer);
			}

			conn.close();
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
