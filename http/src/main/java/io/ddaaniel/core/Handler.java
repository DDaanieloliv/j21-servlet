package io.ddaaniel.core;


import io.ddaaniel.internal.parser.reader.DefaultHttpServletRequest;
import io.ddaaniel.internal.parser.writer.DefaultServletWriter;


@FunctionalInterface
public interface Handler {
    void get(DefaultHttpServletRequest m, DefaultServletWriter w);
}


