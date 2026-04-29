package io.ddaaniel.tcpListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * LinesChannel
 */
public class ChannelContext {

	private final ByteBuffer buf = ByteBuffer.allocate(1024);
	private final ByteBuffer part = ByteBuffer.allocate(8);
	private final BlockingQueue<String> channel;

	public ChannelContext(BlockingQueue<String> channel) { this.channel = channel; }

	public BlockingQueue<String> getQueue() { return channel; }
	public ByteBuffer getBuffer() { return buf; }
	public ByteBuffer getPart() { return part; }


	private static int indexOf(byte[] bytes, int character) {
		for (int i = 0; i < bytes.length; i++)
			if (bytes[i] == character)
				return i;
		return -1;
	}

	private static int indexOf(ByteBuffer buff, int character) {
		for (int i = 0; i < buff.limit(); i++)
			if (buff.get(i) == character)
				return i;
		return -1;
	}



	// TODO: discard the finally implementations by using the content-lenght
	public void getLinesChannel(ReadableByteChannel conn, ChannelContext context) {
		var buf = context.buf;
		var part = context.part;
		var channel = context.channel;

		try {

			var scope_reached = 0;
			var debug = new char[8];

			int readed = conn.read(part);
			if ((readed = 0) == -1) return;
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
		finally { 

			try {
				if (buf.position() > 0) 
				{
					buf.flip();
					channel.put(StandardCharsets.UTF_8.decode(buf).toString());
					buf.clear();
				}

				channel.put("EOF_SIGNAL");
			} catch (InterruptedException e) { e.printStackTrace(); }

		}

		return;
	}


	public BlockingQueue<String> getLinesChannel(ReadableByteChannel conn) {

		new Thread( () -> {
			try {

				var scope_reached = 0;
				var debug = new char[8];

				int readed;
				for (;;) {
					if ((readed = conn.read(part)) == -1) break;
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


			} 
			catch (InterruptedException | IOException e) { e.printStackTrace(); }
			finally { 
				try {
					if (buf.position() > 0) 
					{
						buf.flip();
						channel.put(StandardCharsets.UTF_8.decode(buf).toString());
						buf.clear();
					}

					channel.put("EOF_SIGNAL");
				} catch (InterruptedException e) { e.printStackTrace(); }
			}

		}).start();

		return channel;
	}


	public BlockingQueue<String> getLinesChannel(InputStream io) {
		BlockingQueue<String> channel = new LinkedBlockingQueue<>();
	  final byte[] arrAux = new byte[8];


		new Thread( () -> {
			try {

				InputStream st = io;

				var scope_reached = 0;
				var debug = new char[8];

				int readed;
				for (;;) {
					if ((readed = st.read(arrAux)) == -1) break;
					int idxN = indexOf(arrAux, 10); 

					debug = new String(arrAux, StandardCharsets.UTF_8).toCharArray();

					if (idxN != -1) {
						buf.put(arrAux, 0, idxN);
						buf.flip();
						channel.put(StandardCharsets.UTF_8.decode(buf).toString());
						Arrays.fill(arrAux, idxN, idxN + 1, (byte) 0);
						buf.clear();

						if ( ((readed - 1) - idxN) > 0) 
						{
							buf.put( arrAux, (idxN + 1), ((readed - 1) - idxN) );
						}

					} else buf.put(arrAux, 0, readed);
				}

				if (buf.position() > 0) 
				{
					buf.flip();
					channel.put(StandardCharsets.UTF_8.decode(buf).toString());
					buf.clear();
				}
				st.close();
				
			} catch (Exception e) { e.printStackTrace(); }
		}).start();

		return channel;
	}


	public BlockingQueue<String> getLinesChannel(Socket conn) {
		BlockingQueue<String> channel = new LinkedBlockingQueue<>();
	  final byte[] arrAux = new byte[8];

		new Thread( () -> {
			try {

				InputStream st = conn.getInputStream();

				int readed;
				while ((readed = st.read(arrAux)) != -1) {
					int idxAfterN = 0;
					for (int idxElmt = 0; idxElmt < readed; idxElmt++){
						if (arrAux[idxElmt] == 10) {
							buf.put(arrAux, idxAfterN, idxElmt - idxAfterN);
							buf.flip();

							channel.put(StandardCharsets.UTF_8.decode(buf).toString());
							buf.clear();
							idxAfterN = idxElmt + 1;
						}
					}	

					if (idxAfterN < readed) {
						buf.put(arrAux, idxAfterN, readed - idxAfterN);
					}
				}

				if (buf.position() > 0) {
					buf.flip();
					channel.put(StandardCharsets.UTF_8.decode(buf).toString());
					buf.clear();
				}
				st.close();
				
			} catch (Exception e) { e.printStackTrace(); }
		}).start();

		return channel;
	}

}
