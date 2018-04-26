package com.minecolonies.coremod.colony.workorders;

import com.minecolonies.api.util.BlockPosUtil;
import net.minecraft.nbt.NBTTagCompound;
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
     * Read the WorkOrder data from the NBTTagCompound.
     *
     * @param compound NBT Tag compound.
     */
    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        minerBuilding = BlockPosUtil.readFromNBT(compound, TAG_POS);
    }

    /**
     * Save the Work Order to an NBTTagCompound.
     *
     * @param compound NBT tag compound.
     */
    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        BlockPosUtil.writeToNBT(compound, TAG_POS, minerBuilding);
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
