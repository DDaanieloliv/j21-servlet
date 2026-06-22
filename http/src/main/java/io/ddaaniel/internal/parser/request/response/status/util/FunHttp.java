package io.ddaaniel.internal.parser.request.response.status.util;

/**
 * FunHttp
 */
public abstract class FunHttp {

	public static String respond200() {
		return 
			"<html>" +
			"<head>" +
			"<title>200 OK</title>" +
			"</head>" +
			"<body>" +
			"<h1>Success!</h1>" +
			"<p>Your request was an absolute banger.</p>" +
			"</body>" +
			"</html>" + 
			"\n";
	}

	public static String respond400() {
		return 
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
	}

	public static String respond404() {
		return 
			"<html>" +
			"<head>" +
			"<title>404 Not Found</title>" +
			"</head>" +
			"<body>" +
			"<h1>Not Found</h1>" +
			"<p>What are u doing brotherrr, that is no here.</p>" +
			"</body>" +
			"</html>" +
			"\n";
	}


	public static String respond500() {
		return 
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
	}
	
}
