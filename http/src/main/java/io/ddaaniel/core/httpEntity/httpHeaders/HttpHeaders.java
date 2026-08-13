package io.ddaaniel.core.httpEntity.httpHeaders;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
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

	/**
	 * HttpHeadersNames
	 */
	public class HttpHeadersNames {

		public static final String ACCEPT = "accept";
		public static final String ACCEPT_CHARSET = "accept-charset";
		public static final String ACCEPT_ENCODING = "accept-encoding";
		public static final String ACCEPT_LANGUAGE = "accept-language";
		public static final String ACCEPT_RANGES = "accept-ranges";
		public static final String ACCEPT_PATCH = "accept-patch";
		public static final String ACCEPT_QUERY = "accept-query";
		public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "access-control-allow-credentials";
		public static final String ACCESS_CONTROL_ALLOW_HEADERS = "access-control-allow-headers";
		public static final String ACCESS_CONTROL_ALLOW_METHODS = "access-control-allow-methods";
		public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "access-control-allow-origin";
		public static final String ACCESS_CONTROL_ALLOW_PRIVATE_NETWORK = "access-control-allow-private-network";
		public static final String ACCESS_CONTROL_EXPOSE_HEADERS = "access-control-expose-headers";
		public static final String ACCESS_CONTROL_MAX_AGE = "access-control-max-age";
		public static final String ACCESS_CONTROL_REQUEST_HEADERS = "access-control-request-headers";
		public static final String ACCESS_CONTROL_REQUEST_METHOD = "access-control-request-method";
		public static final String ACCESS_CONTROL_REQUEST_PRIVATE_NETWORK = "access-control-request-private-network";
		public static final String AGE = "age";
		public static final String ALLOW = "allow";
		public static final String AUTHORIZATION = "authorization";
		public static final String CACHE_CONTROL = "cache-control";
		public static final String CONNECTION = "connection";
		public static final String CONTENT_BASE = "content-base";
		public static final String CONTENT_ENCODING = "content-encoding";
		public static final String CONTENT_LANGUAGE = "content-language";
		public static final String CONTENT_LENGTH = "content-length";
		public static final String CONTENT_LOCATION = "content-location";
		public static final String CONTENT_TRANSFER_ENCODING = "content-transfer-encoding";
		public static final String CONTENT_DISPOSITION = "content-disposition";
		public static final String CONTENT_MD5 = "content-md5";
		public static final String CONTENT_RANGE = "content-range";
		public static final String CONTENT_SECURITY_POLICY = "content-security-policy";
		public static final String CONTENT_TYPE = "content-type";
		public static final String COOKIE = "cookie";
		public static final String DATE = "date";
		public static final String DNT = "dnt";
		public static final String ETAG = "etag";
		public static final String EXPECT = "expect";
		public static final String EXPIRES = "expires";
		public static final String FROM = "from";
		public static final String HOST = "host";
		public static final String IF_MATCH = "if-match";
		public static final String IF_MODIFIED_SINCE = "if-modified-since";
		public static final String IF_NONE_MATCH = "if-none-match";
		public static final String IF_RANGE = "if-range";
		public static final String IF_UNMODIFIED_SINCE = "if-unmodified-since";
		public static final String KEEP_ALIVE = "keep-alive";
		public static final String LAST_MODIFIED = "last-modified";
		public static final String LOCATION = "location";
		public static final String MAX_FORWARDS = "max-forwards";
		public static final String ORIGIN = "origin";
		public static final String PRAGMA = "pragma";
		public static final String PROXY_AUTHENTICATE = "proxy-authenticate";
		public static final String PROXY_AUTHORIZATION = "proxy-authorization";
		public static final String PROXY_CONNECTION = "proxy-connection";
		public static final String RANGE = "range";
		public static final String REFERER = "referer";
		public static final String RETRY_AFTER = "retry-after";
		public static final String SEC_WEBSOCKET_KEY1 = "sec-websocket-key1";
		public static final String SEC_WEBSOCKET_KEY2 = "sec-websocket-key2";
		public static final String SEC_WEBSOCKET_LOCATION = "sec-websocket-location";
		public static final String SEC_WEBSOCKET_ORIGIN = "sec-websocket-origin";
		public static final String SEC_WEBSOCKET_PROTOCOL = "sec-websocket-protocol";
		public static final String SEC_WEBSOCKET_VERSION = "sec-websocket-version";
		public static final String SEC_WEBSOCKET_KEY = "sec-websocket-key";
		public static final String SEC_WEBSOCKET_ACCEPT = "sec-websocket-accept";
		public static final String SEC_WEBSOCKET_EXTENSIONS = "sec-websocket-extensions";
		public static final String SERVER = "server";
		public static final String SET_COOKIE = "set-cookie";
		public static final String SET_COOKIE2 = "set-cookie2";
		public static final String TE = "te";
		public static final String TRAILER = "trailer";
		public static final String TRANSFER_ENCODING = "transfer-encoding";
		public static final String UPGRADE = "upgrade";
		public static final String UPGRADE_INSECURE_REQUESTS = "upgrade-insecure-requests";
		public static final String USER_AGENT = "user-agent";
		public static final String VARY = "vary";
		public static final String VIA = "via";
		public static final String WARNING = "warning";
		public static final String WEBSOCKET_LOCATION = "websocket-location";
		public static final String WEBSOCKET_ORIGIN = "websocket-origin";
		public static final String WEBSOCKET_PROTOCOL = "websocket-protocol";
		public static final String WWW_AUTHENTICATE = "www-authenticate";
		public static final String X_FRAME_OPTIONS = "x-frame-options";
		public static final String X_REQUESTED_WITH = "x-requested-with";
		public static final String ALT_SVC = "alt-svc";

	}

	/**
	 * HttpHeadersValue
	 */
	public class HttpHeadersValue {
	
		public static final String APPLICATION_JSON = "application/json";
		public static final String APPLICATION_MANIFEST_JSON = "application/manifest+json";
		public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
		public static final String APPLICATION_OGG = "application/ogg";
		public static final String APPLICATION_PDF = "application/pdf";
		public static final String APPLICATION_RTF = "application/rtf";
		public static final String APPLICATION_WASM = "application/wasm";
		public static final String APPLICATION_X_WWW_FORM_URLENCODED =
			"application/x-www-form-urlencoded";
		public static final String APPLICATION_XHTML = "application/xhtml+xml";
		public static final String APPLICATION_XML = "application/xml";
		public static final String APPLICATION_ZSTD = "application/zstd";
		public static final String AUDIO_MIDI = "audio/midi";
		public static final String AUDIO_X_MIDI = "audio/x-midi";
		public static final String AUDIO_MPEG = "audio/mpeg";
		public static final String AUDIO_OGG = "audio/ogg";
		public static final String AUDIO_WAV = "audio/wav";
		public static final String AUDIO_WEBM = "audio/webm";
		public static final String BASE64 = "base64";
		public static final String BINARY = "binary";
		public static final String BOUNDARY = "boundary";
		public static final String BYTES = "bytes";
		public static final String CHARSET = "charset";
		public static final String CHUNKED = "chunked";
		public static final String CLOSE = "close";
		public static final String COMPRESS = "compress";
		public static final String CONTINUE = "100-continue";
		public static final String DEFLATE = "deflate";
		public static final String X_DEFLATE = "x-deflate";
		public static final String FILENAME = "filename";
		public static final String FONT_OTF = "font/otf";
		public static final String FONT_TTF = "font/ttf";
		public static final String FONT_WOFF = "font/woff";
		public static final String FONT_WOFF2 = "font/woff2";
		public static final String BR = "br";
		public static final String ZSTD = "zstd";
		public static final String GZIP_DEFLATE = "gzip,deflate";
		public static final String X_GZIP = "x-gzip";
		public static final String IDENTITY = "identity";
		public static final String IMAGE_AVIF = "image/avif";
		public static final String IMAGE_BMP = "image/bmp";
		public static final String IMAGE_JPEG = "image/jpeg";
		public static final String IMAGE_PNG = "image/png";
		public static final String IMAGE_SVG_XML = "image/svg+xml";
		public static final String IMAGE_TIFF = "image/tiff";
		public static final String IMAGE_WEBP = "image/webp";
		public static final String KEEP_ALIVE = "keep-alive";
		public static final String MAX_AGE = "max-age";
		public static final String MAX_STALE = "max-stale";
		public static final String MIN_FRESH = "min-fresh";
		public static final String MULTIPART_FORM_DATA = "multipart/form-data";
		public static final String MULTIPART_MIXED = "multipart/mixed";
		public static final String MUST_REVALIDATE = "must-revalidate";
		public static final String NO_STORE = "no-store";
		public static final String NO_TRANSFORM = "no-transform";
		public static final String NONE = "none";
		public static final String ZERO = "0";
		public static final String ONLY_IF_CACHED = "only-if-cached";
		public static final String PRIVATE = "private";
		public static final String PROXY_REVALIDATE = "proxy-revalidate";
		public static final String PUBLIC = "public";
		public static final String QUOTED_PRINTABLE = "quoted-printable";
		public static final String S_MAXAGE = "s-maxage";
		public static final String TEXT_CSS = "text/css";
		public static final String TEXT_CSV = "text/csv";
		public static final String TEXT_HTML = "text/html";
		public static final String TEXT_JAVASCRIPT = "text/javascript";
		public static final String TEXT_MARKDOWN = "text/markdown";
		public static final String TEXT_EVENT_STREAM = "text/event-stream";
		public static final String TEXT_PLAIN = "text/plain";
		public static final String TRAILERS = "trailers";
		public static final String UPGRADE = "upgrade";
		public static final String VIDEO_MP4 = "video/mp4";
		public static final String VIDEO_MPEG = "video/mpeg";
		public static final String VIDEO_OGG = "video/ogg";
		public static final String VIDEO_WEBM = "video/webm";
		public static final String WEBSOCKET = "websocket";
		public static final String XML_HTTP_REQUEST = "XMLHttpRequest";

	}

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
	 * Returns {@code true} if this HttpHeaders contains an entry for the
	 * given header name and the entry for the header value.
	 * @param headerName the header name
	 * @param headerValue the header value
	 */
	public boolean containsHeader(String headerName, String headerValue) {
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
	 * Set the given, single header value under the given name.
	 * @param headerName the header name
	 * @param headerValue the header value
	 * @throws UnsupportedOperationException if adding headers is not supported
	 * @see #put(String, List)
	 * @see #add(String, String)
	 */
	public void set(String headerName, List<String> headerValues) {
		this.headers.put(headerName.toLowerCase(), headerValues);
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
	 * Clear all headers from this HttpHeaders instance.
	 */
	public void clear() {
		this.headers.clear();
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
	 * Set the (new) value of the {@code Origin} header.
	 */
	public void setOrigin(String origin) {
		setOrRemove(HttpHeadersNames.ORIGIN, origin);
	}

	/**
	 * Set the (new) location of a resource,
	 * as specified by the {@code Location} header.
	 */
	public void setLocation(URI location) {
		setOrRemove(HttpHeadersNames.LOCATION, (location != null ? location.toASCIIString() : null));
	}

	/**
	 * Return the media type of the body, as specified
	 * by the {@code Content-Type} header.
	 * <p>Returns {@code null} when the {@code Content-Type} header is not set.
	 */
	public String getContentType() {
		String value = getFirst(HttpHeadersNames.CONTENT_TYPE);
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
		String value = getFirst(HttpHeadersNames.HOST);
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
		return get(HttpHeadersNames.ACCEPT);
	}

	/**
	 * Return the (new) location of a resource
	 * as specified by the {@code Location} header.
	 * <p>Returns {@code null} when the location is unknown.
	 */
	public URI getLocation() {
		String value = getFirst(HttpHeadersNames.LOCATION);
		return (value != null ? URI.create(value) : null);
	}

	/**
	 * Set the media type of the body,
	 * as specified by the {@code Content-Type} header.
	 */
	public void setContentType(String mediaType) {
		if (mediaType != null) {
			String[] parts = mediaType.split("/");
			if (parts.length > 0 && parts[0].trim().equals("*")) {
				throw new IllegalArgumentException("Content-Type cannot contain wildcard type '*'");
			}
			if (parts.length > 1) {
				String subtype = parts[1].split(";")[0].trim();
				if (subtype.equals("*")) {
					throw new IllegalArgumentException("Content-Type cannot contain wildcard subtype '*'");
				}
			}
			set(HttpHeadersNames.CONTENT_TYPE, mediaType);
		}
		else {
			remove(HttpHeadersNames.CONTENT_TYPE);
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
		set(HttpHeadersNames.CONTENT_LENGTH, Long.toString(contentLength));
	}

	/**
	 * Return the length of the body in bytes, as specified by the
	 * {@code Content-Length} header.
	 * <p>Returns -1 when the content-length is unknown.
	 */
	public List<String> getAll(String headersNames) {
		List<String> value = get(headersNames);
		return value ;
	}

	/**
	 * Return the length of the body in bytes, as specified by the
	 * {@code Content-Length} header.
	 * <p>Returns -1 when the content-length is unknown.
	 */
	public long getContentLength() {
		String value = getFirst(HttpHeadersNames.CONTENT_LENGTH);
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

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof HttpHeaders)) {
			return false;
		}

		HttpHeaders h = (HttpHeaders) o;

		if (h.headers.size() != this.headers.size()) {
			return false;
		}

		for (Map.Entry<String, List<String>> entry : this.headerSet()) {
			String key = entry.getKey();
			List<String> newValues = h.get(key);
			List<String> thisValues = entry.getValue();

			if (newValues == null || thisValues.size() != newValues.size()) {
				return false;
			}

			if (!newValues.equals(thisValues)) {
				return false;
			}
		}

		return true;
	}

}
