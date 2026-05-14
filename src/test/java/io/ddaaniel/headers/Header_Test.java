package io.ddaaniel.headers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

import io.ddaaniel.internal.parser.header.HeaderParsing;


/**
 * Header_Test
 */
public class Header_Test {

	@Test
	public void asserting() {
		assertTrue(true);
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
