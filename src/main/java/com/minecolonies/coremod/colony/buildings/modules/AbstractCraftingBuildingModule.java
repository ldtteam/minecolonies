package com.minecolonies.coremod.colony.buildings.modules;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.crafting.IGenericRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Basic implementation of a crafting module.
 *
 * Typically you should not directly extend this module; instead you should extend one of the
 * "policy classes" (inner classes) to specify the type of crafting supported.  The policy
 * classes don't provide any "real" implementation, they just configure this one.
 */
public abstract class AbstractCraftingBuildingModule extends AbstractBuildingModule implements ICraftingBuildingModule
{
    // TODO the theory is that eventually all the recipe-related things in
    //      AbstractBuildingWorker/Crafter would be moved to here, including
    //      the learned recipe lists and related methods...  (this would
    //      require care for backwards compatibility and especially if a
    //      building has more than one crafting module)

    /**
     * Helper function for derived classes; returns the "real" job for the
     * building, if it exists.  Don't use if your building has multiple jobs
     * and the crafter isn't the main one.
     *
     * @return The main citizen's job (if there is one)
     */
    protected Optional<IJob<?>> getMainBuildingJob()
    {
        if (this.building != null)
        {
            final ICitizenData mainCitizen = this.building.getMainCitizen();
            if (mainCitizen != null)
            {
                return Optional.of(mainCitizen.getJob());
            }
        }
        return Optional.empty();
    }

    /** This module is for standard crafters (3x3 by default) */
    public abstract static class Crafting extends AbstractCraftingBuildingModule
    {
        @Override
        public boolean canLearnCraftingRecipes() { return true; }

        @Override
        public boolean canLearnFurnaceRecipes() { return false; }

        @Override
        public boolean canLearnLargeRecipes() { return true; }
    }

    /** this module is for furnace-only users */
    public abstract static class Smelting extends AbstractCraftingBuildingModule
    {
        @Override
        public boolean canLearnCraftingRecipes() { return false; }

        @Override
        public boolean canLearnFurnaceRecipes() { return true; }

        @Override
        public boolean canLearnLargeRecipes() { return false; }
    }

    /** this module is for those who can't be taught recipes but can still use custom recipes */
    public abstract static class Custom extends AbstractCraftingBuildingModule
    {
        @Override
        public boolean canLearnCraftingRecipes() { return false; }

        @Override
        public boolean canLearnFurnaceRecipes() { return false; }

        @Override
        public boolean canLearnLargeRecipes() { return false; }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe) { return false; }
    }
}
