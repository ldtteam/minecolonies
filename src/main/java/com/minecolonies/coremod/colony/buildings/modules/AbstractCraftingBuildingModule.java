package com.minecolonies.coremod.colony.buildings.modules;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.crafting.IGenericRecipe;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Basic implementation of a crafting module.
 *
 * Typically you should not directly extend this module; instead you should extend one of the
 * "policy classes" (inner classes) to specify the type of crafting supported.  The policy
 * classes don't provide any "real" implementation, they just configure this one.
 */
public abstract class AbstractCraftingBuildingModule extends AbstractBuildingModule implements ICraftingBuildingModule, IPersistentModule
{
    // TODO the theory is that eventually all the recipe-related things in
    //      AbstractBuildingWorker/Crafter would be moved to here, including
    //      the learned recipe lists and related methods...  (this would
    //      require care for backwards compatibility and especially if a
    //      building has more than one crafting module)

    @Override
    public void serializeNBT(@NotNull final CompoundNBT compound)
    {
        // TODO there's no state, so nothing to persist ... yet.
        // when we do get around to this, bear in mind:
        //   1. there needs to be some kind of upgrade path from previously-saved "recipes".
        //   2. more than one CraftingBuildingModule can exist on the same building (and the
        //      same compound tag), so they need to keep their data under a uniquely named
        //      parent object.  we assume that "namespace:job/type" will be sufficiently unique.
        //  alternatively, we could keep all recipes in a combined "recipes" tag, as now, with
        //  the modules smart enough to figure out which one "owns" each recipe for UI purposes
        //  (which will usually be obvious by looking at the intermediate block).  this would
        //  be a simpler upgrade path but might be more error-prone or less performant.
    }

    @Override
    public void deserializeNBT(CompoundNBT compound)
    {
        // ditto
    }

    @Override
    public void serializeToView(PacketBuffer buf)
    {
        final IJob<?> job = getMainBuildingJob().orElse(null);
        if (job != null)
        {
            buf.writeBoolean(true);
            buf.writeRegistryId(job.getJobRegistryEntry());
        }
        else
        {
            buf.writeBoolean(false);
        }
        buf.writeBoolean(this.canLearnCraftingRecipes());
        buf.writeBoolean(this.canLearnFurnaceRecipes());
        buf.writeBoolean(this.canLearnLargeRecipes());
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
    public abstract static class Crafting extends AbstractCraftingBuildingModule
    {
        @Override
        public boolean canLearnCraftingRecipes() { return true; }

        @Override
        public boolean canLearnFurnaceRecipes() { return false; }

        @Override
        public boolean canLearnLargeRecipes() { return true; }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            return canLearnCraftingRecipes() &&
                    recipe.getIntermediate() == Blocks.AIR;
        }
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

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            return canLearnFurnaceRecipes() &&
                    recipe.getIntermediate() == Blocks.FURNACE;
        }
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
