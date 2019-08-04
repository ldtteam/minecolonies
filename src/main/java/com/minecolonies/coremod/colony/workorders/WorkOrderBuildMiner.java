package com.minecolonies.coremod.colony.workorders;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.workorders.IWorkManager;
import com.minecolonies.api.util.BlockPosUtil;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;

/**
 * A work order that the build can take to build mine.
 */
public class WorkOrderBuildMiner extends WorkOrderBuildDecoration
{
    /**
     * Position of the issuer of the order.
     */
    private BlockPos minerBuilding;

    /**
     * Unused constructor for reflection.
     */
    public WorkOrderBuildMiner()
    {
        super();
    }

    /**
     * Create a new work order telling the miner to build a mine.
     *
     * @param structureName The name of the mine.
     * @param workOrderName The user friendly name of the mine.
     * @param rotation      The number of times the mine was rotated.
     * @param location      The location where the mine should be built.
     * @param mirror        Is the mine mirrored?
     * @param minerBuilding The id of the building of the miner.
     */
    public WorkOrderBuildMiner(
            final String structureName,
            final String workOrderName,
            final int rotation,
            final BlockPos location,
            final boolean mirror,
            final BlockPos minerBuilding)
    {
        super(structureName, workOrderName, rotation, location, mirror);
        this.minerBuilding = minerBuilding;
    }

    /**
     * Read the WorkOrder data from the CompoundNBT.
     *  @param compound NBT Tag compound.
     * @param manager
     */
    @Override
    public void readFromNBT(@NotNull final CompoundNBT compound, final IWorkManager manager)
    {
        super.readFromNBT(compound, manager);
        minerBuilding = BlockPosUtil.readFromNBT(compound, TAG_POS);
    }

    /**
     * Save the Work Order to an CompoundNBT.
     *
     * @param compound NBT tag compound.
     */
    @Override
    public void writeToNBT(@NotNull final CompoundNBT compound)
    {
        super.writeToNBT(compound);
        BlockPosUtil.writeToNBT(compound, TAG_POS, minerBuilding);
    }

    @Override
    public void onAdded(final IColony colony, final boolean readingFromNbt)
    {
        /*
         * Override this to avoid action!
         */
    }

    @Override
    public void onRemoved(final IColony colony)
    {
        /*
         * Override this to avoid action!
         */
    }

    @Override
    public void onCompleted(final IColony colony)
    {
        /*
         * Override this to avoid action!
         */
    }

    @Override
    public boolean isValid(final IColony colony)
    {
        return colony.getBuildingManager().getBuilding(minerBuilding) != null;
    }

    /**
     * Get the miner building position assigned to this request.
     * @return the BlockPos.
     */
    public BlockPos getMinerBuilding()
    {
        return minerBuilding;
    }
}
