package io.ddaaniel.listener.internal;


import io.ddaaniel.core.httpEntity.httpHeaders.HttpHeaders;
import io.ddaaniel.listener.internal.valueObjects.HttpVersion;

/**
 * DefualtHttpMessage
 */
public abstract class DefaultHttpMessage implements HttpMessage {

	private HttpVersion version;
	private final HttpHeaders headers;

	protected DefaultHttpMessage (final HttpVersion v, HttpHeaders h) {
		if (v == null) {
			throw new IllegalArgumentException("HttpVersion must not be null");
		}
		if (h == null) {
			throw new IllegalArgumentException("HttpHeaders must not be null");
		}
		this.version = v;
		this.headers = h;
	}

	@Override
	public HttpHeaders headers() {
		return headers;
	}

	@Override
	public HttpVersion protocolVersion() {
		return version;
	}

	@Override
	public HttpMessage setProtocolVersion(HttpVersion v) {
		this.version = v;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof DefaultHttpMessage)) {
			return false;
		}

		DefaultHttpMessage m = (DefaultHttpMessage) o;
		return protocolVersion().equals(m.protocolVersion()) && 
			headers().equals(m.headers());
	}
}
