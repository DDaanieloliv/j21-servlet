package io.ddaaniel.internal.parser.request.response.status;


/**
 * HttpStatus
 */
public enum HttpStatus {
	STATUS_OK(200),
	STATUS_BAD_REQUEST(400),
	STATUS_NOT_FOUND(404),
	STATUS_INTERNAL_SERVER_ERROR(500);

	private int code;

	HttpStatus(int code) {
		this.code = code;
	}

	public int GetCode() { return code; }
}
