package com.minecolonies.api.colony.workorders;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.minecolonies.api.colony.IColony;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface IWorkOrder
{
    /**
     * Get the ID of the work order.
     *
     * @return ID of the work order
     */
    int getID();

    /**
     * Set the ID of the work order.
     *
     * @param id the new ID for the work order
     */
    void setID(int id);

    /**
     * Getter for the priority.
     *
     * @return the priority of the work order.
     */
    int getPriority();

    /**
     * Setter for the priority.
     *
     * @param priority the new priority.
     */
    void setPriority(int priority);

    /**
     * Get the structure this work order should be using, if any.
     *
     * @return the schematic path.
     */
    String getStructurePath();

    /**
     * Get the structure this work order should be using, if any.
     *
     * @return the pack name.
     */
    String getStructurePack();

    /**
     * Loads the blueprint if necessary
     * @param world world to use
     * @param afterLoad consumes the loaded blueprint or null
     */
    void loadBlueprint(final Level world, final Consumer<Blueprint> afterLoad);

    /**
     * Get the current level of the structure of the work order.
     *
     * @return the current level.
     */
    int getCurrentLevel();

    /**
     * Get the target level of the structure of the work order.
     *
     * @return the target level.
     */
    int getTargetLevel();

    /**
     * The name of the work order.
     *
     * @return the work order name.
     */
    String getTranslationKey();

    /**
     * The type of the work order.
     *
     * @return the work order type.
     */
    WorkOrderType getWorkOrderType();

    /**
     * Get the current location of the building
     *
     * @return the location
     */
    BlockPos getLocation();

    /**
     * Get the current rotation of the building
     *
     * @return the location
     */
    int getRotation();

    /**
     * Whether the current building is mirrored
     *
     * @return the location
     */
    boolean isMirrored();

    /**
     * Is the Work Order claimed?
     *
     * @return true if the Work Order has been claimed
     */
    boolean isClaimed();

    /**
     * Get the position of the Citizen that the Work Order is claimed by.
     *
     * @return ID of citizen the Work Order has been claimed by, BLockpos.ZERO
     */
    @NotNull
    BlockPos getClaimedBy();

    /**
     * Set the Work order as claimed by a given building.
     *
     * @param builder the building position.
     */
    void setClaimedBy(BlockPos builder);

    /**
     * Get the name of the work order, provides the custom name or the work order name when no custom name is given
     *
     * @return the display name for the work order
     */
    Component getDisplayName();

    /**
     * Get the file name of the structure.
     * Calculates the file name from the path.
     * @return the name without the appendix.
     */
    default String getFileName()
    {
        final String[] split = getStructurePath().contains("\\") ? getStructurePath().split("\\\\") : getStructurePath().split("/");
        return split[split.length - 1].replace(".blueprint", "");
    }

    /**
     * Store a blueprint reference
     *
     * @param blueprint
     */
    void setBlueprint(Blueprint blueprint, final Level world);

    /**
     * Get the stored blueprint
     *
     * @return
     */
    @Nullable
    public Blueprint getBlueprint();

    /**
     * Clears the stored blueprint
     */
    public void clearBlueprint();

    /**
     * Get the blueprints Boundingbox
     */
    @Nullable
    public AABB getBoundingBox();

    /**
     * Get the related colony or view
     *
     * @return
     */
    public IColony getColony();

    /**
     * Set the related colony or view
     *
     * @return
     */
    public void setColony(IColony colony);
}
