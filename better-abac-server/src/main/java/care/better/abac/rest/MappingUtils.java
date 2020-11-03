package care.better.abac.rest;

import java.util.Iterator;
import java.util.Set;

/**
 * @author Bostjan Lah
 */
public class MappingUtils {
    private MappingUtils() {
    }

    @SuppressWarnings("MethodWithMultipleLoops")
    public static <T> void synchronizeSets(Set<T> source, Set<T> target) {
        for (Iterator<T> iterator = target.iterator(); iterator.hasNext(); ) {
            T next = iterator.next();
            if (!source.contains(next)) {
                iterator.remove();
            }
        }
        for (T t : source) {
            if (!target.contains(t)) {
                target.add(t);
            }
        }
    }
}
