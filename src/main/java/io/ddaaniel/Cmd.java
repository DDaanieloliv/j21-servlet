package io.ddaaniel;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;

import io.ddaaniel.tcpListener.ChannelContext;

public class Cmd {
	public static void main(String[] args) {


		try {
			var selector = Selector.open();
			var serverChannel = ServerSocketChannel.open();

			serverChannel.configureBlocking(false);
			serverChannel.bind(new InetSocketAddress(42069));
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);

			while (true) {

				selector.select();
				var setKeys = selector.selectedKeys().iterator();

				while (setKeys.hasNext()) {
					var key = setKeys.next();
					setKeys.remove();

					if (key.isAcceptable()) {
						var server = (ServerSocketChannel) key.channel();
						SocketChannel socket = server.accept();
						socket.configureBlocking(false);

						var queue = new LinkedBlockingQueue<String>();
						var contextChannel = new ChannelContext(queue);

						socket.register(selector, SelectionKey.OP_READ, contextChannel);

						new Thread(() -> {
							try {
								var channel = contextChannel.getQueue();
								while (true) {
									String line = channel.take();
									if ("EOF_SIGNAL".equals(line)) break;
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

				break;
			}

		} catch (Exception e) { e.printStackTrace(); }

	}
}
