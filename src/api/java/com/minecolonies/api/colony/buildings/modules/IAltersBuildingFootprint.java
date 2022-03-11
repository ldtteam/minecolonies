package com.minecolonies.api.colony.buildings.modules;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;

/**
 * Interface for buildings with an extended footprint.
 */
public interface IAltersBuildingFootprint extends IAssignsCitizen
{
    /**
     * Get the additional corners into each direction.
     * @return the positions.
     */
    Tuple<BlockPos, BlockPos> getAdditionalCorners();
}
