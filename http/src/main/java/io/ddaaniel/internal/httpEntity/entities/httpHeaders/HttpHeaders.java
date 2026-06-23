package io.ddaaniel.internal.httpEntity.entities.httpHeaders;

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


	public HttpHeaders(){
		this.headers = new LinkedHashMap<String, List<String>>();
	} 
	public HttpHeaders(Map<String, List<String>> map){
		this.headers = map;
	} 
	public HttpHeaders(HttpHeaders map){ 
		this.headers = map.headers;
	} 

	public void add(String headerName, String headerValue) {
		if (this.headers.get(headerName) != null) {
			List<String> headerlist = this.headers.get(headerName);
			headerlist.add(headerValue);
			this.headers.put(headerName, headerlist);
		}
		else {
			List<String> headerlist = new ArrayList<String>();
			headerlist.add(headerValue);
			this.headers.put(headerName, headerlist);
		}
	}

	public void set(String headerName, String headerValue) {
		List<String> headerList = new ArrayList<>();
		headerList.add(headerValue);
		this.headers.put(headerName, headerList);
	}

	public List<String> remove(String key) {
		return this.headers.remove(key);
	}

	private void setOrRemove(String headerName, String headerValue) {
		if (headerValue != null) {
			set(headerName, headerValue);
		}
		else {
			remove(headerName);
		}
	}

	public void setLocation(URI location) {
		setOrRemove(LOCATION, (location != null ? location.toASCIIString() : null));
	}

	public void setContentType(String mediaType) {
		if (mediaType != null) {
			set(CONTENT_TYPE, mediaType.toString());
		}
		else {
			remove(CONTENT_TYPE);
		}
	}

	public void setContentLength(long contentLength) {
		if (contentLength < 0) {
			throw new IllegalArgumentException("Content-Length must be a non-negative number");
		}
		set(CONTENT_LENGTH, Long.toString(contentLength));
	}

	public List<String> put(String headerName, List<String> headerValues) {
		return this.headers.put(headerName, headerValues);
	}


    public Set<Entry<String, List<String>>> headerSet() {
        return this.headers.entrySet();
    }

    public void forEach(BiConsumer<? super String, ? super List<String>> action) {
        headerSet().forEach(e -> action.accept(e.getKey(), e.getValue()));
    }

	public void putAll(HttpHeaders headers) {
		headers.forEach(this::put);
	}

	public void putAll(Map<? extends String, ? extends List<String>> headers) {
		for (String name : headers.keySet()) {
			put(name, headers.get(name));
		}
	}

}
