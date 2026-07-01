package io.ddaaniel.controller;

import io.ddaaniel.annotations.HTTP;
import io.ddaaniel.internal.support.HttpFun.FunHttp;
import io.ddaaniel.internal.support.httpEntity.ResponseEntity;

public class VideoController {

	@HTTP("/video")
	public static ResponseEntity<?> playVideo() throws Exception {
		System.out.println("Rota /video interceptada com sucesso!");
		return ResponseEntity.ok(FunHttp.respond200());
	}
}
