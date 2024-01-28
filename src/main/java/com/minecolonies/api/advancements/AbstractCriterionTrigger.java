package com.minecolonies.api.advancements;

import com.google.common.collect.Maps;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;

/**
 * The base class of a Trigger that tracks listeners and defines criterion
 * related to the trigger, and how that is fetched from JSON
 * @param <T> the class of the individual listener that will perform the trigger
 * @param <U> the criterion the individual listener is checking
 */
public abstract class AbstractCriterionTrigger<T extends CriterionListeners<U>, U extends CriterionTriggerInstance> implements CriterionTrigger<U>
{
    /** The designation for this trigger. Used by JSON advancement data. */
    private final ResourceLocation                id;

    /** The factory method to create a listener each time this trigger is utilised */
    private final Function<PlayerAdvancements, T> createNew;

    /** A map tracking each of the listeners */
    private final Map<PlayerAdvancements, T>      listeners = Maps.newHashMap();

    /**
     * The trigger constructor that will define the trigger to be called
     * @param id the designation for this trigger. Used by JSON.
     * @param createNew the factory method for the listener
     */
    protected AbstractCriterionTrigger(ResourceLocation id, Function<PlayerAdvancements, T> createNew)
    {
        this.id = id;
        this.createNew = createNew;
    }

    @NotNull
    @Override
    public ResourceLocation getId()
    {
        return id;
    }

    @Override
    public void addPlayerListener(@NotNull PlayerAdvancements playerAdvancements, @NotNull Listener<U> listener)
    {
        T listeners = this.listeners.get(playerAdvancements);
        if (listeners == null)
        {
            listeners = createNew.apply(playerAdvancements);
            this.listeners.put(playerAdvancements, listeners);
        }
        listeners.add(listener);
    }

    @Override
    public void removePlayerListener(@NotNull PlayerAdvancements playerAdvancements, @NotNull Listener<U> listener)
    {
        final T listeners = this.listeners.get(playerAdvancements);

        if (listeners != null)
        {
            listeners.remove(listener);
            if (listeners.isEmpty())
            {
                this.listeners.remove(playerAdvancements);
            }
        }
    }

    @Nullable
    protected T getListeners(PlayerAdvancements playerAdvancements)
    {
        return this.listeners.get(playerAdvancements);
    }

    @Override
    public void removePlayerListeners(@NotNull PlayerAdvancements playerAdvancements)
    {
        this.listeners.remove(playerAdvancements);
    }
}
