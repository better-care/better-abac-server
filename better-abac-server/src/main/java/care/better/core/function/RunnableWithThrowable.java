package com.marand.core.function;

/**
 * @author Rok Lenarcic
 */
@FunctionalInterface
public interface RunnableWithThrowable<E extends Throwable> {
    void runOrThrow() throws E;
}
