package io.ddaaniel.listener.pipe.oldrouting.handlers;

import io.ddaaniel.internal.parser.reader.HttpServletRequest;
import io.ddaaniel.internal.parser.writer.ServletWriter;
import io.ddaaniel.internal.support.HttpFun.FunHttp;
import io.ddaaniel.internal.support.httpEntity.httpHeaders.HttpHeaders;
import io.ddaaniel.internal.support.httpStatus.HttpStatus;

/**
 * MyProblemHandler
 */
public abstract class MyProblemHandler {

	public static void handleMyProblem(HttpServletRequest message, HttpHeaders headers, ServletWriter writer) throws Exception {
		var body = FunHttp.respond500().getBytes();
		headers.set("Content-Length", String.valueOf(body.length));
		headers.set("Content-Type", "text/html");

		writer.WriteStatusLine(HttpStatus.INTERNAL_SERVER_ERROR);
		writer.WriteHeaders(headers);
		writer.WriteBody(body);
	}

}
