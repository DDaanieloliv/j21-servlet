package io.ddaaniel.internal.server;

import io.ddaaniel.internal.parser.response.message.enumns.StatusCode;

/**
 * HandlerError
 */
public class HandlerError {
	public StatusCode code;
	public String Message;

	public HandlerError(StatusCode s, String msg){
		this.code = s;
		this.Message = msg;
	}
}
