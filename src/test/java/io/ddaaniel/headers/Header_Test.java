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
		var badData = ByteBuffer.wrap("        Host : localhost:42069          \r\n\r\n".getBytes());
		var fieldline = headers.fieldline;
		var option = headers.Parse(data);

		assertEquals(52, option._1);
		var err = assertThrowsExactly(MalformedHeaderException.class, () -> {
			headers.Parse(badData); 
		});
		assertNotNull(headers);
		assertEquals("localhost:42069", fieldline.map().get("Host"));
		assertTrue(option._2);
		assertEquals(err.getMessage(), " -> malformed field-name ");
	}

}
