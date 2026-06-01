package io.ddaaniel;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.concurrent.CountDownLatch;


import io.ddaaniel.internal.parser.request.response.status.ResponseStatusCode;
import io.ddaaniel.internal.parser.util.Util;
import io.ddaaniel.listener.Servers;

public class Main {
	public static void main(String[] args) {

		var port = 42069;
		var keepAliveLatch = new CountDownLatch(1);

		try {
			var s = new Servers().Serve(port, (w, req) -> {
				var headers = w.GetDefaultHeaders(0);

				var body = respond200().getBytes();
				var status = ResponseStatusCode.STATUS_OK;

				if ("/yourproblem".equals(req.RequestLine.RequestTarget)) {
					body = respond400().getBytes();
					status = ResponseStatusCode.STATUS_BAD_REQUEST;
				} else if ("/myproblem".equals(req.RequestLine.RequestTarget)) {
					body = respond500().getBytes();
					status = ResponseStatusCode.STATUS_INTERNAL_SERVER_ERROR;
				} else if (req.RequestLine.RequestTarget.equals("/video")) {
					try {
						var videoPath = Path.of("/home/daniel/DEV_ENV/personal/dev/httpfromtcp/src/main/java/io/ddaaniel/assets/video.mp4");
						try (FileChannel fileChannel = FileChannel.open(videoPath, StandardOpenOption.READ)) {
							long fileSize = fileChannel.size();
							headers.Replace("content-type", "video/mp4");
							headers.Replace("content-length", String.valueOf(fileSize));
							headers.Delete("transfer-encoding");

							w.WriteStatusLine(ResponseStatusCode.STATUS_OK);
							w.WriteHeaders(headers.h);

							var buffer = ByteBuffer.allocate(8192);
							while (fileChannel.read(buffer) > 0) {
								buffer.flip();

								var rawBytes = new byte[buffer.remaining()];
								buffer.get(rawBytes);

								w.WriteBody(rawBytes);

								buffer.clear();
							}
						}

					} catch (Exception err) {
						System.err.println(" -> Error when streaming .mp4 file: " + err.getMessage());
					}
				} else if (Util.HasPrefix(req.RequestLine.RequestTarget.getBytes(), "/httpbin/".getBytes())) {
					var target = req.RequestLine.RequestTarget;
					try (HttpClient client = HttpClient.newHttpClient()) {
						var reqOut = HttpRequest.newBuilder()
							.uri(URI.create("https://httpbin.org" + target.substring("/httpbin".length())))
							.GET()
							.build();
						var resOut = client.send(reqOut, HttpResponse.BodyHandlers.ofInputStream());

						var originalContentLength = resOut.headers().firstValue("Content-Length").orElse(null);

						if (originalContentLength == null || target.contains("/stream")) {

							w.WriteStatusLine(ResponseStatusCode.STATUS_OK);
							headers.Delete("Content-Length");
							headers.Set("Transfer-Encoding", "chunked");
							headers.Replace("Content-Type", "text/plain");
							headers.Set("Trailer", "X-Content-SHA256, X-Content-Length");
							w.WriteHeaders(headers.h);

							var fullBody = new ByteArrayOutputStream();
							try (InputStream bodyStream = resOut.body()) {
								int n;
								var data = new byte[32];
								while ((n = bodyStream.read(data)) != -1) {
									if (n == 0) continue;
									fullBody.write(data, 0, n);

									var hexSize = Integer.toHexString(n) + "\r\n";
									w.WriteBody(hexSize.getBytes());

									var chunkData = new byte[n];
									System.arraycopy(data, 0, chunkData, 0, n);
									w.WriteBody(chunkData);
									w.WriteBody("\r\n".getBytes());
								}
							}

							w.WriteBody("0\r\n".getBytes()); 

							var finalPayload = fullBody.toByteArray();
							var digest = MessageDigest.getInstance("SHA-256");
							var sha256bytes = digest.digest(finalPayload);
							var sha256Hex = HexFormat.of().formatHex(sha256bytes);

							String trailersBlock = "X-Content-SHA256: " + sha256Hex + "\r\n" +
								"X-Content-Length: " + finalPayload.length + "\r\n" +
								"\r\n";

							w.WriteBody(trailersBlock.getBytes());
							return;

						} 
						else {
							w.WriteStatusLine(ResponseStatusCode.STATUS_OK);

							headers.Replace("Content-Length", originalContentLength);
							var originalContentType = resOut.headers().firstValue("Content-Type").orElse("text/html");
							headers.Replace("Content-Type", originalContentType);

							headers.Delete("Transfer-Encoding");
							headers.Delete("Trailer");

							w.WriteHeaders(headers.h);

							try (InputStream bodyStream = resOut.body()) {
								int n;
								var buffer = new byte[1024];
								while ((n = bodyStream.read(buffer)) != -1) {
									if (n == 0) continue;

									byte[] rawData = new byte[n];
									System.arraycopy(buffer, 0, rawData, 0, n);
									w.WriteBody(rawData);
								}
							}
							return;
						}

					} catch (Exception err) {
						var errBody = respond500().getBytes();
						headers.Replace("Content-Length", String.valueOf(errBody.length));
						headers.Replace("Content-Type", "text/html");

						w.WriteStatusLine(ResponseStatusCode.STATUS_INTERNAL_SERVER_ERROR);
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
