package io.ddaaniel.internal.server;

import java.io.OutputStream;

import io.ddaaniel.internal.parser.request.message.Request;


@FunctionalInterface
public interface Handler {
    HandlerError handle(OutputStream writer, Request req);
}


