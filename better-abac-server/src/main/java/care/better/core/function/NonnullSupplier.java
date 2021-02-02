package care.better.core.function;

import lombok.NonNull;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Bostjan Vester
 */
@FunctionalInterface
public interface NonnullSupplier<T> extends Supplier<T> {
    @Override
    @NonNull
    default T get() {
        return Objects.requireNonNull(getNonnull());
    }

    @NonNull T getNonnull();
}
