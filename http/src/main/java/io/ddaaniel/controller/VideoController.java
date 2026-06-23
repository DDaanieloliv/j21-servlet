package io.ddaaniel.controller;

import io.ddaaniel.annotations.HTTP;
import io.ddaaniel.internal.parser.util.HttpFun.FunHttp;

public class VideoController {

	@HTTP("/video")
	public static String playVideo() throws Exception {
		System.out.println("Rota /video interceptada com sucesso!");
		return FunHttp.respond404();
	}
}
