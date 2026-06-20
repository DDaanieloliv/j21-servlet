package io.ddaaniel.internal.parser.request.mapper;

import io.ddaaniel.internal.parser.request.header.Headers;
import io.ddaaniel.internal.parser.request.mapper.state.ParsingState;

/**
 * Request
 */
public class Request {
	public RequestLine RequestLine;
	public Headers Headers;
	public ParsingState State;
	public String Body;

	public Request(){
		this.Headers = new Headers();
	}

	public Request(RequestLine r, ParsingState s, Headers h, String b) {
		this.RequestLine = r;
		this.Headers = h;
		this.State = s;
		this.Body = b;
	}
}
