package com.minecolonies.core.colony.workorders.view;

import com.ldtteam.structurize.api.RotationMirror;
import com.minecolonies.api.colony.workorders.IWorkOrderView;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
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
    private String packName;

    /**
     * The name of the work order.
     */
    private String structurePath;

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
    private RotationMirror rotationMirror;

    /**
     * The level it's at before the upgrade.
     */
    private int currentLevel;

    /**
     * Level it's being upgraded to.
     */
    private int targetLevel;

    /**
     * The amount of resources the work order requires.
     */
    private int amountOfResources;

    /**
     * The iterator type (building method) this work order uses.
     */
    private String iteratorType;

    /**
     * Whether the work order area has been cleared.
     */
    private boolean cleared;

    /**
     * Whether the work order resources have been requested.
     */
    private boolean requested;

    /**
     * Translation key.
     */
    private String translationKey;

    public AbstractWorkOrderView()
    {
    }

    @Override
    public int getId()
    {
        return id;
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

    public BlockPos getClaimedBy()
    {
        return claimedBy;
    }

    public void setClaimedBy(final BlockPos position)
    {
        this.claimedBy = position;
    }

    /**
     * Value getter.
     *
     * @return the value String.
     */
    @Override
    public String getPackName()
    {
        return packName.replaceAll("schematics/(?:decorations/)?", "");
    }

    @Override
    public String getStructurePath()
    {
        return structurePath;
    }

    @Override
    public final String getTranslationKey()
    {
        return translationKey;
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

    @Override
    public RotationMirror getRotationMirror()
    {
        return rotationMirror;
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
     * Deserialize the attributes and variables from transition. Buffer may be not readable because the workOrderView may be null.
     *
     * @param buf Byte buffer to deserialize.
     */
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {
        id = buf.readInt();
        priority = buf.readInt();
        claimedBy = buf.readBlockPos();
        packName = buf.readUtf(32767);
        structurePath = buf.readUtf(32767);
        translationKey = buf.readUtf(32767);
        workOrderType = WorkOrderType.values()[buf.readInt()];
        location = buf.readBlockPos();
        rotationMirror = RotationMirror.values()[buf.readByte()];
        currentLevel = buf.readInt();
        targetLevel = buf.readInt();
        amountOfResources = buf.readInt();
        iteratorType = buf.readUtf(32767);
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
