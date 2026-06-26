package io.ddaaniel.listener.pipe.routing;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Supplier;


import io.ddaaniel.internal.httpEntity.entities.ResponseEntity;
import io.ddaaniel.internal.httpEntity.entities.httpHeaders.HttpHeaders;
import io.ddaaniel.internal.httpStatus.HttpStatus;



public class RefRouter {
    private final Map<String, Supplier<Object>> compiledTable;

    @SuppressWarnings("unchecked")
    public RefRouter() {
        try {
            Class<?> table = Class.forName("io.ddaaniel.generated.RouteTable");
            Method method = table.getMethod("table");
            this.compiledTable = (Map<String, Supplier<Object>>) method.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException("Error on initialize the table router", e);
        }
    }

	public ResponseEntity<?> dispatch(String targetPath) throws Exception {
		Supplier<Object> routeAction = compiledTable.get(targetPath);
		if (routeAction == null) return null;

		Object rawResult = routeAction.get();
		if (rawResult instanceof ResponseEntity) {
			return (ResponseEntity<?>) rawResult;
		}

		String bodyText = (rawResult != null) ? rawResult.toString() : "";
		var header = new HttpHeaders();
		header.set("Content-Length", String.valueOf(bodyText.getBytes().length));
		header.set("Connection", "close");
		header.set("Content-Type", "text/plain");
		var res = new ResponseEntity<>(bodyText, header, HttpStatus.OK);
		return res;
	}
}
