package io.ddaaniel.internal.parser.writer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import io.ddaaniel.core.httpEntity.ResponseEntity;
import io.ddaaniel.core.httpEntity.httpHeaders.HttpHeaders;
import io.ddaaniel.core.httpStatus.HttpStatus;
import io.ddaaniel.core.httpStatus.HttpStatusCode;

public class DefaultHttpServletResponse {

    private final HttpServletWriter targetWriter;
    
    private HttpStatusCode status = HttpStatus.OK;
    private final HttpHeaders headers = new HttpHeaders();
    private final ByteArrayOutputStream bodyBuffer = new ByteArrayOutputStream();
    private boolean committed = false;

    public DefaultHttpServletResponse(HttpServletWriter targetWriter) {
        this.targetWriter = targetWriter;
    }


	public void resetBuffer() {
		if (committed) {
			throw new IllegalStateException(" -> Cannot reset buffer: response already committed to TCP socket");
		}
		this.bodyBuffer.reset();
	}

	public void reset() {
		if (committed) {
			throw new IllegalStateException(" -> Cannot reset response: response already committed to TCP socket");
		}
		this.bodyBuffer.reset();
		this.headers.clear();
		this.status = HttpStatus.OK;
	}

	public void sendError(HttpStatusCode status, String message) throws IOException {
		if (committed) {
			throw new IllegalStateException(" -> Cannot send error: response already committed to TCP socket");
		}

		reset();

		this.status = status;
		this.headers.set("Content-Type", "application/json");

		String jsonError = String.format(
				"{\"status\": %d, \"error\": \"%s\", \"message\": \"%s\"}",
				status.value(),
				HttpStatus.resolve(status.value()).getReasonPhrase(),
				message != null ? message : HttpStatus.resolve(status.value()).getReasonPhrase()
				);

		writeBody(jsonError.getBytes(StandardCharsets.UTF_8));
	}

	public void sendError(HttpStatusCode status) throws IOException {
		sendError(status, HttpStatus.resolve(status.value()).getReasonPhrase());
	}



    public void setStatus(HttpStatus status) {
        if (committed) throw new IllegalStateException(" -> Response already committed to TCP socket");
        this.status = status;
    }

    public HttpStatusCode getStatus() {
        return this.status;
    }

    public HttpHeaders getHeaders() {
        return this.headers;
    }

    public void setHeader(String name, String value) {
        if (committed) throw new IllegalStateException(" -> Response already committed to TCP socket");
        this.headers.set(name, value);
    }

    public void writeBody(byte[] bytes) throws IOException {
        if (committed) throw new IllegalStateException(" -> Response already committed to TCP socket");
        this.bodyBuffer.write(bytes);
    }

    public byte[] getBufferedBody() {
        return this.bodyBuffer.toByteArray();
    }

    public void writeResponse(ResponseEntity<?> responseEntity) throws IOException {
        if (responseEntity == null) return;

        this.status = responseEntity.getStatusCode();
        
        if (responseEntity.getHeaders() != null) {
            responseEntity.getHeaders().forEach((key, values) -> {
                values.forEach(val -> this.headers.add(key, val));
            });
        }

        if (responseEntity.getBody() instanceof byte[] bytes) {
            writeBody(bytes);
        } else if (responseEntity.getBody() != null) {
            writeBody(responseEntity.getBody().toString().getBytes());
        }
    }

	public void flushToSocket() throws Throwable {
		if (this.committed) return;
		this.committed = true;

		byte[] bodyBytes = this.bodyBuffer.toByteArray();

		if (!headers.containsHeader("Date")) {
			headers.set("Date", DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC)));
		}

		if (!headers.containsHeader("Server")) {
			headers.set("Server", "CustomJavaEngine/1.0");
		}

		if (!headers.containsHeader("Content-Length") && !headers.containsHeader("Transfer-Encoding")) {
			headers.set("Content-Length", String.valueOf(bodyBytes.length));
		}

		ResponseEntity<byte[]> finalResponse = new ResponseEntity<>(bodyBytes, this.headers, this.status);
		this.targetWriter.writeResponse(finalResponse);
	}
}
