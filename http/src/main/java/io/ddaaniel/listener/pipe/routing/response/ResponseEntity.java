package io.ddaaniel.listener.pipe.routing.response;

import java.util.HashMap;
import java.util.Map;
import io.ddaaniel.internal.parser.request.response.status.HttpStatus;

public class ResponseEntity<T> {
	private final HttpStatus status;
	private final Map<String, String> headers;
	private final T body;

	public ResponseEntity(HttpStatus status, Map<String, String> headers, T body) {
		this.status = status;
		this.headers = headers != null ? headers : new HashMap<>();
		this.body = body;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public T getBody() {
		return body;
	}

	public static <T> ResponseEntity<T> ok(T body) {
		return new ResponseEntity<>(HttpStatus.STATUS_OK, new HashMap<>(), body);
	}

	public static <T> ResponseEntity<T> status(HttpStatus status) {
		return new ResponseEntity<>(status, new HashMap<>(), null);
	}
}
