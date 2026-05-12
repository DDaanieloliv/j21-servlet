package io.ddaaniel.internal.request;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;

import io.ddaaniel.Error.Error;
import io.ddaaniel.internal.parser.message.Request;

import io.ddaaniel.internal.parser.message.RequestLine;
import io.ddaaniel.internal.parser.message.state.ParseState;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;

/**
 * RequestParser
 */
public class RequestParser {

	private final static String INCOMPLETE_START_LINE = " --> incomplete start-line ";
	private final static String ERROR_MALFORMED_REQUEST_LINE = " --> malformed request-line ";
	private final static String END_OF_CONNECTION = " --> connection closed ";

	public final Request r;

	public RequestParser(Request request){ this.r = request; }


	public boolean done() {
		return r.state == ParseState.STATE_DONE || r.state == ParseState.STATE_ERROR;
	}

	public Request NewRequest(Request r) {
		r.state = ParseState.STATE_INIT;
		return r;
	}

	public Tuple3<RequestLine, Integer, Error> parseRequestLine(Buffer bytes) {
		var string = StandardCharsets.UTF_8.decode((ByteBuffer) bytes).toString();
		var SEPARATOR = "\r\n";
		var idx = string.indexOf(SEPARATOR);
		if (idx == -1) { 
			return Tuple.of(null, 0, new Error(INCOMPLETE_START_LINE));
		}

		var startLine = string.substring(0, idx);
		var read = idx + SEPARATOR.length();

		var parts = startLine.split(" ");
		if (parts.length != 3) { 
			return Tuple.of(null, read, new Error(ERROR_MALFORMED_REQUEST_LINE));
		}

		var httpParts = parts[2].split("/");
		if (httpParts.length != 2 || httpParts[0] != "HTTP" || httpParts[1] != "1.1") { 
			return Tuple.of(null, read, new Error(ERROR_MALFORMED_REQUEST_LINE));
		}

		var requestLine = new RequestLine(parts[0], parts[1], httpParts[1]);
		
		return Tuple.of(requestLine, read, null);
	}



	public Tuple2<Request, String> RequestFromReader(ReadableByteChannel reader) throws IOException {
		var request = NewRequest(r);
		var buf = ByteBuffer.allocate(1024);

		while (done()) {
			int bytesRead = reader.read(buf);
			if (bytesRead == -1) {
				throw new Error(END_OF_CONNECTION);
			}

			buf.flip();
			parse(buf);
			buf.compact();
		}

		return Tuple.of(request, null);
	}


	public Tuple2<Integer, Error> parse(Buffer data) {
		var read = 0;
		var error = new Error("");

		outer:
		for (;;) {
			switch (r.state) {
				case STATE_ERROR:
					return Tuple.of(0, error);
			
				case STATE_INIT:
					var table = parseRequestLine(data);
					var requestLine = table._1;
					var n = table._2;
					var err = table._3;

					if (err != null) {
						r.state = ParseState.STATE_ERROR;
						error = err;
					}

					if (n == 0) {
						break outer;
					}

					r.requestLine = requestLine;
					read += n;
					r.state = ParseState.STATE_DONE;


				case STATE_DONE: 
					break outer;
			}
		}

		return Tuple.of(read, null);
	}
	
}
