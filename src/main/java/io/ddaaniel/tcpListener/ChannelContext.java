package io.ddaaniel.tcpListener;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;

import io.ddaaniel.internal.parser.message.Request;
import io.ddaaniel.internal.parser.message.RequestLine;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;


/**
 * ChannelContext
 */
public abstract class ChannelContext {


	private static int IndexOf(ByteBuffer buff, int character) {
		for (int i = 0; i < buff.limit(); i++)
			if (buff.get(i) == character)
				return i;
		return -1;
	}

	// TODO: avoid the usage of "GodObject" - Context
	// TODO: discard the finally implementations by using the content-lenght
	// TODO: getLinesChannel(Context context) -> Class parser | parseRequest(conn)
	public static void getLinesChannel(Context context) {
		var buf = context.getBuff(); 
		var part = context.getPart();
		var channel = context.getChannel();
		var conn = context.getConnection();

		try {

			int readed = conn.read(part);
			if (readed == -1) return;
			part.flip();
			int idxN = IndexOf(part, 10);

			if (idxN != -1) {
				part.limit(idxN);
				buf.put(part);
				buf.flip();
				channel.put(StandardCharsets.UTF_8.decode(buf).toString());
				buf.clear();
				part.clear();

				if ( ((readed - 1) - idxN) > 0)
				{
					part.position(idxN + 1);
					buf.put(part);
					part.clear();
				}

			} else {
				part.limit(readed);
				buf.put(part);
				part.clear();
			}

		}
		catch (InterruptedException | IOException e) { e.printStackTrace(); }

		return;
	}
}
