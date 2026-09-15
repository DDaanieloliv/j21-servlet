package io.ddaaniel.core;

import io.ddaaniel.listener.internal.codec.reader.HttpServletRequest;
import io.ddaaniel.listener.internal.codec.writer.HttpServletResponse;

@FunctionalInterface
public interface Handler {
    void get(HttpServletRequest req, HttpServletResponse res) throws Exception;
}
