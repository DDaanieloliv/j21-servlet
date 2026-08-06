package io.ddaaniel.listener.internal;

import io.ddaaniel.core.httpStatus.HttpStatusCode;
import io.ddaaniel.listener.internal.valueObjects.HttpVersion;

/**
 * HttpResponse
 */
public interface HttpResponse extends HttpMessage{

	/**
	 * Returns the status of this {@link HttpResponse}.
	 *
	 * @return The {@link HttpResponseStatus} of this {@link HttpResponse}
	 */
	HttpStatusCode status();

	/**
	 * Set the status of this {@link HttpResponse}.
	 */
	HttpResponse setStatus(HttpStatusCode status);

	@Override
	HttpResponse setProtocolVersion(HttpVersion version);
}
