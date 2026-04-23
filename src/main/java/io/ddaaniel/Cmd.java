package io.ddaaniel;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.io.BufferedInputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

import io.ddaaniel.tcpListener.LinesChannel;

public class Cmd {
	public static void main(String[] args) {

		try {

			var serverSocket = new ServerSocket(42069);
			var Channel = ServerSocketChannel.open().bind(serverSocket.getLocalSocketAddress());

			// var path = "/home/daniel/DEV_ENV/personal/dev/httpfromtcp/src/test/java/io/ddaaniel/payload/input/rawpost.http";
			// var byteStream = new BufferedInputStream(Files.newInputStream(Path.of(path)));

			// var mockData = "A society grows great when\nold men plant trees whose shade\nthey know they shall never sit in.\nEND";
			// var byteStream = new ByteArrayInputStream(mockData.getBytes(StandardCharsets.UTF_8));

			// var conn = Channel.accept();
			var conn = serverSocket.accept();
			var channel = new LinesChannel().getLinesChannel(conn);


			for (;;) {
				String line = channel.take(); 
				if ("EOF_SIGNAL".equals(line)) {
					break; 
				}
				System.out.println("read: " + line);
			}

			serverSocket.close();
		} catch (IOException | InterruptedException e) { e.printStackTrace(); }

	}
}
