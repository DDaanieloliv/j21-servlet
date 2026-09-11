package io.ddaaniel.listener.internal.codec.reader;

import java.util.Optional;

/**
 * Reader
 */
public interface Reader {

	boolean canRead();

	Optional<DefaultHttpServletRequest> readFromConnection();
}
