package io.ddaaniel;

import java.util.concurrent.LinkedBlockingQueue;

import io.ddaaniel.channel.LinesChannel;

public class App {
	public static void main(String[] args) {

		var lines = new LinkedBlockingQueue<String>();
		var channel = new LinesChannel(lines);
		new Thread(channel).start();

		for (;;) {
			try {
				System.out.println(lines.take());
			} catch (Exception e) { e.printStackTrace(); }
			if (lines.size() == 0) break;
		}
	}
}
