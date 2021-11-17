package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.coremod.client.gui.huts.WindowHutWorkerModulePlaceholder;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;

/**
 * Class of the stone smeltery building.
 */
public class BuildingStoneSmeltery extends AbstractBuilding
{
    /**
     * Description string of the building.
     */
    private static final String STONE_SMELTERY = "stonesmeltery";

    /**
     * Instantiates a new stone smeltery building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingStoneSmeltery(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return STONE_SMELTERY;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return CONST_DEFAULT_MAX_BUILDING_LEVEL;
    }

    /**
     * Stone smeltery View.
     */
    public static class View extends AbstractBuildingView
    {

        /**
         * Instantiate the stone smeltery view.
         *
         * @param c the colonyview to put it in
         * @param l the positon
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public BOWindow getWindow()
        {
            return new WindowHutWorkerModulePlaceholder<>(this, STONE_SMELTERY);
        }
    }

    public static class SmeltingModule extends AbstractCraftingBuildingModule.Smelting
    {
        /**
         * Create a new module.
         *
         * @param jobEntry the entry of the job.
         */
        public SmeltingModule(final JobEntry jobEntry)
        {
            super(jobEntry);
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe)) return false;
            return CraftingUtils.isRecipeCompatibleBasedOnTags(recipe, STONE_SMELTERY).orElse(false);
        }
    }
}
