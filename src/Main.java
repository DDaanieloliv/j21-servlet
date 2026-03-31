
import java.io.IOException;
import java.io.InputStream;
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


	public static void main(String[] args) throws IOException {

		Path path = Paths.get("/home/daniel/personal/tmp/httpfromtcp/messages.txt");
		InputStream st = Files.newInputStream(path);

		String line = new String();
		byte[] part = new byte[8];

		for (;;) {
			if (st.read(part) == -1) break;
			int posBckSlash = indexOf(part, 10);
      
			if (posBckSlash != -1) {
				byte[] bytesBehind = Arrays.copyOf(part, posBckSlash);
				byte[] bytesAhead = Arrays.copyOfRange(part, posBckSlash + 1, part.length);
				
				line = line.concat(new String(part, 0, posBckSlash, StandardCharsets.UTF_8));
				System.out.printf("read: %s\n", line);
				line = "".concat(new String(part, posBckSlash + 1, (part.length - 1) - posBckSlash, StandardCharsets.UTF_8));
			}
			else line = line.concat(new String(part));
		}
		st.close();
	}
}
