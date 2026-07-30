package io.ddaaniel.core.filter;

import io.ddaaniel.listener.internal.parser.reader.DefaultHttpServletRequest;
import io.ddaaniel.listener.internal.parser.writer.DefaultHttpServletResponse;

@FunctionalInterface
public interface Filter {
    void doFilter(DefaultHttpServletRequest request, DefaultHttpServletResponse response, FilterChain chain) throws Exception;
}
