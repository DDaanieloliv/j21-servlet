package io.ddaaniel.core;


import io.ddaaniel.listener.internal.parser.reader.DefaultHttpServletRequest;
import io.ddaaniel.listener.internal.parser.writer.DefaultHttpServletResponse;


@FunctionalInterface
public interface Handler {
    void get(DefaultHttpServletRequest req, DefaultHttpServletResponse res) throws Exception;
}
