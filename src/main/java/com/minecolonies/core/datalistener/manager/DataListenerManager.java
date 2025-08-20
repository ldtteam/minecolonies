package com.minecolonies.core.datalistener.manager;

import com.minecolonies.core.datalistener.BaseDataListener;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.*;
import java.util.function.Function;

/**
 * Manager class for all the {@link BaseDataListener} instances.
 */
public class DataListenerManager
{
    /**
     * Singleton instance.
     */
    private static final DataListenerManager instance = new DataListenerManager();

    /**
     * The data listener instances.
     */
    private final Map<Class<?>, BaseDataListener<?>> instances = new HashMap<>();

    /**
     * Utility class.
     */
    private DataListenerManager()
    {
    }

    /**
     * Get the singleton instance.
     *
     * @return the instance.
     */
    public static DataListenerManager getInstance()
    {
        return instance;
    }

    /**
     * Preparation method to be called during the registration event. This method initializes the data listeners and starts loading the resources.
     *
     * @param listeners        the list of listener providers.
     * @param conditionContext the conditional context for processing Neoforge {@link ICondition} instances.
     * @return the collection of created listeners.
     */
    public Collection<BaseDataListener<?>> prepareAllListeners(final Collection<BaseListenerProvider> listeners, final ICondition.IContext conditionContext)
    {
        final List<BaseDataListener<?>> baseListeners = new ArrayList<>();
        for (final BaseListenerProvider provider : listeners)
        {
            final BaseDataListener<?> baseListener = provider.apply(conditionContext);
            baseListeners.add(baseListener);
            instances.put(baseListener.getClass(), baseListener);
        }
        return baseListeners;
    }

    /**
     * Get the entries for a given data listener.
     *
     * @param clazz the type of the data listener.
     * @param <C>   the generic type of the listener class.
     * @param <T>   the generic type of the listener class entries.
     * @return the map of entries.
     */
    public <C extends BaseDataListener<T>, T> Map<ResourceLocation, T> getEntries(final Class<C> clazz)
    {
        return getObject(clazz).map(BaseDataListener::getEntries).orElseGet(Map::of);
    }

    /**
     * Attempt to get the listener instance via an {@link Optional}. Can return null if the listener has not been
     * instantiated by {@link DataListenerManager#prepareAllListeners(Collection, ICondition.IContext)} yet, or if a class assignable check goes wrong.
     *
     * @param clazz the type of the data listener.
     * @param <C>   the generic type of the listener class.
     * @param <T>   the generic type of the listener class entries.
     * @return the optional containing the listener instance.
     */
    @SuppressWarnings("unchecked")
    private <C extends BaseDataListener<T>, T> Optional<C> getObject(final Class<C> clazz)
    {
        final C instance = (C) instances.computeIfPresent(clazz, (k, v) -> v.getClass().isAssignableFrom(k) ? v : null);
        return Optional.ofNullable(instance);
    }

    /**
     * Check if an entry exists on the given data listener.
     *
     * @param clazz the type of the data listener.
     * @param key   the key to check existence for.
     * @param <C>   the generic type of the listener class.
     * @param <T>   the generic type of the listener class entries.
     * @return true if so.
     */
    public <C extends BaseDataListener<T>, T> boolean isEntry(final Class<C> clazz, final ResourceLocation key)
    {
        return getObject(clazz).map(BaseDataListener::getEntries).map(m -> m.containsKey(key)).orElse(false);
    }

    /**
     * Provider instances for inputting the conditional context into the data listeners.
     */
    @FunctionalInterface
    public interface BaseListenerProvider extends Function<ICondition.IContext, BaseDataListener<?>>
    {}
}
