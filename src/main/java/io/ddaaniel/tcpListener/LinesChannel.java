package io.ddaaniel.tcpListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * LinesChannel
 */
public class LinesChannel {

	private static final ByteBuffer buf = ByteBuffer.allocate(1024);
	private static final ByteBuffer part = ByteBuffer.allocate(8);
	private static final byte[] arrAux = new byte[8];

	public LinesChannel() {}

	private static int indexOf(byte[] bytes, int character) {
		for (int i = 0; i < bytes.length; i++)
			if (bytes[i] == character)
				return i;
		return -1;
	}

	private static int indexOf(ByteBuffer buff, int character) {
		for (int i = 0; i < buff.capacity(); i++)
			if (buff.get(i) == character)
				return i;
		return -1;
	}



	public BlockingQueue<String> getLineChannel(String s) {
		BlockingQueue<String> channel = new LinkedBlockingQueue<>();

		new Thread( () -> {
			try {

				InputStream st = Files.newInputStream(Paths.get(s));
				int var_entrypoint_check = 0;

				int readed;
				for (;;) {
					if ((readed = st.read(arrAux)) == -1) break;
					int idxN = indexOf(arrAux, 10); 

					char[] charArr = new String(arrAux, StandardCharsets.UTF_8).toCharArray();

					if (idxN != -1) {
						buf.put(arrAux, 0, idxN);
						buf.flip();
						channel.put(StandardCharsets.UTF_8.decode(buf).toString());
						buf.clear();

						if ( ((readed - 1) - idxN) > 0) 
						{
							buf.put( arrAux, (idxN + 1), ((readed - 1) - idxN) );
							var_entrypoint_check = var_entrypoint_check + 1;
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

	public BlockingQueue<String> getLineChannel(Socket conn) {
		BlockingQueue<String> channel = new LinkedBlockingQueue<>();

		new Thread( () -> {
			try {

				InputStream st = conn.getInputStream();

				int readed;
				for (;;) {
					if ((readed = st.read(arrAux)) == -1) break;
					int idxN = indexOf(arrAux, 10); 

					if (idxN != -1) {
						buf.put(arrAux, 0, idxN);
						buf.flip();
						channel.put(StandardCharsets.UTF_8.decode(buf).toString());
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


	public BlockingQueue<String> lineChannel(Socket conn) {
		BlockingQueue<String> channel = new LinkedBlockingQueue<>();

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


	public BlockingQueue<String> LineChannel(String s) {
		BlockingQueue<String> chann = new LinkedBlockingQueue<>();

			new Thread( () -> {
				try {

					SeekableByteChannel sChannel = Files.newByteChannel(Paths.get(s));
					for (;;) {
						if (sChannel.read(part) == -1) break;
						part.flip(); // pointer = 0; and limit = last_position
						int idxN = indexOf(part, 10); 
						int partSize = part.capacity();

						if (idxN != -1) {
							part.limit(idxN);
							buf.put(part);
							part.limit(partSize);
							buf.flip(); // pointer = 0; and limit = last_position;
							String line = StandardCharsets.UTF_8.decode(buf).toString();

							buf.clear(); // restore pointer = 0; and limit = capacity;
							part.position(part.position() + 1);

							if ((partSize - (idxN + 1)) > 0) buf.put(part); 
							part.clear();
							chann.put(line);

						} else {
							buf.put(part); 
							part.clear();
						}
					}
					sChannel.close();

				} catch (Exception e) { e.printStackTrace(); }
			} ).start();

		return chann;
	}


	public BlockingQueue<String> LineChannel_ByteBuffer(String s) {
		BlockingQueue<String> channel = new LinkedBlockingQueue<>();

		new Thread( () -> {
			try {

				Path path = Paths.get(s);
				InputStream st = Files.newInputStream(path);
				byte[] part = new byte[8];
				ByteBuffer buf = ByteBuffer.allocate(1024);

				for (;;) {
					if (st.read(part) == -1) break;
					int idxN = indexOf(part, 10);

					if (idxN != -1) {
						buf.put(part, 0, idxN);
						buf.flip();
						String line = StandardCharsets.UTF_8.decode(buf).toString();
						buf.clear();
						if ((part.length - (idxN + 1)) > 0) buf.put(part, idxN + 1, part.length - (idxN + 1));

						channel.put(line);

					} else buf.put(part, 0, part.length);
				}
				st.close();

			} catch (IOException | InterruptedException e) { }
		}).start();

		return channel;
	}


	public BlockingQueue<String> LineChannel_ByteArrayOutputStram(String s) {
		BlockingQueue<String> channel = new LinkedBlockingQueue<>();

		new Thread( () -> {
			try {

				Path path = Paths.get(s);
				InputStream st = Files.newInputStream(path);
				byte[] part = new byte[8];
				ByteArrayOutputStream LINE = new ByteArrayOutputStream();

				for (;;) {
					if (st.read(part) == -1) break;
					int idxBckSlask = indexOf(part, 10);

					if (idxBckSlask != -1) {
						LINE.write(part, 0, idxBckSlask);

						channel.put(LINE.toString());

						LINE.reset();
						LINE.write(part, idxBckSlask + 1, (part.length - 1) - idxBckSlask);
					}
					else LINE.write(part);
				}
				st.close();

			} catch (Exception e) {}
		}).start();

		return channel;
	}


	public BlockingQueue<String> LineChannel_StringBuilder(String s) {
		BlockingQueue<String> channel = new LinkedBlockingQueue<>();

		new Thread( () -> {
			try {

				Path path = Paths.get(s);
				InputStream st = Files.newInputStream(path);

				byte[] part = new byte[8];
				StringBuilder line = new StringBuilder();

				for (;;) {
					if (st.read(part) == -1) break;
					int idxBckSlask = indexOf(part, 10);

					if (idxBckSlask != -1) {
						line.append(new String(part, 0, idxBckSlask, StandardCharsets.UTF_8));
						System.out.printf("read: %s\n", line);
						line.delete(0, line.length());
						line.append((new String(part, idxBckSlask + 1, (part.length - 1) - idxBckSlask, StandardCharsets.UTF_8)));
					}
					else line.append(new String(part));
				}
				st.close();

			} catch (Exception e) {}
		}).start();

		return channel;
	}
}
