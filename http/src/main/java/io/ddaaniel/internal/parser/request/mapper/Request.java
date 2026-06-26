package io.ddaaniel.internal.parser.request.mapper;

import io.ddaaniel.internal.parser.request.header.HeaderHandler;
import io.ddaaniel.internal.parser.request.mapper.state.ParsingState;

/**
 * Request
 */
public class Request {
	public RequestLine RequestLine;
	public HeaderHandler Headers;
	public String Body;
	public ParsingState State;

	public Request(){
		this.Headers = new HeaderHandler();
	}

	public Request(RequestLine r, ParsingState s, HeaderHandler h, String b) {
		this.RequestLine = r;
		this.Headers = h;
		this.State = s;
		this.Body = b;
	}
}
