package io.ddaaniel.internal.parser.reader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import io.ddaaniel.internal.exception.MalformedBodyException;
import io.ddaaniel.internal.exception.URITooLongException;


/**
 * DefaultServletReader
 */
public class DefaultServletReader {

	private HttpProtocol http;

	private NetworkBuffer buffer;

	private ReadableByteChannel stream;


	public DefaultServletReader(ReadableByteChannel stream, HttpProtocol parser) {
		this.http = parser;
		this.stream = stream;
		this.buffer = new NetworkBuffer();
	}

	public DefaultServletReader(ReadableByteChannel stream) {
		this.http = new DefaultHttp11ProtocolParser(stream);
		this.stream = stream;
		this.buffer = new NetworkBuffer();
	}

	private boolean isAvailable() {
		return !http.isTerminated() && !http.isFailed();
	}

	private void assertEnd() {
		if(!http.isTerminated()) {
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
				http.parse(buf, builder);

				if (http.isTerminated()) {
					return builder.build();
				}

				if (buffer.isStalled()) {
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
