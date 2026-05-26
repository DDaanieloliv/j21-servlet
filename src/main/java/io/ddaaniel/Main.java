package io.ddaaniel;

import java.util.concurrent.CountDownLatch;

import io.ddaaniel.internal.server.Servers;

public class Main {
	public static void main(String[] args) {

		CountDownLatch keepAliveLatch = new CountDownLatch(1);

		try {
			var s = new Servers().Serve(42069);

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				System.out.println("\n -> Signal received! Initiating graceful shutdown...");
				s.Close();
				keepAliveLatch.countDown(); 
			}));
		} catch (Exception e) { 
			System.err.println(" -> Error starting server: " + e.getMessage()); 
			System.exit(1);	
		}

		try {
			keepAliveLatch.await();
			System.out.println(" -> Server gracefully stopped");
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
