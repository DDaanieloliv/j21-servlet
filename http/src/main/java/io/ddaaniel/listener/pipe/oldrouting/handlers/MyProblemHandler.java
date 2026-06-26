package io.ddaaniel.listener.pipe.oldrouting.handlers;

import io.ddaaniel.internal.httpEntity.entities.httpHeaders.HttpHeaders;
import io.ddaaniel.internal.httpStatus.HttpStatus;
import io.ddaaniel.internal.parser.request.Request;
import io.ddaaniel.internal.parser.response.Response;
import io.ddaaniel.internal.parser.util.HttpFun.FunHttp;

/**
 * MyProblemHandler
 */
public abstract class MyProblemHandler {

	public static void handleMyProblem(Request req, HttpHeaders headers, Response res) throws Exception {
		var body = FunHttp.respond500().getBytes();
		headers.set("Content-Length", String.valueOf(body.length));
		headers.set("Content-Type", "text/html");

		res.WriteStatusLine(HttpStatus.INTERNAL_SERVER_ERROR);
		res.WriteHeaders(headers);
		res.WriteBody(body);
	}

}
