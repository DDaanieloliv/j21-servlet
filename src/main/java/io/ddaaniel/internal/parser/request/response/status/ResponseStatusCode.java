package io.ddaaniel.internal.parser.request.response.status;


/**
 * ResponseStatusCode
 */
public enum ResponseStatusCode {
	STATUS_OK(200),
	STATUS_BAD_REQUEST(400),
	STATUS_NOT_FOUND(404),
	STATUS_INTERNAL_SERVER_ERROR(500);

	private int code;

	ResponseStatusCode(int code) {
		this.code = code;
	}

	public int GetCode() { return code; }
}
