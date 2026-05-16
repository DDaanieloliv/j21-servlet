package io.ddaaniel.internal.parser.header;

import java.nio.ByteBuffer;

import io.ddaaniel.internal.exception.MalformedHeaderException;
import io.ddaaniel.internal.parser.header.message.Headers;
import io.vavr.Tuple;
import io.vavr.Tuple2;

/**
 * HeaderParsing
 */
public class HeaderParsing {

	public final Headers fieldline = new Headers();

	public HeaderParsing NewHeaders(){
		return this;
	}
	
	private static int IndexOf(ByteBuffer source, String string) {
		var i = source.position();
		var size = source.limit();
		var bytes = string.getBytes();
		var toRead = 0;
		while (i < size) {

			toRead = size - i;
			if (toRead < string.length()) break;
			if (source.get(i) == bytes[0]) {
				var match = true;
				int j = i;
				for (byte c : bytes) {
					if (c != source.get(j)) {
						match = false;
						break;
					}	
					j++;
				}
				if (match) return i;
			}

			i++;
		}
		return -1;
	}

	public static byte[][] Split(ByteBuffer source, String string, Integer times) {
		if (times == 0) return null;
		source.mark();
		var start = source.position();
		var size = source.limit();
		var range = times - 1;
		var toRead = size - start;
		var splits = new byte[times][];
		var splitsCount = 0;
		var condition = splitsCount < times;

		if (times < 0) condition =  true;
		while (condition) {
			var bytes = new byte[size - start];
			var idx = IndexOf(source, string);
			if (idx != -1) {
				bytes = new byte[idx - start];
			}
			if (idx == -1 && toRead < string.length()) break;
			if (splitsCount == range) { 
				bytes = new byte[size - start];
			}

			for (int i = 0; i < bytes.length; i++) {
				bytes[i] = source.get(start++);
			}

			splits[splitsCount] = bytes;
			if (!(splitsCount == range)) {
				for (int i = 0; i < string.length(); i++) { start++; }
			}
			source.position(start);
			splitsCount++;
		}

		source.reset();
		return splits;
	}

	private Tuple2<String, String> parseHeader(ByteBuffer fieldline) {
		var parts = Split(fieldline, ":", 2);
		if (parts.length != 2) {
			throw new MalformedHeaderException(" -> malformed header -- ");
		}

		var name = parts[0];
		var value = parts[1];
		return Tuple.of("", "");
	}


	public Tuple2<Integer, Boolean> Parse(ByteBuffer data) {
		var read = 0;
		var done = false;
		var SEPARATOR = "\r\n";
		var START = data.position();

		for (;;) {
			var EOL = IndexOf(data, SEPARATOR);
			if (EOL == -1) {
				break;
			}

			var headerline = new byte[EOL - START];
			data.get(headerline);
			data.get();
			data.get();
			read += data.position() - START;
			done = true;

			if (EOL == 0) {
				done = true;
				break;
			}

			if (read == SEPARATOR.length()) {
				return Tuple.of(read, done);
			}
		}


		var n = 0;
		return Tuple.of(n, done);
	}
}
