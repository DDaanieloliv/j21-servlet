package io.ddaaniel.internal.parser.message;

import io.ddaaniel.internal.parser.message.state.ParseState;

/**
 * Request
 */
public class Request {
	public RequestLine requestLine;
	public ParseState state;

	public Request(){}

	public Request(RequestLine r, ParseState s) {
		this.requestLine = r;
		this.state = s;
	}
}
