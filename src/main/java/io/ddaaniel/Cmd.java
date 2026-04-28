package io.ddaaniel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import io.ddaaniel.tcpListener.LinesChannel;

public class Cmd {
	public static void main(String[] args) {

		try {
			var selector = SelectorProvider.provider().openSelector();
			var chann = ServerSocketChannel.open().bind(new InetSocketAddress(42069));
			var conn = chann.accept();

			var channel = new LinesChannel().getLinesChannel(conn);

			for (;;) {
				String line = channel.take();
				if ("EOF_SIGNAL".equals(line)) break;
				System.out.println("read: " + line);
			}

		} catch (IOException | InterruptedException e) { e.printStackTrace(); }

	}
}
