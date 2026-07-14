package io.ddaaniel.internal.parser.writer;

import io.ddaaniel.core.httpEntity.ResponseEntity;

/**
 * HttpServletWriter
 */
public interface HttpServletWriter {

	boolean canWrite(ResponseEntity<?> response);

	void writeResponse(ResponseEntity<?> response) throws Exception;
	
}
