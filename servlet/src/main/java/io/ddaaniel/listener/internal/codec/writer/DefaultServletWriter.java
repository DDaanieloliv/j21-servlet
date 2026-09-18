package io.ddaaniel.listener.internal.codec.writer;

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
 * DefaultServletWriter
 */
public class DefaultServletWriter extends AbstractServletWriter {

	private final WritableByteChannel channel;
	private static final Logger log = Logger.getLogger(DefaultServletWriter.class.getName());

	public DefaultServletWriter(WritableByteChannel channel) {
        this.channel = channel;
    }


	@Override
	public boolean canWrite() {
		return true;
		}

	@Override
	public WritableByteChannel channel() { 
		return this.channel;
	}

	@Override
	public void writeToConnection(HttpServletResponse response) throws Exception {
		if (isChunkedMessage(response)) {
			writeChunk(channel, response);
			setSoftwareOrigin(response);
			if (!shouldKeepAlive(response)) {
				channel.close();
			}
			return;
		}
		writeDefault(channel, response);
		if (!shouldKeepAlive(response)) {
			channel.close();
		}
		return;
	}

	public void writeDefault(WritableByteChannel channel, HttpServletResponse response) throws Exception {
		Object body = response.getBufferedBody();
		HttpHeaders headers = response.getHeaders() != null ? response.getHeaders() : new HttpHeaders();
		 
		if (body instanceof InputStream bodyStream) {
			writeStatus(channel, response);
			writeHeaders(channel, headers);
			try (bodyStream) {
				var data = new byte[8192];
				int n;
				while ((n = bodyStream.read(data)) != -1) {
					channel.write(ByteBuffer.wrap(data, 0, n));
				}
			}
		} else {
			byte[] rawBody = body != null && body instanceof byte[] ? (byte[]) body : new byte[0];
			response.beforeFlushToSocket();
			writeStatus(channel, response);
			writeHeaders(channel, headers);
			channel.write(ByteBuffer.wrap(rawBody));
		}
	}

	public void writeChunk(WritableByteChannel channel, HttpServletResponse response) throws Exception {
		InputStream bodyStream = new ByteArrayInputStream(response.getBufferedBody());
		HttpHeaders headers = response.getHeaders() != null ? response.getHeaders() : new HttpHeaders();

		headers.remove(HttpHeaders.HttpHeadersNames.CONTENT_LENGTH);
		headers.set(HttpHeaders.HttpHeadersNames.TRANSFER_ENCODING, "chunked");
		headers.set(HttpHeaders.HttpHeadersNames.TRAILER, "X-Content-SHA256, X-Content-Length");
		if (headers.get(HttpHeaders.HttpHeadersNames.CONTENT_TYPE) == null) {
			headers.set(HttpHeaders.HttpHeadersNames.CONTENT_TYPE, "application/octet-stream");
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
