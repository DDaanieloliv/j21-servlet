package io.ddaaniel.internal.parser.writer;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.security.MessageDigest;
import java.util.HexFormat;

import io.ddaaniel.core.httpEntity.ResponseEntity;
import io.ddaaniel.core.httpEntity.httpHeaders.HttpHeaders;

/**
 * ChunkedServletWriter
 */
public class ChunkedServletWriter implements HttpWriterMatcher {

	@Override
	public boolean matches(ResponseEntity<?> response) {
		Object body = response.getBody();
		HttpHeaders headers = response.getHeaders();
		boolean isStream = body instanceof InputStream;
		boolean hasLength = headers != null && headers.get("Content-Length") != null;

		return isStream && !hasLength;
	}

	@Override
	public void write(WritableByteChannel channel, ResponseEntity<?> response) throws Exception {
		InputStream bodyStream = (InputStream) response.getBody();
		HttpHeaders headers = response.getHeaders() != null ? response.getHeaders() : new HttpHeaders();

		headers.remove("Content-Length");
		headers.set("Transfer-Encoding", "chunked");
		headers.set("Trailer", "X-Content-SHA256, X-Content-Length");
		if (headers.get("Content-Type") == null) {
			headers.set("Content-Type", "application/octet-stream");
		}

		String statusStr = String.format("HTTP/1.1 %d %s\r\n", response.getStatusCode().value(), response.getStatusCode().toString());
		channel.write(ByteBuffer.wrap(statusStr.getBytes()));

		var sb = new StringBuilder();
		headers.forEach((k, v) -> sb.append(k).append(": ").append(String.join(", ", v)).append("\r\n"));
		sb.append("\r\n");
		channel.write(ByteBuffer.wrap(sb.toString().getBytes()));

		try (bodyStream) {
			var digest = MessageDigest.getInstance("SHA-256");
			var data = new byte[8192];
			int n;
			long totalBytes = 0;

			while ((n = bodyStream.read(data)) != -1) {
				if (n == 0) continue;
				digest.update(data, 0, n);
				totalBytes += n;

				var hexSize = Integer.toHexString(n) + "\r\n";
				channel.write(ByteBuffer.wrap(hexSize.getBytes()));
				channel.write(ByteBuffer.wrap(data, 0, n));
				channel.write(ByteBuffer.wrap("\r\n".getBytes()));
			}

			channel.write(ByteBuffer.wrap("0\r\n".getBytes()));

			var sha256Hex = HexFormat.of().formatHex(digest.digest());
			String trailersBlock = "X-Content-SHA256: " + sha256Hex + "\r\n" +
				"X-Content-Length: " + totalBytes + "\r\n" +
				"\r\n";
			channel.write(ByteBuffer.wrap(trailersBlock.getBytes()));
		}
	}
}
