package com.minecolonies.api.util;

import com.google.common.reflect.TypeToken;
import org.apache.logging.log4j.core.config.AppenderControl;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Set;

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
     * @param token The type to get the Supertypes for.
     * @param <T>   The type to get the super types for.
     * @return A set with the super types of the given type.
     */
    @SuppressWarnings("unchecked")
    public static <T> Set<TypeToken<?>> getSuperClasses(final TypeToken<T> token)
    {
        final Set<TypeToken<?>> directSet = new LinkedHashSet<>(token.getTypes());
        final Set<TypeToken<?>> resultingSet = new LinkedHashSet<>();

        directSet.forEach(t ->
        {
            resultingSet.add(t);
            resultingSet.add(TypeToken.of(t.getRawType()));
        });

        return resultingSet;
    }

    public static void setFMLLoggingLevelOnConsoleToDebug(final AppenderControl control)
      throws NoSuchFieldException, IllegalAccessException
    {
        final Field levelField = control.getClass().getField("level");
        levelField.setAccessible(true);
        levelField.set(control, Integer.MAX_VALUE);
    }
}
