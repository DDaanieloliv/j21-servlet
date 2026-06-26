package io.ddaaniel.internal.parser.util.serializer.converters;


import io.ddaaniel.internal.parser.util.serializer.MessageSerializer;

public class ByteMessageConverter implements MessageSerializer {

    @Override
    public boolean canWrite(Class<?> clazz, String contentType) {
        return byte[].class.isAssignableFrom(clazz);
    }

    @Override
    public byte[] write(Object body) {
        if (body == null) return new byte[0];
        return (byte[]) body;
    }
}
