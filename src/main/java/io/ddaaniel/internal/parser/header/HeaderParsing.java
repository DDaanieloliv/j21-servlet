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
	
	private int IndexOf(ByteBuffer source, String string) {
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

	private byte[][] Split(ByteBuffer source, String string, Integer limiter) {

		return new byte[0][0];
	}

	private Tuple2<String, String> parseHeader(ByteBuffer fieldline) {

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
