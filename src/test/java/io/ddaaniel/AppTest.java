package io.ddaaniel;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.ddaaniel.internal.parser.RequestParsing;
import io.ddaaniel.mocks.ChunkReader;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * A simple AppTest
 */
public class AppTest {

	private final ByteBuffer buffer = ByteBuffer.allocate(1024);

	/**
	 * Rigorous Test :-)
	 */
	@Test
	public void shouldAnswerWithTrue() {
		assertTrue(true);
	}

	/**
	 * Rigorous Test :-)
	 */
	@Test
	public void TestRequestLineParse() {
		var bytes = "GET / HTTP/1.1\r\nHost: localhost:42069\r\nUser-Agent: curl/8.20.0\r\nAccept: */*\r\n\r\n".getBytes();
		var reader = new ChunkReader(bytes, 2);
		var request = new RequestParsing().RequestFromReader(reader);

		assertEquals("GET", request.requestLine.Method);
		assertEquals("/", request.requestLine.RequestTarget);
		assertEquals("1.1", request.requestLine.HttpVersion);


		bytes = "GET /coffee HTTP/1.1\r\nHost: localhost:42069\r\nUser-Agent: curl/8.20.0\r\nAccept: */*\r\n\r\n".getBytes();
		reader = new ChunkReader(bytes, 2);
		request = new RequestParsing().RequestFromReader(reader);

		assertEquals("GET", request.requestLine.Method);
    assertEquals("/coffee", request.requestLine.RequestTarget);
    assertEquals("1.1", request.requestLine.HttpVersion);
	}

	@BeforeEach
	void setup() {
		buffer.clear();
	}
}
