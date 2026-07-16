package io.ddaaniel.internal.parser.writer;

import java.nio.channels.WritableByteChannel;

import io.ddaaniel.core.httpEntity.ResponseEntity;

/**
 * HttpWriterMatcher
 */
public interface HttpWriterMatcher {

	boolean matches(ResponseEntity<?> response);

	void write(WritableByteChannel channel, ResponseEntity<?> response) throws Exception;
}
