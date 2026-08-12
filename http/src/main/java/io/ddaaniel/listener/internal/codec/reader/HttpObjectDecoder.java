package io.ddaaniel.listener.internal.codec.reader;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import io.ddaaniel.listener.internal.HttpMessage;
import io.ddaaniel.listener.internal.exception.TooLongHttpHeaderException;
import io.ddaaniel.listener.internal.exception.TooLongHttpLineException;
import io.ddaaniel.listener.internal.support.ByteProcessor;
import jdk.graal.compiler.core.common.type.ArithmeticOpTable.BinaryOp.Max;

import static io.ddaaniel.listener.internal.support.httpUtil.HttpUtil.IndexOf;

public abstract class HttpObjectDecoder {

	private static final int DEFAULT_MAX_INITIAL_LINE_LENGTH = 4096;
	private static final int DEFAULT_INITIAL_BUFFER_SIZE = 128;
	private static final int DEFAULT_MAX_HEADER_SIZE = 8192;
	private static final char DEFAULT_CARRIAGE_RETURN = '\r';
	private static final char DEFAULT_LINE_FEED = '\n';

	private HttpMessage message;
	private Runnable defaultStrictCRLFCheck;

	private Line lineParser;
	private FieldLine fieldParser;
	private ByteBuffer parserScratchBuffer;
	private AtomicBoolean httpRestartRequired = new AtomicBoolean();
	private State decodCurrentState = State.SKIP_INITIAL_LINE_CHARS;

	protected HttpObjectDecoder(int max_initial_line_length, int max_header_size){
		parserScratchBuffer = ByteBuffer.allocate(DEFAULT_INITIAL_BUFFER_SIZE);
		lineParser = new Line(parserScratchBuffer, max_initial_line_length);
		fieldParser = new FieldLine(parserScratchBuffer, max_header_size);
	}


	protected abstract HttpMessage createMessage(String[] initialLine);

	private static final ByteProcessor SKIP_CONTROL_CHARS_BYTES = new ByteProcessor() {
		@Override
		public boolean process(byte value) {
			return Character.isISOControl(value) || Character.isWhitespace(value);
		}
	};

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

	private int forEachByte(ByteBuffer buffer, int startIndex, int length, ByteProcessor processor) {
		try {
			for (;startIndex < length; ++startIndex) {
				if (processor.process(buffer.get(startIndex))) {
					return startIndex;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	private ByteBuffer skipBytes(ByteBuffer buffer, int length) {
		return buffer.position(length);
	}




	public void decode(ReadableByteChannel stream, ByteBuffer buffer, DefaultHttpServletRequest out) {
		if (httpRestartRequired.get()) {
			httpRestartToDefaults();
		}

		switch (this.decodCurrentState) {
			case SKIP_INITIAL_LINE_CHARS:
			case READ_INITIAL:
				try {
					ByteBuffer buf = lineParser.parse(buffer, defaultStrictCRLFCheck);
					String[] initialLine = splitInitialLine(buf);
					message = createMessage(initialLine);
					decodCurrentState = State.READ_HEADER;
				} catch (Exception e) { throw e; }
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

	private void httpRestartRequired() {
		httpRestartRequired.lazySet(true);
	}

	private void httpRestartToDefaults() {
		this.message = null;
		this.decodCurrentState = State.SKIP_INITIAL_LINE_CHARS;
		this.httpRestartRequired.lazySet(false);
	}



	/**
	 * Line
	 */
	public class Line extends FieldLine {

		public Line(ByteBuffer buffer, int maxLength) {
			super(buffer, maxLength);
		}
		
		@Override
		public ByteBuffer parse(ByteBuffer buffer, Runnable strictCRLFCheck) {
			httpRestartRequired();
			final int readableBytes = buffer.remaining();
			if (readableBytes == 0) {
				return null;
			}
			if (decodCurrentState == State.SKIP_INITIAL_LINE_CHARS && skipLineChars(buffer, readableBytes, buffer.position(), strictCRLFCheck)) {
				return null;
			}
			return super.parse(buffer, strictCRLFCheck);
		}

		private boolean skipLineChars(ByteBuffer buffer, int readableBytes, int readerIndex, Runnable strictCRLFCheck) {
			final int maxToSkip = Math.min(maxLength, readableBytes);
			final ByteProcessor processor = strictCRLFCheck == null ? SKIP_CONTROL_CHARS_BYTES : ByteProcessor.FIND_NON_CRLF;
			final int firstNonLineIndex = forEachByte(buffer, readerIndex, maxToSkip, processor);
			if (firstNonLineIndex == -1) {
				skipBytes(buffer, maxToSkip);
				if (readableBytes > maxToSkip) {
					throw new TooLongHttpLineException("An HTTP line is larger than " + maxLength + " bytes.");
				}
				return true;
			}
			if (strictCRLFCheck != null) {
				final int b = buffer.get(firstNonLineIndex) & 0xff;
				if (Character.isISOControl(b)) {
					strictCRLFCheck.run();
				}
			}
			buffer.position(firstNonLineIndex);
			decodCurrentState = State.READ_INITIAL;
			return false;
		}
	}

	/**
	 * FieldLine
	 */
	public class FieldLine {
		
		FieldLine(ByteBuffer seq, int maxLength) {
			this.maxLength = maxLength;
			this.seq = seq;
		}

		int size;
		protected final ByteBuffer seq;
		protected final int maxLength;

		public ByteBuffer parse(ByteBuffer buffer, Runnable strictCRLFCheck) {
			final int readableBytes = buffer.remaining();
			final int start = buffer.position();
			final int maxBodySize = maxLength - size;
			final long maxBodyWithCRLF = maxBodySize + 2L;
			final int toProcess = (int) Math.min(maxBodyWithCRLF, readableBytes);
			final int toIndexExclusive = start + toProcess;
			final int indexOfLf = IndexOf(buffer, start, toIndexExclusive, DEFAULT_LINE_FEED);

			if (indexOfLf == -1) {
				if (readableBytes > maxBodySize) {
					throw new TooLongHttpHeaderException("HTTP header is larger than " + maxLength + " bytes.");
				}
				return null;
			}

			final int endOfSeq;
			if (indexOfLf > start && buffer.get(indexOfLf - 1) == DEFAULT_CARRIAGE_RETURN) {
				endOfSeq = indexOfLf - 1;
			} else {
				if (strictCRLFCheck != null) {
					strictCRLFCheck.run();
				}
				endOfSeq = indexOfLf;
			}
			final int newSize = endOfSeq - start;
			if (newSize == 0) {
				seq.clear();
				buffer.position(indexOfLf - 1);
				return seq;
			}
			int size = this.size + newSize;
			if (size > maxLength) {
				throw new TooLongHttpHeaderException("HTTP header is larger than " + maxLength + " bytes.");
			}
			this.size = size;
			final byte[] temp = new byte[newSize];
			seq.clear();
			// writeBytes(buffer, start, newSize);
			buffer.position(indexOfLf + 1);
			return null;
		}

		public void reset() {
			size = 0;
		}
	}
}
