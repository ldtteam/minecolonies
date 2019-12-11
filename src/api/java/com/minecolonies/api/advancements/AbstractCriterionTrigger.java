package com.minecolonies.api.advancements;

import com.google.common.collect.Maps;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;

public abstract class AbstractCriterionTrigger<T extends CriterionListeners<U>, U extends ICriterionInstance> implements ICriterionTrigger<U>
{
    private final ResourceLocation id;
    private final Function<PlayerAdvancements, T> createNew;
    private final Map<PlayerAdvancements, T> listeners = Maps.newHashMap();

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
    public void addListener(@NotNull PlayerAdvancements playerAdvancements, @NotNull Listener<U> listener)
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
    public void removeListener(@NotNull PlayerAdvancements playerAdvancements, @NotNull Listener<U> listener)
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
    public void removeAllListeners(@NotNull PlayerAdvancements playerAdvancements)
    {
        this.listeners.remove(playerAdvancements);
    }
}
