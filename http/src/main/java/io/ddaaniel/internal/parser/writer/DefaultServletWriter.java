package io.ddaaniel.internal.parser.writer;


import java.io.InputStream;
import java.nio.channels.WritableByteChannel;

import io.ddaaniel.core.httpEntity.ResponseEntity;
import io.ddaaniel.core.httpEntity.httpHeaders.HttpHeaders;


public class DefaultServletWriter extends AbstractHttpServletWriter {

	public DefaultServletWriter(WritableByteChannel channel) {
		super(channel);
	}

	@Override
	public boolean canWrite(ResponseEntity<?> response) {
		return response.getBody() == null || !(response.getBody() instanceof InputStream);
	}

	@Override
	public void writeResponse(ResponseEntity<?> response) {
		Object body = response.getBody();
        byte[] rawBody = body != null ? body.toString().getBytes() : new byte[0];
        HttpHeaders headers = response.getHeaders() != null ? response.getHeaders() : new HttpHeaders();

        if (headers.get("Content-Length") == null) {
            headers.set("Content-Length", String.valueOf(rawBody.length));
        }
        if (headers.get("Content-Type") == null) {
            headers.set("Content-Type", "text/plain");
        }

        this.writeStatusLine(response.getStatusCode());
        this.writeHeaders(headers);
        this.writeRawBytes(rawBody);
	}
}
