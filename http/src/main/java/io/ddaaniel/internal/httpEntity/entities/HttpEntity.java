package io.ddaaniel.internal.httpEntity.entities;

import io.ddaaniel.internal.httpEntity.entities.httpHeaders.HttpHeaders;

/**
 * HttpEntity
 */
public class HttpEntity<T> {

	private final HttpHeaders headers;
	private final T body;

	public static final HttpEntity<?> EMPTY = new HttpEntity<>(HttpHeaders.EMPTY);

	protected HttpEntity() {
		this(null, (HttpHeaders) null);
	}

	public HttpEntity(HttpHeaders headers) {
		this(null, headers);
	}

	public HttpEntity(T body) {
		this(body, (HttpHeaders) null);
	}

	public HttpEntity(T body, HttpHeaders headers) {
		this.headers = (headers != null) ? headers : new HttpHeaders();
		this.body = body;
	}


	public T getBody() {
		return this.body;
	}

	public boolean hasBody() {
		return (this.body != null);
	}

	public HttpHeaders getHeaders() {
		return this.headers;
	}
}
