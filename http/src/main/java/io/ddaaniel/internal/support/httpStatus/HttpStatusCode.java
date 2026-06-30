package io.ddaaniel.internal.support.httpStatus;


/**
 * HttpStatusCode
 */
public interface HttpStatusCode {

	int value();

	boolean is1xxInformational();

	boolean is2xxSuccessful();

	boolean is3xxRedirection();

	boolean is4xxClientError();

	boolean is5xxServerError();

	boolean isError();

	default boolean isSameCodeAs(HttpStatusCode other) {
		return value() == other.value();
	}

	static HttpStatusCode valueOf(int code) {
		if (code < 100 || code > 999) throw new IllegalArgumentException("Status code '" + code + "' should be a three-digit positive integer");
		HttpStatus status = HttpStatus.resolve(code);
		if (status != null) {
			return status;
		}
		else {
			return new DefaultHttpStatusCode(code);
		}
	}
}
