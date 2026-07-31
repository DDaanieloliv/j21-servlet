package io.ddaaniel.listener.internal.parser.writer;

import java.nio.channels.WritableByteChannel;

import io.ddaaniel.core.httpStatus.HttpStatusCode;


/**
 * HttpServletWriter
 */
public interface HttpServletWriter {

	WritableByteChannel channel();
	
	void writeResponse(DefaultHttpServletResponse response) throws Exception;

	void writeErrorResponse(HttpStatusCode statusCode);
}
