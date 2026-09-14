package io.ddaaniel.listener.internal.support.serializer.converters;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.ddaaniel.listener.internal.support.serializer.MessageSerializer;

public class JsonMessageConverter implements MessageSerializer {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean canWrite(Class<?> clazz, String contentType) {
        boolean isJsonType = contentType != null && contentType.contains("application/json");

		boolean isComplexObject = !clazz.isPrimitive() 
			&& !CharSequence.class.isAssignableFrom(clazz) 
			&& !Number.class.isAssignableFrom(clazz)
			&& !Boolean.class.isAssignableFrom(clazz)
			&& !byte[].class.isAssignableFrom(clazz)
			&& !InputStream.class.isAssignableFrom(clazz);

        return isJsonType || isComplexObject;
    }

    @Override
    public byte[] write(Object body) throws IOException {
        if (body == null) return new byte[0];
        return mapper.writeValueAsBytes(body);
    }

    @Override
    public String getContentType() {
        return "application/json";
    }
}
