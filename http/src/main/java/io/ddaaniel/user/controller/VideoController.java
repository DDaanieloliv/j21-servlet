package io.ddaaniel.user.controller;

import io.ddaaniel.annotations.HTTP;
import io.ddaaniel.core.httpEntity.ResponseEntity;

public class VideoController {

	@HTTP("/video")
	public static ResponseEntity<?> playVideo() throws Exception {
		System.out.println("Rota /video interceptada com sucesso!");
		return ResponseEntity.ok("\nYour request was an absolute banger.\n");
	}
}
