package io.ddaaniel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.ddaaniel.tcpListener.ChannelContext;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * A simple AppTest
 */
public class AppTest {

	public final byte[] arrAux = new byte[8];
	public final ByteBuffer buffer = ByteBuffer.allocate(1024);

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
	public void shouldReadAInputStreamCorrectly() {
		var lines = new ChannelContext(new LinkedBlockingQueue<>());
		var mockData =
			"A society grows great when\n" +
			"old men plant trees whose shade\n" +
			"they know they shall never sit in.\n" +
			"END";
		var byteStream = new ByteArrayInputStream(
				mockData.getBytes(StandardCharsets.UTF_8)
				);

		var channel = lines.getLinesChannel(byteStream);

		try {
			assertEquals(
					"A society grows great when",
					channel.poll(500, TimeUnit.MILLISECONDS)
					);
			assertEquals(
					"old men plant trees whose shade",
					channel.poll(500, TimeUnit.MILLISECONDS)
					);
			assertEquals(
					"they know they shall never sit in.",
					channel.poll(500, TimeUnit.MILLISECONDS)
					);
			assertEquals("END", channel.poll(500, TimeUnit.MILLISECONDS));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Rigorous Test :-)
	 */
	@Test
	public void shouldReadAPayloadIOCorrectly() throws IOException {
		var lines = new ChannelContext(new LinkedBlockingDeque<>());
		var mockData = ByteBuffer.wrap(
				(
				 "A society grows great when\n" +
				 "old men plant trees whose shade\n" +
				 "they know they shall never sit in.\n" +
				 "END"
				).getBytes()
				);

		var pipe = Pipe.open();
		pipe.sink().write(mockData);
		pipe.sink().close();
		var socket = pipe.source();
		var channel = lines.getLinesChannel(socket);

		try {
			assertEquals(
					"A society grows great when",
					channel.poll(500, TimeUnit.MILLISECONDS)
					);
			assertEquals(
					"old men plant trees whose shade",
					channel.poll(500, TimeUnit.MILLISECONDS)
					);
			assertEquals(
					"they know they shall never sit in.",
					channel.poll(500, TimeUnit.MILLISECONDS)
					);
			assertEquals("END", channel.poll(500, TimeUnit.MILLISECONDS));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Rigorous Test :-)
	 */
	@Test
	public void shouldReadAPayloadCorrectly() throws IOException {
		var channel = new LinkedBlockingDeque<String>();
		var reader = new ChannelContext(channel);
		var mockData = ByteBuffer.wrap(
				(
				 "A society grows great when\n" +
				 "old men plant trees whose shade\n" +
				 "they know they shall never sit in.\n" +
				 "END"
				).getBytes()
				);

		var pipe = Pipe.open();
		pipe.sink().write(mockData);
		pipe.sink().close();
		var mockSocket = pipe.source();

		// mockSelector
		for (int i = 0; i < 20; i++) { reader.getLinesChannel(mockSocket, reader); }

		try {
			assertEquals(
					"A society grows great when",
					channel.poll(500, TimeUnit.MILLISECONDS)
					);
			assertEquals(
					"old men plant trees whose shade",
					channel.poll(500, TimeUnit.MILLISECONDS)
					);
			assertEquals(
					"they know they shall never sit in.",
					channel.poll(500, TimeUnit.MILLISECONDS)
					);

			assertTrue(channel.isEmpty());
			assertEquals(reader.getBuffer().get(0), (byte) 'E');
			assertEquals(reader.getBuffer().get(1), (byte) 'N');
			assertEquals(reader.getBuffer().get(2), (byte) 'D');
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@BeforeEach
	void setup() {
		buffer.clear();
		Arrays.fill(arrAux, (byte) 0);
	}
}
