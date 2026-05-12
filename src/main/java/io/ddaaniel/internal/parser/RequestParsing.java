package io.ddaaniel.internal.parser;


import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;

import io.ddaaniel.internal.exception.MalformedRequestLineException;
import io.ddaaniel.internal.exception.URITooLongException;
import io.ddaaniel.internal.parser.message.Request;

import io.ddaaniel.internal.parser.message.RequestLine;
import io.ddaaniel.internal.parser.message.state.ParseState;
import io.vavr.Tuple;
import io.vavr.Tuple2;

/**
 * RequestParsing
 */
public class RequestParsing {

	private final Request request_onboard = new Request();


	private int IndexOf(ByteBuffer buff) {
		for (int i = buff.position(); i < buff.limit() - 1; i++) {
			if (buff.get(i) == '\r' && buff.get(i + 1) == '\n') {
				return i;
			}
		}
		return -1;
	}

	private boolean done() {
		return request_onboard.state == ParseState.STATE_DONE || 
			request_onboard.state == ParseState.STATE_ERROR;
	}

	private Request NewRequest(Request request_onboard) {
		request_onboard.state = ParseState.STATE_INIT;
		return request_onboard;
	}

	private Tuple2<RequestLine, Integer> parseRequestLine(ByteBuffer bytes) {
		var SEPARATOR = "\r\n".getBytes();
		var START = bytes.position();
		var EOL = IndexOf(bytes);
		if (EOL == -1) {
			return Tuple.of(null, 0);
		}

		var lineBytes = new byte[EOL - START];
		bytes.get(lineBytes); 
		bytes.get();
		bytes.get();

		int read = (EOL + SEPARATOR.length) - START;
		String startLine = new String(lineBytes, StandardCharsets.UTF_8);
		var parts = startLine.split(" ");
		if (parts.length != 3) { 
			throw new MalformedRequestLineException(
					" -- malformed start-line -- bytes-read: " + read
					);
		}

		var httpParts = parts[2].split("/");
		if (httpParts.length != 2 || !httpParts[0].equals("HTTP") || !httpParts[1].equals("1.1")) { 
			throw new MalformedRequestLineException(
					" -- malformed request-line -- bytes-read: " + read
					);
		}

		var requestLine = new RequestLine(parts[0], parts[1], httpParts[1]);

		return Tuple.of(requestLine, read);
	}


	private Integer parse(ByteBuffer buf) {
		var pos = buf.position();
		outer:
		for (;;) {
			switch (request_onboard.state) {
				case STATE_INIT:
					buf.mark();
					var option = parseRequestLine(buf);
					var requestLine = option._1;
					var read = option._2;
					if (read == 0) {
						buf.reset();
						break outer;
					}
					request_onboard.requestLine = requestLine;
					request_onboard.state = ParseState.STATE_DONE;
					break;

				case STATE_ERROR:
					return 0;
				case STATE_DONE: 
					break outer;
			}
		}
		var read = buf.position() - pos;
		return read;
	}

	
	public Request RequestFromReader(ReadableByteChannel reader) {
		var request = NewRequest(request_onboard);
		var buf = ByteBuffer.allocate(1024);

		while (!done()) {
			try {
				reader.read(buf);
				buf.flip();
				var read = parse(buf);
				if (read == 0 && buf.limit() == buf.limit()) {
					throw new URITooLongException(" -- uri too long, error 414 -- bytes-read: " + read);
				}
				buf.compact();
			} catch (Exception e) { e.printStackTrace(); }
		}

		return request;
	}


}
