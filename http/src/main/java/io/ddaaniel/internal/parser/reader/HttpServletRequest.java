package io.ddaaniel.internal.parser.reader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import io.ddaaniel.internal.support.httpEntity.httpHeaders.HttpHeaders;

/**
 * HttpServletRequest
 */
public record HttpServletRequest(
		String method,
		String uri,
		HttpHeaders headers,
		InputStream body
		) {

	public String getBodyAsString() {
		if (this.body== null) {
			return "";
		}

		try (var result = new java.io.ByteArrayOutputStream()) {
			byte[] buffer = new byte[512];
			int length;
			while ((length = this.body.read(buffer)) != -1) {
				result.write(buffer, 0, length);
			}

			return result.toString(StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(" -> Error when reading the body ", e);
		}
	}
}
