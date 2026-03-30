

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class Main {

		public static void main(String[] args) throws IOException {
			
			Path path = Paths.get("/home/daniel/personal/tmp/httpfromtcp/messages.txt");
			InputStream st = Files.newInputStream(path);
			// FileInputStream st = new FileInputStream(path.toFile());

			for (int i = 0;;i++) {
				byte[] bytes = new byte[8];
				byte[] separator = "\n".getBytes(StandardCharsets.UTF_8);
				st.read(bytes);

				if (bytes[i] == separator[0]);

				System.out.printf("read: %s\n", new String(bytes, StandardCharsets.UTF_8));
				if (st.available() == 0) break;
				if ((i + 1) % 8 == 0) i = 0;
			}
			st.close();
		}
}
