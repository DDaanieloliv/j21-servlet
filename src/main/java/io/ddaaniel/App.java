package io.ddaaniel;


import io.ddaaniel.channel.LinesChannel;

public class App {
	public static void main(String[] args) {

		var chann = new LinesChannel().getLinesChannel("/home/daniel/personal/dev/httpfromtcp/messages.txt");

		for (;;) {
			try {
				System.out.println(chann.take());
			} catch (InterruptedException e) { e.printStackTrace(); }
			if (chann.size() == 0) break;
		}
	}
}
