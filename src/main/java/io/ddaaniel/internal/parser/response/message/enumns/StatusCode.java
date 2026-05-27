package io.ddaaniel.internal.parser.response.message.enumns;


/**
 * StatusCode
 */
public enum StatusCode {
	STATUS_OK(200),
	STATUS_BAD_REQUEST(400),
	STATUS_INTERNAL_SERVER_ERROR(500);

	private int code;

	StatusCode(int code) {
		this.code = code;
	}

	public int GetCode() { return code; }
}
