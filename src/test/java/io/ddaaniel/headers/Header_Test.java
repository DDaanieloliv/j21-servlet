package io.ddaaniel.headers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;

import io.ddaaniel.internal.exception.MalformedHeaderException;
import io.ddaaniel.internal.parser.header.Header;


/**
 * Header_Test
 */
public class Header_Test {

	@Test
	public void TestHeaderParse() {
		var headers = new Header().NewHeaders();
		var data = ByteBuffer.wrap("Host: localhost:42069\r\nFooFoo:       barbar     \r\n\r\n".getBytes());
		var option = headers.Parse(data);
		assertEquals(52, option._1);
		assertTrue(option._2);
		assertEquals("localhost:42069", headers.Get("HOST"));


		var wrongToken = ByteBuffer.wrap("H®st: localhost:42069\r\n\r\n".getBytes());
		var badHeaderByToken = new Header().NewHeaders();
		var errBadToken = assertThrowsExactly(MalformedHeaderException.class, () -> {
			badHeaderByToken.Parse(wrongToken); });
		assertEquals(errBadToken.getMessage(), " -> malformed header-name ");


		var wrongFormat = ByteBuffer.wrap("        Host : localhost:42069          \r\n\r\n".getBytes());
		var badHeaderByName = new Header().NewHeaders();
		var errBadHeader = assertThrowsExactly(MalformedHeaderException.class, () -> {
			badHeaderByName.Parse(wrongFormat); });
		assertEquals(errBadHeader.getMessage(), " -> malformed field-name ");


		headers = new Header().NewHeaders();
		data = ByteBuffer.wrap("Host: localhost:42069\r\nHost: localhost:42069\r\n".getBytes());
		option = headers.Parse(data);
		assertEquals(null, headers.Get("MissingKey"));
		assertFalse(option._2);


		headers = new Header().NewHeaders();
		data = ByteBuffer.wrap("Host: localhost:42069\r\nHost: localhost:42069\r\n".getBytes());
		option = headers.Parse(data);
		assertEquals("localhost:42069,localhost:42069", headers.Get("HOST"));
		assertFalse(option._2);
	}

}
