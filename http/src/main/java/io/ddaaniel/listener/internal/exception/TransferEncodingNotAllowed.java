package io.ddaaniel.listener.internal.exception;

/**
 * TransferEncodingNotAllowed
 */
public class TransferEncodingNotAllowed extends RuntimeException {

	public TransferEncodingNotAllowed() {
		super("The Transfer-Encoding header is only allowed in HTTP/1.1 or newer");
	}
				


}
