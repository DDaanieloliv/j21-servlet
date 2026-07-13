package io.ddaaniel.user.controller;

import io.ddaaniel.annotations.HTTP;
import io.ddaaniel.core.httpEntity.ResponseEntity;
import io.ddaaniel.core.httpEntity.httpHeaders.HttpHeaders;
import io.ddaaniel.core.httpStatus.HttpStatus;

/**
 * MyProblemController
 */
public abstract class MyProblemController {

	@HTTP("/myproblem")
	public static ResponseEntity<?> handleMyProblem() {
		var body = 
			"<html>" +
			"<head>" +
			"<title>500 Internal Server Error</title>" +
			"</head>" +
			"<body>" +
			"<h1>Internal Server Error</h1>" +
			"<p>Okay, you know what? This one is on me.</p>" +
			"</body>" +
			"</html>" + 
			"\n";
		var headers = new HttpHeaders();
		headers.setContentType("text/html");
		return new ResponseEntity<>(body, headers, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
