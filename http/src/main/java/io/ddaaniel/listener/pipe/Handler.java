package io.ddaaniel.listener.pipe;


import io.ddaaniel.internal.parser.reader.HttpServletRequest;
import io.ddaaniel.internal.parser.writer.DefaultServletWriter;


@FunctionalInterface
public interface Handler {
    void get(HttpServletRequest m, DefaultServletWriter w);
}


