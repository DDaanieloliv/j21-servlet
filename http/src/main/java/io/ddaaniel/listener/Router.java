package io.ddaaniel.listener;

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
import io.ddaaniel.internal.parser.reader.DefaultHttpServletRequest;
import io.ddaaniel.internal.support.serializer.SerializationManager;



public class Router {

	private static final SerializationManager SERIALIZER = new SerializationManager();

    private final Map<String, Supplier<Object>> compiledTable;

	private static final Logger log = Logger.getLogger(Router.class.getName());

    @SuppressWarnings("unchecked")
    public Router() {
		/// this.compiledTable = io.ddaaniel.generated.RouteTable.table();
        try {
            Class<?> table = Class.forName("io.ddaaniel.generated.RouteTable");
            Method method = table.getMethod("table");
            this.compiledTable = (Map<String, Supplier<Object>>) method.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException(" -> Error on initialize the table router: ", e);
        }
    }

	public Optional<ResponseEntity<?>>dispatch(DefaultHttpServletRequest message) throws Exception {
		if (message.method() == null || message.uri() == null || message.method().isEmpty() || message.uri().isEmpty()) {
			if (log.isLoggable(Level.FINE)) log.log(Level.FINE, " -> Error to obtain the routing arguments ");
			return Optional.empty();
		}

		String routeKey = message.method().toUpperCase() + " " + message.uri();
		Supplier<Object> routeAction = compiledTable.get(routeKey);

		if (routeAction == null) {
			if (log.isLoggable(Level.FINE)) log.log(Level.FINE, " -> no route-action found to respective route-key: ", routeKey );
			return Optional.empty();
		}

		Object rawResult = routeAction.get();

		Object body = rawResult;
        HttpHeaders headers = new HttpHeaders();
        HttpStatusCode status = HttpStatus.OK;

        if (rawResult instanceof ResponseEntity<?> rEntity) {
            body = rEntity.getBody();
            if (rEntity.getHeaders() != null) {
                headers = rEntity.getHeaders();
            }
            if (rEntity.getStatusCode() != null) {
                status = rEntity.getStatusCode();
            }
        }

		String existingContentType = headers.getFirst("Content-Type");

        var serialized = SERIALIZER.convert(body, existingContentType);

        ensureConnectionHeader(message, headers);
        
        if (headers.get("Content-Type") == null) {
            headers.set("Content-Type", serialized.contentType());
        }

        headers.set("Content-Length", String.valueOf(serialized.data().length));

        if (log.isLoggable(Level.FINE)) {
            log.log(Level.INFO, " -> routing successfully completed for route-key: [{0}] ", routeKey);
        }

        return Optional.of(new ResponseEntity<>(serialized.data(), headers, status));
	}


	private void ensureConnectionHeader(DefaultHttpServletRequest message, HttpHeaders headers) {
		if (headers.get("Connection") == null) {
			boolean shouldClose = "close".equalsIgnoreCase(message.headers().getFirst("Connection"));
			headers.set("Connection", shouldClose ? "close" : "keep-alive");
		}
	}
}
