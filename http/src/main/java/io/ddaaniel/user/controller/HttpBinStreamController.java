package io.ddaaniel.user.controller;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import io.ddaaniel.annotations.HTTP;
import io.ddaaniel.core.httpEntity.ResponseEntity;
import io.ddaaniel.core.httpEntity.httpHeaders.HttpHeaders;
import io.ddaaniel.core.httpStatus.HttpStatus;

/**
 * HttpBinStreamController
 */
public abstract class HttpBinStreamController {

	@HTTP("/stream")
	public static ResponseEntity<?> HttpStreamRes() throws Exception {
		HttpClient client = HttpClient.newHttpClient();
		var reqOut = HttpRequest.newBuilder()
			.uri(URI.create("https://httpbin.org/stream/5"))
			.GET()
			.build();
		var resOut = client.send(reqOut, HttpResponse.BodyHandlers.ofInputStream());

        var headers = new HttpHeaders();
        
		String contentType = resOut.headers().firstValue("Content-Type").orElse("application/json");
		headers.set("Content-Type", contentType);

		resOut.headers().firstValue("Content-Length")
			.ifPresent(length -> headers.set("Content-Length", length));

		var upstreamStatus = HttpStatus.valueOf(resOut.statusCode());

		return new ResponseEntity<>(resOut.body(), headers, upstreamStatus);
	}
}
