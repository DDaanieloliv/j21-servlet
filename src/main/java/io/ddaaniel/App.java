package io.ddaaniel;


import io.ddaaniel.channel.LinesChannel;

public class App {
	public static void main(String[] args) {



		var channel = new LinesChannel().getLineChannel_IptSt("/home/daniel/personal/dev/httpfromtcp/messages.txt");

		for (;;) {
			try {
				System.out.println(channel.take());
			} catch (InterruptedException e) { e.printStackTrace(); }
			if (channel.size() == 0) break;
		}
	}
}
