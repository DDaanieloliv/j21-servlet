package io.ddaaniel.header;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * HttpMsg
 */
public class HttpMsg implements Runnable {

	public HttpMsg() { }

	public void run() {
		try {
			Path path = Paths.get("/home/daniel/personal/tmp/httpfromtcp/messages.txt");
			InputStream st = Files.newInputStream(path);

			ByteBuffer buf = ByteBuffer.allocate(1024);
			byte[] part = new byte[8];

			while (true) {
				if (st.read(part) == -1) break;

			}
			st.close();
		} catch (Exception e) {}
	}

	public String getLinesChannel(byte[] part, ByteBuffer buf) {
			int idxN = indexOf(part, 10);

			if (idxN != -1) {
				buf.put(part, 0, idxN);
				buf.flip();
				String line = StandardCharsets.UTF_8.decode(buf).toString();
				buf.clear();
				if ((part.length - (idxN + 1)) > 0) buf.put(part, idxN + 1, part.length - (idxN + 1));

				System.out.printf("read: %s\n", line);
				return line;
			} else {
				buf.put(part, 0, part.length);
			} 
	}

	public void getLinesChannel() {
		try {
			Path path = Paths.get("/home/daniel/personal/tmp/httpfromtcp/messages.txt");
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
		} catch (Exception e) {}
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


	public static int indexOf(byte[] bytes, int character) {
		for (int i = 0; i < bytes.length; i++)
			if (bytes[i] == character)
				return i;
		return -1;
	}

	public static int indexOf(ByteBuffer buff, int character) {
		for (int i = 0; i < buff.capacity(); i++)
			if (buff.get(i) == character)
				return i;
		return -1;
	}
}
