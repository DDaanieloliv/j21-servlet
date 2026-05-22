package io.ddaaniel.headers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;

import io.ddaaniel.internal.exception.MalformedHeaderException;
import io.ddaaniel.internal.parser.header.HeaderParsing;


/**
 * Header_Test
 */
public class Header_Test {

	@Test
	public void TestHeaderParse() {
		var headers = new HeaderParsing().NewHeaders();
		var data = ByteBuffer.wrap("Host: localhost:42069\r\nFooFoo:       barbar     \r\n\r\n".getBytes());
		var wrongFormat = ByteBuffer.wrap("        Host : localhost:42069          \r\n\r\n".getBytes());
		var wrongToken = ByteBuffer.wrap("H®st: localhost:42069\r\n\r\n".getBytes());
		var option = headers.Parse(data);

		var errBadToken = assertThrowsExactly(MalformedHeaderException.class, () -> {
			headers.Parse(wrongToken); 
		});
		var errBadHeader = assertThrowsExactly(MalformedHeaderException.class, () -> {
			headers.Parse(wrongFormat); 
		});
		assertEquals(52, option._1);
		assertNotNull(headers);
		assertEquals("localhost:42069", headers.Get("Host"));
		assertTrue(option._2);
		assertEquals(errBadToken.getMessage(), " -> malformed header-name ");
		assertEquals(errBadHeader.getMessage(), " -> malformed field-name ");
	}

}
