package com.minecolonies.coremod.colony.workorders;

import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.workorders.IWorkManager;
import com.minecolonies.api.util.BlockPosUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
     * @param location      The location where the mine should be built.
     * @param settings      The placement settings.
     * @param minerBuilding The id of the building of the miner.
     */
    public WorkOrderBuildMiner(
      final String structureName,
      final String workOrderName,
      final BlockPos location,
      final PlacementSettings settings,
      final BlockPos minerBuilding)
    {
        super(structureName, workOrderName, location, settings);
        this.minerBuilding = minerBuilding;
    }

    /**
     * Read the WorkOrder data from the CompoundNBT.
     *
     * @param compound NBT Tag compound.
     * @param manager  the work manager.
     */
    @Override
    public void read(@NotNull final CompoundTag compound, final IWorkManager manager)
    {
        super.read(compound, manager);
        minerBuilding = BlockPosUtil.read(compound, TAG_POS);
    }

    /**
     * Save the Work Order to an CompoundNBT.
     *
     * @param compound NBT tag compound.
     */
    @Override
    public void write(@NotNull final CompoundTag compound)
    {
        super.write(compound);
        BlockPosUtil.write(compound, TAG_POS, minerBuilding);
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
    public void onCompleted(final IColony colony, final ICitizenData citizen)
    {
        /*
         * Override this to avoid action!
         */
    }

    @Override
    public boolean isValid(final IColony colony)
    {
        return super.isValid(colony) && colony.getBuildingManager().getBuilding(minerBuilding) != null;
    }

    /**
     * Get the miner building position assigned to this request.
     *
     * @return the BlockPos.
     */
    public BlockPos getMinerBuilding()
    {
        return minerBuilding;
    }
}
