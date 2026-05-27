package io.ddaaniel.internal.parser.response;

import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import io.ddaaniel.internal.parser.header.Headers;
import io.ddaaniel.internal.parser.header.message.Header;
import io.ddaaniel.internal.parser.response.message.Response;
import io.ddaaniel.internal.parser.response.message.enumns.StatusCode;

/**
 * Responses
 */
public class Responses {
	
	public Response r = new Response();

	public int WriteHeaders(WritableByteChannel w, Header h) {
		try {
			var string = new StringBuilder();
			h.map().forEach( (key, value) -> { 
				string.append(String.format("%s: %s\r\n", key, value)); 
			} );
			string.append("\r\n");
			var buf = ByteBuffer.wrap(string.toString().getBytes());
			var written = 0;
			while (buf.hasRemaining()) {
				written += w.write(buf);
			}
			return written;
		} catch (Exception e) { throw new RuntimeException(" -> Failure to write headers ", e); }
	}

	public Headers GetDefaultHeaders(int contetLen) {
		var h = new Headers();
		h.Replace("Content-Length", String.valueOf(contetLen));
		h.Set("Connection", "close");
		h.Set("Content-Type", "text/plain");

		return h;
	}

	public int WriteStatusLine(WritableByteChannel w, StatusCode statuscode) {
		try {

			var statusStr = "";
			switch (statuscode) {
				case STATUS_OK:
					statusStr = "HTTP/1.1 200 OK\r\n";
					break;
				case STATUS_BAD_REQUEST:
					statusStr = "HTTP/1.1 400 Bad Request\r\n";
					break;
				case STATUS_INTERNAL_SERVER_ERROR:
					statusStr = "HTTP/1.1 500 Internal Server Error\r\n";
					break;
				default: throw new IllegalArgumentException("Unrecognized error code: " + statuscode);
			}

			var statusline = ByteBuffer.wrap(statusStr.getBytes());
			var written = 0; 
			while (statusline.hasRemaining()) {
				written += w.write(statusline);
			}
			return written;

		} catch (Exception e) { throw new RuntimeException(" -> Failed to write status line ", e); }
	}
}
