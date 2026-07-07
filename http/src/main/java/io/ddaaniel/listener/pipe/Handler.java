package io.ddaaniel.listener.pipe;


import io.ddaaniel.internal.parser.reader.HttpServletRequest;
import io.ddaaniel.internal.parser.writer.ServletWriter;


@FunctionalInterface
public interface Handler {
    void get(HttpServletRequest m, ServletWriter w);
}


