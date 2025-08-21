package com.minecolonies.core.datalistener.util;

/**
 * Mapping result class for returning the entry + warnings.
 *
 * @param success whether the entry was successfully parsed or not.
 * @param item    the entry, if successfully parsed.
 * @param reason  the reason when failed.
 * @param <T>     the type of entry.
 */
public record MappingResult<T>(
    boolean success,
    T item,
    String reason)
{
    /**
     * Create an OK mapping result, giving the item entry.
     *
     * @param item the entry instance.
     * @param <T>  the type of entry.
     * @return the mapping result instance.
     */
    public static <T> MappingResult<T> ok(final T item)
    {
        return new MappingResult<>(true, item, null);
    }

    /**
     * Create a FAIL mapping result, giving a failure reason.
     *
     * @param reason the failure reason.
     * @param <T>    the type of entry.
     * @return the mapping result instance.
     */
    public static <T> MappingResult<T> fail(final String reason)
    {
        return new MappingResult<>(false, null, reason);
    }
}
