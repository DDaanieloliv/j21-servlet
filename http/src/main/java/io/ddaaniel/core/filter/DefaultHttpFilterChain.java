package io.ddaaniel.core.filter;

import java.util.List;
import io.ddaaniel.internal.parser.reader.DefaultHttpServletRequest;
import io.ddaaniel.internal.parser.writer.DefaultHttpServletResponse;

public class DefaultHttpFilterChain implements FilterChain {

    private final Filter[] filters;
    private final FilterTarget target;
    private int pos = 0;

    @FunctionalInterface
    public interface FilterTarget {
        void execute(DefaultHttpServletRequest request, DefaultHttpServletResponse response) throws Exception;
    }

    public DefaultHttpFilterChain(List<Filter> filters, FilterTarget target) {
        this.filters = filters.toArray(new Filter[0]);
        this.target = target;
    }

    @Override
    public void doFilter(DefaultHttpServletRequest request, DefaultHttpServletResponse response) throws Exception {
        if (this.pos < this.filters.length) {
            Filter filter = this.filters[this.pos++];
            filter.doFilter(request, response, this);
        } else {
            this.target.execute(request, response);
        }
    }
}
