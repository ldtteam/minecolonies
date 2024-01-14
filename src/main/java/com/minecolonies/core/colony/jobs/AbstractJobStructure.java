package com.minecolonies.core.colony.jobs;

import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.storage.StructurePacks;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.Utils;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.core.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.core.entity.ai.basic.AbstractAISkeleton;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import static com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE.TAG_BLUEPRINTDATA;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_NAME;

/**
 * Common job object for all structure AIs.
 */
public abstract class AbstractJobStructure<AI extends AbstractAISkeleton<J>, J extends AbstractJobStructure<AI, J>> extends AbstractJob<AI, J>
{
    /**
     * Tag to store the workOrder id.
     */
    private static final String TAG_WORK_ORDER = "workorder";

    /**
     * The id of the current workOrder.
     */
    private int workOrderId;

    /**
     * The structure the job should build.
     */
    protected Blueprint blueprint;

    /**
     * Initialize citizen data.
     *
     * @param entity the citizen data.
     */
    public AbstractJobStructure(final ICitizenData entity)
    {
        super(entity);
    }

    /**
     * Does this job has a loaded StructureProxy?
     * <p>
     * if a structure is not null there exists a location for it
     *
     * @return true if there is a loaded structure for this Job
     */
    public boolean hasBlueprint()
    {
        return blueprint != null;
    }

    /**
     * Get the StructureProxy loaded by the Job.
     *
     * @return StructureProxy loaded by the Job
     */
    public Blueprint getBlueprint()
    {
        return blueprint;
    }

    /**
     * Set the structure of the structure job.
     *
     * @param blueprint {@link Blueprint} object
     */
    public void setBlueprint(final Blueprint blueprint)
    {
        this.blueprint = blueprint;
    }

    /**
     * Get the Work Order ID for this Job.
     *
     * @return UUID of the Work Order claimed by this Job, or null
     */
    public int getWorkOrderId()
    {
        return workOrderId;
    }

    /**
     * Does this job have a Work Order it has claimed?
     *
     * @return true if there is a Work Order claimed by this Job
     */
    public boolean hasWorkOrder()
    {
        return workOrderId != 0;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = super.serializeNBT();
        if (workOrderId != 0)
        {
            compound.putInt(TAG_WORK_ORDER, workOrderId);
        }

        return compound;
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);
        if (compound.contains(TAG_WORK_ORDER))
        {
            workOrderId = compound.getInt(TAG_WORK_ORDER);
        }
    }

    /**
     * Do final completion when the Job's current work is complete.
     */
    public void complete()
    {
        getWorkOrder().onCompleted(getCitizen().getColony(), this.getCitizen());

        if (blueprint != null)
        {
            final CompoundTag[][][] tileEntityData = blueprint.getTileEntities();
            for (short x = 0; x < blueprint.getSizeX(); x++)
            {
                for (short y = 0; y < blueprint.getSizeY(); y++)
                {
                    for (short z = 0; z < blueprint.getSizeZ(); z++)
                    {
                        final CompoundTag compoundNBT = tileEntityData[y][z][x];
                        if (compoundNBT != null && compoundNBT.contains(TAG_BLUEPRINTDATA))
                        {
                            final BlockPos tePos = getWorkOrder().getLocation().subtract(blueprint.getPrimaryBlockOffset()).offset(x, y, z);
                            final BlockEntity te = getColony().getWorld().getBlockEntity(tePos);
                            if (te instanceof IBlueprintDataProviderBE)
                            {
                                final CompoundTag tagData = compoundNBT.getCompound(TAG_BLUEPRINTDATA);
                                final String schematicPath = tagData.getString(TAG_NAME);
                                final String location = StructurePacks.getStructurePack(blueprint.getPackName()).getSubPath(Utils.resolvePath(blueprint.getFilePath(), schematicPath));

                                tagData.putString(TAG_NAME, location);
                                tagData.putString(NbtTagConstants.TAG_PACK, blueprint.getPackName());

                                try
                                {
                                    ((IBlueprintDataProviderBE) te).readSchematicDataFromNBT(compoundNBT);
                                }
                                catch (final Exception e)
                                {
                                    Log.getLogger().warn("Broken deco-controller at: " + x + " " + y + " " + z);
                                }
                                ((ServerLevel) getColony().getWorld()).getChunkSource().blockChanged(tePos);
                                te.setChanged();
                            }
                        }
                    }
                }
            }
        }

        getCitizen().getColony().getWorkManager().removeWorkOrder(workOrderId);
        setWorkOrder(null);
        setBlueprint(null);
    }

    /**
     * Get the Work Order for the Job. Warning: WorkOrder is not cached
     *
     * @return WorkOrderBuildDecoration for the Build
     */
    public IWorkOrder getWorkOrder()
    {
        return getColony().getWorkManager().getWorkOrder(workOrderId, IWorkOrder.class);
    }

    /**
     * Reset the needed items list.
     */
    private void resetNeededItems()
    {
        final IBuilding workerBuilding = this.getCitizen().getWorkBuilding();
        if (workerBuilding instanceof AbstractBuildingStructureBuilder)
        {
            ((AbstractBuildingStructureBuilder) workerBuilding).resetNeededResources();
        }
    }

    /**
     * Set a Work Order for this Job.
     *
     * @param order Work Order to associate with this job, or null
     */
    public void setWorkOrder(@Nullable final IWorkOrder order)
    {
        if (order == null)
        {
            workOrderId = 0;
            resetNeededItems();
        }
        else
        {
            workOrderId = order.getID();
        }
    }
}
