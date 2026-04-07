package io.ddaaniel.channel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.sun.org.apache.xpath.internal.operations.String;

/**
 * LinesChannel
 */
public class LinesChannel implements Runnable {

	private final BlockingQueue<String> queue;
	private final InputStream stream;
	private final Path path;


	public LinesChannel(BlockingQueue<String> queue, InputStream stream, Path path) 
	{ this.queue = queue; this.stream = stream; this.path = path; }

	// TODO: should be created a function that fill the inputStream and handle the IOException without modify the constructor 
	public LinesChannel(Path path) 
	{ 
		this.queue = new LinkedBlockingQueue<>(); 
		this.path = path;
		this.stream = Files.newInputStream(path); 
	}
	
	public void run() {
		try {
			Path path = Paths.get("/home/daniel/personal/dev/httpfromtcp/messages.txt");
			InputStream st = Files.newInputStream(path);

			ByteBuffer buf = ByteBuffer.allocate(1024);

			while (true) {
				if (st.available() == 0) break;
				queue.put( getLinesChannel(st, buf) );
			}
			st.close();
		} catch (Exception e) { e.printStackTrace();}
	}

	public BlockingQueue<String> doChannel() {
		BlockingQueue<String> chan = new LinkedBlockingQueue<>();

		try {
			// chan.put(getLinesChannel(st, buf));
		} catch (Exception e) { }

		return chan;
	}

	// TODO: fix the java.nio.BufferOverflowException caused by the flag 'idxN' which doesn't change
	// TODO: the method should return BlockingQueue<String> on his implementation
	// TODO: in case of persist this implementation, changes are required to the stop condition to the while loop in run() 
	public String getLinesChannel(InputStream st, ByteBuffer buf) {
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

						// System.out.printf("read: %s\n", line);
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

	public void getLineChannel(String s) {
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
}
