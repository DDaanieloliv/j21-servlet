package io.ddaaniel.listener.internal.valueObjects;

/**
 * HttpMethod
 */
public class HttpMethod implements Comparable<HttpMethod> {

	private final String name;

	HttpMethod(String name) {
		this.name = name;
	}

	/**
     * The OPTIONS method represents a request for information about the communication options
     * available on the request/response chain identified by the Request-URI. This method allows
     * the client to determine the options and/or requirements associated with a resource, or the
     * capabilities of a server, without implying a resource action or initiating a resource
     * retrieval.
     */
    public static final HttpMethod OPTIONS = new HttpMethod("OPTIONS");

    /**
     * The GET method means retrieve whatever information (in the form of an entity) is identified
     * by the Request-URI.  If the Request-URI refers to a data-producing process, it is the
     * produced data which shall be returned as the entity in the response and not the source text
     * of the process, unless that text happens to be the output of the process.
     */
    public static final HttpMethod GET = new HttpMethod("GET");

    /**
     * The HEAD method is identical to GET except that the server MUST NOT return a message-body
     * in the response.
     */
    public static final HttpMethod HEAD = new HttpMethod("HEAD");

    /**
     * The POST method is used to request that the origin server accept the entity enclosed in the
     * request as a new subordinate of the resource identified by the Request-URI in the
     * Request-Line.
     */
    public static final HttpMethod POST = new HttpMethod("POST");

    /**
     * The PUT method requests that the enclosed entity be stored under the supplied Request-URI.
     */
    public static final HttpMethod PUT = new HttpMethod("PUT");

    /**
     * The PATCH method requests that a set of changes described in the
     * request entity be applied to the resource identified by the Request-URI.
     */
    public static final HttpMethod PATCH = new HttpMethod("PATCH");

    /**
     * The DELETE method requests that the origin server delete the resource identified by the
     * Request-URI.
     */
    public static final HttpMethod DELETE = new HttpMethod("DELETE");

    /**
     * The TRACE method is used to invoke a remote, application-layer loop- back of the request
     * message.
     */
    public static final HttpMethod TRACE = new HttpMethod("TRACE");

    /**
     * This specification reserves the method name CONNECT for use with a proxy that can dynamically
     * switch to being a tunnel
     */
    public static final HttpMethod CONNECT = new HttpMethod("CONNECT");

    /**
     * The QUERY method requests that the request target process the enclosed content in a safe and
     * idempotent manner and then respond with the result of that processing.
     */
    public static final HttpMethod QUERY = new HttpMethod("QUERY");


	public String value() {
		return name;
	}

	public HttpMethod valueOf(String m) {
		switch (m) {
			case "OPTIONS": return HttpMethod.OPTIONS;
            case "GET":     return HttpMethod.GET;
            case "HEAD":    return HttpMethod.HEAD;
            case "POST":    return HttpMethod.POST;
            case "PUT":     return HttpMethod.PUT;
            case "PATCH":   return HttpMethod.PATCH;
            case "DELETE":  return HttpMethod.DELETE;
            case "TRACE":   return HttpMethod.TRACE;
            case "CONNECT": return HttpMethod.CONNECT;
            case "QUERY":   return HttpMethod.QUERY;
            default:        return new HttpMethod(name);		
		}
	}

	@Override
	public String toString() {
		return name.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof HttpMethod)) {
			return false;
		}

		HttpMethod m = (HttpMethod) o;
		return value().equals(m.value());
	}
	
	@Override
	public int compareTo(HttpMethod method) {
	 	if (this == method) {
			return 0;
		}

		return value().compareTo(method.value());
	}
}
