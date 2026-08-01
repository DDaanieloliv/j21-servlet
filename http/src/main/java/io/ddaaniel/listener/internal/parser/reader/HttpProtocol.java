package io.ddaaniel.listener.internal.parser.reader;

import java.nio.ByteBuffer;

/**
 * HttpProtocol
 */
public interface HttpProtocol {

	void decode(ByteBuffer buffer, HttpRequestBuilder builder) throws Exception;

	boolean isTerminated();

	boolean isFailed();

	boolean isInit();

	void restart();
}
