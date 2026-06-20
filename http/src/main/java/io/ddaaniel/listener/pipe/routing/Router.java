package io.ddaaniel.listener.pipe.routing;


import io.ddaaniel.internal.parser.request.header.Headers;
import io.ddaaniel.internal.parser.request.mapper.Request;
import io.ddaaniel.internal.parser.request.response.Response;
import io.ddaaniel.internal.parser.request.response.status.ResponseStatusCode;
import io.ddaaniel.internal.parser.request.response.util.HttpMsg;
import io.ddaaniel.listener.pipe.routing.handlers.HttpBinHandler;
import io.ddaaniel.listener.pipe.routing.handlers.MyProblemHandler;
import io.ddaaniel.listener.pipe.routing.handlers.VideoStreamHandler;
import io.ddaaniel.listener.pipe.routing.handlers.YourProblemHandler;

/**
 * Router
 */
public abstract class Router {

	public static void route(Request req, Response res) {
		var target = req.RequestLine.RequestTarget;
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



	private static void handleNotFound(Request req, Headers headers, Response res) throws Exception {
		var errBody = HttpMsg.respond404().getBytes();
		headers.Replace("Content-Length", String.valueOf(errBody.length));
		headers.Replace("Content-Type", "text/html");

		res.WriteStatusLine(ResponseStatusCode.STATUS_NOT_FOUND);
		res.WriteHeaders(headers.h);
		res.WriteBody(errBody);
	}

	private static void handleInternalError(Headers headers, Response res, Exception err) {
		System.err.println(" -> Global Router Error: " + err.getMessage());
		try {
			var errBody = HttpMsg.respond500().getBytes();
			headers.Replace("Content-Length", String.valueOf(errBody.length));
			headers.Replace("Content-Type", "text/html");
			res.WriteStatusLine(ResponseStatusCode.STATUS_INTERNAL_SERVER_ERROR);
			res.WriteHeaders(headers.h);
			res.WriteBody(errBody);
		} catch (Exception critical) {
			critical.printStackTrace();
		}
	}

}
