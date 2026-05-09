package io.ddaaniel.tcpListener;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;



/**
 * RequestLine
 */
record RequestLine (
	String Method,
	String RequestTarget,
	String HttpVersion
){ }

/**
 * Request
 */
record Request( RequestLine requestLine ) { }


/**
 * ChannelContext
 */
public abstract class ChannelContext {

	private final static String INCOMPLETE_START_LINE = " --> incomplete start-line ";
	private final static String ERROR_MALFORMED_REQUEST_LINE = " --> malformed request-line ";

	private static int indexOf(ByteBuffer buff, int character) {
		for (int i = 0; i < buff.limit(); i++)
			if (buff.get(i) == character)
				return i;
		return -1;
	}

	// TODO: avoid the usage of "GodObject" - Context
	// TODO: discard the finally implementations by using the content-lenght
	// TODO: getLinesChannel(Context context) -> Class parser | parseRequest(conn)
	public static void getLinesChannel(Context context) {
		var buf = context.getBuff(); 
		var part = context.getPart();
		var channel = context.getChannel();
		var conn = context.getConnection();

		try {

			int readed = conn.read(part);
			if (readed == -1) return;
			part.flip();
			int idxN = indexOf(part, 10);

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

		return;
	}



	public static Tuple3<RequestLine, String, String> parseRequestLine(String string) {

		var SEPARATOR = "\r\n";
		var idx = string.indexOf(SEPARATOR);
		if (idx == -1) { 
			return Tuple.of(null, string, INCOMPLETE_START_LINE);
		}

		var startLine = string.substring(0, idx);
		var restOfMsg = string.substring(idx + SEPARATOR.length());

		var parts = startLine.split(" ");
		if (parts.length != 3) { 
			return Tuple.of(null, restOfMsg, ERROR_MALFORMED_REQUEST_LINE);
		}

		var httpParts = parts[2].split("/");
		if (httpParts.length != 2 || httpParts[0] != "HTTP" || httpParts[1] != "1.1") { 
			return Tuple.of(null, restOfMsg, ERROR_MALFORMED_REQUEST_LINE);
		}

		var requestLine = new RequestLine(parts[0], parts[1], httpParts[1]);
		
		return Tuple.of(requestLine, restOfMsg, null);
	}




	public static Tuple2<Request, String> RequestFromReader(ReadableByteChannel reader) throws IOException {
		
			var buf = ByteBuffer.allocate(4096);
			reader.read(buf);
			var data = StandardCharsets.UTF_8.decode(buf).toString();

			var parsedRequest = parseRequestLine(data);
			if (parsedRequest._1 == null) { 
				return Tuple.of(null, parsedRequest._3);
			}

			var request = new Request(parsedRequest._1);

			return Tuple.of(request, null);
	}
}
