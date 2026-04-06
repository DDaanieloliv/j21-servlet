package io.ddaaniel;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import io.ddaaniel.header.HttpMsg;

public class App {
	public static void main(String[] args) {

		BlockingQueue<String> q = new LinkedBlockingQueue<>();
		var channel = new HttpMsg(q);
		new Thread(channel).start();

		for (;;) {
			try {
				System.out.println(q.take());
			} catch (Exception e) { e.printStackTrace(); }
			if (q.size() == 0) break;
		}
	}
}
