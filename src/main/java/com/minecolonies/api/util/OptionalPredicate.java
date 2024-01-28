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
     * Creates a non-empty optional predicate.
     * @param predicate the input predicate
     * @param <T> the input type
     * @return a non-empty optional predicate
     */
    @NotNull
    static<T> OptionalPredicate<T> of(@NotNull final Predicate<T> predicate)
    {
        return t -> Optional.of(predicate.test(t));
    }

    /**
     * Convert the given predicate into an optional success.
     * @param predicate the input predicate
     * @return a predicate which returns true if the input predicate returns true,
     * or empty if the input predicate returns false.
     * @param <T> the input type
     */
    @NotNull
    static<T> OptionalPredicate<T> passIf(@NotNull final Predicate<T> predicate)
    {
        return t -> predicate.test(t) ? Optional.of(true) : Optional.empty();
    }

    /**
     * Convert the given predicate into an optional failure.
     * @param predicate the input predicate
     * @return a predicate which returns false if the input predicate returns true,
     * or empty if the input predicate returns false.
     * @param <T> the input type
     */
    @NotNull
    static<T> OptionalPredicate<T> failIf(@NotNull final Predicate<T> predicate)
    {
        return t -> predicate.test(t) ? Optional.of(false) : Optional.empty();
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
     * @return    the first value, if present; otherwise calculates and returns the second value
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static<X> Optional<X> combine(@NotNull Optional<X> a, @NotNull Supplier<Optional<X>> b)
    {
        return a.isPresent() ? a : b.get();
    }

    /**
     * Negates the predicate -- where the original returns true, this returns false, and vice versa.
     * The empty result does not change.
     *
     * @return the negated predicate.
     */
    @NotNull
    default OptionalPredicate<T> negate()
    {
        return t -> test(t).map(b -> !b);
    }

    /**
     * Performs the logical AND on two optional predicates, such that:
     *  - if both sides are empty, the result is empty
     *  - if one side is empty, the result is the other side
     *  - if both sides are non-empty, the result is the logical AND
     *  - if the left side is false, the right side is not evaluated (short circuit)
     *
     * @param other the other optional predicate
     * @return the logical AND of both predicates
     */
    @NotNull
    default OptionalPredicate<T> and(@NotNull final OptionalPredicate<T> other)
    {
        return t ->
        {
            final Optional<Boolean> lhs = test(t);
            if (lhs.isPresent() && !lhs.get())
            {
                return lhs;
            }
            final Optional<Boolean> rhs = other.test(t);
            return rhs.isPresent() ? rhs : lhs;
        };
    }

    /**
     * Performs the logical OR on two optional predicates, such that:
     *  - if both sides are empty, the result is empty
     *  - if one side is empty, the result is the other side
     *  - if both sides are non-empty, the result is the logical OR
     *  - if the left side is true, the right side is not evaluated (short circuit)
     *
     * @param other the other optional predicate
     * @return the logical OR of both predicates
     */
    @NotNull
    default OptionalPredicate<T> or(@NotNull final OptionalPredicate<T> other)
    {
        return t ->
        {
            final Optional<Boolean> lhs = test(t);
            if (lhs.isPresent() && lhs.get())
            {
                return lhs;
            }
            final Optional<Boolean> rhs = other.test(t);
            return rhs.isPresent() ? rhs : lhs;
        };
    }
}
