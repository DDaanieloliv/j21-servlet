package io.ddaaniel.internal.parser.request;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;

import io.ddaaniel.internal.exception.MalformedBodyException;
import io.ddaaniel.internal.exception.MalformedRequestLineException;
import io.ddaaniel.internal.exception.URITooLongException;
import io.ddaaniel.internal.parser.header.Headers;
import io.ddaaniel.internal.parser.request.message.Request;
import io.ddaaniel.internal.parser.request.message.RequestLine;
import io.ddaaniel.internal.parser.request.message.enums.ParseState;
import io.ddaaniel.internal.parser.util.Util;
import io.vavr.Tuple;
	import io.vavr.Tuple2;

/**
 * Requests
 */
public class Requests {

	public final Request r = new Request();

	private boolean done() {
		return r.State == ParseState.STATE_DONE || r.State == ParseState.STATE_ERROR;
	}

	public int getLength(Headers header, String name, int defaultValue) {
		var valueStr = header.Get(name);
		if (valueStr == null) return defaultValue;
		var value = Integer.parseInt(valueStr);
		return value;
	}

	private Request NewRequest(Request r) {
		r.State = ParseState.STATE_INIT;
		r.Headers = new Headers();
		r.Body = "";
		return r;
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

		read += (bytes.position() - START);
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
			switch (r.State) {
				case STATE_DONE: 
					break outer;
				case STATE_ERROR:
					throw new Exception("Somehow its go wrong");
				case STATE_INIT:
					buf.mark();
					var parsedRequestLine = parseRequestLine(buf);
					var requestLine = parsedRequestLine._1;
					var totalReadR = parsedRequestLine._2;
					if (totalReadR == 0) {
						buf.reset();
						break outer;
					}
					r.RequestLine = requestLine;
					read += totalReadR;
					r.State = ParseState.STATE_HEADERS;
					break;

				case STATE_HEADERS:
					var parsedHeader = r.Headers.Parse(buf);
					var totalReadH = parsedHeader._1;
					var done = parsedHeader._2; 
					if (totalReadH == 0) break outer;
					read += totalReadH;
					if (done) r.State = ParseState.STATE_BODY;
					break;

				case STATE_BODY:
					var length = getLength(r.Headers, "content-length" , 0);
					if (length == 0) {
						r.State = ParseState.STATE_DONE;
						break;
					}
					var stillMissing = length - r.Body.getBytes().length;
					var available = buf.remaining();
					var remaining = Math.min(stillMissing, available);
					if (remaining > 0) {
						var chunk = new byte[remaining];
						buf.get(chunk);
						r.Body += new String(chunk);
						read += remaining;
					}
					if (length == r.Body.length()) {
						r.State = ParseState.STATE_DONE;
					} else break outer;
					break;
				default: 
					throw new Exception("Somehow its go wrong");
			}
		}
		return read;
	}

	
	public Request RequestFromReader(ReadableByteChannel reader) {
		var request = NewRequest(r);
		var buf = ByteBuffer.allocate(1024);
		var fliped = false;

		try {
			while (!done()) {
				var read = reader.read(buf);
				if (read == -1) {
					if (request.State != ParseState.STATE_DONE) {
						request.State = ParseState.STATE_ERROR; 
						throw new MalformedBodyException(" -> body shorter than reported content-length ");
					}
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
			}
		} catch (Exception  exception) { 
			if (exception instanceof RuntimeException) throw (RuntimeException) exception;
			throw new RuntimeException(" -> Failure to parse the request: ", exception);
		}

		if (!fliped) buf.flip();
		return request;
	}


}
