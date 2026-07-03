package io.ddaaniel.internal.parser.reader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;

import io.ddaaniel.internal.exception.MalformedBodyException;
import io.ddaaniel.internal.exception.MalformedHeaderException;
import io.ddaaniel.internal.exception.MalformedRequestLineException;
import io.ddaaniel.internal.exception.URITooLongException;
import io.ddaaniel.internal.parser.reader.state.ParsingState;
import io.ddaaniel.internal.support.HttpBodyInputStream;
import io.ddaaniel.internal.support.collectionUtil.CollectionUtil;
import io.ddaaniel.internal.support.httpEntity.httpHeaders.HttpHeaders;

/**
 * ServletReader
 */
public class ServletReader {

	public final ReadableByteChannel stream;

	public ParsingState State = ParsingState.STATE_INIT;

	public HttpHeaders header;

	public String uriWrap;

	public String methodWrap;

	private InputStream bodyWrap;


	public ServletReader(ReadableByteChannel stream) {
		this.stream = stream;
		this.header = new HttpHeaders();
	}

	public InputStream getBody() {
		if (this.bodyWrap == null) {
			return new ByteArrayInputStream(new byte[0]);
		}
		return this.bodyWrap;
	}

	public String getBodyAsString() {
		if (this.bodyWrap == null) {
			return "";
		}

		try (var result = new java.io.ByteArrayOutputStream()) {
			byte[] buffer = new byte[512];
			int length;
			while ((length = this.bodyWrap.read(buffer)) != -1) {
				result.write(buffer, 0, length);
			}

			return result.toString(StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(" -> Error when reading the body ", e);
		}
	}

	private int ParseRequestLine(ByteBuffer bytes, ServletReader requestWrap) {
		var read = 0;
		var SEPARATOR = "\r\n";
		var START = bytes.position();
		var EOL = CollectionUtil.IndexOf(bytes, SEPARATOR, START);
		if (EOL == -1) return read;
		var lineBytes = new byte[EOL - START];
		bytes.get(lineBytes); 
		bytes.position(bytes.position() + SEPARATOR.length());

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

		requestWrap.methodWrap = parts[0];
		requestWrap.uriWrap = parts[1];
		requestWrap.State = ParsingState.STATE_HEADERS;
		return read;
	}


	private Boolean ParseHeader(ByteBuffer data) { 
		var done = false;
		var START = data.position();
		var SEPARATOR = "\r\n";

		for (;;) {
			var EOL = CollectionUtil.IndexOf(data, SEPARATOR, START);
			if (EOL == -1) {
				break;
			}
			if (EOL - START == 0) {
				data.position(EOL + SEPARATOR.length());
				done = true;
				break;
			}
			var headerline = new byte[EOL - START];
			data.get(headerline);
			data.position(data.position() + SEPARATOR.length());

			var parts = CollectionUtil.Split(headerline, ":", 2);
			if (parts.length != 2) {
				throw new MalformedHeaderException(" -> malformed field-line ");
			}
			var name = parts[0];
			var value = CollectionUtil.TrimSpace(parts[1]);
			if (CollectionUtil.HasSuffix(name, " ".getBytes())) {
				throw new MalformedHeaderException(" -> malformed field-name ");
			}

			if (!CollectionUtil.isToken(name)) {
			  throw new MalformedHeaderException(" -> malformed header-name ");
			}
			header.set(new String(name), new String(value));
			START = data.position();
		}
		if (done) {
			long length = (this.header.getContentLength() != -1) ? this.header.getContentLength() : 0;
			this.bodyWrap = new HttpBodyInputStream(this.stream, data, length);
			this.State = ParsingState.STATE_DONE;
		}
		return done;
	}

	private void Parse(ByteBuffer buf, ServletReader req) throws Exception {
		outer:
		for (;;) {
			switch (req.State) {
				case STATE_DONE : 
					break outer;
				case STATE_ERROR:
					throw new Exception("Somehow its go wrong when parsing");
				case STATE_INIT:
					if (req.ParseRequestLine(buf, req) == 0) return;
					break;
				case STATE_HEADERS:
					if (!req.ParseHeader(buf)) return;
					break;
				default: 
					throw new Exception("Somehow its go wrong");
			}
		}
		return;
	}

	public ServletReader ProcessMessage() {
		var reader = stream;
		var request = new ServletReader(stream);
		var buf = ByteBuffer.allocate(1024);
		var fliped = false;
		try {
			while (request.State != ParsingState.STATE_DONE && request.State != ParsingState.STATE_ERROR) {
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
				if (request.State == ParsingState.STATE_DONE) {
                    fliped = true;
                    break; 
                }
				if (buf.remaining() == buf.capacity()) {
					request.State = ParsingState.STATE_ERROR;
					throw new URITooLongException(" -> uri too long, error 414 -- bytes-read: " + read);
				}
				buf.compact();
				fliped = false;
			}
		} catch (Exception  exception) { 
			if (exception instanceof RuntimeException) throw (RuntimeException) exception;
			throw new RuntimeException(" -> Failure when parsing the request: ", exception);
		}

		if (!fliped) buf.flip();
		return request;
	}
}
