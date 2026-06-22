package io.ddaaniel.listener.pipe.oldrouting.handlers;

import io.ddaaniel.internal.parser.request.header.Headers;
import io.ddaaniel.internal.parser.request.mapper.Request;
import io.ddaaniel.internal.parser.request.response.Response;
import io.ddaaniel.internal.parser.request.response.status.HttpStatus;
import io.ddaaniel.internal.parser.request.response.status.util.FunHttp;

/**
 * YourProblemHandler
 */
public abstract class YourProblemHandler {

	public static void handleYourProblem(Request req, Headers headers, Response res) throws Exception {
		var body = FunHttp.respond400().getBytes();
		headers.Replace("Content-Length", String.valueOf(body.length));
		headers.Replace("Content-Type", "text/html");

		res.WriteStatusLine(HttpStatus.STATUS_BAD_REQUEST);
		res.WriteHeaders(headers.h);
		res.WriteBody(body);
	}

	
}
