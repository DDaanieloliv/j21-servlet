package io.ddaaniel.listener.internal.codec.reader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import io.ddaaniel.core.httpEntity.httpHeaders.HttpHeaders;
import io.ddaaniel.listener.internal.DefaultHttpMessage;
import io.ddaaniel.listener.internal.HttpRequest;
import io.ddaaniel.listener.internal.valueObjects.HttpMethod;
import io.ddaaniel.listener.internal.valueObjects.HttpVersion;

class DefaultHttpRequest extends DefaultHttpMessage implements HttpRequest {

	private String uri;
	private HttpMethod method;
	
	public DefaultHttpRequest(HttpVersion version, HttpHeaders headers){
		super(version, headers);
	}

    public DefaultHttpRequest(HttpVersion httpVersion, HttpMethod method, String uri, HttpHeaders headers) {
        super(httpVersion, headers);
		if (method == null) {
			throw new NullPointerException("method");
		}
		if (uri == null || uri.isEmpty()) {
			throw new NullPointerException("uri");
		}
        this.method = method;
        this.uri = uri;
    }

	@Override
	public HttpMethod method() {
		return method;
	}

	@Override
	public HttpRequest setMethod(HttpMethod m) {
		if (m == null) {
			throw new NullPointerException("method");
		}
		this.method = m;
		return this;
	}

	@Override
	public String uri() {
		return uri;
	}

	@Override
	public HttpRequest setUri(String uri) {
		if (uri == null) {
			throw new IllegalArgumentException("uri");
		}
		this.uri = uri;
		return this;
	}

	@Override
	public HttpRequest setProtocolVersion(HttpVersion v) {
		super.setProtocolVersion(v);
		return this;
	}
}

/**
 * HttpServletRequest
 */
public record HttpServletRequest(
		String method, 
		String uri, 
		HttpHeaders headers, 
		InputStream body,
		Map<String, Object> attributes
		) { 


	public HttpServletRequest {
		if (attributes == null) {
			attributes = new HashMap<>();
		}
	}


	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String name) {
		return (T) this.attributes.get(name);
	}

	public void setAttribute(String name, Object value) {
		this.attributes.put(name, value);
	}

	public void removeAttribute(String name) {
		this.attributes.remove(name);
	}

	public boolean hasAttribute(String name) {
		return this.attributes.containsKey(name);
	}


	/**
	 * Extract the path without thoses Query Parameters (E.G.: "/users?page=1" -> "/users")
	 */
	public String getPath() {
		int queryIndex = uri.indexOf('?');
		return queryIndex != -1 ? uri.substring(0, queryIndex) : uri;
	}

	/**
	 * Read Bearer Token
	 */
	public String getBearerToken() {
		String authHeader = headers.getFirst("Authorization");
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			return authHeader.substring(7).trim();
		}
		return null;
	}
		}


class HttpRequestBuilder {
    private String method;
    private String uri;
    private final HttpHeaders headers = new HttpHeaders();
    private InputStream body;
    private final Map<String, Object> attributes = new HashMap<>();

    public HttpRequestBuilder method(String method) {
        this.method = method; 
        return this; 
    }

    public HttpRequestBuilder uri(String uri) { 
        this.uri = uri; 
        return this; 
    }

    public HttpHeaders headers() { 
        return this.headers; 
    }

    public HttpRequestBuilder body(InputStream body) { 
        this.body = body; 
        return this; 
    }

    public HttpRequestBuilder attribute(String name, Object value) {
        this.attributes.put(name, value);
        return this;
    }

    public HttpServletRequest build() {
        return new HttpServletRequest(
                this.method, 
                this.uri, 
                this.headers, 
                this.body != null ? this.body : new ByteArrayInputStream(new byte[0]),
                this.attributes
        );
    }
}
