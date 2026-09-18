package io.ddaaniel.core.filter;

import io.ddaaniel.listener.internal.codec.reader.HttpServletRequest;
import io.ddaaniel.listener.internal.codec.writer.HttpServletResponse;

public interface FilterChain {
    void doFilter(HttpServletRequest request, HttpServletResponse response) throws Exception;
}
