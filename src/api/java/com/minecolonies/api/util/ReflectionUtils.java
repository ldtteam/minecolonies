package com.minecolonies.api.util;

import com.google.common.reflect.TypeToken;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility class with methods regarding reflection.
 */
public final class ReflectionUtils
{

    /**
     * Method to get all Super types of a given Class.
     *
     * @param token The type to get the Supertypes for.
     * @param <T>   The type to get the super types for.
     * @return A set with the super types of the given type.
     */
    @SuppressWarnings("unchecked")
    public static <T> Set<TypeToken> getSuperClasses(TypeToken<T> token)
    {
        HashSet<TypeToken> directSet = new HashSet<>(token.getTypes());
        HashSet<TypeToken> resultingSet = new HashSet<>();

        directSet.forEach(t ->
        {
            resultingSet.add(t);
            resultingSet.add(TypeToken.of(t.getRawType()));
        });

        return resultingSet;
    }
}
