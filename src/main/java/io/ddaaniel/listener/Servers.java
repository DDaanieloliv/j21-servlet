package io.ddaaniel.listener;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.ddaaniel.internal.parser.request.Requests;
import io.ddaaniel.internal.parser.request.mapper.Request;
import io.ddaaniel.internal.parser.request.response.Response;
import io.ddaaniel.internal.parser.request.response.status.ResponseStatusCode;
import io.ddaaniel.internal.parser.util.Util;
import io.ddaaniel.listener.mapper.Server;
import io.ddaaniel.listener.pipe.Handler;


/**
 * Servers
 */
public class Servers {

	private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
	private ServerSocketChannel listener;
	public Server s = new Server();

	public Servers handleConnection(int port) throws Exception {
		var s = new Servers().Serve(port, (req, res) -> {

			var headers = res.DefaultHeaders(0);
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

						res.WriteStatusLine(ResponseStatusCode.STATUS_OK);
						res.WriteHeaders(headers.h);

						var buffer = ByteBuffer.allocate(8192);
						while (fileChannel.read(buffer) > 0) {
							buffer.flip();

							var rawBytes = new byte[buffer.remaining()];
							buffer.get(rawBytes);

							res.WriteBody(rawBytes);

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

						res.WriteStatusLine(ResponseStatusCode.STATUS_OK);
						headers.Delete("Content-Length");
						headers.Set("Transfer-Encoding", "chunked");
						headers.Replace("Content-Type", "text/plain");
						headers.Set("Trailer", "X-Content-SHA256, X-Content-Length");
						res.WriteHeaders(headers.h);

						var fullBody = new ByteArrayOutputStream();
						try (InputStream bodyStream = resOut.body()) {
							int n;
							var data = new byte[32];
							while ((n = bodyStream.read(data)) != -1) {
								if (n == 0) continue;
								fullBody.write(data, 0, n);

								var hexSize = Integer.toHexString(n) + "\r\n";
								res.WriteBody(hexSize.getBytes());

								var chunkData = new byte[n];
								System.arraycopy(data, 0, chunkData, 0, n);
								res.WriteBody(chunkData);
								res.WriteBody("\r\n".getBytes());
							}
						}

						res.WriteBody("0\r\n".getBytes()); 

						var finalPayload = fullBody.toByteArray();
						var digest = MessageDigest.getInstance("SHA-256");
						var sha256bytes = digest.digest(finalPayload);
						var sha256Hex = HexFormat.of().formatHex(sha256bytes);

						String trailersBlock = "X-Content-SHA256: " + sha256Hex + "\r\n" +
							"X-Content-Length: " + finalPayload.length + "\r\n" +
							"\r\n";

						res.WriteBody(trailersBlock.getBytes());
						return;

					} 
					else {
						res.WriteStatusLine(ResponseStatusCode.STATUS_OK);

						headers.Replace("Content-Length", originalContentLength);
						var originalContentType = resOut.headers().firstValue("Content-Type").orElse("text/html");
						headers.Replace("Content-Type", originalContentType);

						headers.Delete("Transfer-Encoding");
						headers.Delete("Trailer");

						res.WriteHeaders(headers.h);

						try (InputStream bodyStream = resOut.body()) {
							int n;
							var buffer = new byte[1024];
							while ((n = bodyStream.read(buffer)) != -1) {
								if (n == 0) continue;

								byte[] rawData = new byte[n];
								System.arraycopy(buffer, 0, rawData, 0, n);
								res.WriteBody(rawData);
							}
						}
						return;
					}

				} catch (Exception err) {
					var errBody = respond500().getBytes();
					headers.Replace("Content-Length", String.valueOf(errBody.length));
					headers.Replace("Content-Type", "text/html");

					res.WriteStatusLine(ResponseStatusCode.STATUS_INTERNAL_SERVER_ERROR);
					res.WriteHeaders(headers.h);
					res.WriteBody(errBody);
					return;
				}
			}

			headers.Replace("Content-Length", String.valueOf(body.length));
			headers.Replace("Content-Type", "text/html");

			res.WriteStatusLine(status);
			res.WriteHeaders(headers.h);
			res.WriteBody(body);
		});

		return s;
	}


	public void runConnection(Server server, SocketChannel conn) {
		try (conn) {
			var response = new Response(conn);
			var headers = response.DefaultHeaders(0);

			var r = new Request();
			try {
				r = new Requests().RequestFromReader(conn);
			} catch (Exception err) { 
				response.WriteStatusLine(ResponseStatusCode.STATUS_BAD_REQUEST);
				response.WriteHeaders(headers.h);
				return;
			}

			server.handler.handle(r, response);

		} catch (Exception e) {
			if (!server.closed) { 
				System.err.println(" -> Error in connection: " + e.getMessage()); 
			}
		}
	}

	public void runServer(ServerSocketChannel listener) {
		try {
			while (listener.isOpen() && !s.closed) {
				var socketChannel = listener.accept();
				if (s.closed) {
					if (socketChannel != null) socketChannel.close();
					return;
				}
				executor.submit(() -> { runConnection(s, socketChannel); });
			}
		} catch (Exception e) { 
			if (!s.closed) {
				throw new RuntimeException(e); 
			}
		}
	}

	public Servers Serve(int port, Handler handler) throws Exception {
		listener = ServerSocketChannel.open();
		listener.bind(new InetSocketAddress(port));
		s.closed = false;
		s.handler = handler;
		executor.submit(() -> { runServer(listener); } );
		return this;
	}

	public void Close() {
		try {
			s.closed = true;
			if (listener != null && listener.isOpen()) {
				listener.close();
			}
			executor.shutdown();
		} catch (Exception e) { 
			System.err.println(" -> Error when closing server: " + e.getMessage());
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
