package io.ddaaniel.core.filter;

import io.ddaaniel.internal.parser.reader.DefaultHttpServletRequest;
import io.ddaaniel.internal.parser.writer.DefaultHttpServletResponse;

@FunctionalInterface
public interface Filter {
    void doFilter(DefaultHttpServletRequest request, DefaultHttpServletResponse response, FilterChain chain) throws Exception;
}
