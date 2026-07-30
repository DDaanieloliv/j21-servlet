package io.ddaaniel.core.filter;

import io.ddaaniel.listener.internal.parser.reader.DefaultHttpServletRequest;
import io.ddaaniel.listener.internal.parser.writer.DefaultHttpServletResponse;

public interface FilterChain {
    void doFilter(DefaultHttpServletRequest request, DefaultHttpServletResponse response) throws Exception;
}
