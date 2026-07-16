package io.ddaaniel.internal.parser.writer;

import java.nio.channels.WritableByteChannel;

import io.ddaaniel.core.httpEntity.ResponseEntity;
import io.ddaaniel.core.httpStatus.HttpStatusCode;


/**
 * HttpServletWriter
 */
public interface HttpServletWriter {

	WritableByteChannel channel();
	
	void writeResponse(ResponseEntity<?> response) throws Exception;

	void writeErrorResponse(HttpStatusCode statusCode);
}
