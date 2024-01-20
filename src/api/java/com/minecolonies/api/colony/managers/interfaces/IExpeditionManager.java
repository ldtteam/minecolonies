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
     * Check that determines if expeditions to a given dimension may be sent or not.
     *
     * @param dimension the target dimension.
     * @return whether the target dimension is allowed or not.
     */
    boolean canGoToDimension(final ResourceKey<Level> dimension);

    /**
     * Register a new expedition to the manager.
     *
     * @param expedition the expedition instance.
     */
    void registerExpedition(final IExpedition expedition);
}