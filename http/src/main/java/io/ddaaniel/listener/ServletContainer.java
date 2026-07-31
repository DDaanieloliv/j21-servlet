package io.ddaaniel.listener;


/**
 * ServletContainer
 */
public interface ServletContainer {

    /**
     * Initializes the container with the default router on specified port.
     */
    ServletContainer loadContainer(int port) throws Exception;

    /**
     * Terminates the acceptance of connections and gracefully shutdown its threads.
     */
    void close();
}
