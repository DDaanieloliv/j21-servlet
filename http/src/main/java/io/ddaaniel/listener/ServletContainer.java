package io.ddaaniel.listener;

import io.ddaaniel.core.Handler;

/**
 * ServletContainer
 */
public interface ServletContainer {

	/**
     * Appends a pure Handler to one specific port.
     */
    ServletContainer attach(int port, Handler handler) throws Exception;

    /**
     * Initializes the container with the default router on specified port.
     */
    ServletContainer hookUp(int port) throws Exception;

    /**
     * Terminates the acceptance of connections and gracefully shutdown its threads.
     */
    void close();
}
