package io.ddaaniel.internal.parser.writer;

import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.ddaaniel.core.httpEntity.httpHeaders.HttpHeaders;
import io.ddaaniel.core.httpStatus.HttpStatus;
import io.ddaaniel.core.httpStatus.HttpStatusCode;
import io.ddaaniel.internal.support.HttpFun.FunHttp;

/**
 * ServletWriter
 */
public class ServletWriter implements HttpServletWriter {

	private final WritableByteChannel channel;

	private static final Logger log = Logger.getLogger(ServletWriter.class.getName());

	private static final List<HttpWriterConduct> strategies = List.of(
		new ChunkedServletWriter(),
		new DefaultServletWriter()
	);

	public ServletWriter(WritableByteChannel channel) {
        this.channel = channel;
    }

	@Override
	public WritableByteChannel channel() { 
		return this.channel;
	}

	@Override
	public void writeResponse(DefaultHttpServletResponse response) throws Throwable {
		for (HttpWriterConduct strategy : strategies) {
			if (strategy.matches(response)) {
				if (log.isLoggable(Level.FINE)) log.log(Level.FINE, " -> selected writer strategy: {0} for response", strategy.getClass().getSimpleName());
				setSoftwareOrigin(response);
				strategy.write(channel, response);

				boolean keepAlive = !"close".equalsIgnoreCase(response.getHeaders().getFirst("Connection"));
				if (!keepAlive) {
					channel.close();
				}
				return;
			}
		}
		throw new IllegalStateException(" -> No HTTP writers supports its response ");
	}

	@Override
	public void writeErrorResponse(HttpStatusCode status) {
		try {
            byte[] errorBody;
            try {
                errorBody = FunHttp.respond404().getBytes();
            } catch (Exception e) {
				errorBody = 
					("<html>" +
					 "<head>" +
					 "<title>400 Bad Request</title>" +
					 "</head>" +
					 "<body>" +
					 "<h1>Bad Request</h1>" +
					 "<p>Your request honestly kinda sucked.</p>" +
					 "</body>" +
					 "</html>" +
					 "\n")
					.getBytes();
			}

			var code = status.value();
			String statusStr = String.format("HTTP/1.1 %s %s\r\n", code, HttpStatus.resolve(code).getReasonPhrase());
            String headersStr = "Content-Length: " + errorBody.length + "\r\n" +
                                "Connection: close\r\n" +
                                "Content-Type: text/html; charset=UTF-8\r\n\r\n";

            channel.write(ByteBuffer.wrap(statusStr.getBytes()));
            channel.write(ByteBuffer.wrap(headersStr.getBytes()));
            channel.write(ByteBuffer.wrap(errorBody));
        } catch (Exception e) {
            throw new RuntimeException(" -> Critical Error when send error response ", e);
        }
	}


	protected void setSoftwareOrigin(DefaultHttpServletResponse response) {
		var headers = response.getHeaders();
		if (!headers.containsHeader(HttpHeaders.DATE)) {
			headers.set(HttpHeaders.DATE, DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC)));
		}
		if (!headers.containsHeader(HttpHeaders.SERVER)) {
			headers.set(HttpHeaders.SERVER, "CustomJavaEngine/1.0");
		}
	}
}
