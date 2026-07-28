package io.ddaaniel.core.filter;

import io.ddaaniel.internal.parser.reader.DefaultHttpServletRequest;
import io.ddaaniel.internal.parser.writer.DefaultHttpServletResponse;

public interface FilterChain {
    void doFilter(DefaultHttpServletRequest request, DefaultHttpServletResponse response) throws Exception;
}
