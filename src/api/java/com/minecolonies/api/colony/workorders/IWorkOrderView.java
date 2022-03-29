package com.minecolonies.api.colony.workorders;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.NotNull;

public interface IWorkOrderView
{
    /**
     * Id getter.
     *
     * @return the id.
     */
    int getId();

    /**
     * Id setter.
     *
     * @param id the id to set.
     */
    void setId(final int id);

    /**
     * Priority getter.
     *
     * @return the priority.
     */
    int getPriority();

    /**
     * Setter for the priority.
     *
     * @param priority the new priority.
     */
    void setPriority(final int priority);

    /**
     * ClaimedBy getter.
     *
     * @return citizen id who claimed the workOrder.
     */
    BlockPos getClaimedBy();

    /**
     * Claim the view.
     *
     * @param position the pos of the claiming worker.
     */
    void setClaimedBy(BlockPos position);

    /**
     * Type getter.
     *
     * @return the type (defined by Enum).
     */
    WorkOrderType getWorkOrderType();

    /**
     * Get the position of the workorder.
     *
     * @return the position
     */
    BlockPos getLocation();

    /**
     * Get a text component containing the display name of the work order which can be shown on the GUI
     *
     * @return the text component
     */
    ITextComponent getDisplayName();

    /**
     * Checks if a builder may accept this workOrder while ignoring the distance to the builder.
     *
     * @param builderLocation position of the builders own hut.
     * @param builderLevel    level of the builders hut.
     * @return true if so.
     */
    boolean canBuildIgnoringDistance(@NotNull final BlockPos builderLocation, final int builderLevel);
}
