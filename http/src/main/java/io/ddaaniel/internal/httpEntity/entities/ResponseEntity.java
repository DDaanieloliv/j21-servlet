package io.ddaaniel.internal.httpEntity.entities;

import java.net.URI;

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
		super();
		this.status = status;
	}




	public HttpStatus getStatus() {
		return this.status;
	}

	public static <T> ResponseEntity<T> ok(T body) {
		return new ResponseEntity<>(body, HttpStatus.STATUS_OK);
	}

	public static BodyBuilder ok() {
		return new DefaultBuilder(HttpStatus.STATUS_OK);
	}

	public static BodyBuilder accepted() {
		return new DefaultBuilder(HttpStatus.STATUS_ACCEPTED);
	}

	public static BodyBuilder badRequest() {
		return new DefaultBuilder(HttpStatus.STATUS_BAD_REQUEST);
	}

	public static BodyBuilder internalServerError() {
		return new DefaultBuilder(HttpStatus.STATUS_INTERNAL_SERVER_ERROR);
	}

	public static BodyBuilder created(URI location) {
		DefaultBuilder builder = new DefaultBuilder(HttpStatus.STATUS_CREATED);
		if (location != null) {
		}
		return builder;
	}

	public static HeadersBuilder<?> noContent() {
		return new DefaultBuilder(HttpStatus.STATUS_NO_CONTENT);
	}

	public static HeadersBuilder<?> notFound() {
		return new DefaultBuilder(HttpStatus.STATUS_NOT_FOUND);
	}

	public static BodyBuilder status(HttpStatus status) {
		return new DefaultBuilder(status);
	}



	public interface HeadersBuilder<B extends HeadersBuilder<B>> {
		B headers(HttpHeaders headers);
		ResponseEntity<Void> build();
	}

	public interface BodyBuilder extends HeadersBuilder<BodyBuilder> {
		<T> ResponseEntity<T> body(T body);
	}


	private static class DefaultBuilder implements BodyBuilder {
		private final HttpStatus status;
		private HttpHeaders headers = HttpHeaders.EMPTY;

		public DefaultBuilder(HttpStatus status) {
			this.status = status;
		}

		@Override
		public BodyBuilder headers(HttpHeaders headers) {
			if (headers != null) {
				this.headers = headers;
			}
			return this;
		}
		@Override
		public <T> ResponseEntity<T> body(T body) {
			return new ResponseEntity<>(this.headers, body, this.status);
		}
		@Override
		public ResponseEntity<Void> build() {
			return new ResponseEntity<>(this.headers, this.status);
		}
	}

}
