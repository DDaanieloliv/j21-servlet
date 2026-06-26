package io.ddaaniel.listener.pipe.oldrouting;


import io.ddaaniel.internal.httpEntity.entities.httpHeaders.HttpHeaders;
import io.ddaaniel.internal.httpStatus.HttpStatus;
import io.ddaaniel.internal.parser.request.Request;
import io.ddaaniel.internal.parser.response.Response;
import io.ddaaniel.internal.parser.util.HttpFun.FunHttp;
import io.ddaaniel.listener.pipe.oldrouting.handlers.HttpBinHandler;
import io.ddaaniel.listener.pipe.oldrouting.handlers.MyProblemHandler;
import io.ddaaniel.listener.pipe.oldrouting.handlers.VideoStreamHandler;
import io.ddaaniel.listener.pipe.oldrouting.handlers.YourProblemHandler;

/**
 * Router
 */
public abstract class Router {

	public static void route(Request req, Response res) {
		var target = req.uriWrap;
		var headers = res.DefaultHeaders(0);

		try {
			switch (target) {
				case "/video" -> VideoStreamHandler.handleVideoStreaming(req, headers, res);
				case "/yourproblem" -> YourProblemHandler.handleYourProblem(req, headers, res);
				case "/myproblem" -> MyProblemHandler.handleMyProblem(req, headers, res);
				case "/httpbin/stream" -> HttpBinHandler.HttpStreamRes(req, headers, res); 
				default ->  handleNotFound(req, headers, res);
			}
		} catch (Exception e) {
			if (e instanceof java.io.IOException || (e.getCause() != null && e.getCause() instanceof java.io.IOException)) {
				System.out.println(" -> Client close the connection prematurely (Pipe Broken).");
			} else {
				handleInternalError(headers, res, e);
			}
		}
	}



	private static void handleNotFound(Request req, HttpHeaders headers, Response res) throws Exception {
		var errBody = FunHttp.respond404().getBytes();
		headers.set("Content-Length", String.valueOf(errBody.length));
		headers.set("Content-Type", "text/html");

		res.WriteStatusLine(HttpStatus.NOT_FOUND);
		res.WriteHeaders(headers);
		res.WriteBody(errBody);
	}

	private static void handleInternalError(HttpHeaders headers, Response res, Exception err) {
		System.err.println(" -> Global Router Error: " + err.getMessage());
		try {
			var errBody = FunHttp.respond500().getBytes();
			headers.set("Content-Length", String.valueOf(errBody.length));
			headers.set("Content-Type", "text/html");
			res.WriteStatusLine(HttpStatus.INTERNAL_SERVER_ERROR);
			res.WriteHeaders(headers);
			res.WriteBody(errBody);
		} catch (Exception critical) {
			critical.printStackTrace();
		}
	}

}
