package care.better.abac.jpa.repo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Matic Ribic
 */
public final class PersistenceUtil {

    public static final int PARTITION_SIZE = 1000;

    private PersistenceUtil() {
    }

    public static <T> List<T> wrapInConditionToList(Collection<T> inCondition) {
        return inCondition instanceof List ? (List<T>)inCondition : new ArrayList<>(inCondition);
    }
}
