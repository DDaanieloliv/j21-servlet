package io.ddaaniel;

import java.io.IOException;
import java.net.ServerSocket;

import io.ddaaniel.tcpListener.LinesChannel;

public class Cmd {
	public static void main(String[] args) {

		try {
			ServerSocket serverSocket = new ServerSocket(42069);

			// var path = "/home/daniel/DEV_ENV/personal/dev/httpfromtcp/src/test/java/io/ddaaniel/payload/input/messages.txt";
			// var mockData = "A society grows great when\nold men plant trees whose shade\nthey know they shall never sit in.\nEND";
			// var byteStream = new ByteArrayInputStream(mockData.getBytes(StandardCharsets.UTF_8));

			var conn = serverSocket.accept();
			var channel = new LinesChannel().getLinesChannel(conn);


			for (;;) {

				System.out.println("read: " + channel.take());
				if (channel.size() == 0) break;
			}

			serverSocket.close();
		} catch (IOException | InterruptedException e) { e.printStackTrace(); }

	}
}
