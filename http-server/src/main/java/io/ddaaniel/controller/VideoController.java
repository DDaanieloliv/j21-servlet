package io.ddaaniel.controller;

import io.ddaaniel.annotations.GET;
import io.ddaaniel.internal.parser.request.mapper.Request;
import io.ddaaniel.internal.parser.request.response.Response;
import io.ddaaniel.internal.parser.request.response.status.ResponseStatusCode;

public class VideoController {

	@GET("/video")
	public static void playVideo(Request req, Response res) throws Exception {
		System.out.println("Rota /video interceptada com sucesso!");

		res.WriteStatusLine(ResponseStatusCode.STATUS_OK);
		res.WriteHeaders(res.DefaultHeaders(0).h);
		res.WriteBody("<html><body><h1>Video Streaming given by Controller!</h1></body></html>\n".getBytes());
	}
}
