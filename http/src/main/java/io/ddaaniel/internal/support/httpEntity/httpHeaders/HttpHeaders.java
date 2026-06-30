package io.ddaaniel.internal.support.httpEntity.httpHeaders;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiConsumer;



/**
 * HttpHeaders
 */
public class HttpHeaders {

	public static final String ACCEPT = "ACCEPT";
	public static final String ACCEPT_CHARSET = "ACCEPT-CHARSET";
	public static final String ACCEPT_ENCODING = "ACCEPT-ENCODING";
	public static final String ACCEPT_LANGUAGE = "ACCEPT-LANGUAGE";
	public static final String ACCEPT_PATCH = "ACCEPT-PATCH";
	public static final String ACCEPT_RANGES = "ACCEPT-RANGES";
	public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "ACCESS-CONTROL-ALLOW-CREDENTIALS";
	public static final String ACCESS_CONTROL_ALLOW_HEADERS = "ACCESS-CONTROL-ALLOW-HEADERS";
	public static final String ACCESS_CONTROL_ALLOW_METHODS = "ACCESS-CONTROL-ALLOW-METHODS";
	public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "ACCESS-CONTROL-ALLOW-ORIGIN";
	public static final String ACCESS_CONTROL_EXPOSE_HEADERS = "ACCESS-CONTROL-EXPOSE-HEADERS";
	public static final String ACCESS_CONTROL_MAX_AGE = "ACCESS-CONTROL-MAX-AGE";
	public static final String ACCESS_CONTROL_REQUEST_HEADERS = "ACCESS-CONTROL-REQUEST-HEADERS";
	public static final String ACCESS_CONTROL_REQUEST_METHOD = "ACCESS-CONTROL-REQUEST-METHOD";
	public static final String AGE = "AGE";
	public static final String ALLOW = "ALLOW";
	public static final String AUTHORIZATION = "AUTHORIZATION";
	public static final String CACHE_CONTROL = "CACHE-CONTROL";
	public static final String CONNECTION = "CONNECTION";
	public static final String CONTENT_DISPOSITION = "CONTENT-DISPOSITION";
	public static final String CONTENT_ENCODING = "CONTENT-ENCODING";
	public static final String CONTENT_LANGUAGE = "CONTENT-LANGUAGE";
	public static final String CONTENT_LENGTH = "CONTENT-LENGTH";
	public static final String CONTENT_LOCATION = "CONTENT-LOCATION";
	public static final String CONTENT_RANGE = "CONTENT-RANGE";
	public static final String CONTENT_TYPE = "CONTENT-TYPE";
	public static final String COOKIE = "COOKIE";
	public static final String DATE = "DATE";
	public static final String ETAG = "ETAG";
	public static final String EXPECT = "EXPECT";
	public static final String EXPIRES = "EXPIRES";
	public static final String FROM = "FROM";
	public static final String HOST = "HOST";
	public static final String IF_MATCH = "IF-MATCH";
	public static final String IF_MODIFIED_SINCE = "IF-MODIFIED-SINCE";
	public static final String IF_NONE_MATCH = "IF-NONE-MATCH";
	public static final String IF_RANGE = "IF-RANGE";
	public static final String IF_UNMODIFIED_SINCE = "IF-UNMODIFIED-SINCE";
	public static final String LAST_MODIFIED = "LAST-MODIFIED";
	public static final String LINK = "LINK";
	public static final String LOCATION = "LOCATION";
	public static final String MAX_FORWARDS = "MAX-FORWARDS";
	public static final String ORIGIN = "ORIGIN";
	public static final String PRAGMA = "PRAGMA";
	public static final String PROXY_AUTHENTICATE = "PROXY-AUTHENTICATE";
	public static final String PROXY_AUTHORIZATION = "PROXY-AUTHORIZATION";
	public static final String RANGE = "RANGE";
	public static final String REFERER = "REFERER";
	public static final String RETRY_AFTER = "RETRY-AFTER";
	public static final String SERVER = "SERVER";
	public static final String SET_COOKIE = "SET-COOKIE";
	public static final String SET_COOKIE2 = "SET-COOKIE2";
	public static final String TE = "TE";
	public static final String TRAILER = "TRAILER";
	public static final String TRANSFER_ENCODING = "TRANSFER-ENCODING";
	public static final String UPGRADE = "UPGRADE";
	public static final String USER_AGENT = "USER-AGENT";
	public static final String VARY = "VARY";
	public static final String VIA = "VIA";
	public static final String WARNING = "WARNING";
	public static final String WWW_AUTHENTICATE = "WWW-AUTHENTICATE";


	final Map<String, List<String>> headers;

	public static final HttpHeaders EMPTY = new HttpHeaders(new LinkedHashMap<>());


	/**
	 * Construct a new, empty {@code HttpHeaders} instance.
	 */
	public HttpHeaders(){ this(new LinkedHashMap<String, List<String>>()); } 

	/**
	 * Construct a new {@code HttpHeaders} instance backed by the supplied map.
	 * <p>This constructor is available as an optimization for adapting to existing
	 * headers map structures, primarily for internal use within the framework.
	 * @param headers the headers map (expected to operate with case-insensitive keys)
	 */
	public HttpHeaders(Map<String, List<String>> headers){ this.headers = headers; } 

	/**
	 * Construct a new {@code HttpHeaders} instance backed by the supplied
	 * {@code HttpHeaders}.
	 * <p>Changes to the {@code HttpHeaders} created by this constructor will
	 * write through to the supplied {@code HttpHeaders}. If you wish to copy
	 * an existing {@code HttpHeaders} instance, use {@link #copyOf(HttpHeaders)}
	 * instead.
	 * <p>
	 * @param httpHeaders the headers to expose
	 * @see #copyOf(HttpHeaders)
	 */
	public HttpHeaders(HttpHeaders httpHeaders) { 
		this.headers = (httpHeaders == EMPTY) ?
				new LinkedHashMap<>() : new LinkedHashMap<>(httpHeaders.headers);
	}

	/**
	 * Create a new, mutable {@code HttpHeaders} instance and copy the supplied
	 * headers to that new instance.
	 * <p>Changes to the returned {@code HttpHeaders} will not affect the
	 * supplied headers map.
	 * @param headers the headers to copy
	 */
	public static HttpHeaders copyOf(Map<String, List<String>> headers) {
		HttpHeaders httpHeadersCopy = new HttpHeaders();
		for (String name : headers.keySet()) {
			List<String> values = headers.get(name);
			if (values != null) {
				httpHeadersCopy.put(name, new ArrayList<>(values));
			}
		}
		return httpHeadersCopy;
	}

	/**
	 * Create a new, mutable {@code HttpHeaders} instance and copy the supplied
	 * headers to that new instance.
	 * <p>Changes to the returned {@code HttpHeaders} will not affect the
	 * supplied {@code HttpHeaders}.
	 * @param httpHeaders the headers to copy
	 * @see #HttpHeaders(HttpHeaders)
	 */
	public static HttpHeaders copyOf(HttpHeaders httpHeaders) {
		return copyOf(httpHeaders.headers);
	}

	/**
	 * Returns {@code true} if this HttpHeaders contains an entry for the
	 * given header name.
	 * @param headerName the header name
	 */
	public boolean containsHeader(String headerName) {
		return this.headers.containsKey(headerName.toLowerCase());
	}

	/**
	 * Get the list of values associated with the given header name, or null.
	 * @param headerName the header name
	 */
	public List<String> get(String headerName) {
		return this.headers.get(headerName.toLowerCase());
	}

	/**
	 * Add the given, single header value under the given name.
	 * @param headerName the header name
	 * @param headerValue the header value
	 * @throws UnsupportedOperationException if adding headers is not supported
	 * @see #put(String, List)
	 * @see #set(String, String)
	 */
	public void add(String headerName, String headerValue) {
		var headersKey = headerName.toLowerCase();
		this.headers.computeIfAbsent(headersKey, (k) -> new ArrayList<>()).add(headerValue);
	}

	/**
	 * Set the given, single header value under the given name.
	 * @param headerName the header name
	 * @param headerValue the header value
	 * @throws UnsupportedOperationException if adding headers is not supported
	 * @see #put(String, List)
	 * @see #add(String, String)
	 */
	public void set(String headerName, String headerValue) {
		List<String> headerList = new ArrayList<>(1);
		headerList.add(headerValue);
		this.headers.put(headerName.toLowerCase(), headerList);
	}

	/**
	 * Remove a header from this HttpHeaders instance, and return the associated
	 * value list or {@code null} if that header wasn't present.
	 * @param key the name of the header to remove
	 * @return the value list associated with the removed header name or {@code null}
	 */
	public List<String> remove(String key) {
		return this.headers.remove(key.toLowerCase());
	}

	/**
	 * Set the given header value, or remove the header if {@code null}.
	 * @param headerName the header name
	 * @param headerValue the header value, or {@code null} for none
	 */
	private void setOrRemove(String headerName, String headerValue) {
		if (headerValue != null) {
			set(headerName, headerValue);
		}
		else {
			remove(headerName);
		}
	}

	/**
	 * Set the (new) location of a resource,
	 * as specified by the {@code Location} header.
	 */
	public void setLocation(URI location) {
		setOrRemove(LOCATION, (location != null ? location.toASCIIString() : null));
	}

	/**
	 * Return the media type of the body, as specified
	 * by the {@code Content-Type} header.
	 * <p>Returns {@code null} when the {@code Content-Type} header is not set.
	 */
	public String getContentType() {
		String value = getFirst(CONTENT_TYPE);
		return (value != null && !value.isBlank()) ? value : null;
	}

	/**
	 * Return the first header value for the given header name, if any.
	 * @param headerName the header name
	 * @return the first header value, or {@code null} if none
	 */
	public String getFirst(String headerName) {
		List<String> values = this.headers.get(headerName.toLowerCase());
		if (values == null || values.isEmpty()) return null;
		return values.getFirst();
	}

	/**
	 * Return the value of the {@code Host} header, if available.
	 * <p>If the header value does not contain a port, the
	 * {@linkplain InetSocketAddress#getPort() port} in the returned address will
	 * be {@code 0}.
	 */
	public InetSocketAddress getHost() {
		String value = getFirst(HOST);
		if (value == null) {
			return null;
		}

		String host = null;
		int port = 0;
		int separator = (value.startsWith("[") ? value.indexOf(':', value.indexOf(']')) : value.lastIndexOf(':'));
		if (separator != -1) {
			host = value.substring(0, separator);
			String portString = value.substring(separator + 1);
			try {
				port = Integer.parseInt(portString);
			}
			catch (NumberFormatException ignored) {
			}
		}

		if (host == null) {
			host = value;
		}
		return InetSocketAddress.createUnresolved(host, port);
	}

	/**
	 * Return the list of acceptable {@linkplain MediaType media types},
	 * as specified by the {@code Accept} header.
	 * <p>Returns an empty list when the acceptable media types are unspecified.
	 */
	public List<String> getAccept() {
		return get(ACCEPT);
	}

	/**
	 * Return the (new) location of a resource
	 * as specified by the {@code Location} header.
	 * <p>Returns {@code null} when the location is unknown.
	 */
	public URI getLocation() {
		String value = getFirst(LOCATION);
		return (value != null ? URI.create(value) : null);
	}

	public void setContentType(String mediaType) {
		if (mediaType != null) {
			set(CONTENT_TYPE, mediaType.toString());
		}
		else {
			remove(CONTENT_TYPE);
		}
	}

	/**
	 * Set the media type of the body,
	 * as specified by the {@code Content-Type} header.
	 * For while treated like a String.
	 */
	public void setContentLength(long contentLength) {
		if (contentLength < 0) {
			throw new IllegalArgumentException("Content-Length must be a non-negative number");
		}
		set(CONTENT_LENGTH, Long.toString(contentLength));
	}

	/**
	 * Return the length of the body in bytes, as specified by the
	 * {@code Content-Length} header.
	 * <p>Returns -1 when the content-length is unknown.
	 */
	public long getContentLength() {
		String value = getFirst(CONTENT_LENGTH);
		return (value != null ? Long.parseLong(value) : -1);
	}

	/**
	 * Set the list of values associated with the given header name. Returns the
	 * previous list of values, or {@code null} if the header was not present.
	 * @param headerName the header name
	 * @param headerValues the new values
	 * @return the old values for the given header name
	 */
	public List<String> put(String headerName, List<String> headerValues) {
		return this.headers.put(headerName, headerValues);
	}

	/**
	 * Return a view of the headers as an entry {@code Set} of key-list pairs.
	 * <p>Both {@link Iterator#remove()} and {@link Entry#setValue}
	 * are supported and mutate the headers.
	 * <p>This collection is guaranteed to contain one entry per header name
	 * even if the backing structure stores multiple casing variants of names,
	 * at the cost of first copying the names into a case-insensitive set for
	 * filtering the iteration.
	 * @return a {@code Set} view that iterates over all headers in a
	 * case-insensitive manner
	 */
    public Set<Entry<String, List<String>>> headerSet() {
        return this.headers.entrySet();
    }

	/**
	 * Perform an action over each header, as when iterated via
	 * {@link #headerSet()}.
	 * @param action the action to be performed for each entry
	 */
    public void forEach(BiConsumer<? super String, ? super List<String>> action) {
        headerSet().forEach(e -> action.accept(e.getKey(), e.getValue()));
    }

	/**
	 * Put all the entries from the given HttpHeaders into this HttpHeaders.
	 * @param headers the given headers
	 * @see #put(String, List)
	 */
	public void putAll(HttpHeaders headers) {
		headers.forEach(this::put);
	}

	/**
	 * Put all the entries from the given {@code Map} into this HttpHeaders.
	 * @param headers the given headers
	 * @see #put(String, List)
	 */
	public void putAll(Map<? extends String, ? extends List<String>> headers) {
		for (String name : headers.keySet()) {
			put(name, headers.get(name));
		}
	}

}
