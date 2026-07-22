package io.ddaaniel.internal.support.serializer.converters;

import java.nio.charset.StandardCharsets;

import io.ddaaniel.internal.support.serializer.MessageSerializer;


public class StringMessageSerializer implements MessageSerializer {

	@Override
	public boolean canWrite(Class<?> clazz, String contentType) {
		return CharSequence.class.isAssignableFrom(clazz) 
			|| Number.class.isAssignableFrom(clazz)
			|| Boolean.class.isAssignableFrom(clazz)
			|| Character.class.isAssignableFrom(clazz);
	}

    @Override
    public byte[] write(Object body) {
        if (body == null) return new byte[0];
        return body.toString().getBytes(StandardCharsets.UTF_8);
    }

	@Override
	public String getContentType() {
		return "text/plain; charset=UTF-8";
	}
}		
