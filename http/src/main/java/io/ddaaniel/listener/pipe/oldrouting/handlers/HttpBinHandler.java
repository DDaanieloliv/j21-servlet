package io.ddaaniel.listener.pipe.oldrouting.handlers;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.util.HexFormat;

import io.ddaaniel.internal.support.httpEntity.httpHeaders.HttpHeaders;
import io.ddaaniel.internal.support.httpStatus.HttpStatus;
import io.ddaaniel.internal.parser.reader.HttpServletRequest;
import io.ddaaniel.internal.parser.writer.ServletWriter;

/**
 * HttpBin
 */
public abstract class HttpBinHandler {

	public static void HttpStreamRes(HttpServletRequest message, HttpHeaders headers, ServletWriter writer) throws Exception {
		var target = message.uri();
		HttpClient client = HttpClient.newHttpClient();
		var reqOut = HttpRequest.newBuilder()
			.uri(URI.create("https://httpbin.org" + target.substring("/httpbin".length())))
			.GET()
			.build();
		var resOut = client.send(reqOut, HttpResponse.BodyHandlers.ofInputStream());

		var originalContentLength = resOut.headers().firstValue("Content-Length").orElse(null);

		if (originalContentLength == null || target.contains("/stream")) {

			writer.WriteStatusLine(HttpStatus.OK);
			headers.remove("Content-Length");
			headers.set("Transfer-Encoding", "chunked");
			headers.set("Content-Type", "text/plain");
			headers.set("Trailer", "X-Content-SHA256, X-Content-Length");
			writer.WriteHeaders(headers);

			var fullBody = new ByteArrayOutputStream();
			InputStream bodyStream = resOut.body();
			int n;
			var data = new byte[32];
			while ((n = bodyStream.read(data)) != -1) {
				if (n == 0) continue;
				fullBody.write(data, 0, n);

				var hexSize = Integer.toHexString(n) + "\r\n";
				writer.WriteBody(hexSize.getBytes());

				var chunkData = new byte[n];
				System.arraycopy(data, 0, chunkData, 0, n);
				writer.WriteBody(chunkData);
				writer.WriteBody("\r\n".getBytes());
			}

			writer.WriteBody("0\r\n".getBytes()); 

			var finalPayload = fullBody.toByteArray();
			var digest = MessageDigest.getInstance("SHA-256");
			var sha256bytes = digest.digest(finalPayload);
			var sha256Hex = HexFormat.of().formatHex(sha256bytes);

			String trailersBlock = "X-Content-SHA256: " + sha256Hex + "\r\n" +
				"X-Content-Length: " + finalPayload.length + "\r\n" +
				"\r\n";

			writer.WriteBody(trailersBlock.getBytes());
			return;

		} 
		else {
			writer.WriteStatusLine(HttpStatus.OK);

			headers.set("Content-Length", originalContentLength);
			var originalContentType = resOut.headers().firstValue("Content-Type").orElse("text/html");
			headers.set("Content-Type", originalContentType);

			headers.remove("Transfer-Encoding");
			headers.remove("Trailer");

			writer.WriteHeaders(headers);

			InputStream bodyStream = resOut.body();
			int n;
			var buffer = new byte[1024];
			while ((n = bodyStream.read(buffer)) != -1) {
				if (n == 0) continue;

				byte[] rawData = new byte[n];
				System.arraycopy(buffer, 0, rawData, 0, n);
				writer.WriteBody(rawData);
			}
			return;
		}
	}
}
