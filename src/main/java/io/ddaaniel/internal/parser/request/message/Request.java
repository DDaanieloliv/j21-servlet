package io.ddaaniel.internal.parser.request.message;

import io.ddaaniel.internal.parser.header.Headers;
import io.ddaaniel.internal.parser.request.message.enums.ParseState;

/**
 * Request
 */
public class Request {
	public RequestLine RequestLine;
	public Headers Headers;
	public ParseState State;
	public String Body;

	public Request(){
		this.Headers = new Headers();
	}

	public Request(RequestLine r, ParseState s, Headers h, String b) {
		this.RequestLine = r;
		this.Headers = h;
		this.State = s;
		this.Body = b;
	}
}
