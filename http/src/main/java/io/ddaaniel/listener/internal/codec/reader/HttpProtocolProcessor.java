package io.ddaaniel.listener.internal.codec.reader;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.List;


public class HttpProtocolProcessor {

    private State currentState = State.READ_INITIAL;

    public enum State {
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

    public void decode(ReadableByteChannel stream, ByteBuffer buffer, List<Object> out) {

		switch (this.currentState) {
			case SKIP_INITIAL_LINE_CHARS:
			case READ_INITIAL:
			case READ_HEADER:
				break;

			case READ_VARIABLE_LENGTH_CONTENT:
				break;

			case READ_FIXED_LENGTH_CONTENT:
				break;

			case READ_CHUNK_SIZE:
				break;

			case READ_CHUNKED_CONTENT:
				break;

			case READ_CHUNK_DELIMITER:
				break;

			case READ_CHUNK_FOOTER:
				break;

			case BAD_MESSAGE:
			default:
				break;
		}
	}

}
