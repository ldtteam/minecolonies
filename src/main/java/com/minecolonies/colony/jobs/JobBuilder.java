package com.minecolonies.colony.jobs;

import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.workorders.WorkOrderBuild;
import com.minecolonies.entity.ai.EntityAIWorkBuilder;
import com.minecolonies.util.BlockPosUtil;
import com.minecolonies.util.Log;
import com.minecolonies.util.SchematicWrapper;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;

public class JobBuilder extends Job
{
    private static final String TAG_WORK_ORDER = "workorder";
    private static final String TAG_SCHEMATIC  = "schematic";
    private static final String TAG_NAME       = "name";
    private static final String TAG_POSITION   = "position";
    private static final String TAG_PROGRESS   = "progress";
    private static final String TAG_STAGE      = "stage";
    protected SchematicWrapper schematic;
    //TODO save some of this in building
    private   int       workOrderId;
    private   String    schematicName;
    private   BlockPos  schematicPos;
    private   BlockPos  schematicProgress;

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

            if (hasSchematic())
            {
                NBTTagCompound schematicTag = new NBTTagCompound();
                schematicTag.setString(TAG_NAME, schematic.getName());
                BlockPosUtil.writeToNBT(schematicTag, TAG_POSITION, schematic.getPosition());
                BlockPosUtil.writeToNBT(schematicTag, TAG_PROGRESS, schematic.getLocalPosition());
                compound.setTag(TAG_SCHEMATIC, schematicTag);
            }
        }
    }

    /**
     * Does this job have a loaded Schematic?
     * <p>
     * if a schematic is not null there exists a location for it
     *
     * @return true if there is a loaded schematic for this Job
     */
    public boolean hasSchematic()
    {
        return schematic != null;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        if (compound.hasKey(TAG_WORK_ORDER))
        {
            workOrderId = compound.getInteger(TAG_WORK_ORDER);

            if (compound.hasKey(TAG_SCHEMATIC))
            {
                NBTTagCompound schematicTag = compound.getCompoundTag(TAG_SCHEMATIC);
                schematicName = schematicTag.getString(TAG_NAME);
                schematicPos = BlockPosUtil.readFromNBT(schematicTag, TAG_POSITION);
                schematicProgress = BlockPosUtil.readFromNBT(schematicTag, TAG_PROGRESS);
            }
        }
    }

    @Override
    public void addTasks(EntityAITasks tasks)
    {
        if (schematicName != null)
        {
            try
            {
                schematic = new SchematicWrapper(getCitizen().getColony().getWorld(), schematicName);
                schematic.setPosition(schematicPos);
                schematic.setLocalPosition(schematicProgress);
            }
            catch (IllegalStateException e)
            {
                Log.logger.error("Could not create schematic!", e);
            }

            schematicName = null;
            schematicPos = null;
            schematicProgress = null;
        }

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
     * Get the Schematic loaded by the Job
     *
     * @return Schematic loaded by the Job
     */
    public SchematicWrapper getSchematic()
    {
        return schematic;
    }

    /**
     * Set the schematic of builder's job
     *
     * @param schematic {@link SchematicWrapper} object
     */
    public void setSchematic(SchematicWrapper schematic)
    {
        this.schematic = schematic;
    }

    /**
     * Returns the work interval of the worker //unfinished
     *
     * @return work interval
     */
    public int getWorkInterval()
    {
        return 1;//Constants.BUILDERWORKINTERFALL - this.getLevel();//TODO
    }

    /**
     * Do final completion when the Job's current work is complete
     */
    public void complete()
    {
        getCitizen().getColony().getWorkManager().removeWorkOrder(workOrderId);
        setWorkOrder(null);
        setSchematic(null);
    }

}
