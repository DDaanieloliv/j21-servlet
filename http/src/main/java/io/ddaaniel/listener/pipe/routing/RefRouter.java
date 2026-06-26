package io.ddaaniel.listener.pipe.routing;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Supplier;

import io.ddaaniel.internal.httpEntity.entities.ResponseEntity;



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
		return ResponseEntity.ok(bodyText);
	}
}
