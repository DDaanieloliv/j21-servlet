package io.ddaaniel.listener.internal.codec.writer;


import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import io.ddaaniel.core.httpEntity.httpHeaders.HttpHeaders;
import io.ddaaniel.core.httpStatus.HttpStatus;


public class DefaultServletWriter implements HttpWriterConduct {


	@Override
	public boolean matches(DefaultHttpServletResponse response) {
		return true;
	}

	@Override
	public void write(WritableByteChannel channel, DefaultHttpServletResponse response) throws Exception {
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

	private void writeStatus(WritableByteChannel channel, DefaultHttpServletResponse res) throws Exception {
		var code = res.getStatus().value();
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
