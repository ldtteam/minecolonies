package com.minecolonies.coremod.colony.buildings.modules;

import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * This module is used for the buildings that only support simple
 * 2x2 crafting.  They don't have any restrictions on what they can
 * learn to craft beyond this, but they won't craft for the request
 * system either, only for themselves.
 */
public class SimpleCraftingModule extends AbstractCraftingBuildingModule.Crafting
{
    @Nullable
    @Override
    public IJob<?> getCraftingJob()
    {
        // the building may have a job, but it's not a dedicated crafting job.
        // this hides the building from JEI.
        return null;
    }

    @Override
    public List<IRequestResolver<?>> createResolvers()
    {
        return Collections.emptyList();
    }

    @Override
    public boolean canLearnLargeRecipes()
    {
        return false;
    }
}
