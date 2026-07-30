package io.ddaaniel.listener.internal.parser.writer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import io.ddaaniel.core.httpEntity.httpHeaders.HttpHeaders;
import io.ddaaniel.core.httpStatus.HttpStatus;
import io.ddaaniel.core.httpStatus.HttpStatusCode;

public class DefaultHttpServletResponse {

    private boolean committed = false;
    private HttpStatusCode status = HttpStatus.OK;
    private final HttpHeaders headers = new HttpHeaders();
    private final ByteArrayOutputStream bodyBuffer = new ByteArrayOutputStream();



	public void sendError(HttpStatusCode status, String message) throws IOException {
		if (committed) {
			throw new IllegalStateException(" -> Cannot send error: response already committed to TCP socket");
		}
		reset();
		this.status = status;
	}

	public void sendError(HttpStatusCode status) throws IOException {
		sendError(status, HttpStatus.resolve(status.value()).getReasonPhrase());
	}


	public void reset() {
		if (committed) {
			throw new IllegalStateException(" -> Cannot reset response: response already committed to TCP socket");
		}
		this.bodyBuffer.reset();
		this.headers.clear();
		this.status = HttpStatus.OK;
	}

	public void resetBuffer() {
		if (committed) {
			throw new IllegalStateException(" -> Cannot reset buffer: response already committed to TCP socket");
		}
		this.bodyBuffer.reset();
	}



    public HttpStatusCode getStatus() {
        return this.status;
    }

    public HttpHeaders getHeaders() {
        return this.headers;
    }

    public byte[] getBufferedBody() {
        return this.bodyBuffer.toByteArray();
    }



    public void setStatus(HttpStatusCode status) {
        if (committed) throw new IllegalStateException(" -> Response already committed to TCP socket");
        this.status = status;
    }

    public void setHeader(String name, String value) {
        if (committed) throw new IllegalStateException(" -> Response already committed to TCP socket");
        this.headers.set(name, value);
    }

    public void setBody(byte[] bytes) throws IOException {
        if (committed) throw new IllegalStateException(" -> Response already committed to TCP socket");
        this.bodyBuffer.write(bytes);
    }

	public void beforeFlushToSocket() throws Throwable {
		if (this.committed) return;
		this.committed = true;

		byte[] bodyBytes = bodyBuffer.toByteArray();

		if (!headers.containsHeader(HttpHeaders.CONTENT_LENGTH) && 
				!headers.containsHeader(HttpHeaders.TRANSFER_ENCODING)) {
			headers.set(HttpHeaders.CONTENT_LENGTH, String.valueOf(bodyBytes.length));
				}
	}


    public void setResponse(Object bodyBytes, HttpHeaders headers, HttpStatusCode status) throws IOException {

        this.status = status;
        
        if (headers != null) {
            headers.forEach((key, values) -> {
                values.forEach(val -> this.headers.add(key, val));
            });
        }

        if (bodyBytes instanceof byte[] bytes) {
            setBody(bytes);
        } else if (bodyBytes != null) {
            throw new IllegalStateException(" -> Response body was defined with a illegal type instead it's default's 'byte[]'");
        }
    }
}
