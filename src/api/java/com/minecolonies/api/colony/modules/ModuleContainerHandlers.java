package com.minecolonies.api.colony.modules;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Class that contains generic logic for handling with modules.
 */
public class ModuleContainerHandlers
{
    private ModuleContainerHandlers()
    {
    }

    public static <T> boolean hasModule(final Collection<T> modules, Class<? extends T> clazz)
    {
        return getFirstOptionalModuleOccurance(modules, clazz).isPresent();
    }

    public static @NotNull <T, T2 extends T> Optional<T2> getFirstOptionalModuleOccurance(final Collection<T> modules, final Class<T2> clazz)
    {
        return getModules(modules, clazz).stream().findFirst();
    }

    public static @NotNull <T, T2 extends T> List<T2> getModules(final Collection<T> modules, final Class<T2> clazz)
    {
        return modules.stream()
                 .map(module -> castModule(module, clazz))
                 .filter(Optional::isPresent)
                 .map(Optional::get)
                 .toList();
    }

    @SuppressWarnings("unchecked")
    private static <T, T2 extends T> Optional<T2> castModule(final T module, final Class<T2> clazz)
    {
        if (clazz.isInstance(module))
        {
            return Optional.of((T2) module);
        }
        return Optional.empty();
    }

    public static <T, T2 extends T> @NotNull T2 getFirstModuleOccurance(final Collection<T> modules, final Class<T2> clazz, String errorMessage)
    {
        return getModules(modules, clazz).stream()
                 .findFirst()
                 .orElseThrow(() -> new IllegalStateException(errorMessage));
    }

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
