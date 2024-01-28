package com.minecolonies.api.advancements;

import com.google.common.collect.Sets;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.server.PlayerAdvancements;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * A base class for the listening manager of a given criterion that captures and tests
 * and checks each advancement when the criterion is triggered
 * @param <T> the Instance of the criterion that will be tested against
 */
public class CriterionListeners<T extends CriterionTriggerInstance>
{
    private final PlayerAdvancements                 playerAdvancements;
    private final Set<CriterionTrigger.Listener<T>> listeners = Sets.newHashSet();

    /**
     * The base manager for the listeners, where each listener is an advancement
     * that could be triggered by the associated criterion
     * @param playerAdvancements the advancements associated with the player
     */
    public CriterionListeners(PlayerAdvancements playerAdvancements)
    {
        this.playerAdvancements = playerAdvancements;
    }

    public boolean isEmpty()
    {
        return this.listeners.isEmpty();
    }

    /** add a new criterion listener */
    public void add(CriterionTrigger.Listener<T> listener)
    {
        this.listeners.add(listener);
    }

    /** remove an existing criterion listener */
    public void remove(CriterionTrigger.Listener<T> listener)
    {
        this.listeners.remove(listener);
    }

    /**
     * Grants advancements with no conditions or checks (for simple checks to be made in events)
     */
    public void trigger()
    {
        this.listeners.forEach(listener -> listener.run(this.playerAdvancements));
    }

    /**
     * Grants each advancement that passes the test provided by that advancement's listener.
     * @param test the test to return true if the advancement is to be granted
     */
    public void trigger(Predicate<T> test)
    {
        final List<CriterionTrigger.Listener<T>> toGrant = new ArrayList<>();
        for (CriterionTrigger.Listener<T> listener : this.listeners)
        {
            if (test.test(listener.getTriggerInstance()))
            {
                toGrant.add(listener);
            }
        }
        toGrant.forEach(listener -> listener.run(this.playerAdvancements));
    }
}
