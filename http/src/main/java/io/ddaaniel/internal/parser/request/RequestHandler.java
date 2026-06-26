package io.ddaaniel.internal.parser.request;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;


import io.ddaaniel.internal.exception.MalformedRequestLineException;
import io.ddaaniel.internal.httpEntity.entities.RequestEntity;
import io.ddaaniel.internal.parser.request.header.HeaderHandler;
import io.ddaaniel.internal.parser.request.mapper.state.ParsingState;
import io.ddaaniel.internal.parser.util.Util;
import io.vavr.Tuple;
import io.vavr.Tuple2;

/**
 * RequestHandler
 */
public class RequestHandler {

	public final RequestEntity<?> r = new RequestEntity<>(null, null);
	public ParsingState State = ParsingState.STATE_INIT;


	public static int getLength(HeaderHandler header, String name, int defaultValue) {
		var valueStr = header.Get(name);
		if (valueStr == null) return defaultValue;
		var value = Integer.parseInt(valueStr.getFirst());
		return value;
	}

	public static Tuple2<RequestEntity<?>, Integer> parseRequestLine(ByteBuffer bytes) {
		var read = 0;
		var SEPARATOR = "\r\n";
		var START = bytes.position();
		var EOL = Util.IndexOf(bytes, SEPARATOR, START);
		if (EOL == -1) {
			return Tuple.of(null, read);
		}

		var lineBytes = new byte[EOL - START];
		bytes.get(lineBytes); 
		bytes.get();
		bytes.get();

		read += (bytes.position() - START);
		var startLine = new String(lineBytes, StandardCharsets.UTF_8);
		var parts = startLine.split(" ");
		if (parts.length != 3) { 
			throw new MalformedRequestLineException(
					" -> malformed start-line -- bytes-read: " + read
					);
		}

		var httpParts = parts[2].split("/");
		if (httpParts.length != 2 || !httpParts[0].equals("HTTP") || !httpParts[1].equals("1.1")) { 
			throw new MalformedRequestLineException(
					" -> malformed request-line -- bytes-read: " + read
					);
		}

		URI target = null;
		try {
			target = new URI(httpParts[1]);
		} catch (Exception e) { new URISyntaxException(target.toString(), e.getMessage()); }

		var requestEntity = new RequestEntity<>(parts[1], target);
		return Tuple.of(requestEntity, read);
	}
}
