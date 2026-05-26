package io.ddaaniel.internal.server;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

import io.ddaaniel.internal.server.message.Server;

/**
 * Servers
 */
public class Servers {
	
	public Server server = new Server();

	public void runConnection(WritableByteChannel conn) throws Exception {
		var out = ByteBuffer.wrap("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\nHello World!`".getBytes());
		conn.write(out);
		conn.close();
	}

	public void runServer(ServerSocketChannel listener) throws Exception {
		for (;;) {
			if (!listener.isOpen()) {
				return;
			}

			var server = listener;
			var selector = Selector.open();

			server.configureBlocking(false);
			server.bind(new InetSocketAddress(42069));
			server.register(selector, SelectionKey.OP_ACCEPT);

			selector.select();
			var keySet = selector.selectedKeys().iterator();

			while (keySet.hasNext()) {
				var key = keySet.next();
				keySet.remove();

				if (key.isAcceptable()) {
					var socket = ((ServerSocketChannel) key.channel()).accept();
					socket.configureBlocking(false);
					socket.register(selector, SelectionKey.OP_READ);
				}

				if (key.isReadable()) {
					var socketChannel = (SocketChannel) key.channel();
				}
			}
		}

	}

	public Server Serve(int port) {
		return server;
	}

	public void Close() {
	}
}
