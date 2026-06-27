package io.ddaaniel.listener.pipe.oldrouting.handlers;

import io.ddaaniel.internal.httpEntity.entities.httpHeaders.HttpHeaders;
import io.ddaaniel.internal.httpStatus.HttpStatus;
import io.ddaaniel.internal.parser.reader.ServletReader;
import io.ddaaniel.internal.parser.util.HttpFun.FunHttp;
import io.ddaaniel.internal.parser.writer.ServletWriter;

/**
 * YourProblemHandler
 */
public abstract class YourProblemHandler {

	public static void handleYourProblem(ServletReader reader, HttpHeaders headers, ServletWriter wirter) throws Exception {
		var body = FunHttp.respond400().getBytes();
		headers.set("Content-Length", String.valueOf(body.length));
		headers.set("Content-Type", "text/html");

		wirter.WriteStatusLine(HttpStatus.BAD_REQUEST);
		wirter.WriteHeaders(headers);
		wirter.WriteBody(body);
	}

	
}
