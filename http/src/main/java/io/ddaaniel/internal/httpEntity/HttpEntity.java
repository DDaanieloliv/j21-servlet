package io.ddaaniel.internal.httpEntity;

import io.ddaaniel.internal.httpEntity.entities.httpHeaders.HttpHeaders;

/**
 * HttpEntity
 */
public class HttpEntity<T> {

	private final HttpHeaders headers;
	private final T body;

	public static final HttpEntity<?> EMPTY = new HttpEntity<>();

	protected HttpEntity() {
		this.headers = HttpHeaders.EMPTY;
		this.body = null;
	}

	public HttpEntity(HttpHeaders headers) {
		this.headers = headers;
		this.body = null;
	}

	public HttpEntity(T body) {
		this.headers = HttpHeaders.EMPTY;
		this.body = body;
	}

	public HttpEntity(T body, HttpHeaders headers) {
		this.headers = headers;
		this.body = body;
	}


	public T getBody() {
		return this.body;
	}

	public HttpHeaders getHeaders() {
		return this.headers;
	}
}
