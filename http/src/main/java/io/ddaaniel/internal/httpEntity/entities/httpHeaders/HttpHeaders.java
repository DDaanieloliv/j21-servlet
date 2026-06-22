package io.ddaaniel.internal.httpEntity.entities.httpHeaders;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * HttpHeaders
 */
public class HttpHeaders {

	public static final HttpHeaders EMPTY = new HttpHeaders(new LinkedHashMap<>());
	public static final String ACCEPT = "ACCEPT";
	public static final String ACCEPT_CHARSET = "ACCEPT_CHARSET";
	public static final String ACCEPT_ENCODING = "ACCEPT_ENCODING";
	public static final String ACCEPT_LANGUAGE = "ACCEPT_LANGUAGE";
	public static final String ACCEPT_PATCH = "ACCEPT_PATCH";
	public static final String ACCEPT_RANGES = "ACCEPT_RANGES";
	public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "ACCESS_CONTROL_ALLOW_CREDENTIALS";
	public static final String ACCESS_CONTROL_ALLOW_HEADERS = "ACCESS_CONTROL_ALLOW_HEADERS";
	public static final String ACCESS_CONTROL_ALLOW_METHODS = "ACCESS_CONTROL_ALLOW_METHODS";
	public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "ACCESS_CONTROL_ALLOW_ORIGIN";
	public static final String ACCESS_CONTROL_EXPOSE_HEADERS = "ACCESS_CONTROL_EXPOSE_HEADERS";
	public static final String ACCESS_CONTROL_MAX_AGE = "ACCESS_CONTROL_MAX_AGE";
	public static final String ACCESS_CONTROL_REQUEST_HEADERS = "ACCESS_CONTROL_REQUEST_HEADERS";
	public static final String ACCESS_CONTROL_REQUEST_METHOD = "ACCESS_CONTROL_REQUEST_METHOD";
	public static final String AGE = "AGE";
	public static final String ALLOW = "ALLOW";
	public static final String AUTHORIZATION = "AUTHORIZATION";
	public static final String CACHE_CONTROL = "CACHE_CONTROL";
	public static final String CONNECTION = "CONNECTION";
	public static final String CONTENT_DISPOSITION = "CONTENT_DISPOSITION";
	public static final String CONTENT_ENCODING = "CONTENT_ENCODING";
	public static final String CONTENT_LANGUAGE = "CONTENT_LANGUAGE";
	public static final String CONTENT_LENGTH = "CONTENT_LENGTH";
	public static final String CONTENT_LOCATION = "CONTENT_LOCATION";
	public static final String CONTENT_RANGE = "CONTENT_RANGE";
	public static final String CONTENT_TYPE = "CONTENT_TYPE";
	public static final String COOKIE = "COOKIE";
	public static final String DATE = "DATE";
	public static final String ETAG = "ETAG";
	public static final String EXPECT = "EXPECT";
	public static final String EXPIRES = "EXPIRES";
	public static final String FROM = "FROM";
	public static final String HOST = "HOST";
	public static final String IF_MATCH = "IF_MATCH";
	public static final String IF_MODIFIED_SINCE = "IF_MODIFIED_SINCE";
	public static final String IF_NONE_MATCH = "IF_NONE_MATCH";
	public static final String IF_RANGE = "IF_RANGE";
	public static final String IF_UNMODIFIED_SINCE = "IF_UNMODIFIED_SINCE";
	public static final String LAST_MODIFIED = "LAST_MODIFIED";
	public static final String LINK = "LINK";
	public static final String LOCATION = "LOCATION";
	public static final String MAX_FORWARDS = "MAX_FORWARDS";
	public static final String ORIGIN = "ORIGIN";
	public static final String PRAGMA = "PRAGMA";
	public static final String PROXY_AUTHENTICATE = "PROXY_AUTHENTICATE";
	public static final String PROXY_AUTHORIZATION = "PROXY_AUTHORIZATION";
	public static final String RANGE = "RANGE";
	public static final String REFERER = "REFERER";
	public static final String RETRY_AFTER = "RETRY_AFTER";
	public static final String SERVER = "SERVER";
	public static final String SET_COOKIE = "SET_COOKIE";
	public static final String SET_COOKIE2 = "SET_COOKIE2";
	public static final String TE = "TE";
	public static final String TRAILER = "TRAILER";
	public static final String TRANSFER_ENCODING = "TRANSFER_ENCODING";
	public static final String UPGRADE = "UPGRADE";
	public static final String USER_AGENT = "USER_AGENT";
	public static final String VARY = "VARY";
	public static final String VIA = "VIA";
	public static final String WARNING = "WARNING";
	public static final String WWW_AUTHENTICATE = "WWW_AUTHENTICATE";


	private final Map<String, ArrayList<String>> map;

	public HttpHeaders(Map<String, ArrayList<String>> map){
		this.map = map;
	} 

	public HttpHeaders(){
		this.map = new LinkedHashMap<String, ArrayList<String>>();
	} 

	// public HttpHeaders(HttpHeaders map){ } 

}
