package io.ddaaniel.listener.internal.codec.reader;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.ddaaniel.listener.internal.exception.MalformedHeaderException;
import io.ddaaniel.listener.internal.exception.MalformedRequestLineException;
import io.ddaaniel.listener.internal.support.HttpBodyInputStream;
import io.ddaaniel.listener.internal.support.httpUtil.HttpUtil;

/**
 * HttpCommunicationDecoder
 */
public class HttpCommunicationDecoder implements CommunicationProtocol {

	private Parser state;
	private ReadableByteChannel stream;
	private static final Logger log = Logger.getLogger(HttpCommunicationDecoder.class.getName());

	enum Parser {
		SKIP_INITIAL_LINE_CHARS,
		READ_INITIAL,
		READ_HEADER,
		BAD_MESSAGE;
	}

	public HttpCommunicationDecoder(ReadableByteChannel conn) {
		this.stream = conn;
		this.state = Parser.READ_INITIAL;
	}

	@Override
	public void decode(ByteBuffer buffer, HttpRequestBuilder builder) throws Exception {
		switch (state) {
			case READ_INITIAL:
				Parser cwState = lineParse(stream, buffer, builder);
				state = cwState;
				if (log.isLoggable(Level.FINER)) {
					log.log(Level.FINER,
							" -> parser state transition: {0} -> {1}",
							new Object[] { cwState, state });
				}
				if (state != Parser.READ_HEADER) {
					break;
				}
			case READ_HEADER:
				cwState = headerParser(stream, buffer, builder);
				state = cwState;
				if (log.isLoggable(Level.FINER)) {
					log.log(Level.FINER,
							" -> parser state transition: {0} -> {1}",
							new Object[] { cwState, state });
				}
				if (state == Parser.SKIP_INITIAL_LINE_CHARS) {
					return;
				}
				break;
			case BAD_MESSAGE:
				cwState = parserBadMessage(stream, buffer, builder);
				state = cwState;
				if (log.isLoggable(Level.FINER)) {
					log.log(Level.FINER,
							" -> parser state transition: {0} -> {1}",
							new Object[] { cwState, state });
				}
				break;
			case SKIP_INITIAL_LINE_CHARS:
		}
	}

	@Override
	public boolean isTerminated() {
		return this.state == Parser.SKIP_INITIAL_LINE_CHARS;
	}

	@Override
	public boolean isFailed() {
		return this.state == Parser.BAD_MESSAGE;
	}

	@Override
	public boolean isInit() {
		return this.state == Parser.READ_INITIAL;
	}

	@Override
	public void restart() {
		state = Parser.READ_INITIAL;
	}

	Parser lineParse(
			ReadableByteChannel stream,
			ByteBuffer buffer,
			HttpRequestBuilder builder) {
		final String separator = "\r\n";
		int start = getStart(buffer);
		int end = getEndOfLine(buffer, separator, start);
		if (end == -1) {
			return Parser.READ_INITIAL;
		}
		var line = new byte[end - start];
		consumeLine(buffer, line);
		var startLine = new String(line, StandardCharsets.UTF_8);
		var parts = startLine.split(" ");
		if (parts.length != 3) {
			throw new MalformedRequestLineException();
		}
		var httpVersion = parts[2].split("/");
		if (httpVersion.length != 2 || !httpVersion[0].equals("HTTP") || !httpVersion[1].equals("1.1")) {
			throw new MalformedRequestLineException();
		}
		builder.method(parts[0]).uri(parts[1]);
		return Parser.READ_HEADER;
	}

	Parser headerParser(ReadableByteChannel stream, ByteBuffer buffer, HttpRequestBuilder builder) {
		var separator = "\r\n";
		for (;;) {
			var start = getStart(buffer);
			var end = getEndOfLine(buffer, separator, start);
			if (end - start == 0) {
				buffer.position(end + separator.length());
				long length = builder.headers().getContentLength();
				length = (length != -1) ? length : 0;
				builder.body(new HttpBodyInputStream(stream, buffer, length));
				break;
			}
			if (end == -1) {
				return Parser.READ_HEADER;
			}
			var headerline = new byte[end - start];
			consumeLine(buffer, headerline);
			var parts = getParts(headerline);
			if (parts.length != 2) {
				throw new MalformedHeaderException(" -> malformed field-line ");
			}
			var name = parts[0];
			var value = HttpUtil.TrimSpace(parts[1]);
			if (HttpUtil.HasSuffix(name, " ".getBytes())) {
				throw new MalformedHeaderException(" -> malformed field-name ");
			}
			if (!HttpUtil.isToken(name)) {
				throw new MalformedHeaderException(" -> malformed header-name ");
			}
			builder.headers().set(new String(name), new String(value));
		}
		return Parser.SKIP_INITIAL_LINE_CHARS;
	}

	Parser parserBadMessage(ReadableByteChannel stream, ByteBuffer buffer, HttpRequestBuilder builder)
			throws Exception {
		throw new Exception("Somehow its go wrong when parsing");
	}

	int getStart(ByteBuffer buffer) {
		return buffer.position();
	}

	byte[][] getParts(byte[] headerline) {
		return HttpUtil.Split(headerline, ":", 2);
	}

	int getEndOfLine(ByteBuffer buffer, String separator, int start) {
		return HttpUtil.IndexOf(buffer, start, separator);
	}

	void consumeLine(ByteBuffer buffer, byte[] line) {
		String separator = "\r\n";
		buffer.get(line);
		buffer.position(buffer.position() + separator.length());
	}
}
