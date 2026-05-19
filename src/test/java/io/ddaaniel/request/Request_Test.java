package io.ddaaniel.request;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.ddaaniel.internal.parser.request.RequestParsing;
import io.ddaaniel.mocks.ChunkReader;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * A simple Request_Test
 */
public class Request_Test {

	private final ByteBuffer buffer = ByteBuffer.allocate(1024);

	@Test
	public void assertingIsIndexOf() throws Exception {
		var source = ByteBuffer.wrap("AbCdEfGhhhhIjKlMnOpQrStUvWxYz".getBytes());
		var string = "hhh";
		var obj = new RequestParsing();
		var method = RequestParsing.class.getDeclaredMethod(
				"IndexOf", 
				ByteBuffer.class, 
				String.class,
				int.class);
		method.setAccessible(true);

		var result = method.invoke(obj, source, string, 0);
		assertEquals(7, result);

		source = ByteBuffer.wrap("AbCdEfGhhIjKlMnOpQrShhhhtUvWxYz".getBytes());
		string = "hhh";
		result = method.invoke(obj, source, string, 0);
		assertEquals(20, result);

		source = ByteBuffer.wrap("AbCdEfGhhIjKlMnOpQhhrStUhhvWxYz".getBytes());
		string = "hhh";
		result = method.invoke(obj, source, string, 0);
		assertEquals(-1, result);
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
