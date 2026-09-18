package io.ddaaniel.core.filter;

import io.ddaaniel.listener.internal.codec.reader.HttpServletRequest;
import io.ddaaniel.listener.internal.codec.writer.HttpServletResponse;

@FunctionalInterface
public interface Filter {
    void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws Exception;
}
