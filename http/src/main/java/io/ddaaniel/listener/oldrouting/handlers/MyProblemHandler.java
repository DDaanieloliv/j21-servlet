package io.ddaaniel.listener.oldrouting.handlers;

import io.ddaaniel.core.httpEntity.httpHeaders.HttpHeaders;
import io.ddaaniel.core.httpStatus.HttpStatus;
import io.ddaaniel.internal.parser.reader.DefaultHttpServletRequest;
import io.ddaaniel.internal.parser.writer.DefaultServletWriter;
import io.ddaaniel.internal.support.HttpFun.FunHttp;

/**
 * MyProblemHandler
 */
public abstract class MyProblemHandler {

	public static void handleMyProblem(DefaultHttpServletRequest message, HttpHeaders headers, DefaultServletWriter writer) throws Exception {
		var body = FunHttp.respond500().getBytes();
		headers.set("Content-Length", String.valueOf(body.length));
		headers.set("Content-Type", "text/html");

		writer.WriteStatusLine(HttpStatus.INTERNAL_SERVER_ERROR);
		writer.WriteHeaders(headers);
		writer.WriteBody(body);
	}

}
