package io.ddaaniel.tcpListener;

import java.net.InetSocketAddress;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;

/**
 * Reader
 */
public abstract class Reader {

	public static void handleConnection(ReadableByteChannel conn) {
		try {


			conn.close();

		} catch (Exception e) { e.printStackTrace(); }
	}

	// TODO: avoid the usage of "GodObject" - Context
	// TODO: function of unique scope, with Acceptor Loop outside of the function
	// TODO: handleConnection( BlockingQueue<String> ) -> handleConnection(conn)
	public static void handleConnection(BlockingQueue<String> chann) {

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
						var socket = ((ServerSocketChannel) key.channel()).accept();
						socket.configureBlocking(false);

						var queue = chann;
						var context = new Context(queue);

						socket.register(selector, SelectionKey.OP_READ, context);

						// DIRTY STDOUT TO THE LINES IN THE CHANNEL
						new Thread(() -> {
							try {

								while (true) {
									var line = queue.take();
									System.out.println("read: " + line);
								}
								
							} catch (Exception e) { e.printStackTrace(); }
						}).start();
					}

					if (key.isReadable()) {
						var socketChannel = (SocketChannel) key.channel();
						var context = (Context) key.attachment();
						context.attachKeyContext(socketChannel);

						var writer = context;
						ChannelContext.getLinesChannel(writer);
					}
				}
			}

		} catch (Exception e) { e.printStackTrace(); }

	}
}
