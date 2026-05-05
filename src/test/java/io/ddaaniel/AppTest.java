package io.ddaaniel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.ddaaniel.tcpListener.ChannelContext;
import io.ddaaniel.tcpListener.Context;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * A simple AppTest
 */
public class AppTest {

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
	public void shouldReadAPayloadCorrectly() throws IOException {
		var channel = new LinkedBlockingDeque<String>();
		var context = new Context(channel);
		var mockData = ByteBuffer.wrap(
				("A society grows great when\n" +
				 "old men plant trees whose shade\n" +
				 "they know they shall never sit in.\n" +
				 "END").getBytes()
				);

		var pipe = Pipe.open();
		pipe.sink().write(mockData);
		pipe.sink().close();
		var mockSocket = (ReadableByteChannel) pipe.source();
		context.attachKeyContext(mockSocket);

		// mockSelector, 20 rounds cuse we read 8bytes at time in getLinesChannel
		for (int i = 0; i < 20; i++) { ChannelContext.getLinesChannel(context); }

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
			assertEquals(context.getBuff().get(0), (byte) 'E');
			assertEquals(context.getBuff().get(1), (byte) 'N');
			assertEquals(context.getBuff().get(2), (byte) 'D');
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@BeforeEach
	void setup() {
		buffer.clear();
	}
}
