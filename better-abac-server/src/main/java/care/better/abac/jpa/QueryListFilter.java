package care.better.abac.jpa;

import com.google.common.collect.ImmutableList;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Matic Ribic
 */
public class QueryListFilter<T> {
    private final List<T> filter;
    private final boolean enabled;

    public QueryListFilter(Collection<T> filter, boolean enabled) {
        this.filter = filter != null ? ImmutableList.copyOf(filter) : Collections.emptyList();
        this.enabled = enabled;
    }

    public static <U> QueryListFilter<U> of(Collection<U> filter) {
        return of(filter, false);
    }

    public static <U> QueryListFilter<U> of(Collection<U> filter, boolean emptyAsAll) {
        return new QueryListFilter<>(filter, emptyAsAll ? !CollectionUtils.isEmpty(filter) : filter != null);

    }

    public List<T> getFilter() {
        return filter;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
