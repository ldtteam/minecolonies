package com.minecolonies.coremod.quests.type.triggers;

import com.minecolonies.api.colony.IColony;
import net.minecraft.resources.ResourceLocation;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * Triggered when a raid starts
 */
public class RaidTrigger implements IQuestTrigger
{
    /**
     * ID
     */
    public final static ResourceLocation ID = new ResourceLocation(MOD_ID, "raidspawn");

    @Override
    public ResourceLocation getID()
    {
        return ID;
    }

    @Override
    public boolean shouldTrigger(final IColony colony)
    {
        return false;
    }

    @Override
    public void registerWith(final IColony colony)
    {

    }

    @Override
    public void unregister(final IColony colony)
    {

    }
}
