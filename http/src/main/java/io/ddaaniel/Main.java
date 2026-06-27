package io.ddaaniel;

import java.util.concurrent.CountDownLatch;


import io.ddaaniel.listener.Servlet;

public class Main {
	public static void main(String[] args) {

		var port = 42069;
		var keepAliveLatch = new CountDownLatch(1);

		try {
			var s = new Servlet().handleConnection(port);

			System.out.println(" -> Server started on port 42069 ");
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				System.out.println("\n -> Signal received! Initiating graceful shutdown...");
				s.Close();
				keepAliveLatch.countDown(); 
			}));
			keepAliveLatch.await();
			System.out.println(" -> Server gracefully stopped");

		} catch (Exception e) { 
			System.err.println(" -> Error starting server: " + e.getMessage()); 
			System.exit(1);	
		}
	}

}
