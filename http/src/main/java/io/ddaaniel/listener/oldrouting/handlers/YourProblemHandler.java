package io.ddaaniel.listener.oldrouting.handlers;

import io.ddaaniel.core.httpEntity.httpHeaders.HttpHeaders;
import io.ddaaniel.core.httpStatus.HttpStatus;
import io.ddaaniel.internal.parser.reader.DefaultHttpServletRequest;
import io.ddaaniel.internal.parser.writer.DefaultServletWriter;
import io.ddaaniel.internal.support.HttpFun.FunHttp;

/**
 * YourProblemHandler
 */
public abstract class YourProblemHandler {

	public static void handleYourProblem(DefaultHttpServletRequest reader, HttpHeaders headers, DefaultServletWriter wirter) throws Exception {
		var body = FunHttp.respond400().getBytes();
		headers.set("Content-Length", String.valueOf(body.length));
		headers.set("Content-Type", "text/html");

		wirter.WriteStatusLine(HttpStatus.BAD_REQUEST);
		wirter.WriteHeaders(headers);
		wirter.WriteBody(body);
	}

	
}
