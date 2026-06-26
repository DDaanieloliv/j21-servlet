package io.ddaaniel.internal.parser.request.header;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import io.ddaaniel.internal.exception.MalformedHeaderException;
import io.ddaaniel.internal.httpEntity.entities.httpHeaders.HttpHeaders;
import io.ddaaniel.internal.parser.util.Util;
import io.vavr.Tuple;
import io.vavr.Tuple2;

/**
 * HeaderHandler
 */
public class HeaderHandler {

	public final HttpHeaders h = new HttpHeaders();

	public HeaderHandler NewHeaders(){
		return this;
	}

	public List<String> Get(String name) {
		return h.get(name.toLowerCase());
	}

	public void Set(String name, String value) {
		this.h.set(name.toLowerCase(), value);
	}

	public void Replace(String name, String value) {
		var key = name.toLowerCase();
		var headerValue = new ArrayList<String>(1);
		headerValue.add(value);
		h.put(key, headerValue);
	}

	public void Delete(String name) {
		var key = name.toLowerCase();
		h.remove(key);
	}

	private boolean isToken(byte[] bytes) {
		for (byte i : bytes) {
			var found = false;
			if (i >= 'A' && i <= 'Z' || i >= 'a' && i <= 'z' || i >= '0' && i <= '9') {
				found = true;
			}
			switch (i) {
				case '!', '#', '$', '%', '&', '\'', '*', '+', '-', '.', '^', '_','`', '|', '~' : found = true;
			}
			if (!found) return false;
		}
		return true;
	}


	private Tuple2<String, String> parseHeader(byte[] h) {
		var parts = Util.Split(h, ":", 2);
		if (parts.length != 2) {
			throw new MalformedHeaderException(" -> malformed field-line ");
		}

		var name = parts[0];
		var value = Util.TrimSpace(parts[1]);
		if (Util.HasSuffix(name, " ".getBytes())) {
			throw new MalformedHeaderException(" -> malformed field-name ");
		}
		return Tuple.of(new String(name), new String(value));
	} 


	public Tuple2<Integer, Boolean> Parse(ByteBuffer data) { 
		var read = 0; 
		var done = false;
		var START = data.position();
		var SEPARATOR = "\r\n";

		for (;;) {
			var EOL = Util.IndexOf(data, SEPARATOR, START);
			if (EOL == -1) {
				break;
			}
			if (EOL - START == 0) {
				data.get();
				data.get();
				done = true;
				read += SEPARATOR.length();
				break;
			}

			var headerline = new byte[EOL - START];
			data.get(headerline);
			data.get();
			data.get();
			var option = parseHeader(headerline);
			var name = option._1;
			var value = option._2;

			if (!isToken(name.getBytes())) {
				throw new MalformedHeaderException(" -> malformed header-name ");
			}
			Set(name, value);
			read += (EOL - START) + SEPARATOR.length();
			START = data.position();
		}

		return Tuple.of(read, done);
	}
}
