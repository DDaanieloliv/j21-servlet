package io.ddaaniel.listener.pipe.routing;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import io.ddaaniel.internal.support.httpEntity.ResponseEntity;
import io.ddaaniel.internal.support.httpEntity.httpHeaders.HttpHeaders;
import io.ddaaniel.internal.support.httpStatus.HttpStatus;




public class Router {
    private final Map<String, Supplier<Object>> compiledTable;

    @SuppressWarnings("unchecked")
    public Router() {
		// this.compiledTable = io.ddaaniel.generated.RouteTable.table();
        try {
            Class<?> table = Class.forName("io.ddaaniel.generated.RouteTable");
            Method method = table.getMethod("table");
            this.compiledTable = (Map<String, Supplier<Object>>) method.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException(" -> Error on initialize the table router: ", e);
        }
    }

	public Optional<ResponseEntity<?>>dispatch(String targetPath) throws Exception {
		if (targetPath.isEmpty() || targetPath.isEmpty()) {
			System.err.println(" -> Error to obtain the request-target, target should contain a value ");
			return Optional.empty();
		}
		Supplier<Object> routeAction = compiledTable.get(targetPath);
		if (routeAction == null) {
			return Optional.empty();
		}

		Object rawResult = routeAction.get();
		if (rawResult instanceof ResponseEntity) {
			return Optional.of((ResponseEntity<?>) rawResult);
		}

		String bodyText = (rawResult != null) ? rawResult.toString() : "";
		var header = new HttpHeaders();
		header.set("Content-Length", String.valueOf(bodyText.getBytes().length));
		header.set("Connection", "close");
		header.set("Content-Type", "text/plain");
		var res = new ResponseEntity<>(bodyText, header, HttpStatus.OK);
		return Optional.of(res);
	}
}
