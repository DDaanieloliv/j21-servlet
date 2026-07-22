package io.ddaaniel.internal.parser.reader;

import java.util.Optional;

/**
 * HttpReader
 */
public interface HttpReader {

	boolean canRead();

	Optional<DefaultHttpServletRequest> readConnection();
}
