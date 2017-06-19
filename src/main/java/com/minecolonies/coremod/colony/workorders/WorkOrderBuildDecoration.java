package com.minecolonies.coremod.colony.workorders;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.Structures;
import com.minecolonies.coremod.colony.jobs.JobBuilder;
import com.minecolonies.coremod.entity.ai.citizen.builder.ConstructionTapeHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * A work order that the build can take to build decorations.
 */
public class WorkOrderBuildDecoration extends AbstractWorkOrder
{
    private static final String TAG_BUILDING       = "building";
    private static final String TAG_WORKORDER_NAME = "workOrderName";
    private static final String TAG_IS_CLEARED     = "cleared";
    private static final String TAG_IS_REQUESTED   = "requested";
    private static final String TAG_IS_MIRRORED    = "mirrored";

    private static final String TAG_SCHEMATIC_NAME    = "structureName";
    private static final String TAG_SCHEMATIC_MD5     = "schematicMD5";
    private static final String TAG_BUILDING_ROTATION = "buildingRotation";

    protected boolean  isMirrored;
    protected BlockPos buildingLocation;
    protected int      buildingRotation;
    protected String   structureName;
    protected String   md5;
    protected boolean  cleared;
    protected String   workOrderName;

    protected boolean hasSentMessageForThisWorkOrder = false;
    private boolean requested;

    /**
     * Unused constructor for reflection.
     */
    public WorkOrderBuildDecoration()
    {
        super();
    }

    /**
     * Create a new work order telling the building to build a decoration.
     *
     * @param structureName The name of the decoration.
     * @param workOrderName The user friendly name of the decoration.
     * @param rotation      The number of times the decoration was rotated.
     * @param location      The location where the decoration should be built.
     * @param mirror        Is the decoration mirrored?
     */
    public WorkOrderBuildDecoration(final String structureName, final String workOrderName, final int rotation, final BlockPos location, final boolean mirror)
    {
        super();
        //normalise structure name
        final Structures.StructureName sn = new Structures.StructureName(structureName);
        this.structureName = sn.toString();
        this.workOrderName = workOrderName;
        this.buildingRotation = rotation;
        this.buildingLocation = location;
        this.cleared = false;
        this.isMirrored = mirror;
        this.requested = false;
    }

    /**
     * Get the name of the work order.
     *
     * @return the work order name
     */
    public String getName()
    {
        return workOrderName;
    }

    /**
     * Read the WorkOrder data from the NBTTagCompound.
     *
     * @param compound NBT Tag compound.
     */
    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        buildingLocation = BlockPosUtil.readFromNBT(compound, TAG_BUILDING);
        final Structures.StructureName sn = new Structures.StructureName(compound.getString(TAG_SCHEMATIC_NAME));
        structureName = sn.toString();
        workOrderName = compound.getString(TAG_WORKORDER_NAME);
        cleared = compound.getBoolean(TAG_IS_CLEARED);
        md5 = compound.getString(TAG_SCHEMATIC_MD5);
        if (!Structures.hasMD5(structureName))
        {
            // If the schematic move we can use the MD5 hash to find it
            final Structures.StructureName newSN = Structures.getStructureNameByMD5(md5);
            if (newSN == null)
            {
                Log.getLogger().error("WorkOrderBuildDecoration.readFromNBT: Could not find " + structureName);
            }
            else
            {
                Log.getLogger().warn("WorkOrderBuildDecoration.readFromNBT: replace " + sn + " by " + newSN);
                structureName = newSN.toString();
            }
        }

        buildingRotation = compound.getInteger(TAG_BUILDING_ROTATION);
        requested = compound.getBoolean(TAG_IS_REQUESTED);
        isMirrored = compound.getBoolean(TAG_IS_MIRRORED);
    }

    /**
     * Save the Work Order to an NBTTagCompound.
     *
     * @param compound NBT tag compound.
     */
    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        BlockPosUtil.writeToNBT(compound, TAG_BUILDING, buildingLocation);
        if (workOrderName != null)
        {
            compound.setString(TAG_WORKORDER_NAME, workOrderName);
        }
        compound.setBoolean(TAG_IS_CLEARED, cleared);
        if (md5 != null)
        {
            compound.setString(TAG_SCHEMATIC_MD5, md5);
        }
        if (structureName == null)
        {
            Log.getLogger().error("WorkOrderBuild.writeToNBT: structureName should not be null!!!");
        }
        else
        {
            compound.setString(TAG_SCHEMATIC_NAME, structureName);
        }
        compound.setInteger(TAG_BUILDING_ROTATION, buildingRotation);
        compound.setBoolean(TAG_IS_REQUESTED, requested);
        compound.setBoolean(TAG_IS_MIRRORED, isMirrored);
    }

    /**
     * Attempt to fulfill the Work Order.
     * Override this with an implementation for the Work Order to find a Citizen to perform the job
     * <p>
     * finds the closest suitable builder for this job.
     *
     * @param colony The colony that owns the Work Order.
     */
    @Override
    public void attemptToFulfill(@NotNull final Colony colony)
    {
        boolean sendMessage = true;
        boolean hasBuilder = false;
        double distanceToBuilder = 0;
        CitizenData claimedBy = null;

        for (@NotNull final CitizenData citizen : colony.getCitizens().values())
        {
            final JobBuilder job = citizen.getJob(JobBuilder.class);

            if (job == null || citizen.getWorkBuilding() == null)
            {
                continue;
            }

            hasBuilder = true;

            // don't send a message if we have a valid worker that is busy.
            if (canBuild(citizen))
            {
                sendMessage = false;
            }

            if (!job.hasWorkOrder() && canBuild(citizen))
            {
                final double distance = citizen.getWorkBuilding().getID().distanceSq(this.buildingLocation);
                if(claimedBy == null || distance < distanceToBuilder)
                {
                    claimedBy = citizen;
                    distanceToBuilder = distance;
                }
            }
        }

        if(claimedBy != null)
        {
            final JobBuilder job = claimedBy.getJob(JobBuilder.class);
            job.setWorkOrder(this);
            this.setClaimedBy(claimedBy);
            return;
        }

        sendBuilderMessage(colony, hasBuilder, sendMessage);
    }

    /**
     * send a message from the builder.
     * <p>
     * Suppressing Sonar Rule squid:S1172
     * This rule does "Unused method parameters should be removed"
     * But in this case extending class may need to use the sendMessage parameter
     *
     * @param colony      which the work order belong to
     * @param hasBuilder  true if we have a builder for this work order
     * @param sendMessage true if we need to send the message
     */
    @SuppressWarnings("squid:S1172")
    protected void sendBuilderMessage(@NotNull final Colony colony, final boolean hasBuilder, final boolean sendMessage)
    {
        if (hasSentMessageForThisWorkOrder || hasBuilder)
        {
            return;
        }

        hasSentMessageForThisWorkOrder = true;
        LanguageHandler.sendPlayersMessage(colony.getMessageEntityPlayers(),
                "entity.builder.messageNoBuilder");
    }

    /**
     * Checks if a builder may accept this workOrder.
     * <p>
     * Suppressing Sonar Rule squid:S1172
     * This rule does "Unused method parameters should be removed"
     * But in this case extending class may need to use the citizen parameter
     *
     * @param citizen which could build it or not
     * @return true if he is able to.
     */
    @SuppressWarnings("squid:S1172")
    protected boolean canBuild(@NotNull final CitizenData citizen)
    {
        return true;
    }

    @Override
    public boolean isValid(final Colony colony)
    {
        return true;
    }

    @NotNull
    @Override
    protected WorkOrderType getType()
    {
        return WorkOrderType.BUILD;
    }

    @Override
    protected String getValue()
    {
        return workOrderName;
    }

    /**
     * Returns the ID of the building (aka ChunkCoordinates).
     *
     * @return ID of the building.
     */
    public BlockPos getBuildingLocation()
    {
        return buildingLocation;
    }

    /**
     * Get the name the structure for this work order.
     *
     * @return the internal string for this structure.
     */
    public String getStructureName()
    {
        return structureName;
    }

    /**
     * Gets how many times this structure should be rotated.
     * <p>
     * Suppressing Sonar Rule squid:S1172
     * This rule does "Unused method parameters should be removed"
     * But in this case extending class may need to use the world parameter
     *
     * @param world where the decoration is
     * @return building rotation.
     */
    @SuppressWarnings("squid:S1172")
    public int getRotation(final World world)
    {
        return buildingRotation;
    }

    /**
     * Gets whether or not the building has been cleared.
     *
     * @return true if the building has been cleared.
     */
    public boolean isCleared()
    {
        return cleared;
    }

    /**
     * Set whether or not the building has been cleared.
     *
     * @param cleared true if the building has been cleared.
     */
    public void setCleared(final boolean cleared)
    {
        this.cleared = cleared;
    }

    /**
     * Gets whether or not the building materials have been requested already.
     *
     * @return true if the materials has been requested.
     */
    public boolean isRequested()
    {
        return requested;
    }

    /**
     * Set whether or not the building materials have been requested already.
     *
     * @param requested true if so.
     */
    public void setRequested(final boolean requested)
    {
        this.requested = requested;
    }

    /**
     * Check if the workOrder should be built isMirrored.
     *
     * @return true if so.
     */
    public boolean isMirrored()
    {
        return isMirrored;
    }

    @Override
    public void onAdded(final Colony colony)
    {
        super.onAdded(colony);
        if (colony != null && colony.getWorld() != null)
        {
            ConstructionTapeHelper.placeConstructionTape(this, colony.getWorld());
        }
    }

    @Override
    public void onRemoved(final Colony colony)
    {
        super.onRemoved(colony);
        ConstructionTapeHelper.removeConstructionTape(this, colony.getWorld());
    }
}
