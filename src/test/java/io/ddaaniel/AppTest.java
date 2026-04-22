package io.ddaaniel;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

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
			var inputPathMessage = "/home/daniel/DEV_ENV/personal/dev/httpfromtcp/src/test/java/io/ddaaniel/payload/input/messages.txt";
			var outputResult = "/home/daniel/DEV_ENV/personal/dev/httpfromtcp/src/test/java/io/ddaaniel/payload/output/messages.txt";

			try {

				var content = Files.lines(Path.of(outputResult));
				var channel = new LinesChannel().getLinesChannel(inputPathMessage);
				content.forEach( (string) -> {
					try {
						assertEquals(string, channel.take());
					} catch (Exception e) { }
				});
				content.close();

			} catch (Exception e) { e.printStackTrace(); }
		}
}
