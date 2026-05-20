package io.ddaaniel.headers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.ddaaniel.internal.exception.MalformedHeaderException;
import io.ddaaniel.internal.parser.header.HeaderParsing;


/**
 * Header_Test
 */
public class Header_Test {

	@Test
	public void assertingIsIndexOf() throws Exception {
		var source = "AbCdEfGhhhhIjKlMnOpQrStUvWxYz".getBytes();
		var string = "hhh";
		var obj = new HeaderParsing();
		var method = HeaderParsing.class.getDeclaredMethod(
				"IndexOf", 
				byte[].class, 
				String.class,
				int.class);
		method.setAccessible(true);

		var result = method.invoke(obj, source, string, 0);
		assertEquals(7, result);

		source = "AbCdEfGhhIjKlMnOpQrShhhhtUvWxYz".getBytes();
		string = "hhh";
		result = method.invoke(obj, source, string, 0);
		assertEquals(20, result);

		source = "AbCdEfGhhIjKlMnOpQhhrStUhhvWxYz".getBytes();
		string = "hhh";
		result = method.invoke(obj, source, string, 0);
		assertEquals(-1, result);
	}


	@Test
	public void assertingCountPatternsCorrectly() throws Exception {
		var source = "AbChhdEfGhhhhIjKlMnOphhQrStUvWxYz".getBytes();
		var string = "hh";
		var obj = new HeaderParsing();
		var method = HeaderParsing.class.getDeclaredMethod(
				"count", 
				byte[].class, 
				String.class);
		method.setAccessible(true);
		
		var result = method.invoke(obj, source, string);
		assertEquals(4, result);

		source = "AbCdEfGhhhhIjKlMnOpQhhhhrStUvWxYhhhzhhh".getBytes();
		string = "hhh";
		result = method.invoke(obj, source, string);

		assertEquals(4, result);
	}

	@Nested
	class assertingSplits {
		@Test
		public void assertingSplitsLimited() throws Exception {
			var source = "AbCdEfGhhhhIjKlMnOpQhhhhrStUvWxYz".getBytes();
			var string = "hhh";
			var times = 3;
			var obj = new HeaderParsing();
			var method = HeaderParsing.class.getDeclaredMethod(
					"Split", 
					byte[].class, 
					String.class, 
					Integer.class);
			method.setAccessible(true);

			var returns = (byte[][]) method.invoke(obj, source, string, times);
			// returns = HeaderParsing.Split(source, string, times);

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
			var obj = new HeaderParsing();
			var method = HeaderParsing.class.getDeclaredMethod(
					"Split", 
					byte[].class, 
					String.class, 
					Integer.class);
			method.setAccessible(true);

			var returns = (byte[][]) method.invoke(obj, source, string, times);

			assertEquals(returns.length, 5);
			assertEquals(new String(returns[0]), "AbCdEfG");
			assertEquals(new String(returns[1]), "hIjKlMnOpQ");
			assertEquals(new String(returns[2]), "hrStUvWxY");
			assertEquals(new String(returns[3]), "z");
			assertEquals(new String(returns[4]), "");
		}
	}


	@Test
	public void assertingTrimSpaces() throws Exception {
		var arr = "   AbCdEfGhhhhIjK  lMnOpQhrStUvWxYz   ".getBytes();
		var obj = new HeaderParsing();
		var method = HeaderParsing.class.getDeclaredMethod("TrimSpace", byte[].class);
		method.setAccessible(true);

		var result = (byte[]) method.invoke(obj, arr);
		assertEquals("AbCdEfGhhhhIjK  lMnOpQhrStUvWxYz", new String(result));
	}

	@Test
	public void assertingHasSuffix() throws Exception {
		var arr = "   AbCdEfGhIjK  lMnOpQhrStUvWxYz   ".getBytes();
		var slice = "  ".getBytes();
		var obj = new HeaderParsing();
		var method = HeaderParsing.class.getDeclaredMethod(
				"HasSuffix", 
				byte[].class,
				byte[].class);
		method.setAccessible(true);

		var result = (boolean) method.invoke(obj, arr, slice);
		assertEquals(true, result);

		arr = "   AbCdEfGhIjK  lMnOpQhrStUvWxYz ".getBytes();
		slice = "  ".getBytes();
		result = (boolean) method.invoke(obj, arr, slice);
		assertEquals(false, result);
	}

	
	@Test
	public void TestHeaderParse() {
		var headers = new HeaderParsing().NewHeaders();
		var data = ByteBuffer.wrap("Host: localhost:42069\r\nFooFoo:       barbar     \r\n\r\n".getBytes());
		var fieldline = headers.fieldline;
		var option = headers.Parse(data);

		assertNotNull(headers);
		assertEquals("localhost:42069", fieldline.map().get("Host"));
		assertEquals(52, option._1);
		assertTrue(option._2);


		var badData = ByteBuffer.wrap("        Host : localhost:42069          \r\n\r\n".getBytes());
		var err = assertThrowsExactly(MalformedHeaderException.class, () -> {
			headers.Parse(badData); 
		});

		assertEquals(err.getMessage(), " -> malformed field-name ");
	}


}
