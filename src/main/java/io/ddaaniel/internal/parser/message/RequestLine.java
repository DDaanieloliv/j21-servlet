package io.ddaaniel.internal.parser.message;


/**
 * RequestLine
 *
 */
public class RequestLine{
	public String Method;
	public String RequestTarget;
	public String HttpVersion; 

	public RequestLine(){}

	public RequestLine(String m, String rt, String hv) {
		this.Method = m;
		this.RequestTarget = rt;
		this.HttpVersion = hv;
	}
}
