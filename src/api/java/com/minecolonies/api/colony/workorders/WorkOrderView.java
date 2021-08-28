package com.minecolonies.api.colony.workorders;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * The WorkOrderView is the client-side representation of a WorkOrders. Views contain the WorkOrder's data that is relevant to a Client, in a more client-friendly form Mutable
 * operations on a View result in a message to the server to perform the operation
 */
public class WorkOrderView
{
    /**
     * The work orders id.
     */
    private int id;

    /**
     * The priority.
     */
    private int priority;

    /**
     * Its description.
     */
    private String schematicName;

    /**
     * Its description.
     */
    private String displayName;

    /**
     * The type (defined by an enum).
     */
    private WorkOrderType type;

    /**
     * Claimed by building id pos.
     */
    private BlockPos claimedBy;

    /**
     * Position where its being built at.
     */
    private BlockPos pos;

    /**
     * Level it's being upgraded to.
     */
    private int upgradeLevel;

    /**
     * Priority getter.
     *
     * @return the priority.
     */
    public int getPriority()
    {
        return priority;
    }

    /**
     * Setter for the priority.
     *
     * @param priority the new priority.
     */
    public void setPriority(final int priority)
    {
        this.priority = priority;
    }

    /**
     * Value getter.
     *
     * @return the value String.
     */
    public String getSchematicName()
    {
        return schematicName.replaceAll("schematics/(?:decorations/)?", "");
    }

    /**
     * Return the display name
     *
     * @return
     */
    public String getDisplayName()
    {
        return displayName;
    }

    /**
     * Type getter.
     *
     * @return the type (defined by Enum).
     */
    public WorkOrderType getType()
    {
        return type;
    }

    /**
     * Id getter.
     *
     * @return the id.
     */
    public int getId()
    {
        return id;
    }

    /**
     * Id setter.
     *
     * @param id the id to set.
     */
    public void setId(final int id)
    {
        this.id = id;
    }

    /**
     * ClaimedBy getter.
     *
     * @return citizen id who claimed the workOrder.
     */
    public BlockPos getClaimedBy()
    {
        return claimedBy;
    }

    /**
     * Deserialize the attributes and variables from transition. Buffer may be not readable because the workOrderView may be null.
     *
     * @param buf Byte buffer to deserialize.
     */
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {
        id = buf.readInt();
        priority = buf.readInt();
        claimedBy = buf.readBlockPos();
        type = WorkOrderType.values()[buf.readInt()];
        schematicName = buf.readUtf(32767);
        displayName = buf.readUtf(32767);
        pos = buf.readBlockPos();
        upgradeLevel = buf.readInt();
    }

    /**
     * Checks if a builder may accept this workOrder while ignoring the distance to the builder.
     * @param builderLocation position of the builders own hut.
     * @param builderLevel level of the builders hut.
     * @return true if so.
     */
    public boolean canBuildIngoringDistance(@NotNull final BlockPos builderLocation, final int builderLevel)
    {
        //  A Build WorkOrder may be fulfilled by a Builder as long as any ONE of the following is true:
        //  - The Builder's Work AbstractBuilding is built
        //  - OR the WorkOrder is for the Builder's Work AbstractBuilding

        return (builderLevel >= upgradeLevel || builderLevel == 5 || (builderLocation.equals(pos)));
    }

    /**
     * Get the position of the workorder.
     *
     * @return the position
     */
    public BlockPos getPos()
    {
        return this.pos;
    }

    /**
     * Claim the view.
     * @param position the pos of the claiming worker.
     */
    public void setClaimedBy(final BlockPos position)
    {
        this.claimedBy = position;
    }
}
