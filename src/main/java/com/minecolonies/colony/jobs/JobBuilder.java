package com.minecolonies.colony.jobs;

import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.workorders.WorkOrderBuild;
import com.minecolonies.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.entity.ai.citizen.builder.EntityAIWorkBuilder;
import net.minecraft.nbt.NBTTagCompound;

/**
 * The Job a builder has.
 * <p>
 * Manages his current workorder
 * and persists between restarts
 */
public class JobBuilder extends Job
{
    /**
     * NBT Tag for workOrderId
     */
    private static final String TAG_WORK_ORDER = "workorder";
    /**
     * the id of the workorder currently working on
     */
    private int            workOrderId;
    /**
     * Cache for workOrder queries
     */
    private WorkOrderBuild workOrder;

    /**
     * Initialize this job
     *
     * @param entity the citizen doing the job
     */
    public JobBuilder(CitizenData entity)
    {
        super(entity);
    }

    /**
     * Return a Localization label for the Job
     *
     * @return localization label String
     */
    @Override
    public String getName()
    {
        return "com.minecolonies.job.Builder";
    }

    /**
     * Get the RenderBipedCitizen.Model to use when the Citizen performs this job role.
     *
     * @return Model of the citizen
     */
    @Override
    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.BUILDER;
    }

    /**
     * Save the Job to an NBTTagCompound
     *
     * @param compound NBTTagCompound to save the Job to
     */
    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        if(workOrderId != 0)
        {
            compound.setInteger(TAG_WORK_ORDER, workOrderId);
        }
    }

    /**
     * Restore the Job from an NBTTagCompound
     *
     * @param compound NBTTagCompound containing saved Job data
     */
    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        if(compound.hasKey(TAG_WORK_ORDER))
        {
            workOrderId = compound.getInteger(TAG_WORK_ORDER);
        }
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @Override
    public AbstractAISkeleton generateAI()
    {
        return new EntityAIWorkBuilder(this);
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

    /**
     * Do final completion when the Job's current work is complete
     */
    public void complete()
    {
        getCitizen().getColony().getWorkManager().removeWorkOrder(workOrderId);
        setWorkOrder(null);
    }

    /**
     * Get the Work Order for the Job
     * Warning: WorkOrder is not cached
     *
     * @return WorkOrderBuild for the Build
     */
    public WorkOrderBuild getWorkOrder()
    {
        if(this.workOrder == null)
        {
            this.workOrder = getColony().getWorkManager().getWorkOrder(workOrderId, WorkOrderBuild.class);
        }
        return workOrder;
    }

    /**
     * Set a Work Order for this Job
     *
     * @param order Work Order to associate with this job, or null
     */
    public void setWorkOrder(WorkOrderBuild order)
    {
        workOrderId = (order != null) ? order.getID() : 0;
        this.workOrder = null;
    }

}
