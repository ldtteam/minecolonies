package com.minecolonies.colony.jobs;

import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.buildings.AbstractBuilding;
import com.minecolonies.colony.buildings.BuildingBuilder;
import com.minecolonies.colony.workorders.WorkOrderBuild;
import com.minecolonies.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.entity.ai.citizen.builder.EntityAIStructureBuilder;
import com.minecolonies.util.BlockPosUtil;
import com.minecolonies.util.StructureWrapper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The job of the builder.
 */
public class JobBuilder extends AbstractJob
{
    private static final String TAG_WORK_ORDER = "workorder";
    private static final String TAG_SCHEMATIC  = "schematic";
    private static final String TAG_NAME       = "name";
    private static final String TAG_POSITION   = "position";
    private static final String TAG_PROGRESS   = "progress";
    protected StructureWrapper schematic;
    //TODO save some of this in building
    private   int              workOrderId;
    private   String           schematicName;
    private   BlockPos         schematicPos;
    private   BlockPos         schematicProgress;

    /**
     * Instantiates builder job.
     * @param entity citizen.
     */
    public JobBuilder(final CitizenData entity)
    {
        super(entity);
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
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
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        if (workOrderId != 0)
        {
            compound.setInteger(TAG_WORK_ORDER, workOrderId);

            if (hasStructure())
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
     * Does this job have a loaded StructureProxy?
     * <p>
     * if a schematic is not null there exists a location for it
     *
     * @return true if there is a loaded schematic for this Job
     */
    public boolean hasStructure()
    {
        return schematic != null;
    }

    @NotNull
    @Override
    public AbstractAISkeleton<JobBuilder> generateAI()
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
     * Get the StructureProxy loaded by the Job.
     *
     * @return StructureProxy loaded by the Job
     */
    public StructureWrapper getStructure()
    {
        return schematic;
    }

    /**
     * Set the schematic of builder's job.
     *
     * @param schematic {@link StructureWrapper} object
     */
    public void setStructure(final StructureWrapper schematic)
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
        setStructure(null);
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
     * Reset the needed items list.
     */
    private void resetNeededItems()
    {
        final AbstractBuilding workerBuilding = this.getCitizen().getWorkBuilding();
        if(workerBuilding instanceof BuildingBuilder)
        {
            ((BuildingBuilder) workerBuilding).resetNeededResources();
        }
    }

    /**
     * Set a Work Order for this Job.
     *
     * @param order Work Order to associate with this job, or null
     */
    public void setWorkOrder(@Nullable final WorkOrderBuild order)
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
