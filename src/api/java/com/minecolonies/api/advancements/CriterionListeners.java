package com.minecolonies.api.advancements;

import com.google.common.collect.Sets;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class CriterionListeners<T extends ICriterionInstance>
{
    private final PlayerAdvancements playerAdvancements;
    private final Set<ICriterionTrigger.Listener<T>> listeners = Sets.newHashSet();

    public CriterionListeners(PlayerAdvancements playerAdvancements)
    {
        this.playerAdvancements = playerAdvancements;
    }

    public boolean isEmpty()
    {
        return this.listeners.isEmpty();
    }

    public void add(ICriterionTrigger.Listener<T> listener)
    {
        this.listeners.add(listener);
    }

    public void remove(ICriterionTrigger.Listener<T> listener)
    {
        this.listeners.remove(listener);
    }

    public void trigger(Predicate<T> test)
    {
        final List<ICriterionTrigger.Listener<T>> toGrant = new ArrayList<>();
        for (ICriterionTrigger.Listener<T> listener : this.listeners)
        {
            if (test.test(listener.getCriterionInstance()))
            {
                toGrant.add(listener);
            }
        }
        toGrant.forEach(listener -> listener.grantCriterion(this.playerAdvancements));
    }
}
