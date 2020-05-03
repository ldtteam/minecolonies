package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.coremod.client.gui.WindowHutQuarryStation;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class BuildingQuarryStation extends AbstractBuilding
{
    /**
     * The job description.
     */
    private static final String QUARRY_STATION = "quarrystation";

    /**
     * Constructor for the Quarry Station.
     *
     * @param colony Colony the building belongs to.
     * @param pos    Location of the building (it's Hut Block).
     */
    public BuildingQuarryStation(@NotNull final IColony colony, final BlockPos pos)
    {
        super(colony, pos);
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.quarryStation;
    }

    @Override
    public String getSchematicName()
    {
        return QUARRY_STATION;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return 1;
    }

    @Override
    public void serializeToView(@NotNull final PacketBuffer buf)
    {
        super.serializeToView(buf);
    }

    /**
     * Provides a view of the quarry building class.
     */
    public static class View extends AbstractBuildingView
    {
        /**
         * Public constructor of the view, creates an instance of it.
         *
         * @param c the colony.
         * @param l the position.
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            //TODO: Quarry window
            return new WindowHutQuarryStation(this);
        }

        @Override
        public void deserialize(@NotNull final PacketBuffer buf)
        {
            super.deserialize(buf);

            //TODO: Deserialization
        }

        public boolean isFull()
        {
            //TODO: IMPLEMENT
            return false;
        }
    }
}
