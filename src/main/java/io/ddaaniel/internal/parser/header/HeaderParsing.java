package io.ddaaniel.internal.parser.header;

import java.nio.ByteBuffer;

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
	
	private int IndexOf(ByteBuffer source) {
		for (int i = source.position(); i < source.limit() - 1; i++) {
			if (source.get(i) == '\r' && source.get(i + 1) == '\n') {
				return i;
			}
		}
		return -1;
	}

	private int IndexOf(ByteBuffer source, String string) {
		var i = source.position();
		var size = source.limit();
		var toRead = 0;
		while (i < size) {

			toRead = size - i;
			if (toRead < size) break;
			if (source.get(i) == string.charAt(0)) {
				int j = i;
				for (char c : string.toCharArray()) {
					if (c != source.get(j)) break;	
					j++;
				}
				return i;
			}

			i++;
		}

		return -1;
	}

	public Tuple2<Integer, Boolean> Parse(ByteBuffer data) {
		var read = 0;
		var done = false;
		var SEPARATOR = "\r\n";
		var START = data.position();
		var EOL = IndexOf(data);
		if (EOL == -1) {
			return Tuple.of(read, done);
		}

		var headerline = new byte[EOL - START];
		data.get(headerline);
		data.get();
		data.get();
		read += data.position() - START;
		done = true;
		
		if (read == SEPARATOR.length()) {
			return Tuple.of(read, done);
		}

		return null;
	}
}
