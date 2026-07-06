package io.ddaaniel.reader;

import org.junit.jupiter.api.Test;

import io.ddaaniel.internal.exception.MalformedBodyException;
import io.ddaaniel.internal.exception.MalformedHeaderException;
import io.ddaaniel.internal.parser.reader.ServletReader;
import io.ddaaniel.reader.mocks.ChunkReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;


/**
 * A simple ServletReader_Test
 */
public class ServletReader_Test {

	/**
	 * Rigorous Test :-)
	 */
	@Test
	public void TestRequestLineParse() {
		// Test: Good GET Request line without path
		var bytes = "GET / HTTP/1.1\r\nHost: localhost:42069\r\nUser-Agent: curl/8.20.0\r\nAccept: */*\r\n\r\n".getBytes();
		var conn = new ChunkReader(bytes, 2);
		var reader = new ServletReader(conn).ProcessMessage();

		assertEquals("GET", reader.method());
		assertEquals("/", reader.uri());

		// Test: Good GET Request line with path
		bytes = "GET /coffee HTTP/1.1\r\nHost: localhost:42069\r\nUser-Agent: curl/8.20.0\r\nAccept: */*\r\n\r\n".getBytes();
		conn = new ChunkReader(bytes, 2);
		reader = new ServletReader(conn).ProcessMessage();

		assertEquals("GET", reader.method());
		assertEquals("/coffee", reader.uri());
	}

	/**
	 * Rigorous Test :-)
	 */
	@Test
	public void TestParseHeaders() {
		// Test: Standard Headers
		var bytes = "GET / HTTP/1.1\r\nHost: localhost:42069\r\nUser-Agent: curl/8.20.0\r\nAccept: */*\r\n\r\n".getBytes();
		var conn = new ChunkReader(bytes, 3);
		var reader = new ServletReader(conn).ProcessMessage();

		assertNotNull(reader);
		assertEquals("localhost:42069", reader.headers().getHost().getHostString() + ":" + reader.headers().getHost().getPort());
		assertEquals("curl/8.20.0", reader.headers().getFirst("user-agent"));
		assertEquals("*/*", reader.headers().getAccept().getFirst());

		// Test: Malformed Header
		bytes = "GET / HTTP/1.1\r\nHost localhost:42069\r\n\r\n".getBytes();
		var connErr = new ChunkReader(bytes, 3);
		var err = assertThrowsExactly(MalformedHeaderException.class, () -> {
			new ServletReader(connErr).ProcessMessage();
		});
		assertEquals(" -> malformed header-name ", err.getMessage());
	}

	/**
	 * Rigorous Test :-)
	 */
	@Test
	public void TestParseBody() {
		// Test: Standard Body
		var data = ("Post /submit HTTP/1.1\r\n" +
			"Host: localhost:42069\r\n" +
			"Content-Length: 12\r\n" +
			"\r\n" +
			"hello world\n").getBytes();
		var conn = new ChunkReader(data, 3);
		var reader = new ServletReader(conn).ProcessMessage();
		assertNotNull(reader);
		assertEquals("hello world\n", new String(reader.getBodyAsString()));


		// Test: Body shorter than reported content length
		var badData = ("Post /submit HTTP/1.1\r\n" +
				"Host: localhost:42069\r\n" +
				"Content-Length: 20\r\n" +
				"\r\n" +
				"partial content").getBytes();
		var badConn = new ChunkReader(badData, 3);
		var badReader = new ServletReader(badConn).ProcessMessage();

		var err = assertThrowsExactly(MalformedBodyException.class, () -> {
			badReader.getBodyAsString();
		});
		assertEquals(" -> body shorter than reported content-length ", err.getMessage());
	}
}
