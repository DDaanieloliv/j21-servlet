package io.ddaaniel.internal.parser.writer;


import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import io.ddaaniel.core.httpEntity.ResponseEntity;
import io.ddaaniel.core.httpEntity.httpHeaders.HttpHeaders;
import io.ddaaniel.core.httpStatus.HttpStatus;


public class DefaultServletWriter implements HttpWriterConduct {

	@Override
	public boolean matches(ResponseEntity<?> response) {
		return true;
	}

	@Override
	public void write(WritableByteChannel channel, ResponseEntity<?> response) throws Exception {
		Object body = response.getBody();
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
			if (headers.get("Content-Length") == null) {
				headers.set("Content-Length", String.valueOf(rawBody.length));
			}
			if (headers.get("Content-Type") == null) {
				headers.set("Content-Type", "text/plain");
			}
			writeStatus(channel, response);
			writeHeaders(channel, headers);
			channel.write(ByteBuffer.wrap(rawBody));
		}
	}

	private void writeStatus(WritableByteChannel channel, ResponseEntity<?> res) throws Exception {
		var code = res.getStatusCode().value();
		String statusStr = String.format("HTTP/1.1 %s %s\r\n", code, HttpStatus.resolve(code).getReasonPhrase());
		channel.write(ByteBuffer.wrap(statusStr.getBytes()));
	}

	private void writeHeaders(WritableByteChannel channel, HttpHeaders headers) throws Exception {
		var sb = new StringBuilder();
		headers.forEach((k, v) -> sb.append(k).append(": ").append(String.join(", ", v)).append("\r\n"));
		sb.append("\r\n");
		channel.write(ByteBuffer.wrap(sb.toString().getBytes()));
	}
}
