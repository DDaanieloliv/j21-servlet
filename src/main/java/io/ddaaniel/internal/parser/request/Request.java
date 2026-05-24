package io.ddaaniel.internal.parser.request;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;

import io.ddaaniel.internal.exception.MalformedRequestLineException;
import io.ddaaniel.internal.exception.URITooLongException;
import io.ddaaniel.internal.parser.header.Header;
import io.ddaaniel.internal.parser.request.message.Requests;
import io.ddaaniel.internal.parser.request.message.RequestLine;
import io.ddaaniel.internal.parser.request.message.enums.ParseState;
import io.ddaaniel.internal.parser.util.Util;
import io.vavr.Tuple;
import io.vavr.Tuple2;

/**
 * Request
 */
public class Request {

	private final Requests request_onboard = new Requests();

	private boolean done() {
		return request_onboard.State == ParseState.STATE_DONE || 
			request_onboard.State == ParseState.STATE_ERROR;
	}

	private Requests NewRequest(Requests request_onboard) {
		request_onboard.State = ParseState.STATE_INIT;
		request_onboard.Headers = new Header();
		return request_onboard;
	}

	private Tuple2<RequestLine, Integer> parseRequestLine(ByteBuffer bytes) {
		var read = 0;
		var SEPARATOR = "\r\n";
		var START = bytes.position();
		var EOL = Util.IndexOf(bytes, SEPARATOR, START);
		if (EOL == -1) {
			return Tuple.of(null, read);
		}

		var lineBytes = new byte[EOL - START];
		bytes.get(lineBytes); 
		bytes.get();
		bytes.get();

		read += (bytes.position() - START); // (EOL + SEPARATOR.length()) - START;
		var startLine = new String(lineBytes, StandardCharsets.UTF_8);
		var parts = startLine.split(" ");
		if (parts.length != 3) { 
			throw new MalformedRequestLineException(
					" -> malformed start-line -- bytes-read: " + read
					);
		}

		var httpParts = parts[2].split("/");
		if (httpParts.length != 2 || !httpParts[0].equals("HTTP") || !httpParts[1].equals("1.1")) { 
			throw new MalformedRequestLineException(
					" -> malformed request-line -- bytes-read: " + read
					);
		}

		var requestLine = new RequestLine(parts[0], parts[1], httpParts[1]);
		return Tuple.of(requestLine, read);
	}


	private Integer parse(ByteBuffer buf) throws Exception {
		var read = 0;
		outer:
		for (;;) {
			switch (request_onboard.State) {
				case STATE_INIT:
					buf.mark();
					var parsedRequestLine = parseRequestLine(buf);
					var requestLine = parsedRequestLine._1;
					var totalReadR = parsedRequestLine._2;
					if (totalReadR == 0) {
						buf.reset();
						break outer;
					}
					request_onboard.RequestLine = requestLine;
					read += totalReadR;
					request_onboard.State = ParseState.STATE_HEADERS;
					break;
				case STATE_ERROR:
					throw new Exception("Somehow its go wrong");
				case STATE_HEADERS:
					var parsedHeader = request_onboard.Headers.Parse(buf);
					var totalReadH = parsedHeader._1;
					var done = parsedHeader._2; 
					if (totalReadH == 0) break outer;
					read += totalReadH;
					if (done) request_onboard.State = ParseState.STATE_DONE;
					break;
				case STATE_DONE: 
					break outer;
				default: 
					throw new Exception("Somehow its go wrong");
			}
		}
		return read;
	}

	
	public Requests RequestFromReader(ReadableByteChannel reader) {
		var request = NewRequest(request_onboard);
		var buf = ByteBuffer.allocate(1024);
		var fliped = false;

		while (!done()) {
			try {
				var read = reader.read(buf);
				if (read == -1) {
					if (!done()) request.State = ParseState.STATE_ERROR; 
					break;
				}

				buf.flip();
				fliped = true;
				parse(buf);

				if (buf.remaining() == buf.capacity()) {
					request.State = ParseState.STATE_ERROR;
					throw new URITooLongException(" -> uri too long, error 414 -- bytes-read: " + read);
				}
				buf.compact();
				fliped = false;
			} catch (Exception  exception) { 
				if (exception instanceof RuntimeException) throw (RuntimeException) exception;
				throw new RuntimeException(" -> Failure to parse the request: ", exception);
			}
		}
		if (!fliped) buf.flip();

		return request;
	}


}
