package io.ddaaniel.listener.pipe.oldrouting;


import io.ddaaniel.internal.parser.reader.HttpServletRequest;
import io.ddaaniel.internal.parser.writer.ServletWriter;
import io.ddaaniel.internal.support.HttpFun.FunHttp;
import io.ddaaniel.internal.support.httpEntity.httpHeaders.HttpHeaders;
import io.ddaaniel.internal.support.httpStatus.HttpStatus;
import io.ddaaniel.listener.pipe.oldrouting.handlers.HttpBinHandler;
import io.ddaaniel.listener.pipe.oldrouting.handlers.MyProblemHandler;
import io.ddaaniel.listener.pipe.oldrouting.handlers.VideoStreamHandler;
import io.ddaaniel.listener.pipe.oldrouting.handlers.YourProblemHandler;

/**
 * Router
 */
public abstract class Router {

	public static void route(HttpServletRequest message, ServletWriter writer) {
		var target = message.uri();
		var headers = writer.DefaultHeaders(0);

		try {
			switch (target) {
				case "/video" -> VideoStreamHandler.handleVideoStreaming(message, headers, writer);
				case "/yourproblem" -> YourProblemHandler.handleYourProblem(message, headers, writer);
				case "/myproblem" -> MyProblemHandler.handleMyProblem(message, headers, writer);
				case "/httpbin/stream" -> HttpBinHandler.HttpStreamRes(message, headers, writer); 
				default ->  handleNotFound(message, headers, writer);
			}
		} catch (Exception e) {
			if (e instanceof java.io.IOException || (e.getCause() != null && e.getCause() instanceof java.io.IOException)) {
				System.out.println(" -> Client close the connection prematurely (Pipe Broken).");
			} else {
				handleInternalError(headers, writer, e);
			}
		}
	}



	private static void handleNotFound(HttpServletRequest message, HttpHeaders headers, ServletWriter writer) throws Exception {
		var errBody = FunHttp.respond404().getBytes();
		headers.set("Content-Length", String.valueOf(errBody.length));
		headers.set("Content-Type", "text/html");

		writer.WriteStatusLine(HttpStatus.NOT_FOUND);
		writer.WriteHeaders(headers);
		writer.WriteBody(errBody);
	}

	private static void handleInternalError(HttpHeaders headers, ServletWriter writer, Exception err) {
		System.err.println(" -> Global Router Error: " + err.getMessage());
		try {
			var errBody = FunHttp.respond500().getBytes();
			headers.set("Content-Length", String.valueOf(errBody.length));
			headers.set("Content-Type", "text/html");
			writer.WriteStatusLine(HttpStatus.INTERNAL_SERVER_ERROR);
			writer.WriteHeaders(headers);
			writer.WriteBody(errBody);
		} catch (Exception critical) {
			critical.printStackTrace();
		}
	}

}
