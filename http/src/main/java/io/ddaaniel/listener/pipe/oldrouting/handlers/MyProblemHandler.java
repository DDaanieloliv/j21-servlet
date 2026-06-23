package io.ddaaniel.listener.pipe.oldrouting.handlers;

import io.ddaaniel.internal.httpStatus.HttpStatus;
import io.ddaaniel.internal.parser.request.header.Headers;
import io.ddaaniel.internal.parser.request.mapper.Request;
import io.ddaaniel.internal.parser.request.response.Response;
import io.ddaaniel.internal.parser.util.HttpFun.FunHttp;

/**
 * MyProblemHandler
 */
public abstract class MyProblemHandler {

	public static void handleMyProblem(Request req, Headers headers, Response res) throws Exception {
		var body = FunHttp.respond500().getBytes();
		headers.Replace("Content-Length", String.valueOf(body.length));
		headers.Replace("Content-Type", "text/html");

		res.WriteStatusLine(HttpStatus.INTERNAL_SERVER_ERROR);
		res.WriteHeaders(headers.h);
		res.WriteBody(body);
	}

}
