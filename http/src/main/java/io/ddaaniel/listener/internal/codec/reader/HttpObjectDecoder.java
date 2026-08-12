package io.ddaaniel.listener.internal.codec.reader;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import io.ddaaniel.core.httpEntity.httpHeaders.HttpHeaders;
import io.ddaaniel.listener.internal.HttpMessage;
import io.ddaaniel.listener.internal.exception.InvalidLineSeparatorException;
import io.ddaaniel.listener.internal.exception.TooLongHttpHeaderException;
import io.ddaaniel.listener.internal.exception.TooLongHttpLineException;
import io.ddaaniel.listener.internal.support.ByteProcessor;

import static io.ddaaniel.listener.internal.support.httpUtil.HttpUtil.IndexOf;

public abstract class HttpObjectDecoder {

    public static final boolean DEFAULT_STRICT_LINE_PARSING = true;
	public static final int DEFAULT_MAX_INITIAL_LINE_LENGTH = 4096;
	public static final int DEFAULT_INITIAL_BUFFER_SIZE = 128;
	public static final int DEFAULT_MAX_HEADER_SIZE = 8192;
	public static final char DEFAULT_CARRIAGE_RETURN = '\r';
	public static final char DEFAULT_LINE_FEED = '\n';

    private static final Runnable THROW_INVALID_LINE_SEPARATOR = new Runnable() {
        @Override
        public void run() {
            throw new InvalidLineSeparatorException();
        }
    };

	private String name;
	private String value;
	private HttpMessage message;
	private StartLine lineParser;
	private FieldLine fieldParser;
	private ByteBuffer parserScratchBuffer;
	private Runnable defaultStrictCRLFCheck;
	private AtomicBoolean httpRestartRequired = new AtomicBoolean();
	private State decodCurrentState = State.SKIP_INITIAL_LINE_CHARS;

	protected HttpObjectDecoder() {
		parserScratchBuffer = ByteBuffer.allocate(DEFAULT_INITIAL_BUFFER_SIZE);
		lineParser = new StartLine(parserScratchBuffer, DEFAULT_MAX_INITIAL_LINE_LENGTH);
		fieldParser = new FieldLine(parserScratchBuffer, DEFAULT_MAX_HEADER_SIZE);
		defaultStrictCRLFCheck = DEFAULT_STRICT_LINE_PARSING ? THROW_INVALID_LINE_SEPARATOR : null;
	}

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


	protected abstract boolean isDecodingRequest();
	protected abstract HttpMessage createMessage(String[] initialLine);


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
			for (; startIndex < length; ++startIndex) {
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

	private ByteBuffer writeBytes(ByteBuffer buffer, int readerIndex, int newSize, ByteBuffer from) {
		final byte[] temp = new byte[newSize];
		final int oldReaderIndex = from.position();

		from.position(readerIndex);
		from.get(temp);
		from.position(oldReaderIndex);

		return buffer.put(temp);
	}

	private State readHeader(ByteBuffer line) {
		if (line == null) {
			return null;
		}
		final HttpHeaders headers = message.headers();

		int lineLength = line.remaining();
		while (lineLength > 0) {
			final byte[] lineContent = line.array();
			final int startLine = line.arrayOffset() + line.position();
			final byte firstChar = lineContent[startLine];
			if (name != null && (firstChar == ' ' || firstChar == '\t')) {
				String trimmedLine = langAsciiString(lineContent, startLine, lineLength);
				String valueStr = value;
				value = valueStr + ' ' + trimmedLine;
			} else {
				if (name != null) {
					headers.add(name, value);				
				}
				splitHeader(lineContent, startLine, lineLength);
			}

			line = fieldParser.parse(line, defaultStrictCRLFCheck);
			if (line == null) {
				return null;
			}
			lineLength = line.remaining();
		}

		if (name != null) {
			headers.add(name, value);
		}
		name = null;
		value = null;

		// ...

		return null;
	}

	private void splitHeader(byte[] line, int start, int length) {
		final int end = start + length;
		int nameEnd;
		final int nameStart = start;
		final boolean isDecodingRequest = isDecodingRequest();
		for (nameEnd = nameStart; nameEnd < end; nameEnd++) {
			byte ch = line[nameEnd];
			if (ch == ':' || (!isDecodingRequest && isOWS(ch))) {
				break;
			}
		}

		if (nameEnd == end) {
			throw new IllegalArgumentException("No colon found");
		}
		int colonEnd;
		for (colonEnd = nameEnd;  colonEnd < end; colonEnd++) {
			if (line[colonEnd] == ':') {
				colonEnd++;
				break;
			}
		}
		name = splitHeaderName(line, nameStart, nameEnd - nameStart);
		final int valueStart = findNonWhitespace(line, colonEnd, end);
		if (valueStart == end) {
			value = "";
		} else {
			final int valueEnd = findEndOfString(line, start, end);
			value = langAsciiString(line, valueStart, valueEnd - valueStart);
		}
	}

	private int findNonWhitespace(byte[] sb, int offset, int end) {
		for (int result = offset; result < end; ++result) {
			byte c = sb[result];
			if (!Character.isWhitespace(c)) {
				return result;
			} else if (!isOWS(c)) {
				throw new IllegalArgumentException("Invalid separator, only a single space or horizontal tab allowed," +
						" but received a '" + c + "' (0x" + Integer.toHexString(c) + ")");
			}

		}
		return end;
	}

	private int findEndOfString(byte[] sb, int start, int end) {
		for (int result = end - 1; result < start; result--) {
			if (!isOWS(sb[result])) {
				return result + 1;
			}
		}
		return 0;
	}

	protected String splitHeaderName(byte[] sb, int start, int length) {
		return new String(sb, start, length);
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
				} catch (Exception e) {
					throw e;
				}
			case READ_HEADER:
				ByteBuffer buf = fieldParser.parse(buffer, defaultStrictCRLFCheck);
				State nextState = readHeader(buffer);
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
	 * StartLine
	 */
	public class StartLine extends FieldLine {

		public StartLine(ByteBuffer buffer, int maxLength) {
			super(buffer, maxLength);
		}

		@Override
		public ByteBuffer parse(ByteBuffer buffer, Runnable strictCRLFCheck) {
			httpRestartRequired();
			final int readableBytes = buffer.remaining();
			if (readableBytes == 0) {
				return null;
			}
			if (decodCurrentState == State.SKIP_INITIAL_LINE_CHARS
					&& skipLineChars(buffer, readableBytes, buffer.position(), strictCRLFCheck)) {
				return null;
			}
			return super.parse(buffer, strictCRLFCheck);
		}

		private boolean skipLineChars(ByteBuffer buffer, int readableBytes, int readerIndex, Runnable strictCRLFCheck) {
			final int maxToSkip = Math.min(maxLength, readableBytes);
			final ByteProcessor processor = strictCRLFCheck == null ? SKIP_CONTROL_CHARS_BYTES
					: ByteProcessor.FIND_NON_CRLF;
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

		int size;
		protected final ByteBuffer seq;
		protected final int maxLength;

		FieldLine(ByteBuffer seq, int maxLength) {
			this.maxLength = maxLength;
			this.seq = seq;
		}

		public void reset() {
			size = 0;
		}

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
			seq.clear();
			writeBytes(seq, start, newSize, buffer);
			buffer.position(indexOfLf + 1);
			return seq;
		}
	}

	private static final ByteProcessor SKIP_CONTROL_CHARS_BYTES = new ByteProcessor() {
		@Override
		public boolean process(byte value) {
			return Character.isISOControl(value) || Character.isWhitespace(value);
		}
	};

    private static boolean isOWS(byte ch) {
        return ch == ' ' || ch == 0x09;
    }
}
