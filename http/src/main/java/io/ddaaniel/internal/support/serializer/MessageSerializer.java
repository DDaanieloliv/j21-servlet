package io.ddaaniel.internal.support.serializer;


import java.io.IOException;

/**
 * MessageSerializer
 */
public interface MessageSerializer {

    boolean canWrite(Class<?> clazz, String contentType);

    byte[] write(Object body) throws IOException;
}
