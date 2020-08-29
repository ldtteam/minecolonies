package com.minecolonies.api.colony.buildings;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import net.minecraft.util.math.BlockPos;

public interface IBuildingBedProvider
{
    /**
     * Gets a list of all beds in this building.
     *
     * @return a list of all beds in this building.
     */
    @NotNull
    public List<BlockPos> getBedList();
}