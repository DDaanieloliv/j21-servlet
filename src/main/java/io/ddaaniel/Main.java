package io.ddaaniel;

import java.util.concurrent.CountDownLatch;

import io.ddaaniel.internal.parser.response.message.enumns.StatusCode;
import io.ddaaniel.internal.server.HandlerError;
import io.ddaaniel.internal.server.Servers;

public class Main {
	public static void main(String[] args) {

		CountDownLatch keepAliveLatch = new CountDownLatch(1);

		try {
			var s = new Servers().Serve(42069, (writer, req) -> {
				try {
					if ("/yourproblem".equals(req.RequestLine.RequestTarget)) {
						return new HandlerError(
								StatusCode.STATUS_BAD_REQUEST, 
								"Your problem is not my problem\n"
								);
					} 

					else if ("/myproblem".equals(req.RequestLine.RequestTarget)) {
						return new HandlerError(
								StatusCode.STATUS_INTERNAL_SERVER_ERROR, 
								"Woopsie, my bad\n"
								);
					} 

					else {
						String successMessage = "Hello World centralizado e roteado pelo Handler!\n";
						writer.write(successMessage.getBytes());
						return null;
					}

				} catch (Exception e) {
					return new HandlerError(StatusCode.STATUS_INTERNAL_SERVER_ERROR, e.getMessage());
				}
			}) ;

			System.out.println(" -> Server started with Handler on port 42069 ");

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
