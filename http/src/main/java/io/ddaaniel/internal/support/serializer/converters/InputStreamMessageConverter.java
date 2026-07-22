package io.ddaaniel.internal.support.serializer.converters;

import io.ddaaniel.internal.support.serializer.MessageSerializer;
import java.io.InputStream;
import java.io.IOException;

public class InputStreamMessageConverter implements MessageSerializer {

    @Override
    public boolean canWrite(Class<?> clazz, String contentType) {
        return InputStream.class.isAssignableFrom(clazz);
    }

    @Override
    public byte[] write(Object body) throws IOException {
        if (body == null) return new byte[0];
        try (InputStream is = (InputStream) body) {
            return is.readAllBytes();
        }
    }

    @Override
    public String getContentType() {
        return "application/octet-stream";
    }
}
