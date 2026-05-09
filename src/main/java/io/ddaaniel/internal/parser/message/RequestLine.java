package io.ddaaniel.internal.parser.message;


/**
 * RequestLine
 *
 */
public record RequestLine (
	String Method,
	String RequestTarget,
	String HttpVersion
) { }
