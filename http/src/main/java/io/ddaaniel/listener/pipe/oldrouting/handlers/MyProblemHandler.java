package io.ddaaniel.listener.pipe.oldrouting.handlers;

import io.ddaaniel.internal.httpEntity.entities.httpHeaders.HttpHeaders;
import io.ddaaniel.internal.httpStatus.HttpStatus;
import io.ddaaniel.internal.parser.reader.ServletReader;
import io.ddaaniel.internal.parser.util.HttpFun.FunHttp;
import io.ddaaniel.internal.parser.writer.ServletWriter;

/**
 * MyProblemHandler
 */
public abstract class MyProblemHandler {

	public static void handleMyProblem(ServletReader reader, HttpHeaders headers, ServletWriter writer) throws Exception {
		var body = FunHttp.respond500().getBytes();
		headers.set("Content-Length", String.valueOf(body.length));
		headers.set("Content-Type", "text/html");

		writer.WriteStatusLine(HttpStatus.INTERNAL_SERVER_ERROR);
		writer.WriteHeaders(headers);
		writer.WriteBody(body);
	}

}
