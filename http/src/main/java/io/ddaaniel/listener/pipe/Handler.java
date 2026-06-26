package io.ddaaniel.listener.pipe;


import io.ddaaniel.internal.parser.request.Request;
import io.ddaaniel.internal.parser.response.Response;


@FunctionalInterface
public interface Handler {
    void handle(Request req, Response res);
}


