package io.ddaaniel.headers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

import io.ddaaniel.internal.parser.header.HeaderParsing;


/**
 * Header_Test
 */
public class Header_Test {

	@Test
	public void assertingIsIndexOf() throws Exception {
		var source = ByteBuffer.wrap("AbCdEfGhhhhIjKlMnOpQrStUvWxYz".getBytes());
		var string = "hhh";
		var obj = new HeaderParsing();
		var method = HeaderParsing.class.getDeclaredMethod(
				"IndexOf", 
				ByteBuffer.class, 
				String.class);
		method.setAccessible(true);

		var result = method.invoke(obj, source, string);
		assertEquals(7, result);

		source = ByteBuffer.wrap("AbCdEfGhhIjKlMnOpQrShhhhtUvWxYz".getBytes());
		string = "hhh";
		result = method.invoke(obj, source, string);
		assertEquals(20, result);

		source = ByteBuffer.wrap("AbCdEfGhhIjKlMnOpQhhrStUhhvWxYz".getBytes());
		string = "hhh";
		result = method.invoke(obj, source, string);
		assertEquals(-1, result);
	}


	@Test
	public void assertingNsplites() throws Exception {
		var source = ByteBuffer.wrap("AbCdEfGhhhhIjKlMnOpQrStUvWxYz".getBytes());
		var string = "hhh";
		var times = 2;

		var obj = new HeaderParsing();
		var method = HeaderParsing.class.getDeclaredMethod(
				"Split", 
				ByteBuffer.class, 
				String.class, 
				Integer.class);
		method.setAccessible(true);
		var returns = (byte[][]) method.invoke(obj, source, string, times);
		// var returns = HeaderParsing.Split(source, string, times);

		assertEquals(returns.length, times);
		assertEquals(new String(returns[0]), "AbCdEfG");
		assertEquals(new String(returns[1]), "hIjKlMnOpQrStUvWxYz");

		source = ByteBuffer.wrap("AbCdEfGhhhhIjKlMnOpQhhhhrStUvWxYz".getBytes());
		string = "hhh";
		times = 2;

		returns = (byte[][]) method.invoke(obj, source, string, times);
		// returns = HeaderParsing.Split(source, string, times);

		assertEquals(returns.length, times);
		assertEquals(new String(returns[0]), "AbCdEfG");
		assertEquals(new String(returns[1]), "hIjKlMnOpQhhhhrStUvWxYz");

		source = ByteBuffer.wrap("AbCdEfGhhhhIjKlMnOpQhhhhrStUvWxYz".getBytes());
		string = "hhh";
		times = 3;

		returns = (byte[][]) method.invoke(obj, source, string, times);
		// returns = HeaderParsing.Split(source, string, times);

		assertEquals(returns.length, times);
		assertEquals(new String(returns[0]), "AbCdEfG");
		assertEquals(new String(returns[1]), "hIjKlMnOpQ");
		assertEquals(new String(returns[2]), "hrStUvWxYz");
	}

	
	@Test
	public void TestHeaderParse() {
		var headers = new HeaderParsing().NewHeaders();
		var data = ByteBuffer.wrap("Host: localhost:42069\r\n\r\n".getBytes());
		var fieldline = headers.fieldline;
		var option = headers.Parse(data);

		assertNotNull(headers);
		assertEquals("localhost:42069", fieldline.map.get("Host:"));
		assertEquals(23, option._1);
		assertFalse(option._2);


		headers = new HeaderParsing().NewHeaders();
		data = ByteBuffer.wrap("        Host : localhost:42069          \r\n\r\n".getBytes());
		option = headers.Parse(data);

		assertEquals(0, option._1);
		assertFalse(option._2);
	}


}
