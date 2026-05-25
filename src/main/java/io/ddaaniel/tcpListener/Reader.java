package io.ddaaniel.tcpListener;


import java.net.InetSocketAddress;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.HashMap;

import io.ddaaniel.internal.parser.request.Requests;
import io.ddaaniel.internal.parser.request.message.Request;

/**
 * Reader
 */
public abstract class Reader {

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





	public static void handleConnection(ReadableByteChannel conn) {
		try {
			var selector = Selector.open();
			var server = ServerSocketChannel.open();

			server.configureBlocking(false);
			server.bind(new InetSocketAddress(42069));
			server.register(selector, SelectionKey.OP_ACCEPT);

			while (true) {

				selector.select();
				var keySet = selector.selectedKeys().iterator();

				while (keySet.hasNext()) {
					var key = keySet.next();
					keySet.remove();

					if (key.isAcceptable()) {
						// var socket = ((ServerSocketChannel) key.channel()).accept();
						// socket.configureBlocking(false);

						// var queue = chann;
						// var context = new Context(queue);

						// socket.register(selector, SelectionKey.OP_READ, context);
					}

					if (key.isReadable()) {
						// var socketChannel = (SocketChannel) key.channel();
						// var context = (Context) key.attachment();
						// context.attachKeyContext(socketChannel);

						// var writer = context;
						// ChannelContext.getLinesChannel(writer);
					}
				}
			}
		} catch (Exception e) { e.printStackTrace(); }

	}
}
