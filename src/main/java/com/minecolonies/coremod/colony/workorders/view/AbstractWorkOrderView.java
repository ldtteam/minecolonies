package com.minecolonies.coremod.colony.workorders.view;

import com.minecolonies.api.colony.workorders.IWorkOrderView;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * The WorkOrderView is the client-side representation of a WorkOrders. Views contain the WorkOrder's data that is relevant to a Client, in a more client-friendly form Mutable
 * operations on a View result in a message to the server to perform the operation
 */
public abstract class AbstractWorkOrderView implements IWorkOrderView
{
    /**
     * The work order its id.
     */
    private int id;

    /**
     * The priority.
     */
    private int priority;

    /**
     * Claimed by building id pos.
     */
    private BlockPos claimedBy;

    /**
     * Its description.
     */
    private String structureName;

    /**
     * The name of the work order
     */
    private String workOrderName;

    /**
     * The type (defined by an enum).
     */
    private WorkOrderType workOrderType;

    /**
     * Position where its being built at.
     */
    private BlockPos location;

    /**
     * Position where its being built at.
     */
    private int rotation;

    /**
     * Position where its being built at.
     */
    private boolean isMirrored;

    /**
     * The level it's at before the upgrade.
     */
    private int currentLevel;

    /**
     * Level it's being upgraded to.
     */
    private int targetLevel;

    private int amountOfResources;
    private String iteratorType;
    private boolean cleared;
    private boolean requested;

    public AbstractWorkOrderView()
    {
    }

    @Override
    public int getId()
    {
        return id;
    }

    @Override
    public void setId(final int id)
    {
        this.id = id;
    }

    @Override
    public int getPriority()
    {
        return priority;
    }

    @Override
    public void setPriority(final int priority)
    {
        this.priority = priority;
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
     * Claim the view.
     *
     * @param position the pos of the claiming worker.
     */
    public void setClaimedBy(final BlockPos position)
    {
        this.claimedBy = position;
    }

    /**
     * Value getter.
     *
     * @return the value String.
     */
    public String getStructureName()
    {
        return structureName.replaceAll("schematics/(?:decorations/)?", "");
    }

    public String getWorkOrderName()
    {
        return workOrderName;
    }

    public WorkOrderType getWorkOrderType()
    {
        return workOrderType;
    }

    @Override
    public BlockPos getLocation()
    {
        return this.location;
    }

    public int getRotation()
    {
        return rotation;
    }

    public boolean isMirrored()
    {
        return isMirrored;
    }

    public int getCurrentLevel()
    {
        return currentLevel;
    }

    public int getTargetLevel()
    {
        return targetLevel;
    }

    public int getAmountOfResources()
    {
        return amountOfResources;
    }

    public String getIteratorType()
    {
        return iteratorType;
    }

    public boolean isCleared()
    {
        return cleared;
    }

    public boolean isRequested()
    {
        return requested;
    }

    /**
     * Whether this work order should be shown in the town hall.
     *
     * @return a boolean
     */
    public abstract boolean shouldShowInTownHall();

    /**
     * Whether this work order should be shown in the builder.
     *
     * @return a boolean
     */
    public abstract boolean shouldShowInBuilder();

    /**
     * Deserialize the attributes and variables from transition. Buffer may be not readable because the workOrderView may be null.
     *
     * @param buf Byte buffer to deserialize.
     */
    public void deserialize(@NotNull final PacketBuffer buf)
    {
        id = buf.readInt();
        priority = buf.readInt();
        claimedBy = buf.readBlockPos();
        structureName = buf.readUtf();
        workOrderName = buf.readUtf();
        workOrderType = WorkOrderType.values()[buf.readInt()];
        location = buf.readBlockPos();
        rotation = buf.readInt();
        isMirrored = buf.readBoolean();
        currentLevel = buf.readInt();
        targetLevel = buf.readInt();
        amountOfResources = buf.readInt();
        iteratorType = buf.readUtf();
        cleared = buf.readBoolean();
        requested = buf.readBoolean();
    }

    /**
     * Checks if a builder may accept this workOrder while ignoring the distance to the builder.
     *
     * @param builderLocation position of the builders own hut.
     * @param builderLevel    level of the builders hut.
     * @return true if so.
     */
    public boolean canBuildIgnoringDistance(@NotNull final BlockPos builderLocation, final int builderLevel)
    {
        //  A Build WorkOrder may be fulfilled by a Builder as long as any ONE of the following is true:
        //  - The Builder's Work AbstractBuilding is built
        //  - OR the WorkOrder is for the Builder's Work AbstractBuilding

        return (builderLevel >= targetLevel || builderLevel == 5 || (builderLocation.equals(location)));
    }
}
