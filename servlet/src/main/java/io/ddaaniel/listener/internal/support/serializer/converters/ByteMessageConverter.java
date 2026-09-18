package io.ddaaniel.listener.internal.support.serializer.converters;

import io.ddaaniel.listener.internal.support.serializer.MessageSerializer;

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

	@Override
	public String getContentType() {
		return "application/octet-stream";
	}
}
