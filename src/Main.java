
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class Main {

	public static int indexOf(byte[] bytes, int character) {
		for (int i = 0; i < bytes.length; i++)
			if (bytes[i] == character) return i;
		return -1;
	}

	public static int indexOf(ByteBuffer buff, int character) {
		for (int i = 0; i < buff.capacity(); i++)
			if (buff.get(i) == character) return i;
		return -1;
	}


	public static void main(String[] args) throws IOException {

		Path path = Paths.get("/home/daniel/personal/tmp/httpfromtcp/messages.txt");
		InputStream st = Files.newInputStream(path);

		byte[] part = new byte[8];
		ByteBuffer buf = ByteBuffer.allocate(1024);
		ByteArrayOutputStream LINE = new ByteArrayOutputStream();
		// StringBuilder line = new StringBuilder();

		for (;;) {
			if (st.read(part) == -1) break;
			int idxN = indexOf(part, 10);
	
			if (idxN != -1) {
				buf.put(part, 0, idxN);
				buf.flip();
				String line = StandardCharsets.UTF_8.decode(buf).toString();
				System.out.printf("read: %s\n", line);
				buf.clear();

				if ((part.length - (idxN + 1)) > 0) buf.put(part, idxN + 1, part.length - (idxN + 1));
			}
			else buf.put(part, 0, part.length);
		}
      

		// for (;;) {
		// 	if (st.read(part) == -1) break;
		// 	int idxBckSlask = indexOf(part, 10);
		//
		// 	if (idxBckSlask != -1) {
		// 		LINE.write(part, 0, idxBckSlask);
		// 		System.out.printf("read: %s\n", LINE);
		// 		LINE.reset();
		// 		LINE.write(part, idxBckSlask + 1, (part.length - 1) - idxBckSlask);
		//
		// 	// 	line.append(new String(part, 0, idxBckSlask, StandardCharsets.UTF_8));
		// 	// 	System.out.printf("read: %s\n", line);
		// 	// 	line.delete(0, line.length());
		// 	// 	line.append((new String(part, idxBckSlask + 1, (part.length - 1) - idxBckSlask, StandardCharsets.UTF_8)));
		// 	// }
		// 	// else line.append(new String(part));
		// 	}
		// 	else LINE.write(part);
		// }
		st.close();
	}
}
