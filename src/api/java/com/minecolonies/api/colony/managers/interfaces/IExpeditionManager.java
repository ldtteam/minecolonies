package com.minecolonies.api.colony.managers.interfaces;

import com.minecolonies.api.colony.expeditions.IExpedition;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Interface for the colony expedition manager. From here all outgoing expeditions to external places are managed.
 */
public interface IExpeditionManager
{
    /**
     * Register a finished expedition to the manager.
     *
     * @param expedition the expedition instance.
     * @param owner      the owning class of the expedition.
     */
    void addExpedition(final IExpedition expedition, final Class<?> owner);

    /**
     * Check that determines if expeditions to a given dimension may be sent or not.
     *
     * @param dimension the target dimension.
     * @return whether the target dimension is allowed or not.
     */
    boolean canGoToDimension(final ResourceKey<Level> dimension);
}