package io.ddaaniel.listener.internal.codec.reader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Optional;

import io.ddaaniel.listener.internal.exception.MalformedBodyException;
import io.ddaaniel.listener.internal.exception.URITooLongException;


/**
 * DefaultServletReader
 */
public class DefaultServletReader implements Reader {

	private NetworkBuffer buffer;
	private ReadableByteChannel stream;
	private CommunicationProtocol decoder;

	public DefaultServletReader(ReadableByteChannel stream, CommunicationProtocol parser) {
		this.decoder = parser;
		this.stream = stream;
		this.buffer = new NetworkBuffer();
	}

	public DefaultServletReader(ReadableByteChannel stream) {
		this.stream = stream;
		this.buffer = new NetworkBuffer();
		this.decoder = new HttpCommunicationDecoder(stream);
	}

	private void isEndOrThrow(Exception e) throws Exception {
		if(!decoder.isTerminated()) {
			throw e;
		}
	}

	@Override
	public boolean canRead() {
		return !decoder.isTerminated() && !decoder.isFailed();
	}


	@Override
	public ReadableByteChannel channel() {
		return stream;
	}

	@Override
	public Optional<HttpServletRequest> readFromConnection() {
		var builder = new HttpRequestBuilder();
		try {
			while (canRead()) {
				int bytesRead = buffer.readFrom(stream);
				if (bytesRead == -1) {
					if (decoder.isInit() && !buffer.hasUnparsedData()) {
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
				decoder.decode(buf, builder);
				if (decoder.isTerminated()) {
					decoder.restart();
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
