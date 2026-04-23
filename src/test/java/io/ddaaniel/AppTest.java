package io.ddaaniel;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.ddaaniel.tcpListener.LinesChannel;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Unit test for simple App.
 */
public class AppTest {

		public static final byte[] arrAux = new byte[8];
		public static final ByteBuffer buffer = ByteBuffer.allocate(1024);


		@BeforeEach
		void setup() { 
			buffer.clear();
			Arrays.fill(arrAux, (byte) 0);
		}

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
		public void shouldReadAPayloadCorrectly() {
			var lines = new LinesChannel();
			var mockData = "A society grows great when\nold men plant trees whose shade\nthey know they shall never sit in.\nEND";
			var byteStream = new ByteArrayInputStream(mockData.getBytes(StandardCharsets.UTF_8));

			var channel = lines.getLinesChannel(byteStream);

			try {

				assertEquals("A society grows great when", channel.poll(500, TimeUnit.MILLISECONDS));
				assertEquals("old men plant trees whose shade", channel.poll(500, TimeUnit.MILLISECONDS));
				assertEquals("they know they shall never sit in.", channel.poll(500, TimeUnit.MILLISECONDS));
				assertEquals("END", channel.poll(500, TimeUnit.MILLISECONDS));
				
			} catch (Exception e) { e.printStackTrace(); }
		}
}
