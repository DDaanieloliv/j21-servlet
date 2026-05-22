package io.ddaaniel.internal.parser.request.message;

import io.ddaaniel.internal.parser.header.Header;
import io.ddaaniel.internal.parser.request.message.enums.ParseState;

/**
 * Requests
 */
public class Requests {
	public RequestLine RequestLine;
	public Header Headers;
	public ParseState State;

	public Requests(){}

	public Requests(RequestLine r, ParseState s, Header h) {
		this.RequestLine = r;
		this.Headers = h;
		this.State = s;
	}
}
