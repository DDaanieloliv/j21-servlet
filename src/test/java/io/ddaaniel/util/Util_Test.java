package io.ddaaniel.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

import io.ddaaniel.internal.parser.util.Util;

/**
 * A simple Util_Test
 */
public class Util_Test {

	@Test
	public void assertingIsIndexOf() throws Exception {
		var source = ByteBuffer.wrap("AbCdEfGhhhhIjKlMnOpQrStUvWxYz".getBytes());
		var string = "hhh";
		var result = Util.IndexOf(source, string, 0);
		assertEquals(7, result);

		source = ByteBuffer.wrap("AbCdEfGhhIjKlMnOpQrShhhhtUvWxYz".getBytes());
		string = "hhh";
		result = Util.IndexOf(source, string, 0);
		assertEquals(20, result);

		source = ByteBuffer.wrap("AbCdEfGhhIjKlMnOpQhhrStUhhvWxYz".getBytes());
		string = "hhh";
		result = Util.IndexOf(source, string, 0);
		assertEquals(-1, result);
	}

	@Test
	public void assertingIsIndexOfBuf() throws Exception {
		var source = ByteBuffer.wrap("AbCdEfGhhhhIjKlMnOpQrStUvWxYz".getBytes());
		var string = "hhh";
		var result = Util.IndexOf(source, string, 0);
		assertEquals(7, result);

		source = ByteBuffer.wrap("AbCdEfGhhIjKlMnOpQrShhhhtUvWxYz".getBytes());
		string = "hhh";
		result = Util.IndexOf(source, string, 0);
		assertEquals(20, result);

		source = ByteBuffer.wrap("AbCdEfGhhIjKlMnOpQhhrStUhhvWxYz".getBytes());
		string = "hhh";
		result = Util.IndexOf(source, string, 0);
		assertEquals(-1, result);
	}

	@Test
	public void assertingIsIndexOfArr() throws Exception {
		var source = "AbCdEfGhhhhIjKlMnOpQrStUvWxYz".getBytes();
		var string = "hhh";
		var result = Util.IndexOf(source, string, 0);
		assertEquals(7, result);

		source = "AbCdEfGhhIjKlMnOpQrShhhhtUvWxYz".getBytes();
		string = "hhh";
		result = Util.IndexOf(source, string, 0);
		assertEquals(20, result);

		source = "AbCdEfGhhIjKlMnOpQhhrStUhhvWxYz".getBytes();
		string = "hhh";
		result = Util.IndexOf(source, string, 0);
		assertEquals(-1, result);
	}

	@Test
	public void assertingCountPatternsCorrectly() throws Exception {
		var source = "AbChhdEfGhhhhIjKlMnOphhQrStUvWxYz".getBytes();
		var string = "hh";
		var result = Util.count(source, string);
		assertEquals(4, result);

		source = "AbCdEfGhhhhIjKlMnOpQhhhhrStUvWxYhhhzhhh".getBytes();
		string = "hhh";
		result = Util.count(source, string);
		assertEquals(4, result);
	}



	@Test
	public void assertingSplitsLimited() throws Exception {
		var source = "AbCdEfGhhhhIjKlMnOpQhhhhrStUvWxYz".getBytes();
		var string = "hhh";
		var times = 3;
		var returns = Util.Split(source, string, times);

		assertEquals(returns.length, times);
		assertEquals(new String(returns[0]), "AbCdEfG");
		assertEquals(new String(returns[1]), "hIjKlMnOpQ");
		assertEquals(new String(returns[2]), "hrStUvWxYz");
	}



	@Test
	public void assertingSplitsUnlimited() throws Exception {
		var source = "AbCdEfGhhhhIjKlMnOpQhhhhrStUvWxYhhhzhhh".getBytes();
		var string = "hhh";
		var times = -1;
		var returns = Util.Split(source, string, times);

		assertEquals(returns.length, 5);
		assertEquals(new String(returns[0]), "AbCdEfG");
		assertEquals(new String(returns[1]), "hIjKlMnOpQ");
		assertEquals(new String(returns[2]), "hrStUvWxY");
		assertEquals(new String(returns[3]), "z");
		assertEquals(new String(returns[4]), "");
	}



	@Test
	public void assertingTrimSpaces() throws Exception {
		var arr = "   AbCdEfGhhhhIjK  lMnOpQhrStUvWxYz   ".getBytes();
		var result = Util.TrimSpace(arr);
		assertEquals("AbCdEfGhhhhIjK  lMnOpQhrStUvWxYz", new String(result));
	}



	@Test
	public void assertingHasSuffix() throws Exception {
		var arr = "   AbCdEfGhIjK  lMnOpQhrStUvWxYz   ".getBytes();
		var slice = "  ".getBytes();
		var result = Util.HasSuffix(arr, slice);
		assertEquals(true, result);

		arr = "   AbCdEfGhIjK  lMnOpQhrStUvWxYz ".getBytes();
		slice = "  ".getBytes();
		result = Util.HasSuffix(arr, slice);
		assertEquals(false, result);
	}
}
