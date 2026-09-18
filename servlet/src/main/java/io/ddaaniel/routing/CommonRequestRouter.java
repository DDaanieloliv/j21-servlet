package io.ddaaniel.routing;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.ddaaniel.core.httpEntity.ResponseEntity;
import io.ddaaniel.core.httpEntity.httpHeaders.HttpHeaders;
import io.ddaaniel.core.httpStatus.HttpStatus;
import io.ddaaniel.core.httpStatus.HttpStatusCode;
import io.ddaaniel.listener.internal.codec.reader.HttpServletRequest;
import io.ddaaniel.listener.internal.support.serializer.SerializationManager;
import io.ddaaniel.listener.internal.support.serializer.SerializationManager.SerializedResult;



public class CommonRequestRouter {

	private static final SerializationManager SERIALIZER = new SerializationManager();

    private final Map<String, Supplier<Object>> compiledTable;

	private static final Logger log = Logger.getLogger(CommonRequestRouter.class.getName());

    @SuppressWarnings("unchecked")
    public CommonRequestRouter() {
		/// this.compiledTable = io.ddaaniel.generated.RouteTable.table();
        try {
            Class<?> table = Class.forName("io.ddaaniel.generated.RouteTable");
            Method method = table.getMethod("table");
            this.compiledTable = (Map<String, Supplier<Object>>) method.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException(" -> Error on initialize the table router: ", e);
        }
    }


	public Optional<ResponseEntity<?>>dispatch(HttpServletRequest message) throws Exception {

		String routeKey = message.method().toUpperCase() + " " + message.uri();
		Supplier<Object> routeAction = compiledTable.get(routeKey);

		if (routeAction == null) {
			if (log.isLoggable(Level.FINE)) 
				log.log(Level.FINE, " -> no route-action found to respective route-key: ", routeKey );
			return Optional.empty();
		}

		Object rawResult = routeAction.get();
        HttpHeaders headers = new HttpHeaders();
        HttpStatusCode status = HttpStatus.OK;

        if (rawResult instanceof ResponseEntity<?> rEntity) {
            rawResult = rEntity.getBody();
            if (rEntity.getHeaders() != null) headers = rEntity.getHeaders();
            if (rEntity.getStatusCode() != null) status = rEntity.getStatusCode();
        }

        SerializedResult serialized = SERIALIZER.convert(rawResult, headers.getFirst(HttpHeaders.HttpHeadersNames.CONTENT_TYPE));
        if (headers.get(HttpHeaders.HttpHeadersNames.CONTENT_TYPE) == null && serialized.hasContent()) {
			headers.set(HttpHeaders.HttpHeadersNames.CONTENT_TYPE, serialized.contentType());
		}
		headers.set(HttpHeaders.HttpHeadersNames.CONTENT_LENGTH, String.valueOf(serialized.data().length));

        return Optional.of(new ResponseEntity<>(serialized.data(), headers, status));
	}
}
