package io.ddaaniel.listener.internal.parser.reader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Optional;

import io.ddaaniel.listener.internal.exception.MalformedBodyException;
import io.ddaaniel.listener.internal.exception.URITooLongException;


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

	private void isEndOrThrow(Exception e) throws Exception {
		if(!http.isTerminated()) {
			throw e;
		}
	}

	@Override
	public boolean canRead() {
		return !http.isTerminated() && !http.isFailed();
	}

	@Override
	public Optional<DefaultHttpServletRequest> readConnection() {
		var builder = new HttpRequestBuilder();

		try {
			while (canRead()) {
				int bytesRead = buffer.readFrom(stream);

				if (bytesRead == -1) {
					if (http.isInit() && !buffer.hasUnparsedData()) {
						return Optional.empty();
					}
					buffer.forceFlipForBody();
					isEndOrThrow(new MalformedBodyException(" -> body shorter than reported content-length "));
					return Optional.of(builder.build());
				}

				if (bytesRead == 0 && !buffer.hasUnparsedData()) {
					return Optional.empty(); 
				}

				var buf = buffer.prepareForParsing();
				http.parse(buf, builder);

				if (http.isTerminated()) {
					http.restart();
					return Optional.of(builder.build());
				}

				if (buffer.isStalled()) {
					buffer.resetForNextRequest();
					throw new URITooLongException(" -> uri too long, error 414 "); 
				}
			}
		} catch (Exception  exception) { 
             if (exception instanceof RuntimeException) throw (RuntimeException) exception;
			 throw new RuntimeException(" -> Failure when parsing the servlet-request: ", exception); 
		}

		return Optional.of(builder.build());
	}


	private static class NetworkBuffer {
		private final ByteBuffer buf = ByteBuffer.allocate(1024);
		private boolean parsingMode = false;

		public int readFrom(ReadableByteChannel channel) throws IOException {
			if (parsingMode) {
				buf.compact();
				parsingMode = false;
            }
			return channel.read(buf);
		}

		public ByteBuffer prepareForParsing() {
			if (!parsingMode) {
				buf.flip();
				parsingMode = true;
			}
			return buf;
		}

		public void forceFlipForBody() {
			if (!parsingMode) {
				buf.flip();
				parsingMode = true;
			}
		}

		public boolean isStalled() {
			return parsingMode && buf.remaining() == buf.capacity();
		}

		public void resetForNextRequest() {
			buf.compact();
			parsingMode = false;
		}

		public boolean hasUnparsedData() {
			return parsingMode ? buf.hasRemaining() : buf.position() > 0;
		}
	}
}
