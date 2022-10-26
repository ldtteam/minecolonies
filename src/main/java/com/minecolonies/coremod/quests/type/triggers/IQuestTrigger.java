package com.minecolonies.coremod.quests.type.triggers;

import com.minecolonies.api.colony.IColony;
import net.minecraft.resources.ResourceLocation;

/**
 * Quest trigger type, those control occurrence of quests.
 * Triggers are part of the colony - agnostic quest type and thus need to get a colony passed to check their condition on, or get subscribed to a colony bus
 */
public interface IQuestTrigger
{
    /**
     * Gets the quest effects ID
     *
     * @return res location id
     */
    ResourceLocation getID();

    /**
     * Whether this quest should trigger
     *
     * @param colony to check
     * @return true if should appear
     */
    public boolean shouldTrigger(final IColony colony);

    /**
     * Register the triggers listeners to that colony
     *
     * @param colony colony to listen to
     */
    public void registerWith(final IColony colony);

    /**
     * Unregisters the triggers listeners of that colony
     *
     * @param colony colony to un-listen
     */
    public void unregister(final IColony colony);
}
