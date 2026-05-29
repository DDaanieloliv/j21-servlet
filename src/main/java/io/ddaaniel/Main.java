package io.ddaaniel;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CountDownLatch;


import io.ddaaniel.internal.parser.response.enums.StatusCode;
import io.ddaaniel.internal.parser.util.Util;
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
				} else if (Util.HasPrefix(req.RequestLine.RequestTarget.getBytes(), "/httpbin/stream".getBytes())) {
					var target = req.RequestLine.RequestTarget;
					try (HttpClient client = HttpClient.newHttpClient()) {
						HttpRequest reqOut = HttpRequest.newBuilder()
							.uri(URI.create("https://httpbin.org" + target.substring("/httpbin".length())))
							.GET()
							.build();

						HttpResponse<InputStream> resOut = client.send(reqOut, HttpResponse.BodyHandlers.ofInputStream());

						w.WriteStatusLine(StatusCode.STATUS_OK);

						headers.Delete("Content-Length");
						headers.Set("Transfer-Encoding", "chunked");
						headers.Replace("Content-Type", "text/plain");
						w.WriteHeaders(headers.h);
						try (InputStream bodyStream = resOut.body()) {
							var data = new byte[32];
							int n;

							while ((n = bodyStream.read(data)) != -1) {
								if (n == 0) continue;

								String hexSize = Integer.toHexString(n) + "\r\n";
								w.WriteBody(hexSize.getBytes());

								byte[] chunkData = new byte[n];
								System.arraycopy(data, 0, chunkData, 0, n);
								w.WriteBody(chunkData);
								w.WriteBody("\r\n".getBytes());
							}
						}

						w.WriteBody("0\r\n\r\n".getBytes());
						return;

					} catch (Exception err) {
						byte[] errBody = respond500().getBytes();
						headers.Replace("Content-Length", String.valueOf(errBody.length));
						headers.Replace("Content-Type", "text/html");

						w.WriteStatusLine(StatusCode.STATUS_INTERNAL_SERVER_ERROR);
						w.WriteHeaders(headers.h);
						w.WriteBody(errBody);
						return;
					}
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
