package com.minecolonies.api.colony.modules;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Class that contains generic logic for handling with modules.
 */
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
        return getFirstOptionalModuleOccurance(modules, clazz).isPresent();
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
        return getModules(modules, clazz).stream().findFirst();
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
        return modules.stream()
                 .map(module -> castModule(module, clazz))
                 .filter(Optional::isPresent)
                 .map(Optional::get)
                 .toList();
    }

    /**
     * Internal method which casts the module to the correct implementation of said module.
     * Returns an optional containing the cast class if it was able to cast, or an empty optional.
     *
     * @param module the module to cast
     * @param clazz  the expected type to cast to.
     * @return an optional instance containing the cast class, or empty.
     */
    @SuppressWarnings("unchecked")
    private static <T, T2 extends T> Optional<T2> castModule(final T module, final Class<T2> clazz)
    {
        if (clazz.isInstance(module))
        {
            return Optional.of((T2) module);
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
        return getModules(modules, clazz).stream()
                 .findFirst()
                 .orElseThrow(() -> new IllegalStateException(errorMessage));
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
        return getModules(modules, clazz).stream()
                 .filter(modulePredicate)
                 .findFirst()
                 .orElseThrow(() -> new IllegalArgumentException(errorMessage));
    }
}
