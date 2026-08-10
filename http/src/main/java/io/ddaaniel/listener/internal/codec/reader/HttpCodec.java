package io.ddaaniel.listener.internal.codec.reader;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import io.ddaaniel.listener.internal.HttpMessage;

import static io.ddaaniel.listener.internal.support.codecUtil.CodecUtil.IndexOf;

public abstract class HttpCodec {

	private static final char DEFAULT_CARRIAGE_RETURN = '\r';
	private static final char DEFAULT_LINE_FEED = '\n';


	private State currentState = State.SKIP_INITIAL_LINE_CHARS;
	private HttpMessage message;
	private Line lineParser;
	private FieldLine fieldParser;

	protected abstract HttpMessage createMessage(String[] initialLine);

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

	private String[] splitInitialLine(ByteBuffer buffer) {
		final byte[] asciiBytes = buffer.array();
		final int startContent = buffer.position();
		final int endContent = buffer.remaining();

		final int aStart = findNonSPLenient(buffer, startContent, endContent);
		final int aEnd = findSPLenient(buffer, aStart, endContent);

		final int bStart = findNonSPLenient(buffer, aEnd, endContent);
		final int bEnd = findSPLenient(buffer, bStart, endContent);

		final int cStart = findNonSPLenient(buffer, bEnd, endContent);
		final int cEnd = findSPLenient(buffer, cStart, endContent);

		return new String[] {
				splitFirstWordInitialLine(asciiBytes, aStart, aEnd - aStart),
				splitSecondWordInitialLine(asciiBytes, bStart, bEnd - aStart),
				cStart < cEnd ? splitThirdWordInitialLine(asciiBytes, cStart, cEnd - cStart) : ""
		};
	}

	protected String splitFirstWordInitialLine(final byte[] asciiBytes, int start, int length) {
		return langAsciiString(asciiBytes, start, length);
	}

	protected String splitSecondWordInitialLine(final byte[] asciiBytes, int start, int length) {
		return langAsciiString(asciiBytes, start, length);
	}

	protected String splitThirdWordInitialLine(final byte[] asciiBytes, int start, int length) {
		return langAsciiString(asciiBytes, start, length);
	}

	protected String langAsciiString(byte[] asciiBytes, int start, int length) {
		return new String(asciiBytes, start, length);
	}

	private int findNonSPLenient(ByteBuffer buffer, final int start, final int end) {
		for (int result = start; result < end; result++) {
			final byte ch = buffer.get(result);
			if (ch == ' ') {
				continue;
			}
			if (Character.isSpaceChar(ch)) {
				throw new IllegalArgumentException("Invalid char on start-line");
			}
			return result;
		}
		return end;
	}

	private int findSPLenient(ByteBuffer buffer, final int start, final int end) {
		for (int result = start; result < end; result++) {
			if (buffer.get(result) == ' ') {
				return result;
			}
		}
		return end;
	}


	public void decode(ReadableByteChannel stream, ByteBuffer buffer, DefaultHttpServletRequest out) {

		switch (this.currentState) {
			case SKIP_INITIAL_LINE_CHARS:
			case READ_INITIAL:
				try {
					ByteBuffer buf = lineParser.parse(buffer);
					String[] initialLine = splitInitialLine(buf);
					message = createMessage(initialLine);
					currentState = State.READ_HEADER;
				} catch (Exception e) { }
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

	/**
	 * Line
	 */
	public class Line extends FieldLine {

		public Line(ByteBuffer buffer) {
			super(buffer);
		}
		
		public ByteBuffer parse(ByteBuffer buffer) {

			return null;
		}
	}

	/**
	 * FieldLine
	 */
	public class FieldLine {
		
		FieldLine(ByteBuffer seq) {
			this.seq = seq;
		}

		protected final ByteBuffer seq;

		public ByteBuffer parse(ByteBuffer buffer) {
			int start = buffer.position();
			final int indexOfLf = IndexOf(buffer, start, DEFAULT_LINE_FEED);

			if (indexOfLf == -1) {
				return null;
			}

			final int endOfSeq;
			if (indexOfLf > start && buffer.get(indexOfLf - 1) == DEFAULT_CARRIAGE_RETURN) {
				endOfSeq = indexOfLf - 1;
			} else {
				endOfSeq = indexOfLf;
			}
			
			return null;
		}
	}
}
