package io.ddaaniel.core;


import io.ddaaniel.internal.parser.reader.DefaultHttpServletRequest;
import io.ddaaniel.internal.parser.writer.DefaultHttpServletResponse;


@FunctionalInterface
public interface Handler {
    void get(DefaultHttpServletRequest req, DefaultHttpServletResponse res) throws Exception;
}
