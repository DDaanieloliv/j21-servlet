package io.ddaaniel;


import java.io.IOException;
import java.net.ServerSocket;

import io.ddaaniel.tcpListener.LinesChannel;

public class Cmd {
	public static void main(String[] args) {

		try {
			ServerSocket serverSocket = new ServerSocket(42069);
			var conn = serverSocket.accept();
			var channel = new LinesChannel().getLineChannel(conn);



			for (;;) {

				System.out.println("read: " + channel.take());
				if (channel.size() == 0) break;
			}

			serverSocket.close();
		} catch (IOException | InterruptedException e) { }

	}
}
