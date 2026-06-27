package io.ddaaniel.internal.parser.reader;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;

import io.ddaaniel.internal.exception.MalformedBodyException;
import io.ddaaniel.internal.exception.MalformedHeaderException;
import io.ddaaniel.internal.exception.MalformedRequestLineException;
import io.ddaaniel.internal.exception.URITooLongException;
import io.ddaaniel.internal.httpEntity.entities.httpHeaders.HttpHeaders;
import io.ddaaniel.internal.parser.reader.state.ParsingState;
import io.ddaaniel.internal.parser.util.Util;
import io.vavr.Tuple;
import io.vavr.Tuple2;

/**
 * ServletReader
 */
public class ServletReader extends HttpHeaders {

	public final ReadableByteChannel conn;

	public String uriWrap;
	public String methodWrap;
	public long readSoFar;

	public ParsingState State = ParsingState.STATE_INIT;

	public ServletReader(ReadableByteChannel conn) {
		this.conn = conn;
	}

	public int parseRequestLine(ByteBuffer bytes, ServletReader requestWrap) {
		var read = 0;
		var SEPARATOR = "\r\n";
		var START = bytes.position();
		var EOL = Util.IndexOf(bytes, SEPARATOR, START);
		if (EOL == -1) return read;
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

		requestWrap.methodWrap = parts[1];
		requestWrap.uriWrap = httpParts[1];
		return read;
	}

	public Tuple2<Integer, Boolean> Parse(ByteBuffer data) { 
		var read = 0; 
		var done = false;
		var START = data.position();
		var SEPARATOR = "\r\n";

		for (;;) {
			var EOL = Util.IndexOf(data, SEPARATOR, START);
			if (EOL == -1) {
				break;
			}
			if (EOL - START == 0) {
				data.get();
				data.get();
				done = true;
				read += SEPARATOR.length();
				break;
			}
			var headerline = new byte[EOL - START];
			data.get(headerline);
			data.get();
			data.get();
			var option = parseHeader(headerline);
			var name = option._1;
			var value = option._2;

			if (!Util.isToken(name.getBytes())) {
				throw new MalformedHeaderException(" -> malformed header-name ");
			}
			set(name, value);
			read += (EOL - START) + SEPARATOR.length();
			START = data.position();
		}

		return Tuple.of(read, done);
	}

	private Tuple2<String, String> parseHeader(byte[] h) {
		var parts = Util.Split(h, ":", 2);
		if (parts.length != 2) {
			throw new MalformedHeaderException(" -> malformed field-line ");
		}

		var name = parts[0];
		var value = Util.TrimSpace(parts[1]);
		if (Util.HasSuffix(name, " ".getBytes())) {
			throw new MalformedHeaderException(" -> malformed field-name ");
		}
		return Tuple.of(new String(name), new String(value));
	} 


	private Integer Parse(ByteBuffer buf, ServletReader req) throws Exception {
		var read = 0;
		outer:
		for (;;) {
			switch (req.State) {
				case STATE_DONE: 
					break outer;
				case STATE_ERROR:
					throw new Exception("Somehow its go wrong");
				case STATE_INIT:
					buf.mark();
					var parsed = req.parseRequestLine(buf, req);
					if (parsed == 0) {
						buf.reset();
						break outer;
					}
					req.State = ParsingState.STATE_HEADERS;
					read += parsed;
					break;

				case STATE_HEADERS:
					var parsedHeader = req.Parse(buf);
					var totalReadH = parsedHeader._1;
					var done = parsedHeader._2;
					if (totalReadH == 0) break outer;
					read += totalReadH;
					if (done) req.State = ParsingState.STATE_BODY;
					break;

				case STATE_BODY:
					var length = (req.getContentLength() != -1) ? req.getContentLength() : 0;
					if (length == 0) {
						req.State = ParsingState.STATE_DONE;
						break;
					}
					long alreadyRead = req.readSoFar;
					long stillMissing = length - alreadyRead;
					int available = buf.remaining();
					long remaining = Math.min(stillMissing, available);
					if (remaining > 0) {
						var chunk = new byte[(int) remaining];
						buf.get(chunk);
						req.readSoFar += remaining;
						read += remaining;
					}
					if (req.readSoFar == length) {
						req.State = ParsingState.STATE_DONE;
					}
					else break outer;
					break;
				default: 
					throw new Exception("Somehow its go wrong");
			}
		}
		return read;
	}

	public ServletReader RequestFromReader() {
		var reader = conn;
		var request = new ServletReader(conn);
		var buf = ByteBuffer.allocate(1024);
		var fliped = false;

		try {
			while (request.State == ParsingState.STATE_DONE || request.State == ParsingState.STATE_ERROR) {
				var read = reader.read(buf);
				if (read == -1) {
					if (request.State != ParsingState.STATE_DONE) {
						request.State = ParsingState.STATE_ERROR; 
						throw new MalformedBodyException(" -> body shorter than reported content-length ");
					}
					break;
				}
				buf.flip();
				fliped = true;
				Parse(buf, request);

				if (buf.remaining() == buf.capacity()) {
					request.State = ParsingState.STATE_ERROR;
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
