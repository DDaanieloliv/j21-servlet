package io.ddaaniel.core.filter;

import io.ddaaniel.listener.internal.codec.reader.DefaultHttpServletRequest;
import io.ddaaniel.listener.internal.codec.writer.DefaultHttpServletResponse;

@FunctionalInterface
public interface Filter {
    void doFilter(DefaultHttpServletRequest request, DefaultHttpServletResponse response, FilterChain chain) throws Exception;
}
