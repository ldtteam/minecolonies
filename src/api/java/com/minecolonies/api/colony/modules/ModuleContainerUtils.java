package com.minecolonies.api.colony.modules;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Class that contains generic logic for handling with modules.
 */
@SuppressWarnings("unchecked")
public class ModuleContainerUtils
{
    private ModuleContainerUtils()
    {
    }

    /**
     * Check if the object has a particular module.
     *
     * @param modules the collection of modules given from the implementing module handler.
     * @param clazz   the class or interface of the module to check.
     * @return true if so.
     */
    public static <T> boolean hasModule(final Collection<T> modules, Class<? extends T> clazz)
    {
        for (final T module : modules)
        {
            if (clazz.isInstance(module))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the first module with a particular class or interface.
     *
     * @param modules the collection of modules given from the implementing module handler.
     * @param clazz   the module's class or interface.
     * @return the module or empty if not existent.
     */
    public static @NotNull <T, T2 extends T> Optional<T2> getFirstOptionalModuleOccurance(final Collection<T> modules, final Class<T2> clazz)
    {
        for (final T module : modules)
        {
            if (clazz.isInstance(module))
            {
                return Optional.of((T2) module);
            }
        }
        return Optional.empty();
    }

    /**
     * Get the first module with a particular class or interface.
     *
     * @param modules the collection of modules given from the implementing module handler.
     * @param clazz   the module's class or interface.
     * @return the module or empty if not existent.
     */
    public static <T, T2 extends T> @NotNull T2 getFirstModuleOccurance(final Collection<T> modules, final Class<T2> clazz, String errorMessage)
    {
        for (final T module : modules)
        {
            if (clazz.isInstance(module))
            {
                return (T2) module;
            }
        }

        throw new IllegalStateException(errorMessage);
    }

    /**
     * Get a module matching a certain predicate.
     *
     * @param modules         the collection of modules given from the implementing module handler.
     * @param clazz           the class of the module.
     * @param modulePredicate the predicate to match.
     * @param <T2>            the module type.
     * @return the first matching module.
     * @throws IllegalArgumentException if your condition does not match any modules
     */
    public static <T, T2 extends T> @NotNull T2 getModuleMatching(
      final Collection<T> modules,
      final Class<T2> clazz,
      final Predicate<? super T2> modulePredicate,
      String errorMessage)
    {
        for (final T module : modules)
        {
            if (clazz.isInstance(module) && modulePredicate.test((T2) module))
            {
                return (T2) module;
            }
        }

        throw new IllegalStateException(errorMessage);
    }

    /**
     * Get all modules with a particular class or interface.
     *
     * @param modules the collection of modules given from the implementing module handler.
     * @param clazz   the module's interface (or class, but prefer getModule in that case)
     * @return the list of modules or empty if none match.
     */
    public static @NotNull <T, T2 extends T> List<T2> getModules(final Collection<T> modules, final Class<T2> clazz)
    {
        final List<T2> result = new ArrayList<>();

        for (T module : modules)
        {
            if (clazz.isInstance(module))
            {
                result.add((T2) module);
            }
        }

        return result;
    }
}
