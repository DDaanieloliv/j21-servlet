
import java.io.FileInputStream;
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
		// FileInputStream st = new FileInputStream(path.toFile());

		byte[] part = new byte[8];
		String line = new String();
		byte[] byteChunk = new byte[7];

		var partPreview = new char[8];

		for (;;) {
			if (st.read(part) == -1) break;
			String tempStr = new String(part, StandardCharsets.UTF_8);
		 
			for ( int i = 0; i < 8; i++) {
				partPreview[i] = (char) part[i];
			}
      
			if (indexOf(part, 10) != -1) {
				byte[] byteArr = Arrays.copyOf(part, indexOf(part, 10));
				tempStr = new String(byteArr, StandardCharsets.UTF_8);
				line = line.concat(tempStr);
				System.out.printf("read: %s\n", line);
				tempStr = "";
				line = "";
			}
			else { 
				line = line.concat(tempStr);
			}

			if (st.available() == 0) break;
		}
		st.close();
	}
}
