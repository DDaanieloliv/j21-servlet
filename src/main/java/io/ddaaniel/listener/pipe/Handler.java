package io.ddaaniel.listener.pipe;


import io.ddaaniel.internal.parser.request.mapper.Request;
import io.ddaaniel.internal.parser.request.response.Response;


@FunctionalInterface
public interface Handler {
    void handle(Request req, Response res);
}


