package io.ddaaniel.internal.support.serializer.converters;

import java.nio.charset.StandardCharsets;

import io.ddaaniel.internal.support.serializer.MessageSerializer;


public class StringMessageSerializer implements MessageSerializer {

    @Override
    public boolean canWrite(Class<?> clazz, String contentType) {
        return String.class.isAssignableFrom(clazz) || 
               (contentType != null && contentType.startsWith("text/"));
    }

    @Override
    public byte[] write(Object body) {
        if (body == null) return new byte[0];
        return body.toString().getBytes(StandardCharsets.UTF_8);
    }
}		
