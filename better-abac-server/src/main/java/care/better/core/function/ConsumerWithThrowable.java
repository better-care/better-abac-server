package com.marand.core.function;

/**
 * @author Rok Lenarcic
 */
@FunctionalInterface
public interface ConsumerWithThrowable<T, E extends Throwable> {
    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    void acceptOrThrow(T t) throws E;
}