package io.ddaaniel.tcpListener;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * LinesChannel
 */
public abstract class ChannelContext {

	private static int indexOf(ByteBuffer buff, int character) {
		for (int i = 0; i < buff.limit(); i++)
			if (buff.get(i) == character)
				return i;
		return -1;
	}

	// TODO: discard the finally implementations by using the content-lenght
	public static void getLinesChannel(Context context) {
		var buf = context.getBuff(); 
		var part = context.getPart();
		var channel = context.getChannel();
		var conn = context.getConnection();

		try {

			var scope_reached = 0;
			var debug = new char[8];

			int readed = conn.read(part);
			if (readed == -1) return;
			part.flip();
			int idxN = indexOf(part, 10);

			debug = new String(part.array(), StandardCharsets.UTF_8).toCharArray();

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
