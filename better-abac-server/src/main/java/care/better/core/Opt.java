package com.marand.core;

import com.marand.core.function.BiFunctionWithThrowable;
import com.marand.core.function.ConsumerWithThrowable;
import com.marand.core.function.FunctionWithThrowable;
import com.marand.core.function.RunnableWithThrowable;
import com.marand.core.function.SupplierWithThrowable;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Rok Lenarcic
 * @author Bostjan Vester
 */
public class Opt<T> implements Iterable<T>, Serializable {
    private static final int CHARACTERISTICS =
            Spliterator.DISTINCT | Spliterator.SIZED | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED | Spliterator.SUBSIZED;
    /**
     * Common instance for {@code none()}.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final Opt NONE = new Opt(null);
    /**
     * If non-null, the value; if null, indicates no value is present
     */
    private final T value;

    /**
     * Constructs an {@code Opt} instance.
     *
     * @implNote Generally only one empty instance, {@link Opt#NONE}, should exist per VM.
     */
    private Opt(final T value) {
        this.value = value;
    }

    /**
     * Construct a new {@code Opt} from java's {@code Optional}
     *
     * @param opt java {@code Optional} object
     * @param <T> value type
     *
     * @return new {@code Opt}
     */
    public static <T> Opt<T> from(@SuppressWarnings("OptionalUsedAsFieldOrParameterType") @NonNull final Optional<T> opt) {
        return opt.isPresent() ? of(opt.get()) : none();
    }

    /**
     * Returns an empty {@code Opt} instance.  No value is present for this
     * Opt.
     *
     * @param <T> Type of the non-existent value
     *
     * @return an empty {@code Opt}
     *
     * @apiNote Though it may be tempting to do so, avoid testing if an object is empty by comparing with {@code ==} against
     * instances returned by {@code Opt.none()}. There is no guarantee that it is a singleton. Instead, use {@link
     * #isPresent()}.
     */
    @SuppressWarnings("unchecked")
    public static <T> Opt<T> none() {
        //noinspection unchecked
        return NONE;
    }

    /**
     * Construct a new Opt from the value. If value is null, the returned {@code Opt} is the same object as
     * the one returned by {@code Opt.none()}.
     *
     * @param value value
     * @param <T> value type
     *
     * @return an Opt
     */
    public static <T> Opt<T> of(final T value) {
        return value == null ? none() : new Opt<>(value);
    }

    /**
     * Construct a new Opt from the {@link Iterable} value. If value is null or empty,
     * the returned {@code Opt} is the same object as the one returned by {@code Opt.none()}.
     *
     * @param value value
     * @param <T> value type
     *
     * @return an Opt
     */
    public static <T extends Iterable<?>> Opt<T> ofNonEmpty(final T value) {
        return value == null || !value.iterator().hasNext() ? none() : new Opt<>(value);
    }

    /**
     * Construct a new Opt from an array value. If value is null or empty,
     * the returned {@code Opt} is the same object as the one returned by {@code Opt.none()}.
     *
     * @param value value
     * @param <T> value type
     *
     * @return an Opt
     */
    public static <T> Opt<T[]> ofNonEmpty(final T[] value) {
        return value == null || Array.getLength(value) == 0 ? none() : new Opt<>(value);
    }

    /**
     * Construct a new Opt from a {@link String} value. If value is null or empty,
     * the returned {@code Opt} is the same object as the one returned by {@code Opt.none()}.
     *
     * @param value value
     *
     * @return an Opt
     */
    public static Opt<String> ofNonEmpty(final String value) {
        return value == null || value.isEmpty() ? none() : new Opt<>(value);
    }

    /**
     * Construct a new Opt from a {@link String} value. If value is null or blank,
     * the returned {@code Opt} is the same object as the one returned by {@code Opt.none()}.
     *
     * @param value value
     *
     * @return an Opt
     */
    public static Opt<String> ofNonBlank(final String value) {
        return StringUtils.isNotBlank(value) ? of(value) : none();
    }

    /**
     * Runs the supplier function and if it results in a null value or a {@link NullPointerException}, {@link IndexOutOfBoundsException} or {@link NoSuchElementException},
     * it will return an absent value {@code Opt}, otherwise it will return an {@code Opt}.
     *
     * @param valueSupplier the supplier function
     * @param <T> value type
     *
     * @return an {@code Opt}
     */
    public static <T, E extends Throwable> Opt<T> resolve(@NonNull final SupplierWithThrowable<T, E> valueSupplier) throws E {
        //noinspection ProhibitedExceptionCaught
        try {
            return of(valueSupplier.getOrThrow());
        }
        catch (final NullPointerException | IndexOutOfBoundsException | NoSuchElementException e) {
            return none();
        }
    }

    /**
     * Runs the combiner function on values of this and other {@code Opt}, but only if both are present.
     * Returns a new {@code Opt} from the result of the function.
     *
     * @param other other {@code Opt}
     * @param combiner combiner function
     * @param <U> other {@code Opt} type
     * @param <V> return {@code Opt} type
     *
     * @return a new {@code Opt}
     *
     * @throws NullPointerException if the combiner function is null or other {@code Opt} is null
     */
    public <U, V, E extends Throwable> Opt<V> and(
            @NonNull final Opt<U> other,
            @NonNull final BiFunctionWithThrowable<T, U, V, E> combiner)
            throws E {
        if (isPresent() && other.isPresent()) {
            @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
            final V ret = combiner.applyOrThrow(value, other.value);
            return ret == null ? none() : new Opt<>(ret);
        }
        return none();
    }

    /**
     * Runs the combiner function on values of this and other {@code Opt}, but only if both are present.
     * The supplier called only if this {@code Opt} is present.
     * Returns a new {@code Opt} from the result of the function.
     *
     * @param otherSupplier supplier of other {@code Opt}
     * @param combiner combiner function
     * @param <U> other {@code Opt} type
     * @param <V> return {@code Opt} type
     *
     * @return a new {@code Opt}
     *
     * @throws NullPointerException if the combiner function is null or supplier is null or supplier returns a null value
     */
    public <U, V, SE extends Throwable, CE extends Throwable> Opt<V> and(
            @NonNull final SupplierWithThrowable<Opt<U>, SE> otherSupplier,
            @NonNull final BiFunctionWithThrowable<T, U, V, CE> combiner) throws SE, CE {
        if (isPresent()) {
            final Opt<U> other = otherSupplier.getOrThrow();
            if (other.isPresent()) {
                @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
                final V ret = combiner.applyOrThrow(value, other.value);
                return ret == null ? none() : new Opt<>(ret);
            }
        }
        return none();
    }

    /**
     * Indicates whether some other object is "equal to" this Opt. The
     * other object is considered equal if:
     * <ul>
     * <li>it is also an {@code Opt} and;
     * <li>both instances have no value present or;
     * <li>the present values are "equal to" each other via {@code equals()}.
     * </ul>
     *
     * @param o an object to be tested for equality
     *
     * @return {code true} if the other object is "equal to" this object otherwise {@code false}
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Opt)) {
            return false;
        }

        final Opt<?> opt = (Opt<?>)o;

        //noinspection AccessingNonPublicFieldOfAnotherObject
        return Objects.equals(value, opt.value);
    }

    /**
     * If a value is present, and the value matches the given predicate,
     * return an {@code Opt} describing the value, otherwise return an
     * empty {@code Opt}.
     *
     * @param predicate a predicate to apply to the value, if present
     *
     * @return an {@code Opt} describing the value of this {@code Opt} if a value is present and the value matches the given
     * predicate, otherwise an empty {@code Opt}
     *
     * @throws NullPointerException if the predicate is null
     */
    public Opt<T> filter(@NonNull final Predicate<? super T> predicate) {
        return isPresent() && predicate.test(value) ? this : none();
    }

    /**
     * If a value is present, apply the mapping function to it, return that {@code Opt} result,
     * otherwise return an empty {@code Opt}.
     * This method is similar to {@link #map(FunctionWithThrowable)}, but the mapping function
     * returns the {@code Opt} which is not additionally wrapped.
     *
     * @param <U> The type parameter to the {@code Opt} returned by
     * @param function a mapping function to apply to the value, if present the mapping function
     *
     * @return the result of applying an {@code Opt}-bearing mapping function to the value of this {@code Opt}, if a value is
     * present, otherwise an empty {@code Opt}
     *
     * @throws NullPointerException if the mapping function is null or returns a null result
     */
    public <U, E extends Throwable> Opt<U> flatMap(@NonNull final FunctionWithThrowable<? super T, Opt<U>, E> function) throws E {
        return flatMap(function, Opt::none);
    }

    /**
     * If a value is present, apply the mapping function to it, return that {@code Opt} result,
     * otherwise return the {@code Opt} returned by the supplier.
     * This method is similar to {@link #map(FunctionWithThrowable, SupplierWithThrowable)}, but the
     * {@code Opt} objects returned by the mapping/supplier functions are not wrapped.
     *
     * @param <U> The type parameter to the {@code Opt} returned by
     * @param function a mapping function to apply to the value, if present the mapping function
     * @param supplier a supplier to use, if value is not present
     *
     * @return the result of applying an {@code Opt}-bearing mapping function to the value of this {@code Opt}, if a value is
     * present, otherwise an {@code Opt} supplied by a supplier
     *
     * @throws NullPointerException if the mapping or supplier function is null or returns a null result
     */
    public <U, E extends Throwable> Opt<U> flatMap(
            @NonNull final FunctionWithThrowable<? super T, Opt<U>, ? extends E> function,
            @NonNull final SupplierWithThrowable<Opt<U>, ? extends E> supplier
    ) throws E {
        final Opt<U> ret = isPresent() ? function.applyOrThrow(value) : supplier.getOrThrow();
        if (ret == null) {
            throw new NullPointerException("Function supplied to flatMap returned null.");
        }
        return ret;
    }

    /**
     * If a value is present, invoke the specified {@link Consumer} with the value,
     * otherwise do nothing.
     *
     * @param action block to be executed if a value is present
     *
     * @throws NullPointerException if value is present and {@code consumer} is null
     */
    @Override
    public void forEach(@NonNull final Consumer<? super T> action) {
        forEach(action::accept, () -> {});
    }

    /**
     * If a value is present, invoke the specified {@link Consumer} with the value,
     * otherwise invoke the {@link Runnable}.
     *
     * @param action block to be executed if a value is present
     * @param elseAction block to be executed if a value is not present
     *
     * @throws NullPointerException if value is present and {@code consumer} is null
     */
    public <E extends Throwable> void forEach(
            @NonNull final ConsumerWithThrowable<? super T, ? extends E> action,
            @NonNull final RunnableWithThrowable<? extends E> elseAction
    ) throws E {
        if (isPresent()) {
            action.acceptOrThrow(value);
        } else {
            elseAction.runOrThrow();
        }
    }

    /**
     * If a value is present in this {@code Opt}, returns the value,
     * otherwise returns {@code null}.
     *
     * @return the value held by this {@code Opt} or {@code null} if no value
     *
     * @see #isPresent()
     */
    public T get() {
        return value;
    }

    /**
     * Returns the hash code value of the present value, if any, or 0 (zero) if
     * no value is present.
     *
     * @return hash code value of the present value or 0 if no value is present
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    /**
     * If a value is absent, invoke the specified {@link RunnableWithThrowable} with the value,
     * otherwise do nothing.
     *
     * @param action block to be executed if a value is present
     *
     * @throws NullPointerException if {@code consumer} is null
     */
    public <E extends Throwable> Opt<T> ifAbsent(@NonNull final RunnableWithThrowable<E> action) throws E {
        if (isAbsent()) {
            action.runOrThrow();
        }
        return this;
    }

    /**
     * If a value is present, invoke the specified {@link ConsumerWithThrowable} with the value,
     * otherwise do nothing.
     *
     * @param action block to be executed if a value is present
     *
     * @throws NullPointerException if value is present and {@code consumer} is null
     */
    public <E extends Throwable> Opt<T> ifPresent(@NonNull final ConsumerWithThrowable<? super T, E> action) throws E {
        if (isPresent()) {
            action.acceptOrThrow(value);
        }
        return this;
    }

    /**
     * Return {@code true} if there is a value absent, otherwise {@code false}.
     *
     * @return {@code true} if there is a value absent, otherwise {@code false}
     */
    public boolean isAbsent() {
        return !isPresent();
    }

    /**
     * Return {@code true} if there is a value present, otherwise {@code false}.
     *
     * @return {@code true} if there is a value present, otherwise {@code false}
     */
    public boolean isPresent() {
        return value != null;
    }

    /**
     * This {@code Opt} as a {@code Iterator} of one or no elements.
     *
     * @return {@code Opt} as {@code Iterator}
     */
    @Override
    public Iterator<T> iterator() {
        if (isPresent()) {
            return new Iterator<T>() {
                private boolean hasNext = true;

                @Override
                public boolean hasNext() {
                    return hasNext;
                }

                @Override
                public T next() {
                    if (hasNext) {
                        hasNext = false;
                        return value;
                    } else {
                        throw new NoSuchElementException();
                    }
                }

                @Override
                public String toString() {
                    return "Iterator(Some(" + value + "), " + (hasNext ? "unspent" : "spent") + ")";
                }
            };
        }
        return Collections.emptyIterator();
    }

    /**
     * If a value is present, apply the provided mapping function to it,
     * and if the result is non-null, return an {@code Opt} containing the
     * result.  Otherwise return an empty {@code Opt}.
     *
     * @param <U> The type of the result of the mapping function
     * @param function a mapping function to apply to the value, if present
     *
     * @return an {@code Opt} describing the result of applying a mapping function to the value of this {@code Opt}, if a
     * value is present, otherwise an empty {@code Opt}
     *
     * @throws NullPointerException if the mapping function is null
     */
    public <U, E extends Throwable> Opt<U> map(@NonNull final FunctionWithThrowable<? super T, ? extends U, E> function) throws E {
        return isPresent() ? of(function.applyOrThrow(value)) : none();
    }

    /**
     * If a value is present, apply the provided mapping function to it,
     * and if the result is non-null, return an {@code Opt} containing the
     * result.  Otherwise invoke the supplier and if the result is non-null,
     * return an {@code Opt} containing the result. Else return {@link #none()}
     *
     * @param <U> The type of the result of the mapping functions
     * @param function a mapping function to apply to the value, if present
     * @param supplier a supplier to use, if not present
     *
     * @return an {@code Opt} describing the result of applying a mapping function to the value of this {@code Opt}, if a
     * value is present, otherwise the value provided by the supplier wrapped in {@code Opt}
     *
     * @throws NullPointerException if the mapping function is null
     */
    public <U, E extends Throwable> Opt<U> map(
            @NonNull final FunctionWithThrowable<? super T, ? extends U, ? extends E> function,
            @NonNull final SupplierWithThrowable<? extends U, ? extends E> supplier
    ) throws E {
        return of(isPresent() ? function.applyOrThrow(value) : supplier.getOrThrow());
    }

    /**
     * If a value is present in this {@code Opt}, returns this instance,
     * otherwise returns the Opt instance returned by {@code Supplier}.
     *
     * @return the {@code Opt} with present value out of the two {@code Opt}, {@link #none()} otherwise
     */
    @SuppressWarnings("unchecked")
    public <E extends Throwable> Opt<T> or(@NonNull final SupplierWithThrowable<Opt<? extends T>, E> other) throws E {
        return isPresent() ? this : (Opt<T>)other.getOrThrow();
    }

    /**
     * Return the value if present, otherwise return {@code other}.
     *
     * @param other the value to be returned if there is no value present, may be null
     *
     * @return the value, if present, otherwise {@code other}
     */
    public T orElse(final T other) {
        return isPresent() ? value : other;
    }

    /**
     * Return the value if present, otherwise invoke {@code other} and return
     * the result of that invocation.
     *
     * @param other a {@code Supplier} whose result is returned if no value is present
     *
     * @return the value if present otherwise the result of {@code other.get()}
     *
     * @throws NullPointerException if {@code other} is null
     */
    public <E extends Throwable> T orElseGet(@NonNull final SupplierWithThrowable<? extends T, E> other) throws E {
        return isPresent() ? value : other.getOrThrow();
    }

    /**
     * Return the contained value, if present, otherwise throw an exception
     * to be created by the provided supplier.
     *
     * @param <E> Type of the exception to be thrown
     * @param exceptionSupplier The supplier which will return the exception to be thrown
     *
     * @return the present value
     *
     * @throws E if there is no value present
     * @throws NullPointerException if {@code exceptionSupplier} is null
     * @apiNote A method reference to the exception constructor with an empty argument list can be used as the supplier. For
     * example, {@code IllegalStateException::new}
     */
    public <E extends Throwable> T orElseThrow(@NonNull final Supplier<? extends E> exceptionSupplier) throws E {
        if (isPresent()) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
    }

    /**
     * If a value is present in this {@code Opt}, returns this instance,
     * otherwise returns the Opt instance returned by {@code Supplier}.
     *
     * @return the {@code Opt} with present value out of the two {@code Opt}, {@link #none()} otherwise
     *
     * @throws NullPointerException if {@code other} is null
     */
    @SuppressWarnings("unchecked")
    public <E extends Throwable> Opt<T> orGet(@NonNull final SupplierWithThrowable<Opt<? extends T>, E> other) throws E {
        return isPresent() ? this : (Opt<T>)other.getOrThrow();
    }

    /**
     * This {@code Opt} as a {@code Spliterator} of one or no elements.
     *
     * @return {@code Opt} as {@code Spliterator}
     */
    @Override
    public Spliterator<T> spliterator() {
        if (isPresent()) {
            return new Spliterator<T>() {
                private int size = 1;

                @Override
                public int characteristics() {
                    return CHARACTERISTICS;
                }

                @Override
                public long estimateSize() {
                    return size;
                }

                @Override
                public void forEachRemaining(final Consumer<? super T> action) {
                    if (size != 0) {
                        size--;
                        action.accept(value);
                    }
                }

                @Override
                public Comparator<? super T> getComparator() {
                    return null;
                }

                @Override
                public long getExactSizeIfKnown() {
                    return size;
                }

                @Override
                public String toString() {
                    return "Spliterator(Some(" + value + "), " + (size == 0 ? "spent" : "unspent") + ")";
                }

                @Override
                public boolean tryAdvance(final Consumer<? super T> action) {
                    if (size == 0) {
                        return false;
                    } else {
                        size--;
                        action.accept(value);
                        return true;
                    }
                }

                @Override
                public Spliterator<T> trySplit() {
                    return null;
                }
            };
        }
        return Spliterators.emptySpliterator();
    }

    /**
     * This {@code Opt} as a {@link Stream} of one or no elements.
     *
     * @return {@code Opt} as {@link Stream}
     */
    public Stream<T> stream() {
        return isPresent() ? StreamSupport.stream(spliterator(), false) : Stream.empty();
    }

    public Optional<T> toOptional() {
        return Optional.ofNullable(value);
    }

    /**
     * Returns a non-empty string representation of this Optional suitable for
     * debugging.
     *
     * @return the string representation of this instance
     */
    @Override
    public String toString() {
        return isPresent() ? ("Some(" + value + ")") : "None";
    }

    /**
     * Changes {@code Opt} generic type and does typechecking.
     * If the type cannot be coerced returns {@link #none()}.
     * There are two type parameters to enable easier generic type handling,
     * i.e., while {@code U}, can be {@code List} (it exists as a class),
     * the type parameter {@code R} of the return can be {@code List&lt;String&gt;}, which
     * doesn't exist as a class.
     *
     * @param clazz class to try to coerce to
     * @param <U> class of the {@code clazz} parameter
     * @param <R> subclass of U, usually a generic subclass
     *
     * @return Typecast {@code this}, {@link #none()} if not possible
     */
    @SuppressWarnings("unchecked")
    public <U, R extends U> Opt<R> toType(@NonNull final Class<U> clazz) {
        //noinspection unchecked
        return value != null && clazz.isAssignableFrom(value.getClass()) ? (Opt<R>)this : none();
    }

    /**
     * Serialization resolve function that takes care of ensuring all empty {@code Opt} instances
     * are the singleton.
     *
     * @return a resolved object
     */
    private Object readResolve() throws ObjectStreamException {
        //noinspection VariableNotUsedInsideIf
        return value == null ? NONE : this;
    }
}
