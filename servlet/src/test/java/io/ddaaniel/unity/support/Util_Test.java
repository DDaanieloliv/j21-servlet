package io.ddaaniel.unity.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

import io.ddaaniel.listener.internal.support.httpUtil.HttpUtil;




/**
 * A simple Util_Test
 */
public class Util_Test {

	@Test
	public void assertingIsIndexOf() throws Exception {
		var source = ByteBuffer.wrap("AbCdEfGhhhhIjKlMnOpQrStUvWxYz".getBytes());
		var string = "hhh";
		var result = HttpUtil.IndexOf(source, 0, string);
		assertEquals(7, result);

		source = ByteBuffer.wrap("AbCdEfGhhIjKlMnOpQrShhhhtUvWxYz".getBytes());
		string = "hhh";
		result = HttpUtil.IndexOf(source, 0, string);
		assertEquals(20, result);

		source = ByteBuffer.wrap("AbCdEfGhhIjKlMnOpQhhrStUhhvWxYz".getBytes());
		string = "hhh";
		result = HttpUtil.IndexOf(source, 0, string);
		assertEquals(-1, result);
	}

	@Test
	public void assertingIsIndexOfBuf() throws Exception {
		var source = ByteBuffer.wrap("AbCdEfGhhhhIjKlMnOpQrStUvWxYz".getBytes());
		var string = "hhh";
		var result = HttpUtil.IndexOf(source, 0, string);
		assertEquals(7, result);

		source = ByteBuffer.wrap("AbCdEfGhhIjKlMnOpQrShhhhtUvWxYz".getBytes());
		string = "hhh";
		result = HttpUtil.IndexOf(source, 0, string);
		assertEquals(20, result);

		source = ByteBuffer.wrap("AbCdEfGhhIjKlMnOpQhhrStUhhvWxYz".getBytes());
		string = "hhh";
		result = HttpUtil.IndexOf(source, 0, string);
		assertEquals(-1, result);
	}

	@Test
	public void assertingIsIndexOfArr() throws Exception {
		var source = "AbCdEfGhhhhIjKlMnOpQrStUvWxYz".getBytes();
		var string = "hhh";
		var result = HttpUtil.IndexOf(source, string, 0);
		assertEquals(7, result);

		source = "AbCdEfGhhIjKlMnOpQrShhhhtUvWxYz".getBytes();
		string = "hhh";
		result = HttpUtil.IndexOf(source, string, 0);
		assertEquals(20, result);

		source = "AbCdEfGhhIjKlMnOpQhhrStUhhvWxYz".getBytes();
		string = "hhh";
		result = HttpUtil.IndexOf(source, string, 0);
		assertEquals(-1, result);
	}

	@Test
	public void assertingCountPatternsCorrectly() throws Exception {
		var source = "AbChhdEfGhhhhIjKlMnOphhQrStUvWxYz".getBytes();
		var string = "hh";
		var result = HttpUtil.count(source, string);
		assertEquals(4, result);

		source = "AbCdEfGhhhhIjKlMnOpQhhhhrStUvWxYhhhzhhh".getBytes();
		string = "hhh";
		result = HttpUtil.count(source, string);
		assertEquals(4, result);
	}



	@Test
	public void assertingSplitsLimited() throws Exception {
		var source = "AbCdEfGhhhhIjKlMnOpQhhhhrStUvWxYz".getBytes();
		var string = "hhh";
		var times = 3;
		var returns = HttpUtil.Split(source, string, times);

		assertEquals(times, returns.length);
		assertEquals("AbCdEfG", new String(returns[0]));
		assertEquals("hIjKlMnOpQ", new String(returns[1]));
		assertEquals("hrStUvWxYz", new String(returns[2]));
	}



	@Test
	public void assertingSplitsUnlimited() throws Exception {
		var source = "AbCdEfGhhhhIjKlMnOpQhhhhrStUvWxYhhhzhhh".getBytes();
		var string = "hhh";
		var times = -1;
		var returns = HttpUtil.Split(source, string, times);

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
		var result = HttpUtil.TrimSpace(arr);
		assertEquals("AbCdEfGhhhhIjK  lMnOpQhrStUvWxYz", new String(result));
	}



	@Test
	public void assertingHasSuffix() throws Exception {
		var arr = "   AbCdEfGhIjK  lMnOpQhrStUvWxYz   ".getBytes();
		var slice = "  ".getBytes();
		var result = HttpUtil.HasSuffix(arr, slice);
		assertEquals(true, result);

		arr = "   AbCdEfGhIjK  lMnOpQhrStUvWxYz ".getBytes();
		slice = "  ".getBytes();
		result = HttpUtil.HasSuffix(arr, slice);
		assertEquals(false, result);
	}
}
