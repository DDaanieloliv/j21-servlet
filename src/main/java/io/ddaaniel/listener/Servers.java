package io.ddaaniel.listener;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.ddaaniel.internal.parser.request.Requests;
import io.ddaaniel.internal.parser.request.mapper.Request;
import io.ddaaniel.internal.parser.request.response.Response;
import io.ddaaniel.internal.parser.request.response.status.ResponseStatusCode;
import io.ddaaniel.internal.parser.util.Util;
import io.ddaaniel.listener.mapper.Server;
import io.ddaaniel.listener.pipe.Handler;
import io.ddaaniel.listener.pipe.sample.HttpBin;


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
				try {
					HttpBin.HttpStreamRes(req, headers, res);
					return;
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
