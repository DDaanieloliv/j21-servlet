package io.ddaaniel.listener.internal.codec.writer;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import io.ddaaniel.core.httpEntity.httpHeaders.HttpHeaders;
import io.ddaaniel.core.httpStatus.HttpStatus;

/**
 * AbstractServletWriter
 */
public abstract class AbstractServletWriter implements Writer {

	protected boolean isChunkedMessage(HttpServletResponse response) {
		Object body = response.getBufferedBody();
		HttpHeaders headers = response.getHeaders();
		boolean isStream = body instanceof InputStream;
		boolean hasLength = headers != null && headers.get("Content-Length") != null;

		return isStream && !hasLength;
	}

	protected void writeStatus(WritableByteChannel channel, HttpServletResponse res) throws Exception {
		var code = res.getStatus().value();
		String statusStr = String.format("HTTP/1.1 %s %s\r\n", code, HttpStatus.resolve(code).getReasonPhrase());
		channel.write(ByteBuffer.wrap(statusStr.getBytes()));
	}

	protected void writeHeaders(WritableByteChannel channel, HttpHeaders headers) throws Exception {
		var sb = new StringBuilder();
		headers.forEach((k, v) -> sb.append(k).append(": ").append(String.join(", ", v)).append("\r\n"));
		sb.append("\r\n");
		channel.write(ByteBuffer.wrap(sb.toString().getBytes()));
	}


	protected void setSoftwareOrigin(HttpServletResponse response) {
		var headers = response.getHeaders();
		if (!headers.containsHeader(HttpHeaders.HttpHeadersNames.DATE)) {
			headers.set(HttpHeaders.HttpHeadersNames.DATE,
					DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC)));
		}
		if (!headers.containsHeader(HttpHeaders.HttpHeadersNames.SERVER)) {
			headers.set(HttpHeaders.HttpHeadersNames.SERVER, "Cooffee/0.1");
		}
	}

	protected boolean shouldKeepAlive(HttpServletResponse response) {
		return !"close".equalsIgnoreCase(response.getHeaders().getFirst("Connection"));
	}
}
