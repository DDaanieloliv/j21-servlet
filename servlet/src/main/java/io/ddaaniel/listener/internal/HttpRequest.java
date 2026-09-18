package io.ddaaniel.listener.internal;

import io.ddaaniel.listener.internal.valueObjects.HttpMethod;
import io.ddaaniel.listener.internal.valueObjects.HttpVersion;

/**
 * HttpRequest
 */
public interface HttpRequest extends HttpMessage {

	/**
	 * Returns the {@link HttpMethod} of this {@link HttpRequest}.
	 *
	 * @return The {@link HttpMethod} of this {@link HttpRequest}
	 */
	HttpMethod method();

	/**
	 * Set the {@link HttpMethod} of this {@link HttpRequest}.
	 */
	HttpRequest setMethod(HttpMethod method);

	/**
	 * Returns the requested URI (or alternatively, path)
	 *
	 * @return The URI being requested
	 */
	String uri();

	/**
	 *  Set the requested URI (or alternatively, path)
	 */
	HttpRequest setUri(String uri);

	@Override
	HttpRequest setProtocolVersion(HttpVersion version);
}
