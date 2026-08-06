package io.ddaaniel.core;


import io.ddaaniel.listener.internal.codec.reader.DefaultHttpServletRequest;
import io.ddaaniel.listener.internal.codec.writer.DefaultHttpServletResponse;


@FunctionalInterface
public interface Handler {
    void get(DefaultHttpServletRequest req, DefaultHttpServletResponse res) throws Exception;
}
