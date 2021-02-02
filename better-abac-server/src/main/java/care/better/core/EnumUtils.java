package care.better.core;

import lombok.NonNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Bostjan Vester
 * @author Andrej Dolenc
 */
public interface EnumUtils {
    static <E extends Enum<E>> E findEnumByProperty(
            @NonNull final Class<E> enumType,
            @NonNull final Function<E, ?> propertySupplier,
            final Object value) {
        return findEnumByProperty(enumType, propertySupplier, value, false);
    }

    @SuppressWarnings({"BooleanParameter", "ThrowInsideCatchBlockWhichIgnoresCaughtException"})
    static <E extends Enum<?>> E findEnumByProperty(
            @NonNull final Class<E> enumType,
            @NonNull final Function<E, ?> propertySupplier,
            final Object value,
            final boolean returnNullOnMissing) {
        if (value == null) {
            if (returnNullOnMissing) {
                return null;
            }
            throw new IllegalArgumentException();
        }

        try {
            return findEnumByPredicate(enumType, e -> Objects.equals(value, propertySupplier.apply(e)), returnNullOnMissing);
        }
        catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Could not find " + enumType.getSimpleName() + " by value [" + value + "]!");
        }
    }

    static <E extends Enum<?>> E findEnumByPredicate(
            @NonNull final Class<E> enumType,
            @NonNull final Predicate<E> predicate,
            final boolean returnNullOnMissing) {
        final Optional<E> found = Stream.of(enumType.getEnumConstants())
                .filter(predicate)
                .findFirst();
        return
                returnNullOnMissing ?
                found.orElse(null) :
                found.orElseThrow(() -> new IllegalArgumentException("Could not find " + enumType.getSimpleName() + " by given predicate!"));
    }

    static <E extends Enum<?>> List<E> findEnumsByPredicate(
            @NonNull final Class<E> enumType,
            @NonNull final Predicate<E> predicate) {
        return Stream.of(enumType.getEnumConstants()).filter(predicate).collect(Collectors.toList());
    }
}
