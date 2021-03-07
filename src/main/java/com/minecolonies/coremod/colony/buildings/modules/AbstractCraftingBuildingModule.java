package com.minecolonies.coremod.colony.buildings.modules;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingCrafter;
import net.minecraft.nbt.CompoundNBT;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@SuppressWarnings("ClassWithoutLogger")
public abstract class AbstractCraftingBuildingModule extends AbstractBuildingModule implements ICraftingBuildingModule
{
    @Override
    public boolean canLearnRecipes(final boolean rightNow)
    {
        if (rightNow)
        {
            if (this.building instanceof AbstractBuildingCrafter)
            {
                // TODO probably the logic that decides this should be left to the
                // derived classes of this instead eventually...
                return ((AbstractBuildingCrafter) this.building).isRecipeAlterationAllowed();
            }
            return false;
        }
        return true;
    }

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
    @SuppressWarnings("PublicMethodWithoutLogging")
    public abstract static class Crafting extends AbstractCraftingBuildingModule implements IPersistentModule
    {
        // TODO the theory is that eventually all the recipe-related things in
        // AbstractBuildingWorker/Crafter would be moved to here...

        @Override
        public void deserializeNBT(final CompoundNBT compound) {
            // TODO eventually move the taught recipe list into here?
        }

        @Override
        public void serializeNBT(final CompoundNBT compound) {

        }

        @Override
        public boolean canLearnCraftingRecipes() { return true; }

        @Override
        public boolean canLearnFurnaceRecipes() { return false; }

        @Override
        public boolean canLearnLargeRecipes() { return true; }
    }

    /** this module is for furnace-only users */
    @SuppressWarnings("PublicMethodWithoutLogging")
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
    @SuppressWarnings("PublicMethodWithoutLogging")
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

        @Override
        public boolean canLearnRecipes(final boolean rightNow) { return false; }
    }
}
