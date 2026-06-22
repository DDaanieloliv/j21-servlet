package io.ddaaniel.internal.httpEntity.entities;

import io.ddaaniel.internal.httpEntity.HttpEntity;
import io.ddaaniel.internal.httpEntity.entities.httpHeaders.HttpHeaders;
import io.ddaaniel.internal.parser.request.response.status.HttpStatus;

/**
 * ResponseEntity
 */
public class ResponseEntity<T> extends HttpEntity<T> {

	private final HttpStatus status;

	public ResponseEntity(HttpHeaders headers, T body, HttpStatus status) {
		super(body, headers);
		this.status = status;
	}
	public ResponseEntity(HttpHeaders headers, HttpStatus status) {
		super(headers);
		this.status = status;
	}
	public ResponseEntity(T body, HttpStatus status) {
		super(body);
		this.status = status;
	}
	public ResponseEntity(HttpStatus status) {
		this.status = status;
	}

	public static <T> ResponseEntity<T> ok(T body) {
		return new ResponseEntity<>(HttpStatus.STATUS_OK);
	}
	
}
