package io.ddaaniel;

import java.util.concurrent.CountDownLatch;

import io.ddaaniel.internal.parser.response.enums.StatusCode;
import io.ddaaniel.internal.server.Servers;

public class Main {
	public static void main(String[] args) {

		var port = 42069;
		var keepAliveLatch = new CountDownLatch(1);

		try {
			var s = new Servers().Serve(port, (w, req) -> {
				var headers = w.GetDefaultHeaders(0);

				var body = respond200().getBytes();
				var status = StatusCode.STATUS_OK;

				if ("/yourproblem".equals(req.RequestLine.RequestTarget)) {
					body = respond400().getBytes();
					status = StatusCode.STATUS_BAD_REQUEST;
				} else if ("/myproblem".equals(req.RequestLine.RequestTarget)) {
					body = respond500().getBytes();
					status = StatusCode.STATUS_INTERNAL_SERVER_ERROR;
				}

				headers.Replace("Content-Length", String.valueOf(body.length));
				headers.Replace("Content-Type", "text/html");

				w.WriteStatusLine(status);
				w.WriteHeaders(headers.h);
				w.WriteBody(body);
			});

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

	private static String respond200() {
		return 
			"<html>" +
			"<head>" +
			"<title>200 OK</title>" +
			"</head>" +
			"<body>" +
			"<h1>Success!</h1>" +
			"<p>Your request was an absolute banger.</p>" +
			"</body>" +
			"</html>" + 
			"\n";
	}

	private static String respond400() {
		return 
			"<html>" +
			"<head>" +
			"<title>400 Bad Request</title>" +
			"</head>" +
			"<body>" +
			"<h1>Bad Request</h1>" +
			"<p>Your request honestly kinda sucked.</p>" +
			"</body>" +
			"</html>" +
			"\n";
	}

	private static String respond500() {
		return 
			"<html>" +
			"<head>" +
			"<title>500 Internal Server Error</title>" +
			"</head>" +
			"<body>" +
			"<h1>Internal Server Error</h1>" +
			"<p>Okay, you know what? This one is on me.</p>" +
			"</body>" +
			"</html>" + 
			"\n";
	}

}
