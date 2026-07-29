package io.ddaaniel.internal.parser.writer;

import java.nio.channels.WritableByteChannel;

import io.ddaaniel.core.httpStatus.HttpStatusCode;


/**
 * HttpServletWriter
 */
public interface HttpServletWriter {

	WritableByteChannel channel();
	
	void writeResponse(DefaultHttpServletResponse response) throws Throwable;

	void writeErrorResponse(HttpStatusCode statusCode);
}
