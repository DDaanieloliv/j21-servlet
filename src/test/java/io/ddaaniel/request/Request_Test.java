package io.ddaaniel.request;

import org.junit.jupiter.api.Test;

import io.ddaaniel.internal.exception.MalformedBodyException;
import io.ddaaniel.internal.exception.MalformedHeaderException;
import io.ddaaniel.internal.parser.request.Requests;
import io.ddaaniel.mocks.ChunkReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;


/**
 * A simple Request_Test
 */
public class Request_Test {

	/**
	 * Rigorous Test :-)
	 */
	@Test
	public void TestRequestLineParse() {
		// Test: Good GET Request line without path
		var bytes = "GET / HTTP/1.1\r\nHost: localhost:42069\r\nUser-Agent: curl/8.20.0\r\nAccept: */*\r\n\r\n".getBytes();
		var reader = new ChunkReader(bytes, 2);
		var request = new Requests().RequestFromReader(reader);

		assertEquals("GET", request.RequestLine.Method);
		assertEquals("/", request.RequestLine.RequestTarget);
		assertEquals("1.1", request.RequestLine.HttpVersion);

		// Test: Good GET Request line with path
		bytes = "GET /coffee HTTP/1.1\r\nHost: localhost:42069\r\nUser-Agent: curl/8.20.0\r\nAccept: */*\r\n\r\n".getBytes();
		reader = new ChunkReader(bytes, 2);
		request = new Requests().RequestFromReader(reader);

		assertEquals("GET", request.RequestLine.Method);
		assertEquals("/coffee", request.RequestLine.RequestTarget);
		assertEquals("1.1", request.RequestLine.HttpVersion);
	}

	/**
	 * Rigorous Test :-)
	 */
	@Test
	public void TestParseHeaders() {
		// Test: Standard Headers
		var bytes = "GET / HTTP/1.1\r\nHost: localhost:42069\r\nUser-Agent: curl/8.20.0\r\nAccept: */*\r\n\r\n".getBytes();
		var reader = new ChunkReader(bytes, 3);
		var request = new Requests().RequestFromReader(reader);

		assertNotNull(request);
		assertEquals("localhost:42069", request.Headers.Get("host"));
		assertEquals("curl/8.20.0", request.Headers.Get("user-agent"));
		assertEquals("*/*", request.Headers.Get("accept"));

		// Test: Malformed Header
		bytes = "GET / HTTP/1.1\r\nHost localhost:42069\r\n\r\n".getBytes();
		var readerErr = new ChunkReader(bytes, 3);
		var err = assertThrowsExactly(MalformedHeaderException.class, () -> {
			new Requests().RequestFromReader(readerErr);
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
			"Content-Length: 13\r\n" +
			"\r\n" +
			"hello world!\n").getBytes();
		var reader = new ChunkReader(data, 3);
		var request = new Requests().RequestFromReader(reader);
		assertNotNull(request);
		assertEquals("hello world!\n", new String(request.Body));


		// Test: Body shorter than reported content length
		var badData = ("Post /submit HTTP/1.1\r\n" +
			"Host: localhost:42069\r\n" +
			"Content-Length: 20\r\n" +
			"\r\n" +
			"partial content").getBytes();
		var badReader = new ChunkReader(badData, 3);
		var err = assertThrowsExactly(MalformedBodyException.class, () -> {
			new Requests().RequestFromReader(badReader); 
		});
		assertEquals(" -> body shorter than reported content-length ", err.getMessage());
	}
}
