package com.minecolonies.colony.jobs;

import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.buildings.AbstractBuilding;
import com.minecolonies.colony.workorders.WorkOrderBuild;
import com.minecolonies.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.entity.ai.citizen.builder.EntityAIStructureBuilder;
import com.minecolonies.util.BlockPosUtil;
import com.minecolonies.util.SchematicWrapper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JobBuilder extends AbstractJob
{
    private static final String TAG_WORK_ORDER = "workorder";
    private static final String TAG_SCHEMATIC  = "schematic";
    private static final String TAG_NAME       = "name";
    private static final String TAG_POSITION   = "position";
    private static final String TAG_PROGRESS   = "progress";
    private static final String TAG_STAGE      = "stage";
    protected SchematicWrapper schematic;
    //TODO save some of this in building
    private   int              workOrderId;
    private   String           schematicName;
    private   BlockPos         schematicPos;
    private   BlockPos         schematicProgress;

    public JobBuilder(CitizenData entity)
    {
        super(entity);
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        if (compound.hasKey(TAG_WORK_ORDER))
        {
            workOrderId = compound.getInteger(TAG_WORK_ORDER);

            if (compound.hasKey(TAG_SCHEMATIC))
            {
                final NBTTagCompound schematicTag = compound.getCompoundTag(TAG_SCHEMATIC);
                schematicName = schematicTag.getString(TAG_NAME);
                schematicPos = BlockPosUtil.readFromNBT(schematicTag, TAG_POSITION);
                schematicProgress = BlockPosUtil.readFromNBT(schematicTag, TAG_PROGRESS);
            }
        }
    }

    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.job.Builder";
    }

    @NotNull
    @Override
    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.BUILDER;
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        if (workOrderId != 0)
        {
            compound.setInteger(TAG_WORK_ORDER, workOrderId);

            if (hasSchematic())
            {
                @NotNull final NBTTagCompound schematicTag = new NBTTagCompound();
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

    @NotNull
    @Override
    public AbstractAISkeleton generateAI()
    {
        return new EntityAIStructureBuilder(this);
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

    /**
     * Get the Schematic loaded by the Job.
     *
     * @return Schematic loaded by the Job
     */
    public SchematicWrapper getSchematic()
    {
        return schematic;
    }

    /**
     * Set the schematic of builder's job.
     *
     * @param schematic {@link SchematicWrapper} object
     */
    public void setSchematic(SchematicWrapper schematic)
    {
        this.schematic = schematic;
    }

    /**
     * Returns the work interval of the worker.
     *
     * @return work interval
     */
    public int getWorkInterval()
    {
        //TODO
        //Constants.BUILDERWORKINTERFALL - this.getLevel();
        return 1;
    }

    /**
     * Do final completion when the Job's current work is complete.
     */
    public void complete()
    {
        final BlockPos buildingLocation = this.getWorkOrder().getBuildingLocation();
        final AbstractBuilding building = this.getCitizen().getColony().getBuilding(buildingLocation);

        if (building != null)
        {
            this.getCitizen().getColony().onBuildingUpgradeComplete(building, this.getWorkOrder().getUpgradeLevel());
        }

        getCitizen().getColony().getWorkManager().removeWorkOrder(workOrderId);
        setWorkOrder(null);
        setSchematic(null);
    }

    /**
     * Get the Work Order for the Job.
     * Warning: WorkOrder is not cached
     *
     * @return WorkOrderBuild for the Build
     */
    public WorkOrderBuild getWorkOrder()
    {
        return getColony().getWorkManager().getWorkOrder(workOrderId, WorkOrderBuild.class);
    }

    /**
     * Set a Work Order for this Job.
     *
     * @param order Work Order to associate with this job, or null
     */
    public void setWorkOrder(@Nullable WorkOrderBuild order)
    {
        if (order == null)
        {
            workOrderId = 0;
        }
        else
        {
            workOrderId = order.getID();
        }
    }
}
