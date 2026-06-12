package io.ddaaniel.listener.pipe.sample;

import io.ddaaniel.internal.parser.request.header.Headers;
import io.ddaaniel.internal.parser.request.mapper.Request;
import io.ddaaniel.internal.parser.request.response.Response;

/**
 * HttpBin
 */
public abstract class HttpBin {

	public void HttpStreamRes(Request req, Headers headers, Response res) {
	}
}
