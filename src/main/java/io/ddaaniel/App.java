package io.ddaaniel;

import java.nio.file.Paths;

import io.ddaaniel.channel.LinesChannel;

public class App {
	public static void main(String[] args) {

		var channel = new LinesChannel(Paths.get("/home/daniel/personal/dev/httpfromtcp/messages.txt")).doChannel();

		for (;;) {
			try {
				System.out.println(channel.take());
			} catch (InterruptedException e) { e.printStackTrace(); }
			if (channel.size() == 0) break;
		}
	}
}
