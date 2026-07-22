package io.ddaaniel.listener;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.ddaaniel.core.httpEntity.ResponseEntity;
import io.ddaaniel.core.httpEntity.httpHeaders.HttpHeaders;
import io.ddaaniel.core.httpStatus.HttpStatus;
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
			if (log.isLoggable(Level.FINE)) log.log(Level.FINE, " -> No route-action found to respective route-key: ", routeKey );
			return Optional.empty();
		}

		Object rawResult = routeAction.get();
		if (rawResult instanceof ResponseEntity rEntity) {
			if (log.isLoggable(Level.FINE)) log.log(Level.FINE, " -> Routing successfully completed, obtained response as Object ");
			ensureConnectionHeader(message, rEntity.getHeaders());
			return Optional.of(rEntity);
		}

		if (rawResult instanceof InputStream) {
			var header = new HttpHeaders();
			ensureConnectionHeader(message, header);
			header.set("Content-Type", "application/octet-stream");
			header.setContentLength(((InputStream) rawResult).available());

			var res = new ResponseEntity<>(rawResult, header, HttpStatus.OK);

			if (log.isLoggable(Level.FINE)) log.log(Level.FINE, " -> Routing successfully completed, obtained response as InputStream ");
			return Optional.of(res);
		}

		var result = SERIALIZER.convert(rawResult, null);

		var header = new HttpHeaders();
		header.set("Content-Length", String.valueOf(result.data().length));
		ensureConnectionHeader(message, header);
		header.set("Content-Type", "text/plain");
		var res = new ResponseEntity<>(result.data(), header, HttpStatus.OK);

		if (log.isLoggable(Level.FINE)) log.log(Level.FINE, " -> Routing successfully completed, obtained response as Text ");
		return Optional.of(res);
	}

	private void ensureConnectionHeader(DefaultHttpServletRequest message, HttpHeaders headers) {
		if (headers.get("Connection") == null) {
			boolean shouldClose = "close".equalsIgnoreCase(message.headers().getFirst("Connection"));
			headers.set("Connection", shouldClose ? "close" : "keep-alive");
		}
	}
}
