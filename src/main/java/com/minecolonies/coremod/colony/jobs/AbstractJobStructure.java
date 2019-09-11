package com.minecolonies.coremod.colony.jobs;

import com.ldtteam.structures.helpers.Structure;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildDecoration;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.Nullable;

/**
 * Common job object for all structure AIs.
 */
public abstract class AbstractJobStructure extends AbstractJob
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
    protected Structure structure;

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
    public boolean hasStructure()
    {
        return structure != null;
    }

    /**
     * Get the StructureProxy loaded by the Job.
     *
     * @return StructureProxy loaded by the Job
     */
    public Structure getStructure()
    {
        return structure;
    }

    /**
     * Set the structure of the structure job.
     *
     * @param structure {@link Structure} object
     */
    public void setStructure(final Structure structure)
    {
        this.structure = structure;
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
    public NBTTagCompound serializeNBT()
    {
        final NBTTagCompound compound = super.serializeNBT();
        if (workOrderId != 0)
        {
            compound.setInteger(TAG_WORK_ORDER, workOrderId);
        }

        return compound;
    }

    @Override
    public void deserializeNBT(final NBTTagCompound compound)
    {
        super.deserializeNBT(compound);
        if (compound.hasKey(TAG_WORK_ORDER))
        {
            workOrderId = compound.getInteger(TAG_WORK_ORDER);
        }
    }

    /**
     * Do final completion when the Job's current work is complete.
     */
    public void complete()
    {
        getWorkOrder().onCompleted(getCitizen().getColony());
        getCitizen().getColony().getWorkManager().removeWorkOrder(workOrderId);
        setWorkOrder(null);
        setStructure(null);
        this.getColony().getStatsManager().incrementStatistic("huts");
    }

    /**
     * Get the Work Order for the Job.
     * Warning: WorkOrder is not cached
     *
     * @return WorkOrderBuildDecoration for the Build
     */
    public WorkOrderBuildDecoration getWorkOrder()
    {
        return getColony().getWorkManager().getWorkOrder(workOrderId, WorkOrderBuildDecoration.class);
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
    public void setWorkOrder(@Nullable final WorkOrderBuildDecoration order)
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
