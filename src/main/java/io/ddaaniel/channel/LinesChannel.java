package io.ddaaniel.channel;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

	private final BlockingQueue<String> chan;
	private final InputStream stream;

	public LinesChannel(
			BlockingQueue<String> chan,
			InputStream stream,
			SeekableByteChannel sChannel) 
	{ 
		this.chan = chan; 
		this.stream = stream; 
	}

	public LinesChannel(Path path) 
	{ 
		this.chan = new LinkedBlockingQueue<>(); 
		this.stream = setInputStream(path); 
	}

	public LinesChannel() 
	{ 
		this.chan = new LinkedBlockingQueue<>(); 
		this.stream = setInputStream(Paths.get("/home/daniel/personal/dev/httpfromtcp/messages.txt")); 
	}

	public static SeekableByteChannel setByteChannel(Path path) {
		try {
			return Files.newByteChannel(path);
		} catch (IOException e) {	
			throw new RuntimeException("There is not possible open the file", e);
		}
	}



	public static InputStream setInputStream(Path path) {
		try {
			return new BufferedInputStream(Files.newInputStream(path));
		} catch (IOException e) {	
			throw new RuntimeException("There is not possible open the file", e);
		}
	}

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
		BlockingQueue<String> chann = new LinkedBlockingQueue<>();

		try {
			SeekableByteChannel sChannel = Files.newByteChannel(Paths.get(s));
			
			new Thread( () -> {
				try {
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

		} catch (Exception e) { e.printStackTrace(); }

		return chann;
	}





	public BlockingQueue<String> doChannel() {

		new Thread( () -> {
			try {
				while (true) {
					if (stream.markSupported()) 
					{
						stream.mark(1);
						if (stream.read() == -1) break;
						stream.reset();
					}
					chan.put( getLinesChannel(stream, buf) );
				}
				stream.close();
			} catch (InterruptedException | IOException e) { }
		}).start();

		return chan;
	}

	private String getLinesChannel(InputStream st, ByteBuffer buf) {
			byte[] part = new byte[8];

			try {
				for (;;) {
					if (st.read(part) == -1) break;
					int idxN = indexOf(part, 10); 

					if (idxN != -1) {
						buf.put(part, 0, idxN);
						buf.flip();
						String line = StandardCharsets.UTF_8.decode(buf).toString();
						buf.clear();
						if ((part.length - (idxN + 1)) > 0) buf.put(part, idxN + 1, part.length - (idxN + 1));

						return line;

					} else buf.put(part, 0, part.length); 
				}
			} catch (Exception e) { e.printStackTrace(); }

			return "should never be reached";
	}

	public void getLinesChannel() {
		try {
			Path path = Paths.get("/home/daniel/personal/dev/httpfromtcp/messages.txt");
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

					System.out.printf("read: %s\n", line);
				} else buf.put(part, 0, part.length);
			}
			st.close();
		} catch (IOException e) { }
	}

	public void getLinesChannel(String s) {
		try {
			Path path = Paths.get("/home/daniel/personal/tmp/httpfromtcp/messages.txt");
			InputStream st = Files.newInputStream(path);

			byte[] part = new byte[8];
			ByteArrayOutputStream LINE = new ByteArrayOutputStream();

			for (;;) {
				if (st.read(part) == -1) break;
				int idxBckSlask = indexOf(part, 10);

				if (idxBckSlask != -1) {
					LINE.write(part, 0, idxBckSlask);
					System.out.printf("read: %s\n", LINE);
					LINE.reset();
					LINE.write(part, idxBckSlask + 1, (part.length - 1) - idxBckSlask);
				}
				else LINE.write(part);
			}
			st.close();
		} catch (Exception e) {}
	}


	public void getLineChannel(Path p) {
		try {
			Path path = Paths.get("/home/daniel/personal/tmp/httpfromtcp/messages.txt");
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
	}
}
