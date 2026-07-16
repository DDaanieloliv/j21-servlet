package io.ddaaniel.internal.parser.reader;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;

import io.ddaaniel.internal.exception.MalformedHeaderException;
import io.ddaaniel.internal.exception.MalformedRequestLineException;
import io.ddaaniel.internal.support.HttpBodyInputStream;
import io.ddaaniel.internal.support.collectionUtil.CollectionUtil;

/**
 * HttpProtocolParser
 */
public class HttpProtocolParser implements HttpProtocol {

	private Parser state;

	private ReadableByteChannel stream;

	public HttpProtocolParser(ReadableByteChannel conn) {
		this.stream = conn;
		this.state = Parser._INIT;
	}

	@Override
	public void parse(ByteBuffer buffer, HttpRequestBuilder builder) throws Exception {
		for (;;) {
			Parser currentState = this.state;
			Parser nextState = currentState.parse(this.stream, buffer, builder);
			this.state = nextState;
			if (nextState == currentState || nextState == Parser._DONE || nextState == Parser._ERROR) {
				break;
			}
		}
	}

	@Override
	public boolean isTerminated() {
		return this.state == Parser._DONE;
	}

	@Override
	public boolean isFailed() {
		return this.state == Parser._ERROR;
	}

	enum Parser {

		_INIT {
			@Override
			public Parser parse(ReadableByteChannel stream, ByteBuffer buffer, HttpRequestBuilder builder) {
				var read = 0;
				var separator = "\r\n";
				var start = getStart(buffer);
				var end = getEndOfLine(buffer, separator, start);

				if (end == -1) {
					return this;
				}

				var line = new byte[end - start];
				consumeLine(buffer, line);
				read += (buffer.position() - start);
				var startLine = new String(line, StandardCharsets.UTF_8);
				var parts = startLine.split(" ");

				if (parts.length != 3) {
					throw new MalformedRequestLineException(" -> malformed start-line -- buffer-read: " + read);
				}

				var httpVersion = parts[2].split("/");
				if (httpVersion.length != 2 || !httpVersion[0].equals("HTTP") || !httpVersion[1].equals("1.1")) {
					throw new MalformedRequestLineException(" -> malformed request-line -- buffer-read: " + read);
				}

				builder.method(parts[0]).uri(parts[1]);
				return _HEADER;
			}

			int getStart(ByteBuffer buffer) {
				return buffer.position();
			}

			int getEndOfLine(ByteBuffer buffer, String separator, int start) {
				return CollectionUtil.IndexOf(buffer, separator, start);
			}

			void consumeLine(ByteBuffer buffer, byte[] line) {
				String separator = "\r\n";
				buffer.get(line);
				buffer.position(buffer.position() + separator.length());
			}
		},

		_HEADER {
			@Override
			public Parser parse(ReadableByteChannel stream, ByteBuffer buffer, HttpRequestBuilder builder) {
				var separator = "\r\n";
				for (;;) {
					var start = getStart(buffer);
					var end = getEndOfLine(buffer, separator, start);

					if (end - start == 0) {
						buffer.position(end + separator.length());
						long length = builder.headers().getContentLength();
						length = (length != -1) ? length : 0;
						builder.body(new HttpBodyInputStream(stream, buffer, length));
						break;
					}
					if (end == -1) {
						return this;
					}

					var headerline = new byte[end - start];
					consumeLine(buffer, headerline);

					var parts = getParts(headerline);
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

			int getStart(ByteBuffer buffer) {
				return buffer.position();
			}

			int getEndOfLine(ByteBuffer buffer, String separator, int start) {
				return CollectionUtil.IndexOf(buffer, separator, start);
			}

			void consumeLine(ByteBuffer buffer, byte[] line) {
				String separator = "\r\n";
				buffer.get(line);
				buffer.position(buffer.position() + separator.length());
			}

			byte[][] getParts(byte[] headerline) {
				return CollectionUtil.Split(headerline, ":", 2);
			}
		},

		_ERROR {
			@Override
			public Parser parse(ReadableByteChannel stream, ByteBuffer buffer, HttpRequestBuilder builder)
					throws Exception {
				throw new Exception("Somehow its go wrong when parsing");
			}
		},

		_DONE {
			@Override
			public Parser parse(ReadableByteChannel stream, ByteBuffer buffer, HttpRequestBuilder builder)
					throws Exception {
				return _DONE;
			}
		};

		abstract Parser parse(ReadableByteChannel stream, ByteBuffer buffer, HttpRequestBuilder builder)
				throws Exception;
	}
}
