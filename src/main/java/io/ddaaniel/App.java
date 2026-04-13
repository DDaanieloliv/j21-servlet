package io.ddaaniel;


import java.net.ServerSocket;
import java.net.Socket;

import io.ddaaniel.channel.LinesChannel;

public class App {
	public static void main(String[] args) {

		try {
			ServerSocket serverSocket = new ServerSocket(42069);

			for (;;) {
				Socket conn = serverSocket.accept();
				var channel = new LinesChannel().getLineChannel(conn);

				try {
					System.out.println("read: " + channel.take());
				} catch (InterruptedException e) { e.printStackTrace(); }
				if (channel.size() == 0) break;
			}

			serverSocket.close();
		} catch (Exception e) { }

	}
}
