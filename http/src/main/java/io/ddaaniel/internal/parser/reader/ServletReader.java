package io.ddaaniel.internal.parser.reader;

import java.io.IOException;
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

	private NetworkBuffer buffer;

	private ReadableByteChannel stream;


	public ServletReader(ReadableByteChannel stream) {
		this.stream = stream;
		this.state = Parser._INIT;
		this.buffer = new NetworkBuffer();
	}

	private void parserSet(Parser state) {
		this.state = state;
	}

	private boolean isTerminated() {
		return this.state == Parser._DONE;
	}

	private boolean isAvailable() {
		return this.state != Parser._DONE && this.state != Parser._ERROR;
	}

	private void assertEnd() {
		if (!isTerminated()) {
			parserSet(Parser._ERROR); 
			throw new MalformedBodyException(" -> body shorter than reported content-length ");
		}
	}

	public HttpServletRequest processMessage() {
		var builder = new HttpRequestBuilder();

		try {
			while (isAvailable()) {
				if (buffer.readFrom(stream) == -1) {
					buffer.forceFlipForBody();
					assertEnd();
					return builder.build();
				}

				var buf = buffer.prepareForParsing();
				parse(buf, builder);
				if (isTerminated()) {
					return builder.build();
				}

				if (buffer.isStalled()) {
					parserSet(Parser._ERROR); 
					throw new URITooLongException(" -> uri too long, error 414 ");
				}
				buffer.prepareForNextRead();

			}
		} catch (Exception  exception) { 
			if (exception instanceof RuntimeException) throw (RuntimeException) exception;
			throw new RuntimeException(" -> Failure when parsing the servlet-request: ", exception);
		}

		return builder.build();
	}


	private void parse(ByteBuffer buffer, HttpRequestBuilder builder) throws Exception {
		for (;;) {
			Parser currentState = this.state;
			Parser nextState = currentState.parse(this.stream, buffer, builder);
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


	private static class NetworkBuffer {
		private final ByteBuffer buf = ByteBuffer.allocate(1024);

		public int readFrom(ReadableByteChannel channel) throws IOException {
			return channel.read(buf);
		}

		public ByteBuffer prepareForParsing() {
			buf.flip();
			return buf;
		}

		public void prepareForNextRead() {
			buf.compact();
		}

		public void forceFlipForBody() {
			buf.flip();
		}

		public boolean isStalled() {
			return buf.remaining() == buf.capacity();
		}
	}
}
