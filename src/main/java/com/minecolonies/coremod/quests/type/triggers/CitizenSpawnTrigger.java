package com.minecolonies.coremod.quests.type.triggers;

import com.minecolonies.api.colony.IColony;
import net.minecraft.util.ResourceLocation;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * Triggered upon citizen spawn
 */
public class CitizenSpawnTrigger implements IQuestTrigger
{
    /**
     * ID
     */
    public final static ResourceLocation ID = new ResourceLocation(MOD_ID, "citizenspawn");

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
