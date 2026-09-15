package io.ddaaniel.core.filter;

import java.util.List;
import io.ddaaniel.listener.internal.codec.reader.HttpServletRequest;
import io.ddaaniel.listener.internal.codec.writer.HttpServletResponse;

public class DefaultHttpFilterChain implements FilterChain {

    private final Filter[] filters;
    private final FilterTarget target;
    private int pos = 0;

    @FunctionalInterface
    public interface FilterTarget {
        void execute(HttpServletRequest request, HttpServletResponse response) throws Exception;
    }

    public DefaultHttpFilterChain(List<Filter> filters, FilterTarget target) {
        this.filters = filters.toArray(new Filter[0]);
        this.target = target;
    }

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (this.pos < this.filters.length) {
            Filter filter = this.filters[this.pos++];
            filter.doFilter(request, response, this);
        } else {
            this.target.execute(request, response);
        }
    }
}
