package com.minecolonies.coremod.colony.workorders;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.util.IOPool;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.workorders.IWorkManager;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.jobs.JobMiner;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Future;

import static com.minecolonies.api.util.constant.Constants.STORAGE_STYLE;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;

/**
 * A work order that the build can take to build mine.
 */
public class WorkOrderMiner extends AbstractWorkOrder
{
    /**
     * Position of the issuer of the order.
     */
    private BlockPos minerBuilding;

    /**
     * Unused constructor for reflection.
     */
    public WorkOrderMiner()
    {
        super();
    }

    /**
     * Create a new work order telling the miner to build a mine.
     *
     * @param packName      The name of the pack.
     * @param structureName The path of the blueprint.
     * @param workOrderName The user friendly name of the mine.
     * @param rotation      The number of times the mine was rotated.
     * @param location      The location where the mine should be built.
     * @param mirror        Is the mine mirrored?
     * @param minerBuilding The id of the building of the miner.
     */
    public WorkOrderMiner(
      final String packName,
      final String structureName,
      final String workOrderName,
      final int rotation,
      final BlockPos location,
      final boolean mirror,
      final BlockPos minerBuilding)
    {
        super(packName, structureName, workOrderName, WorkOrderType.BUILD, location, rotation, mirror, 0, 1);
        this.minerBuilding = minerBuilding;
    }

    @Override
    public Future<Blueprint> getBlueprintFuture()
    {
        return IOPool.submit(() ->
        {
            Blueprint blueprint = StructurePacks.getBlueprint(getStructurePack(), getStructurePath(), true);
            if (blueprint == null)
            {
                // automatic fallback to default style
                blueprint = StructurePacks.getBlueprint(STORAGE_STYLE, getStructurePath());
                if (blueprint != null)
                {
                    packName = STORAGE_STYLE;
                    changed = true;
                }
            }
            return blueprint;
        });
    }

    @Override
    public boolean canBuild(@NotNull ICitizenData citizen)
    {
        return this.minerBuilding.equals(citizen.getWorkBuilding().getID());
    }

    @Override
    public boolean canBeMadeBy(final IJob<?> job)
    {
        return job instanceof JobMiner;
    }

    @Override
    public boolean isValid(final IColony colony)
    {
        return super.isValid(colony) && colony.getBuildingManager().getBuilding(minerBuilding) != null;
    }

    /**
     * Read the WorkOrder data from the CompoundTag.
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
     * Save the Work Order to an CompoundTag.
     *
     * @param compound NBT tag compound.
     */
    @Override
    public void write(@NotNull final CompoundTag compound)
    {
        super.write(compound);
        BlockPosUtil.write(compound, TAG_POS, minerBuilding);
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
