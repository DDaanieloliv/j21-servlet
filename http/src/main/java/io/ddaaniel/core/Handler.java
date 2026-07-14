package io.ddaaniel.core;


import io.ddaaniel.internal.parser.reader.DefaultHttpServletRequest;
import io.ddaaniel.internal.parser.writer.HttpServletWriter;


@FunctionalInterface
public interface Handler {
    void get(DefaultHttpServletRequest m, HttpServletWriter w);
}


