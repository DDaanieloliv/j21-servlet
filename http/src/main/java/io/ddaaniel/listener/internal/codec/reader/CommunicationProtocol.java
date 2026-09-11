package io.ddaaniel.listener.internal.codec.reader;

import java.nio.ByteBuffer;

/**
 * CommunicationProtocol
 */
public interface CommunicationProtocol {

	void decode(ByteBuffer buffer, HttpRequestBuilder builder) throws Exception;

	boolean isTerminated();

	boolean isFailed();

	boolean isInit();

	void restart();
}
