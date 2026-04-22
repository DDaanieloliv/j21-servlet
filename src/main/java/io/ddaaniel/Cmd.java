package io.ddaaniel;


import java.io.IOException;
import java.net.ServerSocket;

import io.ddaaniel.tcpListener.LinesChannel;

public class Cmd {
	public static void main(String[] args) {

		try {
			ServerSocket serverSocket = new ServerSocket(42069);
			var conn = "/home/daniel/DEV_ENV/personal/dev/httpfromtcp/src/test/java/io/ddaaniel/payload/input/rawget.http";
			var channel = new LinesChannel().getLinesChannel(conn);


			for (;;) {

				System.out.println("read: " + channel.take());
				if (channel.size() == 0) break;
			}

			serverSocket.close();
		} catch (IOException | InterruptedException e) { e.printStackTrace(); }

	}
}
