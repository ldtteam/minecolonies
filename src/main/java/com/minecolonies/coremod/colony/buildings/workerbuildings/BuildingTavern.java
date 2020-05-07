package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.coremod.client.gui.WindowHutTavern;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class BuildingTavern extends BuildingHome
{
    public static final String TARVERN_SCHEMATIC = "tavern";

    /**
     * Instantiates a new citizen hut.
     *
     * @param c the colony.
     * @param l the location.
     */
    public BuildingTavern(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return TARVERN_SCHEMATIC;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return 3;
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.tavern;
    }

    @Override
    public int getMaxInhabitants()
    {
        if (getBuildingLevel() <= 0)
        {
            return 0;
        }

        return 4;
    }

    /**
     * ClientSide representation of the building.
     */
    public static class View extends BuildingHome.View
    {
        /**
         * Instantiates the view of the building.
         *
         * @param c the colonyView.
         * @param l the location of the block.
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutTavern(this);
        }
    }
}
