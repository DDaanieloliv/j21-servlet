package io.ddaaniel.internal.parser.header;

import java.nio.ByteBuffer;
import java.util.HashMap;

import io.ddaaniel.internal.exception.MalformedHeaderException;
import io.ddaaniel.internal.parser.header.message.Headers;
import io.ddaaniel.internal.parser.util.Util;
import io.vavr.Tuple;
import io.vavr.Tuple2;

/**
 * HeaderParsing
 */
public class HeaderParsing {

	public final Headers fieldline = new Headers(new HashMap<>());


	public HeaderParsing NewHeaders(){
		return this;
	}

	public String Get(String name) {
		return fieldline.map().get(name.toLowerCase());
	}

	public void Set(String name, String value) {
		fieldline.map().put(name.toLowerCase(), value);
	}

	public boolean isToken(byte[] bytes) {
		var found = false;
		for (byte i : bytes) {
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

	private Tuple2<String, String> parseHeader(byte[] fieldline) {
		var parts = Util.Split(fieldline, ":", 2);
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
			Set(name, value);
			read += (EOL - START) + SEPARATOR.length();
			START = data.position();
		}


		return Tuple.of(read, done);
	}
}
