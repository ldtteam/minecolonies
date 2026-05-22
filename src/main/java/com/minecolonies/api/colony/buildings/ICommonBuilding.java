package com.minecolonies.api.colony.buildings;

import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import com.minecolonies.api.colony.IColony;


/**
 * Common building interface for both client & server.
 */
public interface ICommonBuilding
{
    /**
     * Get the current level of the building.
     *
     * @return AbstractBuilding current level.
     */
    int getBuildingLevel();

    /**
     * Gets the location of this building.
     *
     * @return A BlockPos, where this building is.
     */
    @NotNull
    BlockPos getPosition();

    /**
     * Get the Building type
     *
     * @return building type
     */
    BuildingEntry getBuildingType();

    /**
     * Get the equivalent building level for equipment, etc.
     * Normally it's just the building level, but for buildings with fewer levels it can be 1,3,5 for example.
     * @return the adjusted level.
     */
    default int getBuildingLevelEquivalent()
    {
        return getBuildingLevel();
    }

    /**
     * Get the BlockPos of the Containers.
     *
     * @return containerList.
     */
    List<BlockPos> getContainers();

    /**
     * Get the colony from a building.
     * @return the colony it belongs to.
     */
    IColony getColony();

    /**
     * Get the prestige value of the building.
     * @return the prestige value.
     */
    int getPrestige();
}
