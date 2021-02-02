package care.better.core.function;

/**
 * @author Rok Lenarcic
 */
@FunctionalInterface
public interface FunctionWithThrowable<T, R, E extends Throwable> {
    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     *
     * @return the function result
     */
    R applyOrThrow(T t) throws E;
}