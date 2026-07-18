package io.ddaaniel;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.ddaaniel.listener.DefaultServletContainer;
import io.ddaaniel.listener.ServletContainer;

public class Main {

	private static final Logger log = Logger.getLogger(Main.class.getName());

	public static void main(String[] args) {

		GlobalConfig.initialize();

		var port = GlobalConfig.getPort();
		var keepAliveLatch = new CountDownLatch(1);

		try {
			ServletContainer s = new DefaultServletContainer().hookUp(port);
			log.info(" -> Server started on port 42069 ");

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				log.info(" -> Signal received! Initiating graceful shutdown...");
				s.close();

				keepAliveLatch.countDown(); 
			}));
			keepAliveLatch.await();
			log.info(" -> Server gracefully stopped");

		} catch (Exception e) { 
			log.log(Level.SEVERE, " -> Error starting server: " + e.getMessage(), e); 
			System.exit(1);	
		}
	}
}
