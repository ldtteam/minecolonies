package com.minecolonies.colony.jobs;

import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.workorders.WorkOrderBuild;
import com.minecolonies.entity.ai.EntityAIWorkBuilder;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.nbt.NBTTagCompound;

public class JobBuilder extends Job
{
    private static final String TAG_WORK_ORDER = "workorder";
    private int       workOrderId;

    public JobBuilder(CitizenData entity)
    {
        super(entity);
    }

    @Override
    public String getName()
    {
        return "com.minecolonies.job.Builder";
    }

    @Override
    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.BUILDER;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        if (workOrderId != 0)
        {
            compound.setInteger(TAG_WORK_ORDER, workOrderId);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        if (compound.hasKey(TAG_WORK_ORDER))
        {
            workOrderId = compound.getInteger(TAG_WORK_ORDER);
        }
    }

    @Override
    public void addTasks(EntityAITasks tasks)
    {
        tasks.addTask(3, new EntityAIWorkBuilder(this));
    }

    /**
     * Get the Work Order ID for this Job
     *
     * @return UUID of the Work Order claimed by this Job, or null
     */
    public int getWorkOrderId()
    {
        return workOrderId;
    }

    /**
     * Get the Work Order for the Job
     * Warning: WorkOrder is not cached
     *
     * @return WorkOrderBuild for the Build
     */
    public WorkOrderBuild getWorkOrder()
    {
        return getColony().getWorkManager().getWorkOrder(workOrderId, WorkOrderBuild.class);
    }

    /**
     * Set a Work Order for this Job
     *
     * @param order Work Order to associate with this job, or null
     */
    public void setWorkOrder(WorkOrderBuild order)
    {
        workOrderId = (order != null) ? order.getID() : 0;
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
        this.getWorkOrder().setSchematic(null);
    }

}
