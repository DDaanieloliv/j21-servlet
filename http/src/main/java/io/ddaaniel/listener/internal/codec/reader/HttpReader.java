package io.ddaaniel.listener.internal.codec.reader;

import java.util.Optional;

/**
 * HttpReader
 */
public interface HttpReader {

	boolean canRead();

	Optional<DefaultHttpServletRequest> readFromConnection();
}
