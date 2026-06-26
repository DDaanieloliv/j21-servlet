package io.ddaaniel.listener.pipe.oldrouting.handlers;

import io.ddaaniel.internal.httpStatus.HttpStatus;
import io.ddaaniel.internal.parser.request.header.HeaderHandler;
import io.ddaaniel.internal.parser.request.mapper.Request;
import io.ddaaniel.internal.parser.request.response.Response;
import io.ddaaniel.internal.parser.util.HttpFun.FunHttp;

/**
 * YourProblemHandler
 */
public abstract class YourProblemHandler {

	public static void handleYourProblem(Request req, HeaderHandler headers, Response res) throws Exception {
		var body = FunHttp.respond400().getBytes();
		headers.Replace("Content-Length", String.valueOf(body.length));
		headers.Replace("Content-Type", "text/html");

		res.WriteStatusLine(HttpStatus.BAD_REQUEST);
		res.WriteHeaders(headers.h);
		res.WriteBody(body);
	}

	
}
