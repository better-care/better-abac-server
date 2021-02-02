package care.better.core.function;

/**
 * @author Rok Lenarcic
 */
@FunctionalInterface
public interface RunnableWithThrowable<E extends Throwable> {
    void runOrThrow() throws E;
}
