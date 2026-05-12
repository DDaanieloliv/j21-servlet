package io.ddaaniel.Error;

/**
 * Error
 */
public class Error extends RuntimeException {
	public Error(String msg) {
		super(msg, null, false, false);
	}
}
