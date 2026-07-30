package io.ddaaniel.core.filter;

import io.ddaaniel.listener.internal.parser.reader.DefaultHttpServletRequest;
import io.ddaaniel.listener.internal.parser.writer.DefaultHttpServletResponse;

public abstract class OncePerRequestFilter implements Filter {

    public static final String ALREADY_FILTERED_SUFFIX = ".FILTERED";

    @Override
    public final void doFilter(DefaultHttpServletRequest request, DefaultHttpServletResponse response, FilterChain chain) throws Exception {
        String attributeName = getClass().getName() + ALREADY_FILTERED_SUFFIX;

        boolean hasAlreadyFiltered = request.getAttribute(attributeName) != null;

        if (hasAlreadyFiltered) {
            chain.doFilter(request, response);
        } else {
            request.setAttribute(attributeName, Boolean.TRUE);
            try {
                doFilterInternal(request, response, chain);
            } finally {
                request.removeAttribute(attributeName);
            }
        }
    }

    protected abstract void doFilterInternal(DefaultHttpServletRequest request, DefaultHttpServletResponse response, FilterChain chain) throws Exception;
}
