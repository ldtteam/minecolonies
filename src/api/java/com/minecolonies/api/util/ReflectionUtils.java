package com.minecolonies.api.util;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class with methods regarding reflection.
 */
public final class ReflectionUtils
{

    /**
     * Private constructor to hide the implicit public one.
     */
    private ReflectionUtils()
    {
    }

    /**
     * Method to get all Super types of a given Class.
     *
     * @param clazz The type to get the Supertypes for.
     * @param <T>   The type to get the super types for.
     * @return A set with the super types of the given type.
     */
    @SuppressWarnings("unchecked")
    public static <T> Set<Class> getSuperClasses(final Class<T> clazz)
    {
        final TypeToken<T> token = new TypeToken() {};
        final HashSet<TypeToken> directSet = new HashSet<>(token.getTypes());
        final HashSet<TypeToken> resultingSet = new HashSet<>();

        directSet.forEach(t ->
        {
            resultingSet.add(t);
            resultingSet.add(TypeToken.of(t.getRawType()));
        });

        return resultingSet.stream().map(t -> t.getRawType()).collect(Collectors.toSet());
    }
}
