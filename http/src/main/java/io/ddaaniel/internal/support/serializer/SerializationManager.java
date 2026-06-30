package io.ddaaniel.internal.support.serializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.ddaaniel.internal.support.serializer.converters.ByteMessageConverter;
import io.ddaaniel.internal.support.serializer.converters.StringMessageSerializer;


public class SerializationManager {

    private final List<MessageSerializer> converters = new ArrayList<>();

    public SerializationManager() {
        this.converters.add(new ByteMessageConverter());
        this.converters.add(new StringMessageSerializer());
    }

    public byte[] convert(Object body, String contentType) throws IOException {
        if (body == null) {
            return new byte[0];
        }

        Class<?> bodyClass = body.getClass();

        for (MessageSerializer converter : converters) {
            if (converter.canWrite(bodyClass, contentType)) {
                return converter.write(body);
            }
        }

        throw new IllegalArgumentException("Nenhum HttpMessageConverter encontrado para o tipo: " + bodyClass.getName());
    }
}
