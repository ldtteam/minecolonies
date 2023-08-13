package com.minecolonies.api.colony.modules;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Default interface for objects that contain module instances.
 */
public interface IModuleContainer<T>
{
    /**
     * Check if the object has a particular module.
     *
     * @param clazz the class or interface of the module to check.
     * @return true if so.
     */
    boolean hasModule(final Class<? extends T> clazz);

    /**
     * Get the first module with a particular class or interface.
     *
     * @param clazz the module's class or interface.
     * @return the module or empty if not existent.
     */
    @NotNull <T2 extends T> T2 getFirstModuleOccurance(Class<T2> clazz);

    /**
     * Get the first module with a particular class or interface.
     *
     * @param clazz the module's class or interface.
     * @return the module or empty if not existent.
     */
    @NotNull <T2 extends T> Optional<T2> getFirstOptionalModuleOccurance(Class<T2> clazz);

    /**
     * Get all modules with a particular class or interface.
     *
     * @param clazz the module's interface (or class, but prefer getModule in that case)
     * @return the list of modules or empty if none match.
     */
    @NotNull <T2 extends T> List<T2> getModules(Class<T2> clazz);

    /**
     * Get a module matching a certain predicate.
     *
     * @param clazz           the class of the module.
     * @param modulePredicate the predicate to match.
     * @param <T2>            the module type.
     * @return the first matching module.
     * @throws IllegalArgumentException if your condition does not match any modules
     */
    @NotNull <T2 extends T> T2 getModuleMatching(Class<T2> clazz, Predicate<? super T2> modulePredicate);

    /**
     * Register a specific module to the object.
     *
     * @param module the module to register.
     */
    void registerModule(@NotNull final T module);
}
