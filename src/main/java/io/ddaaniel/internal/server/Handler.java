package io.ddaaniel.internal.server;


import io.ddaaniel.internal.parser.request.message.Request;
import io.ddaaniel.internal.parser.response.Response;


@FunctionalInterface
public interface Handler {
    void handle(Response writer, Request req);
}


