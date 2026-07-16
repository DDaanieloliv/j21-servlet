package io.ddaaniel.internal.parser.reader;

/**
 * HttpReader
 */
public interface HttpReader {

	boolean canRead();

	DefaultHttpServletRequest processMessage();
}
