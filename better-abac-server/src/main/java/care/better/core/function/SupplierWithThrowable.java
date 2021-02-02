package care.better.core.function;

/**
 * @author Rok Lenarcic
 */
@FunctionalInterface
public interface SupplierWithThrowable<T, E extends Throwable> {
    /**
     * Gets a result.
     *
     * @return a result
     */
    T getOrThrow() throws E;
}
