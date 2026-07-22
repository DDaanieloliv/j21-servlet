package io.ddaaniel.internal.support.serializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.ddaaniel.internal.support.serializer.converters.ByteMessageConverter;
import io.ddaaniel.internal.support.serializer.converters.InputStreamMessageConverter;
import io.ddaaniel.internal.support.serializer.converters.JsonMessageConverter;
import io.ddaaniel.internal.support.serializer.converters.StringMessageSerializer;


public class SerializationManager {

    private final List<MessageSerializer> converters = new ArrayList<>();

    public SerializationManager() {
		this.converters.add(new ByteMessageConverter());
        this.converters.add(new InputStreamMessageConverter());
        this.converters.add(new StringMessageSerializer());
        this.converters.add(new JsonMessageConverter());
    }

    public SerializedResult convert(Object body, String contentType) throws IOException {
        if (body == null) {
            return new SerializedResult(new byte[0], "text/plain");
        }

        Class<?> bodyClass = body.getClass();

        for (MessageSerializer converter : converters) {
			if (converter.canWrite(bodyClass, contentType)) {
				byte[] bytes = converter.write(body);
				String typeContent = (contentType != null && !contentType.isBlank())
					? contentType
					: converter.getContentType();

				return new SerializedResult(bytes, typeContent);
			}
		}

		throw new IllegalArgumentException("Nenhum MessageSerializer encontrado para o tipo: " + bodyClass.getName());
	}

	public record SerializedResult(byte[] data, String contentType) {}
}
