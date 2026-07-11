package io.ddaaniel.internal.parser.reader;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;

import io.ddaaniel.internal.exception.MalformedHeaderException;
import io.ddaaniel.internal.exception.MalformedRequestLineException;
import io.ddaaniel.internal.support.HttpBodyInputStream;
import io.ddaaniel.internal.support.collectionUtil.CollectionUtil;

/**
 * DefaultHttp11ProtocolParser
 */
public class DefaultHttp11ProtocolParser implements HttpProtocol {

	private Parser state;

	private ReadableByteChannel stream;

	public DefaultHttp11ProtocolParser(ReadableByteChannel conn) {
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
