package io.ddaaniel.internal.parser.reader;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;

import io.ddaaniel.internal.exception.MalformedBodyException;
import io.ddaaniel.internal.exception.MalformedHeaderException;
import io.ddaaniel.internal.exception.MalformedRequestLineException;
import io.ddaaniel.internal.exception.URITooLongException;
import io.ddaaniel.internal.support.HttpBodyInputStream;
import io.ddaaniel.internal.support.collectionUtil.CollectionUtil;


/**
 * ServletReader
 */
public class ServletReader {

	private Parser state;

	private ReadableByteChannel stream;

	public ServletReader(ReadableByteChannel stream) {
		this.stream = stream;
		this.state = Parser._INIT;
	}


	public HttpServletRequest ProcessMessage() {
		var reader = stream;
		var buf = ByteBuffer.allocate(1024);
		var builder = new HttpRequestBuilder();
		var fliped = false;
		try {
			while (this.state != Parser._DONE && this.state != Parser._ERROR) {
				var read = reader.read(buf);
				if (read == -1) {
					if (this.state != Parser._DONE) {
						this.state = Parser._ERROR; 
						throw new MalformedBodyException(" -> body shorter than reported content-length ");
					}
					break;
				}
				buf.flip();
				fliped = true;
				parse(buf, builder);
				if (this.state == Parser._DONE) {
                    fliped = true;
                    break; 
                }
				if (buf.remaining() == buf.capacity()) {
					this.state = Parser._ERROR;
					throw new URITooLongException(" -> uri too long, error 414 -- bytes-read: " + read);
				}
				buf.compact();
				fliped = false;
			}
		} catch (Exception  exception) { 
			if (exception instanceof RuntimeException) throw (RuntimeException) exception;
			throw new RuntimeException(" -> Failure when parsing the servlet-request: ", exception);
		}

		if (!fliped) buf.flip();
		return builder.build();
	}


	private void parse(ByteBuffer buf, HttpRequestBuilder builder) throws Exception {
		for (;;) {
			Parser currentState = this.state;
			Parser nextState = currentState.parse(this.stream, buf, builder);
			this.state = nextState;
			if (nextState == currentState || nextState == Parser._DONE || nextState == Parser._ERROR) {
				break;
			}
		}
	}

	enum Parser {
		_INIT {
			@Override
			public Parser parse(ReadableByteChannel stream, ByteBuffer buffer, HttpRequestBuilder builder) {
				var read = 0;
				var SEPARATOR = "\r\n";
				var START = buffer.position();
				var EOL = CollectionUtil.IndexOf(buffer, SEPARATOR, START);
				if (EOL == -1) return this;
				var lineBytes = new byte[EOL - START];
				buffer.get(lineBytes); 
				buffer.position(buffer.position() + SEPARATOR.length());
				read += (buffer.position() - START);
				var startLine = new String(lineBytes, StandardCharsets.UTF_8);
				var parts = startLine.split(" ");
				if (parts.length != 3) { 
					throw new MalformedRequestLineException( " -> malformed start-line -- buffer-read: " + read);
				}
				var httpParts = parts[2].split("/");
				if (httpParts.length != 2 || !httpParts[0].equals("HTTP") || !httpParts[1].equals("1.1")) { 
					throw new MalformedRequestLineException( " -> malformed request-line -- buffer-read: " + read);
				}

				builder.method(parts[0]).uri(parts[1]);
				return _HEADER;
			}
		},

		_HEADER {
			@Override
			public Parser parse(ReadableByteChannel stream, ByteBuffer buffer, HttpRequestBuilder builder) {
				var SEPARATOR = "\r\n";
				for (;;) {
					var START = buffer.position();
					var EOL = CollectionUtil.IndexOf(buffer, SEPARATOR, START);
					if (EOL - START == 0) {
						buffer.position(EOL + SEPARATOR.length());
						long length = builder.headers().getContentLength();
						length = (length != -1) ? length : 0;
						builder.body(new HttpBodyInputStream(stream, buffer, length));
						break;
					}
					if (EOL == -1) return this;
					var headerline = new byte[EOL - START];
					buffer.get(headerline);
					buffer.position(buffer.position() + SEPARATOR.length());
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
					builder.headers().set(new String(name), new String(value));
				}
				return _DONE;
			}
		},

		_ERROR {
			@Override
			public Parser parse(ReadableByteChannel stream, ByteBuffer buffer, HttpRequestBuilder builder) throws Exception {
				throw new Exception("Somehow its go wrong when parsing");
			}
		},

		_DONE {
			@Override
			public Parser parse(ReadableByteChannel stream, ByteBuffer buffer, HttpRequestBuilder builder) throws Exception {
				return _DONE;
			}
		};

		abstract Parser parse(ReadableByteChannel stream, ByteBuffer buffer, HttpRequestBuilder builder) throws Exception;
	}
}
