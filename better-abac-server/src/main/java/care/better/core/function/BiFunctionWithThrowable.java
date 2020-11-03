package com.marand.core.function;

/**
 * @author Rok Lenarcic
 */
@FunctionalInterface
public interface BiFunctionWithThrowable<T, U, R, E extends Throwable> {
    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param u the second function argument
     *
     * @return the function result
     */
    R applyOrThrow(T t, U u) throws E;
}