package com.minecolonies.api.colony.workorder;

import net.minecraft.util.math.BlockPos;

/**
 * Objects implementing this Interface are workorders that are used to build structures in the world.
 */
public interface IWorkOrderBuild extends IWorkOrder
{
    /**
     * Get the name of the work order.
     *
     * @return the work order name
     */
    String getName();

    /**
     * Returns the level up level of the building.
     *
     * @return Level after upgrade.
     */
    int getUpgradeLevel();

    /**
     * Returns the ID of the building (aka ChunkCoordinates).
     *
     * @return ID of the building.
     */
    BlockPos getBuildingLocation();

    /**
     * Get the name the structure for this work order.
     *
     * @return the internal string for this structure.
     */
    String getStructureName();

    /**
     * Gets how many times this structure should be rotated.
     *
     * @return building rotation.
     */
    int getRotation();

    /**
     * Gets whether or not the building has been cleared.
     *
     * @return true if the building has been cleared.
     */
    boolean isCleared();

    /**
     * Set whether or not the building has been cleared.
     *
     * @param cleared true if the building has been cleared.
     */
    void setCleared(boolean cleared);

    /**
     * Gets whether or not the building materials have been requested already.
     *
     * @return true if the materials has been requested.
     */
    boolean isRequested();

    /**
     * Set whether or not the building materials have been requested already.
     *
     * @param requested true if so.
     */
    void setRequested(boolean requested);

    /**
     * Check if the workOrder should be built isMirrored.
     *
     * @return true if so.
     */
    boolean isMirrored();

    /**
     * Check if the work order is a decoration or not.
     *
     * @return True if so.
     */
    boolean isDecoration();
}
