package io.ddaaniel.internal.parser.reader;

import java.io.InputStream;

import io.ddaaniel.core.httpEntity.httpHeaders.HttpHeaders;

/**
 * DefaultHttpServletRequest
 */
public record DefaultHttpServletRequest(
		String method, 
		String uri, 
		HttpHeaders headers, 
		InputStream body
		) { }

class HttpRequestBuilder {
	private String method;
	private String uri;
	private final HttpHeaders headers = new HttpHeaders();
	private InputStream body;

	public HttpRequestBuilder method(String method) { this.method = method; return this; }
	public HttpRequestBuilder uri(String uri) { this.uri = uri; return this; }
	public HttpHeaders headers() { return this.headers; }
	public HttpRequestBuilder body(InputStream body) { this.body = body; return this; }

	public DefaultHttpServletRequest build() {
		return new DefaultHttpServletRequest(
				this.method, 
				this.uri, 
				this.headers, 
				this.body != null ? this.body : new java.io.ByteArrayInputStream(new byte[0])
				);
	}
}
