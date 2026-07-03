package io.ddaaniel.listener.pipe;


import io.ddaaniel.internal.parser.reader.ServletReader;
import io.ddaaniel.internal.parser.writer.ServletWriter;


@FunctionalInterface
public interface Handler {
    void lock(ServletReader r, ServletWriter w);
}


