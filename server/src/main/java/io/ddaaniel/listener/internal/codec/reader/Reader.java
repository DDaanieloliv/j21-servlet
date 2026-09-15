package io.ddaaniel.listener.internal.codec.reader;

import java.nio.channels.ReadableByteChannel;
import java.util.Optional;

/**
 * Reader
 */
public interface Reader {

	boolean canRead();

	ReadableByteChannel channel();

	Optional<HttpServletRequest> readFromConnection();
}
