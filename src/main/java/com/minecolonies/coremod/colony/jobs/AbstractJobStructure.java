package com.minecolonies.coremod.colony.jobs;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.coremod.colony.workorders.AbstractWorkOrder;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.jetbrains.annotations.Nullable;

import static com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider.TAG_BLUEPRINTDATA;

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
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();
        if (workOrderId != 0)
        {
            compound.putInt(TAG_WORK_ORDER, workOrderId);
        }

        return compound;
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);
        if (compound.getAllKeys().contains(TAG_WORK_ORDER))
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
            final CompoundNBT[][][] tileEntityData = blueprint.getTileEntities();
            for (short x = 0; x < blueprint.getSizeX(); x++)
            {
                for (short y = 0; y < blueprint.getSizeY(); y++)
                {
                    for (short z = 0; z < blueprint.getSizeZ(); z++)
                    {
                        final CompoundNBT compoundNBT = tileEntityData[y][z][x];
                        if (compoundNBT != null && compoundNBT.contains(TAG_BLUEPRINTDATA))
                        {
                            final BlockPos tePos = getWorkOrder().getLocation().subtract(blueprint.getPrimaryBlockOffset()).offset(x, y, z);
                            final TileEntity te = getColony().getWorld().getBlockEntity(tePos);
                            if (te instanceof IBlueprintDataProvider)
                            {
                                ((IBlueprintDataProvider) te).readSchematicDataFromNBT(compoundNBT);
                                ((ServerWorld) getColony().getWorld()).getChunkSource().blockChanged(tePos);
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
    public AbstractWorkOrder getWorkOrder()
    {
        return getColony().getWorkManager().getWorkOrder(workOrderId, AbstractWorkOrder.class);
    }

    /**
     * Reset the needed items list.
     */
    private void resetNeededItems()
    {
        final IBuilding workerBuilding = this.getCitizen().getWorkBuilding();
        if (workerBuilding instanceof BuildingBuilder)
        {
            ((BuildingBuilder) workerBuilding).resetNeededResources();
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
