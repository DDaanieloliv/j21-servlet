package io.ddaaniel.listener.internal.exception;

/**
 * InvalidLineSeparatorException
 */
public class InvalidLineSeparatorException extends RuntimeException {

	public InvalidLineSeparatorException() {
		super("Line Feed must be preceded by Carriage Return when terminating HTTP start- and header field-lines");
	}
}
