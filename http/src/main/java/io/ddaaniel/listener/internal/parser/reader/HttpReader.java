package io.ddaaniel.listener.internal.parser.reader;

import java.util.Optional;

/**
 * HttpReader
 */
public interface HttpReader {

	boolean canRead();

	Optional<DefaultHttpServletRequest> readConnection();
}
