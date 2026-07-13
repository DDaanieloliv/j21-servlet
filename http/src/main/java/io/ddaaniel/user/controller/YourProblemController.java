package io.ddaaniel.user.controller;

import io.ddaaniel.annotations.HTTP;
import io.ddaaniel.core.httpEntity.ResponseEntity;
import io.ddaaniel.core.httpEntity.httpHeaders.HttpHeaders;
import io.ddaaniel.core.httpStatus.HttpStatus;

/**
 * YourProblemController
 */
public abstract class YourProblemController {

	@HTTP("/yourproblem")
	public static ResponseEntity<?> handleYourProblem() throws Exception {
		var body = 
			"<html>" +
			"<head>" +
			"<title>400 Bad Request</title>" +
			"</head>" +
			"<body>" +
			"<h1>Bad Request</h1>" +
			"<p>Your request honestly kinda sucked.</p>" +
			"</body>" +
			"</html>" +
			"\n";
		var headers = new HttpHeaders();
		headers.setContentType("text/html");
		return new ResponseEntity<>(body, headers, HttpStatus.BAD_REQUEST);
	}
}
