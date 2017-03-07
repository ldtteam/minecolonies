package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.coremod.colony.IColony;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Created by marcf on 3/7/2017.
 */
public interface IBuilding {
    /**
     * Returns the {@link BlockPos} of the current object, also used as ID.
     *
     * @return {@link BlockPos} of the current object.
     */
    BlockPos getLocation();

    /**
     * Returns the colony of the building.
     *
     * @return {@link com.minecolonies.coremod.colony.Colony} of the current object.
     */
    @NotNull
    IColony getColony();

    /**
     * Checks if this building have a work order.
     *
     * @return true if the building is building, upgrading or repairing.
     */
    boolean hasWorkOrder();

    /**
     * Children must return their max building level.
     *
     * @return Max building level.
     */
    int getMaxBuildingLevel();

    /**
     * Returns the {@link BlockPos} of the current object, also used as ID.
     *
     * @return {@link BlockPos} of the current object.
     */
    BlockPos getID();

    /**
     * Returns the level of the current object.
     *
     * @return Level of the current object.
     */
    int getBuildingLevel();
}
