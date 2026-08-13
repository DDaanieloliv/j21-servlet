package io.ddaaniel.listener.internal.codec.reader;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.ddaaniel.core.httpEntity.httpHeaders.HttpHeaders;
import io.ddaaniel.core.httpEntity.httpHeaders.HttpHeaders.HttpHeadersNames;
import io.ddaaniel.core.httpEntity.httpHeaders.HttpHeaders.HttpHeadersValue;
import io.ddaaniel.core.httpStatus.HttpStatus;
import io.ddaaniel.core.httpStatus.HttpStatusCode;
import io.ddaaniel.listener.internal.HttpMessage;
import io.ddaaniel.listener.internal.HttpRequest;
import io.ddaaniel.listener.internal.HttpResponse;
import io.ddaaniel.listener.internal.exception.InvalidLineSeparatorException;
import io.ddaaniel.listener.internal.exception.TooLongHttpHeaderException;
import io.ddaaniel.listener.internal.exception.TooLongHttpLineException;
import io.ddaaniel.listener.internal.support.ByteProcessor;
import io.ddaaniel.listener.internal.valueObjects.HttpMethod;
import io.ddaaniel.listener.internal.valueObjects.HttpVersion;

import static io.ddaaniel.listener.internal.support.httpUtil.HttpUtil.IndexOf;

public abstract class HttpObjectDecoder {

	public static final boolean DEFAULT_ALLOW_DUPLICATE_CONTENT_LENGTHS = false;
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
	private long contentLength = Long.MIN_VALUE;
	private boolean allowDuplicateContentLength;
	private boolean isSwitchingToNonHttp1Protocol;
	private AtomicBoolean httpRestartRequired = new AtomicBoolean();
	private State decodCurrentState = State.SKIP_INITIAL_LINE_CHARS;

	protected HttpObjectDecoder() {
		parserScratchBuffer = ByteBuffer.allocate(DEFAULT_INITIAL_BUFFER_SIZE);
		lineParser = new StartLine(parserScratchBuffer, DEFAULT_MAX_INITIAL_LINE_LENGTH);
		fieldParser = new FieldLine(parserScratchBuffer, DEFAULT_MAX_HEADER_SIZE);
		defaultStrictCRLFCheck = DEFAULT_STRICT_LINE_PARSING ? THROW_INVALID_LINE_SEPARATOR : null;
		allowDuplicateContentLength = DEFAULT_ALLOW_DUPLICATE_CONTENT_LENGTHS;
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

		/*
		 *	Set DecoderResult to be usefull on the pipeline ...
		 *
		 *	main purpose
		 *
    	 *	Indicar ao restante da pipeline (handlers a jusante, agregadores, testes, etc.) se a
		 *	mensagem foi decodificada com sucesso ou se ocorreu um erro, e passar a causa do erro 
		 *	quando houver. No caso de HttpMessageDecoderResult, além de sinalizar sucesso, transportar 
		 *	initialLineLength e headerSize (úteis para diagnóstico / limites).
		 *
		 *	where is used
		 *
    	 *	O decoder (HttpObjectDecoder) define message.setDecoderResult(...) depois de parsear a
		 *	linha inicial e headers, ou define DecoderResult.failure(cause) quando detecta uma mensagem
		 *	inválida (veja invalidMessage/invalidChunk). Handlers e utilitários a jusante verificam
		 *	message.decoderResult().isSuccess() / isFailure() e agem: retornar 400, fechar conexão, propagar
		 *	o erro, ou copiar o resultado para um FullHttpRequest/FullHttpResponse (ex.: WebSocket handshakers,
		 *	HttpObjectAggregator, exemplos de testes). Vários testes e handlers no repositório consultam
		 *	decoderResult() para decidir comportamento (ex.: StompWebSocketClientPageHandler,
		 *	WebSocketServerHandshaker, HttpObjectAggregatorTest, MultipleContentLengthHeadersTest).
		 *
		 *
		 * */

		List<String> contentLengthFields = headers.getAll(HttpHeadersNames.CONTENT_LENGTH);
		if (!contentLengthFields.isEmpty()) {
			HttpVersion version = message.protocolVersion();
			boolean isHttp10OrEarlier = version.majorVersion() < 1 ||
					(version.majorVersion() == 1 && version.minorVersion() == 1);
			contentLength = normalizeAndGetContentLength(contentLengthFields, isHttp10OrEarlier,
					allowDuplicateContentLength);
			if (contentLength != -1) {
				String lengthValue = contentLengthFields.get(0).trim();
				if (contentLengthFields.size() > 1 || !isLengthEqual(lengthValue, contentLength)) {
					headers.set(HttpHeadersNames.CONTENT_LENGTH, String.valueOf(contentLength));
				}
			}
		} else {
			contentLength = getWebSocketContentLength(message);
		}

		if (!isDecodingRequest() && message instanceof HttpResponse) {
			HttpResponse res = (HttpResponse) message;
			this.isSwitchingToNonHttp1Protocol = isSwitchingToNonHttp1Protocol(res);
		}
		if (isContentAwaysEmpty(message)) {
			setTransferEncodingChunked(message, false);
			return State.SKIP_CONTROL_CHARS;
		}

		return null;
	}

	private void setTransferEncodingChunked(HttpMessage message, boolean chunked) {
		if (chunked) {
			message.headers().set(HttpHeadersNames.TRANSFER_ENCODING, HttpHeadersValue.CHUNKED);
			message.headers().remove(HttpHeadersNames.CONTENT_LENGTH);
		} else {
			List<String> encodings = message.headers().getAll(HttpHeadersNames.TRANSFER_ENCODING);
			if (encodings.isEmpty()) {
				return;
			}
			List<String> values = new ArrayList<String>(encodings);
			Iterator<String> valuesIt = values.iterator();
			while (valuesIt.hasNext()) {
				CharSequence value = valuesIt.next();
				if (HttpHeadersValue.CHUNKED.contentEquals(value)) {
					valuesIt.remove();
				}
			}
			if (values.isEmpty()) {
				message.headers().remove(HttpHeadersNames.TRANSFER_ENCODING);
			} else {
				message.headers().set(HttpHeadersNames.TRANSFER_ENCODING, values);
			}
		}
	}

	private boolean isContentAwaysEmpty(HttpMessage msg) {
		if (msg instanceof HttpResponse) {
			HttpResponse res = (HttpResponse) msg;
			final HttpStatusCode status = res.status();
			final int code = status.value();

			if (status.is1xxInformational()) {
				return !(code == 101 && !res.headers().containsHeader(HttpHeadersNames.SEC_WEBSOCKET_ACCEPT)
						&& res.headers().containsHeader(HttpHeadersNames.UPGRADE, HttpHeadersValue.WEBSOCKET));
			}
			switch (code) {
				case 204: 
				case 304:
					return true;
				default:
					return false;
			}
		}
		return false;
	}

	private boolean isSwitchingToNonHttp1Protocol(HttpResponse msg) {
		if (msg.status().value() != HttpStatus.SWITCHING_PROTOCOLS.value()) {
			return false;
		}
		String newProtocol = msg.headers().getFirst(HttpHeadersNames.UPGRADE);
		return newProtocol == null || 
			!newProtocol.contains(HttpVersion.HTTP_1_0.text()) &&
			!newProtocol.contains(HttpVersion.HTTP_1_1.text());
	}

	private int getWebSocketContentLength(HttpMessage message) {
		HttpHeaders headers = message.headers();
		if (message instanceof HttpRequest) {
			HttpRequest req = (HttpRequest) message;
			if (HttpMethod.GET.equals(req.method()) &&
					headers.containsHeader(HttpHeadersNames.SEC_WEBSOCKET_KEY1) &&
					headers.containsHeader(HttpHeadersNames.SEC_WEBSOCKET_KEY2)) {
				return 8;
			}
		} else if (message instanceof HttpResponse) {
			HttpResponse res = (HttpResponse) message;
			if (res.status().value() == 101 && 
					headers.containsHeader(HttpHeadersNames.SEC_WEBSOCKET_ORIGIN) &&
					headers.containsHeader(HttpHeadersNames.SEC_WEBSOCKET_LOCATION)) {
				return 16;
			}
		}

		return -1;
	}

	private boolean isLengthEqual(String lengthValue, long contentLength) {
		try {
			return Long.parseLong(lengthValue) == contentLength;
		} catch (Exception e) {
			return false;
		}
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
		for (colonEnd = nameEnd; colonEnd < end; colonEnd++) {
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

	private long normalizeAndGetContentLength(
			List<String> contentLengthFields, boolean isHttp10OrEarlier, boolean allowDuplicateContentLength) {

		if (contentLengthFields.isEmpty()) {
			return -1;
		}
		String firstField = contentLengthFields.get(0).toString();
		boolean multipleContentLengths = contentLengthFields.size() > 1 || firstField.indexOf(',') >= 0;

		if (multipleContentLengths && !isHttp10OrEarlier) {
			if (allowDuplicateContentLength) {
				String firstValue = null;
				for (CharSequence field : contentLengthFields) {
					String[] tokens = field.toString().split(String.valueOf(','), -1);
					for (String token : tokens) {
						String trimmed = token.trim();
						if (firstValue == null) {
							firstValue = trimmed;
						} else if (!trimmed.equals(firstValue)) {
							throw new IllegalArgumentException(
									"Multiple Content-Length values found: " + contentLengthFields);
						}
					}
				}
				firstField = firstValue;
			} else {
				throw new IllegalArgumentException("Multiple Content-Length values found: " + contentLengthFields);
			}
		}

		if (firstField.isEmpty() || !Character.isDigit(firstField.charAt(0))) {
			throw new IllegalArgumentException("Content-Length value is not a number: " + firstField);
		}

		try {
			final long value = Long.parseLong(firstField);
			if (value < 0L) {
				throw new IllegalArgumentException("Content-Length value: " + value + " (expected: >= 0)");
			}
			return value;
		} catch (Exception e) {
			throw new IllegalArgumentException("Content-Length value is not a number: " + firstField, e);
		}
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
