package io.ddaaniel.internal.parser.request.mapper;


/**
 * RequestLine
 *
 */
public class RequestLine{
	public String Method;
	public String RequestTarget;
	public String HttpVersion; 

	public RequestLine(){}

	public RequestLine(String method, String r_tarteg, String http_version) {
		this.Method = method;
		this.RequestTarget = r_tarteg;
		this.HttpVersion = http_version;
	}
}
