package io.ddaaniel.tcpListener;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;

/**
 * Reader
 */
public abstract class Reader {


	public static void readConn(BlockingQueue<String> chann) {

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
						var contextChannel = new ChannelContext(queue);

						socket.register(selector, SelectionKey.OP_READ, contextChannel);

						new Thread(() -> {
							try {
								var channel = contextChannel.getQueue();
								while (true) {
									String line = channel.take();
									System.out.println("read: " + line);
								}
							} catch (InterruptedException e) { Thread.currentThread().interrupt(); }
						}).start();
					}

					if (key.isReadable()) {
						var socketChannel = (SocketChannel) key.channel();
						var context = (ChannelContext) key.attachment();

						context.getLinesChannel(socketChannel, context);
					}
				}
			}

		} catch (Exception e) { e.printStackTrace(); }

	}
}
