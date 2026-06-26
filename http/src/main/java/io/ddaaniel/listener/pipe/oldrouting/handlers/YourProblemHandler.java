package io.ddaaniel.listener.pipe.oldrouting.handlers;

import io.ddaaniel.internal.httpEntity.entities.httpHeaders.HttpHeaders;
import io.ddaaniel.internal.httpStatus.HttpStatus;
import io.ddaaniel.internal.parser.request.Request;
import io.ddaaniel.internal.parser.response.Response;
import io.ddaaniel.internal.parser.util.HttpFun.FunHttp;

/**
 * YourProblemHandler
 */
public abstract class YourProblemHandler {

	public static void handleYourProblem(Request req, HttpHeaders headers, Response res) throws Exception {
		var body = FunHttp.respond400().getBytes();
		headers.set("Content-Length", String.valueOf(body.length));
		headers.set("Content-Type", "text/html");

		res.WriteStatusLine(HttpStatus.BAD_REQUEST);
		res.WriteHeaders(headers);
		res.WriteBody(body);
	}

	
}
