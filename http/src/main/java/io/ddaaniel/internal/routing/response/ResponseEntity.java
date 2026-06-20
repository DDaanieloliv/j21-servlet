package io.ddaaniel.internal.routing.response;

import java.util.HashMap;
import java.util.Map;
import io.ddaaniel.internal.parser.request.response.status.ResponseStatusCode;

public class ResponseEntity<T> {
    private final ResponseStatusCode status;
    private final Map<String, String> headers;
    private final T body;

    public ResponseEntity(ResponseStatusCode status, Map<String, String> headers, T body) {
        this.status = status;
        this.headers = headers != null ? headers : new HashMap<>();
        this.body = body;
    }

    public ResponseStatusCode getStatus() { return status; }
    public Map<String, String> getHeaders() { return headers; }
    public T getBody() { return body; }

    public static <T> ResponseEntity<T> ok(T body) {
        return new ResponseEntity<>(ResponseStatusCode.STATUS_OK, new HashMap<>(), body);
    }

    public static <T> ResponseEntity<T> status(ResponseStatusCode status) {
        return new ResponseEntity<>(status, new HashMap<>(), null);
    }
}
