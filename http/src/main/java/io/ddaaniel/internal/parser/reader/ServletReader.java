package io.ddaaniel.internal.parser.reader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import io.ddaaniel.internal.exception.MalformedBodyException;
import io.ddaaniel.internal.exception.URITooLongException;


/**
 * ServletReader
 */
public class ServletReader implements HttpReader {

	private HttpProtocol http;

	private NetworkBuffer buffer;

	private ReadableByteChannel stream;


	public ServletReader(ReadableByteChannel stream, HttpProtocol parser) {
		this.http = parser;
		this.stream = stream;
		this.buffer = new NetworkBuffer();
	}

	public ServletReader(ReadableByteChannel stream) {
		this.stream = stream;
		this.buffer = new NetworkBuffer();
		this.http = new HttpProtocolParser(stream);
	}

	private void isEndOrThrow(Throwable e) throws Throwable {
		if(!http.isTerminated()) {
			throw e;
		}
	}

	@Override
	public boolean canRead() {
		return !http.isTerminated() && !http.isFailed();
	}

	@Override
	public DefaultHttpServletRequest processMessage() {
		var builder = new HttpRequestBuilder();

		try {
			while (canRead()) {
				int bytesRead = buffer.readFrom(stream);

				if (bytesRead == -1) {
					if (http.isInit() && !buffer.hasUnparsedData()) {
						return null;
					}
					buffer.forceFlipForBody();
					isEndOrThrow(new MalformedBodyException(" -> body shorter than reported content-length "));
					return builder.build();
				}

				if (bytesRead == 0 && !buffer.hasUnparsedData()) {
					return null; 
				}

				var buf = buffer.prepareForParsing();
				http.parse(buf, builder);

				if (http.isTerminated()) {
					http.reset();
					return builder.build();
				}

				if (buffer.isStalled()) {
					buffer.resetForNextRequest();
					throw new URITooLongException(" -> uri too long, error 414 "); 
				}
				buffer.prepareForNextRead();
			}
		} catch (Throwable  exception) { 
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

		public void resetForNextRequest() {
			buf.compact();
		}

		public boolean hasUnparsedData() {
			return buf.position() > 0;
		}
	}
}
