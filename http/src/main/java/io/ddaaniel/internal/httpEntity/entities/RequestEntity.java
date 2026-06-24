package io.ddaaniel.internal.httpEntity.entities;

import java.net.URI;
import java.util.Map;
import java.util.function.Consumer;

import io.ddaaniel.internal.httpEntity.entities.httpHeaders.HttpHeaders;

/**
 * RequestEntity
 */
public class RequestEntity<T> extends HttpEntity<T> {

	private final String method;

	private final URI target;

	/**
	 * Constructor with method and URL but without body nor headers.
	 * @param method the method
	 * @param url the URL
	 */
	public RequestEntity(String method, URI target) {
		this(null, (HttpHeaders) null, method, target);
	}

	/**
	 * Constructor with method, URL and body but without headers.
	 * @param body the body
	 * @param method the method
	 * @param url the URL
	 */
	public RequestEntity(T body, String method, URI url) {
		this(body, (HttpHeaders) null, method, url);
	}

	/**
	 * Constructor with method, URL and headers but without body.
	 * @param headers the headers
	 * @param method the method
	 * @param url the URL
	 */
	public RequestEntity(HttpHeaders headers, String method, URI url) {
		this(null, headers, method, url);
	}

	/**
	 * Constructor with method, URL, headers, body and type.
	 * @param body the body
	 * @param headers the headers
	 * @param method the method
	 * @param url the URL
	 */
	public RequestEntity(T body, HttpHeaders headers, String method, URI target) {
		super(body, headers);
		this.method = method;
		this.target = target;
	}

	/**
	 * Return the {@link URI} for the target HTTP endpoint.
	 * <p><strong>Note:</strong> This method raises
	 * {@link UnsupportedOperationException} if the {@code RequestEntity} was
	 * created with a URI template and variables rather than with a {@link URI}
	 * instance. This is because a URI cannot be created without further input
	 * on how to expand template and encode the URI. In such cases, the
	 * {@code URI} is prepared by the
	 * {@link org.springframework.web.client.RestTemplate} with the help of the
	 * {@link org.springframework.web.util.UriTemplateHandler} it is configured with.
	 */
	public URI getUrl() {
		if (this.target == null) {
			throw new UnsupportedOperationException(
					"The RequestEntity was created with a URI template and variables, " +
							"and there is not enough information on how to correctly expand and " +
							"encode the URI template.");
		}
		return this.target;
	}

	/**
	 * Return the HTTP method of the request.
	 * @return the HTTP method as an string value
	 */
	public String getMethod() {
		return this.method;
	}

	// Static builder methods

	/**
	 * Create a builder with the given method and url.
	 * @param method the HTTP method (GET, POST, etc)
	 * @param url the URL
	 * @return the created builder
	 */
	public static BodyBuilder method(String method, URI url) {
		return new DefaultMessageBuilder(method, url);
	}

	/**
	 * Create a builder with the given HTTP method, URI template, and variables.
	 * @param method the HTTP method (GET, POST, etc)
	 * @param uriTemplate the uri template to use
	 * @param uriVariables variables to expand the URI template with
	 * @return the created builder
	 */
	public static BodyBuilder method(String method, String uriTemplate, Object... uriVariables) {
		return new DefaultMessageBuilder(method, uriTemplate, uriVariables);
	}

	/**
	 * Create a builder with the given HTTP method, URI template, and variables.
	 * @param method the HTTP method (GET, POST, etc)
	 * @param uriTemplate the uri template to use
	 * @return the created builder
	 */
	public static BodyBuilder method(String method, String uriTemplate, Map<String, ?> uriVariables) {
		return new DefaultMessageBuilder(method, uriTemplate, uriVariables);
	}


	/**
	 * Create an HTTP GET builder with the given url.
	 * @param url the URL
	 * @return the created builder
	 */
	public static HeadersBuilder<?> get(URI url) {
		return method("GET", url);
	}

	/**
	 * Create an HTTP GET builder with the given string base uri template.
	 * @param uriTemplate the uri template to use
	 * @param uriVariables variables to expand the URI template with
	 * @return the created builder
	 */
	public static HeadersBuilder<?> get(String uriTemplate, Object... uriVariables) {
		return method("GET", uriTemplate, uriVariables);
	}

	/**
	 * Create an HTTP HEAD builder with the given url.
	 * @param url the URL
	 * @return the created builder
	 */
	public static HeadersBuilder<?> head(URI url) {
		return method("HEAD", url);
	}

	/**
	 * Create an HTTP HEAD builder with the given string base uri template.
	 * @param uriTemplate the uri template to use
	 * @param uriVariables variables to expand the URI template with
	 * @return the created builder
	 */
	public static HeadersBuilder<?> head(String uriTemplate, Object... uriVariables) {
		return method("HEAD", uriTemplate, uriVariables);
	}

	/**
	 * Create an HTTP POST builder with the given url.
	 * @param url the URL
	 * @return the created builder
	 */
	public static BodyBuilder post(URI url) {
		return method("POST", url);
	}

	/**
	 * Create an HTTP POST builder with the given string base uri template.
	 * @param uriTemplate the uri template to use
	 * @param uriVariables variables to expand the URI template with
	 * @return the created builder
	 */
	public static BodyBuilder post(String uriTemplate, Object... uriVariables) {
		return method("POST", uriTemplate, uriVariables);
	}

	/**
	 * Create an HTTP PUT builder with the given url.
	 * @param url the URL
	 * @return the created builder
	 */
	public static BodyBuilder put(URI url) {
		return method("PUT", url);
	}

	/**
	 * Create an HTTP PUT builder with the given string base uri template.
	 * @param uriTemplate the uri template to use
	 * @param uriVariables variables to expand the URI template with
	 * @return the created builder
	 */
	public static BodyBuilder put(String uriTemplate, Object... uriVariables) {
		return method("PUT", uriTemplate, uriVariables);
	}

	/**
	 * Create an HTTP PATCH builder with the given url.
	 * @param url the URL
	 * @return the created builder
	 */
	public static BodyBuilder patch(URI url) {
		return method("PATCH", url);
	}

	/**
	 * Create an HTTP PATCH builder with the given string base uri template.
	 * @param uriTemplate the uri template to use
	 * @param uriVariables variables to expand the URI template with
	 * @return the created builder
	 */
	public static BodyBuilder patch(String uriTemplate, Object... uriVariables) {
		return method("PATCH", uriTemplate, uriVariables);
	}

	/**
	 * Create an HTTP DELETE builder with the given url.
	 * @param url the URL
	 * @return the created builder
	 */
	public static HeadersBuilder<?> delete(URI url) {
		return method("DELETE", url);
	}

	/**
	 * Create an HTTP DELETE builder with the given string base uri template.
	 * @param uriTemplate the uri template to use
	 * @param uriVariables variables to expand the URI template with
	 * @return the created builder
	 */
	public static HeadersBuilder<?> delete(String uriTemplate, Object... uriVariables) {
		return method("DELETE", uriTemplate, uriVariables);
	}

	/**
	 * Creates an HTTP OPTIONS builder with the given url.
	 * @param url the URL
	 * @return the created builder
	 */
	public static HeadersBuilder<?> options(URI url) {
		return method("OPTIONS", url);
	}

	/**
	 * Creates an HTTP OPTIONS builder with the given string base uri template.
	 * @param uriTemplate the uri template to use
	 * @param uriVariables variables to expand the URI template with
	 * @return the created builder
	 */
	public static HeadersBuilder<?> options(String uriTemplate, Object... uriVariables) {
		return method("OPTIONS", uriTemplate, uriVariables);
	}



	/**
	 * Defines a builder that adds headers to the request entity.
	 * @param <B> the builder subclass
	 */
	public interface HeadersBuilder<B extends HeadersBuilder<B>> {

		/**
		 * Add the given, single header value under the given name.
		 * @param headerName  the header name
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
		 * Builds the request entity with no body.
		 * @return the request entity
		 * @see BodyBuilder#body(Object)
		 */
		RequestEntity<Void> build();
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
		 * Set the media type of the body, as specified
		 * by the {@code Content-Type} header.
		 * @param contentType the content type
		 * @return this builder
		 * @see HttpHeaders#setContentType(MediaType)
		 */
		BodyBuilder contentType(String contentType);

		/**
		 * Set the body of the request entity and build the RequestEntity.
		 * @param <T> the type of the body
		 * @param body the body of the request entity
		 * @return the built request entity
		 */
		<T> RequestEntity<T> body(T body);
	}


	private static class DefaultMessageBuilder implements BodyBuilder {

		private final String method;

		private final HttpHeaders headers = new HttpHeaders();

		private final URI uri;

		private final String uriTemplate;

		DefaultMessageBuilder(String method, URI url) {
			this.method = method;
			this.uri = url;
			this.uriTemplate = null;
		}

		DefaultMessageBuilder(String method, String uriTemplate, Object... uriVars) {
			this.method = method;
			this.uri = null;
			this.uriTemplate = uriTemplate;
		}

		DefaultMessageBuilder(String method, String uriTemplate, Map<String, ? extends Object> uriVars) {
			this.method = method;
			this.uri = null;
			this.uriTemplate = uriTemplate;
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
		public RequestEntity<Void> build() {
			return buildInternal(null);
		}

		@Override
		public <T> RequestEntity<T> body(T body) {
			return buildInternal(body);
		}

		private <T> RequestEntity<T> buildInternal(T body) {
			if (this.uri != null) {
				return new RequestEntity<>(body, this.headers, this.method, this.uri);
			}
			else if (this.uriTemplate != null) {
				URI resolvedUri = URI.create(this.uriTemplate); 

				return new RequestEntity<>(body, this.headers, this.method, resolvedUri);
			}
			else {
				throw new IllegalStateException("Neither URI nor URI template");
			}
		}
	}
}
