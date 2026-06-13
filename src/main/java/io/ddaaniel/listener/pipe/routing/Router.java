package io.ddaaniel.listener.pipe.routing;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import io.ddaaniel.internal.parser.request.header.Headers;
import io.ddaaniel.internal.parser.request.mapper.Request;
import io.ddaaniel.internal.parser.request.response.Response;
import io.ddaaniel.internal.parser.request.response.status.ResponseStatusCode;
import io.ddaaniel.internal.parser.request.response.util.HttpMsg;
import io.ddaaniel.internal.parser.util.Util;
import io.ddaaniel.listener.pipe.http_sample.HttpBin;

/**
 * Router
 */
public abstract class Router {

	public static void route(Request req, Response res) {
		String target = req.RequestLine.RequestTarget;
		var headers = res.DefaultHeaders(0);

		try {
			switch (target) {
				case "/video" -> handleVideoStreaming(req, headers, res);
				case "/yourproblem" -> handleYourProblem(req, headers, res);
				case "/myproblem" -> handleMyProblem(req, headers, res);
				default -> {
					if (Util.HasPrefix(target.getBytes(), "/httpbin/".getBytes())) {
						HttpBin.HttpStreamRes(req, headers, res);
					} else {
						handleNotFound(req, headers, res);
					}
				}
			}
		} catch (Exception e) {
			handleInternalError(headers, res, e);
		}
	}

	private static void handleVideoStreaming(Request req, Headers headers, Response res) throws Exception {
		var videoPath = Path
				.of("/home/daniel/DEV_ENV/personal/dev/httpfromtcp/src/main/java/io/ddaaniel/assets/video.mp4");
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
	}

	private static void handleYourProblem(Request req, Headers headers, Response res) throws Exception {
		var body = HttpMsg.respond400().getBytes();
		headers.Replace("Content-Length", String.valueOf(body.length));
		headers.Replace("Content-Type", "text/html");

		res.WriteStatusLine(ResponseStatusCode.STATUS_BAD_REQUEST);
		res.WriteHeaders(headers.h);
		res.WriteBody(body);
	}

	private static void handleMyProblem(Request req, Headers headers, Response res) throws Exception {
		var body = HttpMsg.respond500().getBytes();
		headers.Replace("Content-Length", String.valueOf(body.length));
		headers.Replace("Content-Type", "text/html");

		res.WriteStatusLine(ResponseStatusCode.STATUS_INTERNAL_SERVER_ERROR);
		res.WriteHeaders(headers.h);
		res.WriteBody(body);
	}

	private static void handleNotFound(Request req, Headers headers, Response res) throws Exception {
		var errBody = "<html><h1>404 Not Found</h1></html>\n".getBytes();
		headers.Replace("Content-Length", String.valueOf(errBody.length));
		headers.Replace("Content-Type", "text/html");

		res.WriteStatusLine(ResponseStatusCode.STATUS_NOT_FOUND);
		res.WriteHeaders(headers.h);
		res.WriteBody(errBody);
	}

	private static void handleInternalError(Headers headers, Response res, Exception err) {
		System.err.println(" -> Global Router Error: " + err.getMessage());
		try {
			var errBody = "<html><h1>500 Internal Server Error</h1></html>\n".getBytes();
			headers.Replace("Content-Length", String.valueOf(errBody.length));
			headers.Replace("Content-Type", "text/html");
			res.WriteStatusLine(ResponseStatusCode.STATUS_INTERNAL_SERVER_ERROR);
			res.WriteHeaders(headers.h);
			res.WriteBody(errBody);
		} catch (Exception critical) {
			critical.printStackTrace();
		}
	}

}
