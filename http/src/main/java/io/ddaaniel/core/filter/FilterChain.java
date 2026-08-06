package io.ddaaniel.core.filter;

import io.ddaaniel.listener.internal.codec.reader.DefaultHttpServletRequest;
import io.ddaaniel.listener.internal.codec.writer.DefaultHttpServletResponse;

public interface FilterChain {
    void doFilter(DefaultHttpServletRequest request, DefaultHttpServletResponse response) throws Exception;
}
