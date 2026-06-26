package io.ddaaniel.listener.pipe;


import io.ddaaniel.internal.parser.request.RequestHandler;
import io.ddaaniel.internal.parser.request.response.Response;


@FunctionalInterface
public interface Handler {
    void handle(RequestHandler req, Response res);
}


