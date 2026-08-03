package io.ddaaniel.listener.internal;

import io.ddaaniel.core.httpEntity.httpHeaders.HttpHeaders;

/**
 * HttpMessage
 */
public interface HttpMessage {

	/**
     * Returns the protocol version of this HttpMessage
     */
    HttpVersion protocolVersion();

    /**
     * Set the protocol version of this HttpMessage
     */
    HttpMessage setProtocolVersion(HttpVersion version);

    /**
     * Returns the headers of this message.
     */
    HttpHeaders headers();
}
