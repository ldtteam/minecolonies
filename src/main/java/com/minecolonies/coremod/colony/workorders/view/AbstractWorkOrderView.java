package com.minecolonies.coremod.colony.workorders.view;

import com.minecolonies.api.colony.workorders.IWorkOrderView;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
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
     * Its description.
     */
    private String structureName;

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
    private int targetLevel;

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
     * @param position the pos of the claiming worker.
     */
    public void setClaimedBy(final BlockPos position)
    {
        this.claimedBy = position;
    }

    @Override
    public WorkOrderType getType()
    {
        return type;
    }

    @Override
    public BlockPos getLocation()
    {
        return this.pos;
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

    @Override
    public ITextComponent getDisplayName()
    {
        String workOrderName = new TranslationTextComponent(displayName).getString();
        return new StringTextComponent(String.format("%s %d", workOrderName, targetLevel));
    }

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
        type = WorkOrderType.values()[buf.readInt()];
        structureName = buf.readUtf(32767);
        displayName = buf.readUtf(32767);
        pos = buf.readBlockPos();
        targetLevel = buf.readInt();
    }

    /**
     * Checks if a builder may accept this workOrder while ignoring the distance to the builder.
     * @param builderLocation position of the builders own hut.
     * @param builderLevel level of the builders hut.
     * @return true if so.
     */
    public boolean canBuildIgnoringDistance(@NotNull final BlockPos builderLocation, final int builderLevel)
    {
        //  A Build WorkOrder may be fulfilled by a Builder as long as any ONE of the following is true:
        //  - The Builder's Work AbstractBuilding is built
        //  - OR the WorkOrder is for the Builder's Work AbstractBuilding

        return (builderLevel >= targetLevel || builderLevel == 5 || (builderLocation.equals(pos)));
    }
}
