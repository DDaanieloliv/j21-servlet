package io.ddaaniel.internal.parser.util;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Util
 */
public class Util {

	public  static int IndexOf(ByteBuffer source, String string, int start) {
		var i = start;
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


	public static int IndexOf(byte[] source, String string, int start) {
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

	public static int count(byte[] source, String string) {
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
	

	public static byte[][] Split(byte[] source, String string, Integer times) {
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

	public static byte[] TrimSpace(byte[] array) {
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


	public static boolean HasPrefix(byte[] arr, byte[] slice) {
		if (slice.length > arr.length) return false;
		var match = false;
		for (int count = 0; count < slice.length; count++) {
			if (arr[count] == slice[count]) {
				match = true;
			} else {
				match = false;
				break;
			} 
		}

		return match;
	}

	public static boolean HasSuffix(byte[] arr, byte[] slice) {
		if (slice.length > arr.length) return false;
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
}
