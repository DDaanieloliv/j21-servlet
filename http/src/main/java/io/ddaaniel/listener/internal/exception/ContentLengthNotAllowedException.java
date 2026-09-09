package io.ddaaniel.listener.internal.exception;

/**
 * ContentLengthNotAllowedException
 */
public class ContentLengthNotAllowedException extends RuntimeException {

	public ContentLengthNotAllowedException(String msg) {
		super(msg);
	}

	public ContentLengthNotAllowedException() {
		super("Content-Length are not allowed in HTTP/1.1 messages that contains a Transfer-Encoding header.");
	}
}
