package com.minecolonies.api.colony.modules;

import com.google.common.base.Predicates;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Default interface for objects that contain module instances.
 */
public interface IModuleContainer<T>
{
    /**
     * Get a list containing all modules registered to this container.
     *
     * @return the list of all modules.
     */
    @NotNull
    List<T> getModules();

    /**
     * Get the class type of the module container.
     *
     * @return the class type.
     */
    @NotNull
    Class<T> getClassType();

    /**
     * Get a list containing all modules that extend from the given class.
     *
     * @param <T2>  the generic type of the specific classes we're looking for.
     * @param clazz the class type to check against.
     * @return the list of modules.
     */
    @NotNull
    default <T2 extends T> List<T2> getModules(final Class<T2> clazz)
    {
        return getModules(clazz, Predicates.alwaysTrue());
    }

    /**
     * Get a list containing all modules that match the given predicate.
     *
     * @param predicate the predicate to check each module against.
     * @return the list of modules.
     */
    @NotNull
    default List<T> getModules(final Predicate<T> predicate)
    {
        return getModules(getClassType(), predicate);
    }

    /**
     * Get a list containing all modules that extend from the given class and match the given predicate.
     *
     * @param <T2>      the generic type of the specific classes we're looking for.
     * @param clazz     the class type to check against.
     * @param predicate the predicate to check each module against.
     * @return the list of modules.
     */
    @NotNull
    default <T2 extends T> List<T2> getModules(final Class<T2> clazz, final Predicate<T2> predicate)
    {
        final List<T2> modules = new ArrayList<>();
        for (final T module : getModules())
        {
            if (clazz.isInstance(module))
            {
                final T2 castedModule = clazz.cast(module);
                if (predicate.test(castedModule))
                {
                    modules.add(castedModule);
                }
            }
        }
        return modules;
    }

    /**
     * Get a specific module that extends from the given class.
     *
     * @param <T2>  the generic type of the specific classes we're looking for.
     * @param clazz the class type to check against.
     * @return the found module, or null.
     */
    @Nullable
    default <T2 extends T> T2 getModule(final Class<T2> clazz)
    {
        return clazz.cast(getModule(clazz::isInstance));
    }

    /**
     * Get a specific module that that matches the given predicate.
     *
     * @param predicate the predicate to check each module against.
     * @return the found module, or null.
     */
    @Nullable
    default T getModule(final Predicate<T> predicate)
    {
        return getModule(getClassType(), predicate);
    }

    /**
     * Get a specific module that extends from the given class and matches the given predicate.
     *
     * @param <T2>      the generic type of the specific classes we're looking for.
     * @param clazz     the class type to check against.
     * @param predicate the predicate to check each module against.
     * @return the found module, or null.
     */
    @Nullable
    default <T2 extends T> T2 getModule(final Class<T2> clazz, final Predicate<T2> predicate)
    {
        for (final T module : getModules())
        {
            if (clazz.isInstance(module))
            {
                final T2 castedModule = clazz.cast(module);
                if (predicate.test(castedModule))
                {
                    return castedModule;
                }
            }
        }
        return null;
    }

    /**
     * Check if a certain module exists that extends from the given class.
     *
     * @param <T2>  the generic type of the specific classes we're looking for.
     * @param clazz the class type to check against.
     * @return true if the module was found.
     */
    default <T2 extends T> boolean hasModule(final Class<T2> clazz)
    {
        return hasModule(clazz, Predicates.alwaysTrue());
    }

    /**
     * Check if a certain module exists that matches the given predicate.
     *
     * @param predicate the predicate to check each module against.
     * @return true if the module was found.
     */
    default boolean hasModule(final Predicate<T> predicate)
    {
        return hasModule(getClassType(), predicate);
    }

    /**
     * Check if a certain module exists that extends from the given class and matches the given predicate.
     *
     * @param <T2>      the generic type of the specific classes we're looking for.
     * @param clazz     the class type to check against.
     * @param predicate the predicate to check each module against.
     * @return true if the module was found.
     */
    default <T2 extends T> boolean hasModule(final Class<T2> clazz, final Predicate<T2> predicate)
    {
        return getModule(clazz, predicate) != null;
    }

    // TODO: 1.22 Phase these old methods out
    //<editor-fold desc="Old methods">

    /**
     * Get the first module with a particular class or interface.
     *
     * @param clazz the module's class or interface.
     * @return the module or empty if not existent.
     * @deprecated switch to {@link IModuleContainer#getModule(Class)}
     */
    @Deprecated
    @NotNull
    default <T2 extends T> T2 getFirstModuleOccurance(final Class<T2> clazz)
    {
        final T2 module = getModule(clazz);
        if (module == null)
        {
            throw new IllegalArgumentException(String.format("No module found for class %s", clazz.getName()));
        }
        return module;
    }

    /**
     * Get a module matching a certain predicate.
     *
     * @param modulePredicate the predicate to match.
     * @param <T2>            the module type.
     * @return the first matching module.
     * @throws IllegalArgumentException if your condition does not match any modules
     * @deprecated switch to {@link IModuleContainer#getModule(Class)}
     */
    @Deprecated
    @NotNull
    default <T2 extends T> T2 getModuleMatching(final Class<T2> clazz, final Predicate<T2> modulePredicate)
    {
        final T2 module = getModule(clazz, modulePredicate);
        if (module == null)
        {
            throw new IllegalArgumentException(String.format("No module found for class %s and predicate", clazz.getName()));
        }
        return module;
    }

    /**
     * Get all modules with a particular class or interface.
     *
     * @param clazz the module's interface (or class, but prefer getModule in that case)
     * @return the list of modules or empty if none match.
     */
    @Deprecated
    @NotNull
    default <T2 extends T> List<T2> getModulesByType(final Class<T2> clazz)
    {
        return getModules(clazz);
    }

    //</editor-fold>
}
