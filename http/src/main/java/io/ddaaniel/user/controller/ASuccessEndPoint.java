package io.ddaaniel.user.controller;

import io.ddaaniel.annotations.HTTP;

/**
 * ASuccessEndPoint
 */
public class ASuccessEndPoint {

	@HTTP
	public String successResponse() {
		return "If you can read this, it means the read and write was a absolute banger.\n";
	}
}
