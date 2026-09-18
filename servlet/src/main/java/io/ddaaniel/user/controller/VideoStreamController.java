package io.ddaaniel.user.controller;

import io.ddaaniel.annotations.HTTP;
import io.ddaaniel.core.httpEntity.ResponseEntity;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import io.ddaaniel.core.httpEntity.httpHeaders.HttpHeaders;
import io.ddaaniel.core.httpStatus.HttpStatus;

/**
 * VideoStreamController
 */
public class VideoStreamController {

	@HTTP(method = "GET", path = "/videoStream")
	public static ResponseEntity<?> handleVideoStreaming() throws Exception {
		var videoPath = Path
				.of("/home/daniel/DEV_ENV/personal/dev/httpfromtcp/http/src/main/java/io/ddaaniel/user/assets/video.mp4");
		InputStream vInputStream = Files.newInputStream(videoPath);
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "video/mp4");
		return new ResponseEntity<>(vInputStream, headers, HttpStatus.OK);
	}
	
	@HTTP(method = "GET", path = "/video")
	public static ResponseEntity<?> playVideo() throws Exception {
		var videoPath = Path
				.of("/home/daniel/DEV_ENV/personal/dev/httpfromtcp/http/src/main/java/io/ddaaniel/user/assets/video.mp4");
		InputStream videoStream = Files.newInputStream(videoPath);
		long fileSize = Files.size(videoPath);

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "video/mp4");
		headers.set("Content-Length", String.valueOf(fileSize));
		headers.set("Connection", "keep-alive");

		return new ResponseEntity<>(videoStream, headers, HttpStatus.OK);
	}
}
