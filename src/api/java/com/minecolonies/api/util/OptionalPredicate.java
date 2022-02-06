package com.minecolonies.api.util;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A predicate that can return success, failure, or undetermined.
 */
@FunctionalInterface
public interface OptionalPredicate<T>
{
    /**
     * Evaluates this predicate on the given argument.
     * @param t the input argument
     * @return true, false, or empty
     */
    @NotNull
    Optional<Boolean> test(T t);

    /**
     * A predicate that always returns empty.
     * @param <T> the input type
     * @return empty
     */
    @NotNull
    static<T> OptionalPredicate<T> empty()
    {
        return t -> Optional.empty();
    }

    /**
     * Turns an OptionalPredicate into a regular {@link Predicate} by
     * providing a default value returned when empty.
     *
     * @param fallback the value to return when empty
     * @return a predicate that returns the original or fallback result
     */
    @NotNull
    default Predicate<T> orElse(final boolean fallback)
    {
        return t -> test(t).orElse(fallback);
    }

    /**
     * If this predicate fails to produce a result, tries the next
     * one instead.
     *
     * @param other the other predicate to try
     * @return the result from this predicate, or the result of the other if this returns empty
     */
    @NotNull
    default OptionalPredicate<T> combine(@NotNull final OptionalPredicate<T> other)
    {
        return t -> combine(test(t), () -> other.test(t));
    }

    /**
     * Combines two optional values, with short-circuiting.
     * Similar to {@link Optional#orElseGet(Supplier)} except that
     * the final result may still be empty.
     *
     * @param a   the first optional value
     * @param b   a supplier to calculate the second optional value
     * @param <X> the value type
     * @return    the first value, if present; otherwise calculates & returns the second value
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static<X> Optional<X> combine(@NotNull Optional<X> a, @NotNull Supplier<Optional<X>> b)
    {
        return a.isPresent() ? a : b.get();
    }
}
