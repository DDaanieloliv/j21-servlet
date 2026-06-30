package io.ddaaniel.internal.support.httpEntity;

import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;

import io.ddaaniel.internal.support.httpEntity.httpHeaders.HttpHeaders;
import io.ddaaniel.internal.support.httpStatus.HttpStatus;
import io.ddaaniel.internal.support.httpStatus.HttpStatusCode;


/**
 * ResponseEntity
 */
public class ResponseEntity<T> extends HttpEntity<T> {

	private final HttpStatusCode status;

	/**
	 * Create a {@code ResponseEntity} with a status code only.
	 * @param status the status code
	 */
	public ResponseEntity(HttpStatusCode status) {
		this(null, (HttpHeaders) null, status);
	}

	/**
	 * Create a {@code ResponseEntity} with a body and status code.
	 * @param body the entity body
	 * @param status the status code
	 */
	public ResponseEntity(T body, HttpStatusCode status) {
		this(body, (HttpHeaders) null, status);
	}

	/**
	 * Create a {@code ResponseEntity} with headers and a status code.
	 * @param headers the entity headers
	 * @param status the status code
	 */
	public ResponseEntity(HttpHeaders headers, HttpStatusCode status) {
		this(null, headers, status);
	}

	/**
	 * Create a {@code ResponseEntity} with a body, headers, and a raw status code.
	 * @param body the entity body
	 * @param headers the entity headers
	 * @param rawStatus the status code value
	 */
	public ResponseEntity(T body, HttpHeaders headers, int rawStatus) {
		this(body, headers, HttpStatusCode.valueOf(rawStatus));
	}

	/**
	 * Create a {@code ResponseEntity} with a body, headers, and a status code.
	 * @param body the entity body
	 * @param headers the entity headers
	 * @param statusCode the status code
	 */
	public ResponseEntity(T body, HttpHeaders headers, HttpStatusCode statusCode) {
		super(body, headers);
		if (statusCode == null) throw new IllegalArgumentException("HttpStatusCode must not be null");

		this.status = statusCode;
	}

	/**
	 * Return the HTTP status code of the response.
	 * @return the HTTP status as an HttpStatus enum entry
	 */
	public HttpStatusCode getStatusCode() {
		return this.status;
	}



	// Static builder methods

	/**
	 * Create a builder with the given status.
	 * @param status the response status
	 * @return the created builder
	 */
	public static BodyBuilder status(HttpStatusCode status) {
		if (status == null) throw new IllegalArgumentException("HttpStatusCode must not be null");
		return new DefaultMessageBuilder(status);
	}

	/**
	 * Create a builder with the given status.
	 * @param status the response status
	 * @return the created builder
	 */
	public static BodyBuilder status(int status) {
		return new DefaultMessageBuilder(status);
	}

	/**
	 * Create a builder with the status set to {@linkplain HttpStatus#OK OK}.
	 * @return the created builder
	 */
	public static BodyBuilder ok() {
		return status(HttpStatus.OK);
	}

	/**
	 * A shortcut for creating a {@code ResponseEntity} with the given body
	 * and the status set to {@linkplain HttpStatus#OK OK}.
	 * @param body the body of the response entity (possibly empty)
	 * @return the created {@code ResponseEntity}
	 */
	public static <T> ResponseEntity<T> ok( T body) {
		return ok().body(body);
	}

	/**
	 * A shortcut for creating a {@code ResponseEntity} with the given body
	 * and the {@linkplain HttpStatus#OK OK} status, or an empty body and a
	 * {@linkplain HttpStatus#NOT_FOUND NOT FOUND} status in case of an
	 * {@linkplain Optional#empty()} parameter.
	 * @return the created {@code ResponseEntity}
	 */
	public static <T> ResponseEntity<T> of(Optional<T> body) {
		if (body == null) throw new IllegalArgumentException("Body must not be null");
		return body.map(ResponseEntity::ok).orElseGet(() -> notFound().build());
	}

	/**
	 * A shortcut for creating a {@code ResponseEntity} with the given body
	 * and the {@linkplain HttpStatus#OK OK} status, or an empty body and a
	 * {@linkplain HttpStatus#NOT_FOUND NOT FOUND} status in case of a
	 * {@code null} parameter.
	 * @return the created {@code ResponseEntity}
	 */
	public static <T> ResponseEntity<T> ofNullable( T body) {
		if (body == null) {
			return notFound().build();
		}
		return ResponseEntity.ok(body);
	}

	/**
	 * Create a new builder with a {@linkplain HttpStatus#CREATED CREATED} status
	 * and a location header set to the given URI.
	 * @param location the location URI
	 * @return the created builder
	 */
	public static BodyBuilder created(URI location) {
		return status(HttpStatus.CREATED).location(location);
	}

	/**
	 * Create a builder with an {@linkplain HttpStatus#ACCEPTED ACCEPTED} status.
	 * @return the created builder
	 */
	public static BodyBuilder accepted() {
		return status(HttpStatus.ACCEPTED);
	}

	/**
	 * Create a builder with a {@linkplain HttpStatus#NO_CONTENT NO_CONTENT} status.
	 * @return the created builder
	 */
	public static HeadersBuilder<?> noContent() {
		return status(HttpStatus.NO_CONTENT);
	}

	/**
	 * Create a builder with a {@linkplain HttpStatus#BAD_REQUEST BAD_REQUEST} status.
	 * @return the created builder
	 */
	public static BodyBuilder badRequest() {
		return status(HttpStatus.BAD_REQUEST);
	}

	/**
	 * Create a builder with a {@linkplain HttpStatus#NOT_FOUND NOT_FOUND} status.
	 * @return the created builder
	 */
	public static HeadersBuilder<?> notFound() {
		return status(HttpStatus.NOT_FOUND);
	}

	/**
	 * Create a builder with an
	 * {@linkplain HttpStatus#UNPROCESSABLE_CONTENT UNPROCESSABLE_CONTENT} status.
	 * @return the created builder
	 */
	public static BodyBuilder unprocessableContent() {
		return status(HttpStatus.UNPROCESSABLE_CONTENT);
	}


	/**
	 * Create a builder with an
	 * {@linkplain HttpStatus#INTERNAL_SERVER_ERROR INTERNAL_SERVER_ERROR} status.
	 * @return the created builder
	 */
	public static BodyBuilder internalServerError() {
		return status(HttpStatus.INTERNAL_SERVER_ERROR);
	}


	/**
	 * Defines a builder that adds headers to the response entity.
	 * @param <B> the builder subclass
	 */
	public interface HeadersBuilder<B extends HeadersBuilder<B>> {

		/**
		 * Add the given, single header value under the given name.
		 * @param headerName the header name
		 * @param headerValues the header value(s)
		 * @return this builder
		 * @see HttpHeaders#add(String, String)
		 */
		B header(String headerName, String... headerValues);

		/**
		 * Copy the given headers into the entity's headers map.
		 * @param headers the existing HttpHeaders to copy from
		 * @return this builder
		 * @see HttpHeaders#add(String, String)
		 */
		B headers(HttpHeaders headers);

		/**
		 * Manipulate this entity's headers with the given consumer. The
		 * headers provided to the consumer are "live", so that the consumer can be used to
		 * {@linkplain HttpHeaders#set(String, String) overwrite} existing header values,
		 * {@linkplain HttpHeaders#remove(String) remove} values, or use any of the other
		 * {@link HttpHeaders} methods.
		 * @param headersConsumer a function that consumes the {@code HttpHeaders}
		 * @return this builder
		 */
		B headers(Consumer<HttpHeaders> headersConsumer);

		/**
		 * Set the location of a resource, as specified by the Location} header.
		 * @param location the location
		 * @return this builder
		 * @see HttpHeaders#setLocation(URI)
		 */
		B location(URI location);

		/**
		 * Build the response entity with no body.
		 * @return the response entity
		 * @see BodyBuilder#body(Object)
		 */
		<T> ResponseEntity<T> build();
	}


	/**
	 * Defines a builder that adds a body to the response entity.
	 */
	public interface BodyBuilder extends HeadersBuilder<BodyBuilder> {

		/**
		 * Set the length of the body in bytes, as specified by the
		 * {@code Content-Length} header.
		 * @param contentLength the content length
		 * @return this builder
		 * @see HttpHeaders#setContentLength(long)
		 */
		BodyBuilder contentLength(long contentLength);

		/**
		 * Set the of the body, as specified by the
		 * {@code Content-Type} header.
		 * @param contentType the content type
		 * @return this builder
		 */
		 BodyBuilder contentType(String contentType);

		/**
		 * Set the body of the response entity and returns it.
		 * @param <T> the type of the body
		 * @param body the body of the response entity
		 * @return the built response entity
		 */
		<T> ResponseEntity<T> body( T body);
	}



	private static class DefaultMessageBuilder implements BodyBuilder {

		private final HttpStatusCode statusCode;

		private final HttpHeaders headers = new HttpHeaders();

		public DefaultMessageBuilder(int statusCode) {
			this(HttpStatusCode.valueOf(statusCode));
		}

		public DefaultMessageBuilder(HttpStatusCode statusCode) {
			this.statusCode = statusCode;
		}

		@Override
		public BodyBuilder header(String headerName, String... headerValues) {
			for (String headerValue : headerValues) {
				this.headers.add(headerName, headerValue);
			}
			return this;
		}

		@Override
		public BodyBuilder headers(HttpHeaders headers) {
			if (headers != null) {
				this.headers.putAll(headers);
			}
			return this;
		}

		@Override
		public BodyBuilder headers(Consumer<HttpHeaders> headersConsumer) {
			headersConsumer.accept(this.headers);
			return this;
		}

		@Override
		public BodyBuilder contentLength(long contentLength) {
			this.headers.setContentLength(contentLength);
			return this;
		}

		@Override
		public BodyBuilder contentType(String contentType) {
			this.headers.setContentType(contentType);
			return this;
		}

		@Override
		public BodyBuilder location(URI location) {
			this.headers.setLocation(location);
			return this;
		}

		@Override
		public <T> ResponseEntity<T> build() {
			return body(null);
		}

		@Override
		public <T> ResponseEntity<T> body( T body) {
			return new ResponseEntity<>(body, this.headers, this.statusCode);
		}
	}

}
