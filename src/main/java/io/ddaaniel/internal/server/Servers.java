package io.ddaaniel.internal.server;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.ddaaniel.internal.server.message.Server;

/**
 * Servers
 */
public class Servers {

	private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
	private ServerSocketChannel listener;
	public Server s = new Server();

	public void runConnection(Server server, WritableByteChannel conn) {
		try {
			var out = ByteBuffer.wrap("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\nHello World!`".getBytes());
			conn.write(out);
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

	public Servers Serve(int port) throws Exception {
		listener = ServerSocketChannel.open();
		listener.bind(new InetSocketAddress(port));
		s.closed = false;
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
