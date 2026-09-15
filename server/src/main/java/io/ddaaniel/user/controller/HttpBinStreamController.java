package io.ddaaniel.user.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import io.ddaaniel.annotations.HTTP;
import io.ddaaniel.core.httpEntity.ResponseEntity;
import io.ddaaniel.core.httpEntity.httpHeaders.HttpHeaders;
import io.ddaaniel.core.httpStatus.HttpStatus;

/**
 * HttpBinStreamController
 */
public abstract class HttpBinStreamController {

	@HTTP(method = "GET", path = "/stream")
	public static ResponseEntity<?> HttpStreamRes() throws Exception {
		byte[] mockdata = "[this is a test data to chunked-encoding behavior]".getBytes(StandardCharsets.UTF_8);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for (int i = 0; i < 300000; i++) {
			baos.write(mockdata, 0, mockdata.length);
		}
		
		var resOut = new ByteArrayInputStream(baos.toByteArray());
        var headers = new HttpHeaders();
		headers.set("Content-Type", "plain/text");

		return new ResponseEntity<>(resOut, headers, HttpStatus.OK);
	}
}
