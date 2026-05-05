package io.ddaaniel.internal.request;

/**
 * RequestLine
 */
record RequestLine(
		String HttpVersion,
		String RequestTarget,
		String Method) { }


/**
 * Request
 */
public record Request(RequestLine RequestLine) { }
