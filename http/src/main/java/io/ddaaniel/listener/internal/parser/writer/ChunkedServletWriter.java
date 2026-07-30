package io.ddaaniel.listener.internal.parser.writer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.ddaaniel.core.httpEntity.httpHeaders.HttpHeaders;
import io.ddaaniel.core.httpStatus.HttpStatus;

/**
 * ChunkedServletWriter
 */
public class ChunkedServletWriter implements HttpWriterConduct {

	private static final Logger log = Logger.getLogger(ChunkedServletWriter.class.getName());

	@Override
	public boolean matches(DefaultHttpServletResponse response) {
		Object body = response.getBufferedBody();
		HttpHeaders headers = response.getHeaders();
		boolean isStream = body instanceof InputStream;
		boolean hasLength = headers != null && headers.get("Content-Length") != null;

		return isStream && !hasLength;
	}

	@Override
	public void write(WritableByteChannel channel, DefaultHttpServletResponse response) throws Exception {
		InputStream bodyStream = new ByteArrayInputStream(response.getBufferedBody());
		HttpHeaders headers = response.getHeaders() != null ? response.getHeaders() : new HttpHeaders();

		headers.remove(HttpHeaders.CONTENT_LENGTH);
		headers.set(HttpHeaders.TRANSFER_ENCODING, "chunked");
		headers.set(HttpHeaders.TRAILER, "X-Content-SHA256, X-Content-Length");
		if (headers.get(HttpHeaders.CONTENT_TYPE) == null) {
			headers.set(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
		}

		var code = response.getStatus().value();
		String statusStr = String.format("HTTP/1.1 %s %s\r\n", code, HttpStatus.resolve(code).getReasonPhrase());
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
			if (log.isLoggable(Level.FINE)) log.log(Level.FINE, " -> Chunked transfer complete. Total bytes: {0}, SHA-256: {1}", new Object[]{totalBytes, sha256Hex});
			String trailersBlock = "X-Content-SHA256: " + sha256Hex + "\r\n" +
				"X-Content-Length: " + totalBytes + "\r\n" +
				"\r\n";
			channel.write(ByteBuffer.wrap(trailersBlock.getBytes()));
		}
	}
}
