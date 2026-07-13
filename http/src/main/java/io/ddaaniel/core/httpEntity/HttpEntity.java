package io.ddaaniel.core.httpEntity;

import io.ddaaniel.core.httpEntity.httpHeaders.HttpHeaders;

/**
 * HttpEntity
 */
public class HttpEntity<T> {

	/**
	 * An {@code HttpEntity} instance with a {@code null} body and
	 * {@link HttpHeaders#EMPTY empty headers}.
	 */
	public static final HttpEntity<?> EMPTY = new HttpEntity<>(HttpHeaders.EMPTY);


	private final HttpHeaders headers;

	private final T body;


	/**
	 * Create a new, empty {@code HttpEntity}.
	 */
	protected HttpEntity() {
		this(null, (HttpHeaders) null);
	}

	/**
	 * Create a new {@code HttpEntity} with the given body and no headers.
	 * @param body the entity body
	 */
	public HttpEntity(T body) {
		this(body, (HttpHeaders) null);
	}

	/**
	 * Create a new {@code HttpEntity} with the given headers and no body.
	 * @param headers the entity headers
	 */
	public HttpEntity(HttpHeaders headers) {
		this(null, headers);
	}

	/**
	 * Create a new {@code HttpEntity} with the given body and headers.
	 * @param body the entity body
	 * @param headers the entity headers
	 */
	public HttpEntity(T body, HttpHeaders headers) {
		this.headers = (headers != null) ? headers : new HttpHeaders();
		this.body = body;
	}

	/**
	 * Returns the body of this entity.
	 */
	public T getBody() {
		return this.body;
	}

	/**
	 * Indicates whether this entity has a body.
	 */
	public boolean hasBody() {
		return (this.body != null);
	}

	/**
	 * Returns the headers of this entity.
	 */
	public HttpHeaders getHeaders() {
		return this.headers;
	}
}
