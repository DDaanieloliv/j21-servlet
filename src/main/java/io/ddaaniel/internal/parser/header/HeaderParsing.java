package io.ddaaniel.internal.parser.header;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

import io.ddaaniel.internal.exception.MalformedHeaderException;
import io.ddaaniel.internal.parser.header.message.Headers;
import io.vavr.Tuple;
import io.vavr.Tuple2;

/**
 * HeaderParsing
 */
public class HeaderParsing {

	public final Headers fieldline = new Headers(new HashMap<>());

	private static int IndexOf(byte[] source, String string, int start) {
		var i = start;
		var size = source.length;
		var bytes = string.getBytes();
		var toRead = 0;
		while (i < size) {

			toRead = size - i;
			if (toRead < string.length()) break;
			if (source[i] == bytes[0]) {
				var match = true;
				int j = i;
				for (byte c : bytes) {
					if (c != source[j]) {
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


	private static int count(byte[] source, String string) {
		var i = 0;
		var size = source.length;
		var start = 0;
		var counts = 0;
		while (i < size) {
			var idx = IndexOf(source, string, start);
			if (idx == -1) {
				break;
			}

			start = idx + string.length();
			counts++;
			i++;
		}

		return counts;
	}
	

	private static byte[][] Split(byte[] source, String string, Integer times) {
		if (times == 0) return null;

		var start = 0;
		var size = source.length;
		var range = times - 1;
		
		var splits = new byte[count(source, string) + 1][];
		if (times > 0) splits = new byte[times][];
		var splitsCount = 0;

		while (times < 0 || splitsCount < times) {
			var toRead = size - start;
			var bytes = new byte[size - start];
			var idx = IndexOf(source, string, start);
			if (idx != -1) {
				bytes = new byte[idx - start];
			}
			if (idx == -1) {
				bytes = new byte[toRead];
				for (int i = 0; i < bytes.length; i++) {
					bytes[i] = source[start++];
				}
				splits[splitsCount++] = bytes;
				break;
			}
			if (splitsCount == range && times > 0) { 
				bytes = new byte[size - start];
			}

			for (int i = 0; i < bytes.length; i++) {
				bytes[i] = source[start++];
			}
			start += string.length();

			splits[splitsCount++] = bytes;
		}

		return splits;
	}

	private static byte[] TrimSpace(byte[] array) {
		var offsetLeft = 0;
		var offsetRight = array.length - 1;
		for (int i = 0; i < array.length; i++) {
			if (Character.isWhitespace(array[i])) offsetLeft++;
			else break;
		}
		for (int i = array.length - 1; i >= 0; i--) {
			if (Character.isWhitespace(array[i])) offsetRight--;
			else break;
		}
		return Arrays.copyOfRange(array, offsetLeft, offsetRight + 1);
	}


	private boolean HasSuffix(byte[] arr, byte[] slice) {
		var match = false;
		int j = slice.length - 1;
		int i = arr.length - 1;
		for (int count = 0; count < slice.length; count++) {
			if (arr[i--] == slice[j--]) {
				match = true;
			} else {
				match = false;
				break;
			} 
		}

		return match;
	}

	public HeaderParsing NewHeaders(){
		return this;
	}


	private Tuple2<String, String> parseHeader(byte[] fieldline) {
		var parts = Split(fieldline, ":", 2);
		if (parts.length != 2) {
			throw new MalformedHeaderException(" -> malformed field-line ");
		}

		var name = parts[0];
		var value = TrimSpace(parts[1]);

		if (HasSuffix(name, " ".getBytes())) {
			throw new MalformedHeaderException(" -> malformed field-name ");
		}
		return Tuple.of(new String(name), new String(value));
	}


	public Tuple2<Integer, Boolean> Parse(ByteBuffer data) {
		var read = 0;
		var done = false;
		var SEPARATOR = "\r\n";
		var bytes = new byte[data.limit() - data.position()];
		data.get(bytes);

		for (;;) {
			var EOL = IndexOf(bytes, SEPARATOR, 0);
			if (EOL == -1) {
				break;
			}
			if (EOL == 0) {
				done = true;
				read += SEPARATOR.length();
				break;
			}

			var headerline = Arrays.copyOfRange(bytes, 0, EOL);
			var option = parseHeader(headerline);
			var name = option._1;
			var value = option._2;
			read += EOL + SEPARATOR.length();
			fieldline.map().put(name, value);
			bytes = Arrays.copyOfRange(bytes, EOL + SEPARATOR.length(), bytes.length);
		}


		return Tuple.of(read, done);
	}
}
