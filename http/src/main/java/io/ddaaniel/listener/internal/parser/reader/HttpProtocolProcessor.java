package io.ddaaniel.listener.internal.parser.reader;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import io.ddaaniel.listener.internal.exception.InvalidLineSeparatorException;
import io.ddaaniel.listener.internal.exception.MalformedRequestLineException;
import io.ddaaniel.listener.internal.support.collectionUtil.CollectionUtil;

/**
 * HttpProtocolProcessor
 */
public class HttpProtocolProcessor {

	private Processor processor;

	private static final String CRLF = "\r\n";

	private Runnable defaultStrictCRLFCheck;

	private static final Runnable THROW_INVALID_LINE_SEPARATOR = new Runnable() {
        @Override
        public void run() {
            throw new InvalidLineSeparatorException();
        }
    };

	private State currentState = State.SKIP_INITIAL_LINE_CHARS;

	private enum State {
		SKIP_INITIAL_LINE_CHARS,
		SKIP_CONTROL_CHARS,
		READ_INITIAL,
		READ_HEADER,
		READ_VARIABLE_LENGTH_CONTENT,
		READ_FIXED_LENGTH_CONTENT,
		READ_CHUNK_SIZE,
		READ_CHUNKED_CONTENT,
		READ_CHUNK_DELIMITER,
		READ_CHUNK_FOOTER,
		BAD_MESSAGE,
		UPGRADED
	}	

	public void decode(ByteBuffer buffer, HttpRequestBuilder builder) {
		switch (currentState) {
			case SKIP_INITIAL_LINE_CHARS:

			case READ_INITIAL: 
				ByteBuffer line = processor.parseline(buffer);		
				break;
			default:
				break;
		}
	}

	private final class Processor {

		public ByteBuffer parseline(ByteBuffer buffer) {
			final int start = buffer.position();
			final int end = CollectionUtil.IndexOf(buffer, CRLF, start);

			if (end == -1) {
				return null;
			}

			byte[] line = new byte[end - start];
			buffer.get(line);
			buffer.position(buffer.position() + CRLF.length());
			String startLine = new String(line, StandardCharsets.UTF_8);
			String[] parts = startLine.split(" ");

			String[] httpVersion = parts[2].split("/");
			if (parts.length != 3 			  || 
				httpVersion.length != 2 	  || 
				!httpVersion[0].equals("HTTP")|| 
				!httpVersion[1].equals("1.1")) {
				throw new MalformedRequestLineException();
			}

			out.method(parts[0]).uri(parts[1]);
			return buffer;
		}
	}
}
