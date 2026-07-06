package io.ddaaniel.internal.parser.reader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;

import io.ddaaniel.internal.exception.MalformedBodyException;
import io.ddaaniel.internal.exception.MalformedHeaderException;
import io.ddaaniel.internal.exception.MalformedRequestLineException;
import io.ddaaniel.internal.exception.URITooLongException;
import io.ddaaniel.internal.support.HttpBodyInputStream;
import io.ddaaniel.internal.support.collectionUtil.CollectionUtil;
import io.ddaaniel.internal.support.httpEntity.httpHeaders.HttpHeaders;


/**
 * ServletReader
 */
public class ServletReader {

	private final ReadableByteChannel stream;

	private final HttpHeaders headers;


	private Parser state;

	public String uri;

	public String method;

	private InputStream body;


	public ServletReader(ReadableByteChannel stream) {
		this.stream = stream;
		this.headers = new HttpHeaders();
		this.state = Parser._INIT;
	}

	public InputStream getBody() {
		if (this.body == null) {
			return new ByteArrayInputStream(new byte[0]);
		}
		return this.body;
	}


	public HttpServletRequest ProcessMessage() {
		var reader = stream;
		var buf = ByteBuffer.allocate(1024);
		var fliped = false;
		try {
			while (this.state != Parser._DONE && this.state != Parser._ERROR) {
				var read = reader.read(buf);
				if (read == -1) {
					if (this.state != Parser._DONE) {
						this.state = Parser._ERROR; 
						throw new MalformedBodyException(" -> body shorter than reported content-length ");
					}
					break;
				}
				buf.flip();
				fliped = true;
				parse(buf);
				if (this.state == Parser._DONE) {
                    fliped = true;
                    break; 
                }
				if (buf.remaining() == buf.capacity()) {
					this.state = Parser._ERROR;
					throw new URITooLongException(" -> uri too long, error 414 -- bytes-read: " + read);
				}
				buf.compact();
				fliped = false;
			}
		} catch (Exception  exception) { 
			if (exception instanceof RuntimeException) throw (RuntimeException) exception;
			throw new RuntimeException(" -> Failure when parsing the servlet-request: ", exception);
		}

		if (!fliped) buf.flip();
		return new HttpServletRequest(
				this.method, 
				this.uri, 
				this.headers, 
				this.body != null ? this.body : new ByteArrayInputStream(new byte[0])
				);
	}


	private void parse(ByteBuffer buf) throws Exception {
		for (;;) {
			Parser state = this.state;
			state.parse(buf, this);
			if (this.state == state || this.state == Parser._DONE || this.state == Parser._ERROR) {
				break;
            }
		}
	}

	enum Parser {

		_INIT() {
			void parse (ByteBuffer bytes, ServletReader reader) {
				var read = 0;
				var SEPARATOR = "\r\n";
				var START = bytes.position();
				var EOL = CollectionUtil.IndexOf(bytes, SEPARATOR, START);
				if (EOL == -1) return;
				var lineBytes = new byte[EOL - START];
				bytes.get(lineBytes); 
				bytes.position(bytes.position() + SEPARATOR.length());

				read += (bytes.position() - START);
				var startLine = new String(lineBytes, StandardCharsets.UTF_8);
				var parts = startLine.split(" ");
				if (parts.length != 3) { 
					throw new MalformedRequestLineException(
							" -> malformed start-line -- bytes-read: " + read
							);
				}

				var httpParts = parts[2].split("/");
				if (httpParts.length != 2 || !httpParts[0].equals("HTTP") || !httpParts[1].equals("1.1")) { 
					throw new MalformedRequestLineException(
							" -> malformed request-line -- bytes-read: " + read
							);
				}

				reader.method = parts[0];
				reader.uri = parts[1];
				reader.state = Parser._HEADER;
				return;
			}
		},

		_HEADER() {
			@Override
			void parse(ByteBuffer data, ServletReader reader) {
				var done = false;
				var START = data.position();
				var SEPARATOR = "\r\n";

				for (;;) {
					var EOL = CollectionUtil.IndexOf(data, SEPARATOR, START);
					if (EOL == -1) {
						break;
					}
					if (EOL - START == 0) {
						data.position(EOL + SEPARATOR.length());
						done = true;
						break;
					}
					var headerline = new byte[EOL - START];
					data.get(headerline);
					data.position(data.position() + SEPARATOR.length());

					var parts = CollectionUtil.Split(headerline, ":", 2);
					if (parts.length != 2) {
						throw new MalformedHeaderException(" -> malformed field-line ");
					}
					var name = parts[0];
					var value = CollectionUtil.TrimSpace(parts[1]);
					if (CollectionUtil.HasSuffix(name, " ".getBytes())) {
						throw new MalformedHeaderException(" -> malformed field-name ");
					}

					if (!CollectionUtil.isToken(name)) {
						throw new MalformedHeaderException(" -> malformed header-name ");
					}
					reader.headers.set(new String(name), new String(value));
					START = data.position();
				}
				if (done) {
					long length = (reader.headers.getContentLength() != -1) ? reader.headers.getContentLength() : 0;
					reader.body = new HttpBodyInputStream(reader.stream, data, length);
					reader.state = Parser._DONE;
				}
			}
		},

		_ERROR() {
			@Override
            void parse(ByteBuffer bytes, ServletReader reader) throws Exception {
                throw new Exception("Somehow its go wrong when parsing");
            }
		},

		_DONE() {
			@Override
			void parse(ByteBuffer bytes, ServletReader reader) {
			}
		};

		abstract void parse(ByteBuffer bytes, ServletReader reader) throws Exception;
	}
}
