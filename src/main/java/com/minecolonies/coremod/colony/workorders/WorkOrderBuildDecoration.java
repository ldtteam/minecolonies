package com.minecolonies.coremod.colony.workorders;

import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.workorders.IWorkManager;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.entity.ai.citizen.builder.ConstructionTapeHelper;
import com.minecolonies.coremod.tileentities.TileEntityDecorationController;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_LEVEL;
import static com.minecolonies.api.util.constant.Suppression.UNUSED_METHOD_PARAMETERS_SHOULD_BE_REMOVED;

/**
 * A work order that the build can take to build decorations.
 */
public class WorkOrderBuildDecoration extends AbstractWorkOrder
{
    /**
     * NBT Tags for storage.
     */
    private static final String TAG_WORKORDER_NAME = "workOrderName";
    private static final String TAG_IS_CLEARED     = "cleared";
    private static final String TAG_IS_REQUESTED   = "requested";
    private static final String TAG_IS_MIRRORED    = "mirrored";

    private static final String TAG_SCHEMATIC_NAME    = "structureName";
    private static final String TAG_SCHEMATIC_MD5     = "schematicMD5";
    private static final String TAG_BUILDING_ROTATION = "buildingRotation";
    private static final String TAG_AMOUNT_OF_RES     = "resQuantity";

    protected boolean  isBuildingMirrored;
    protected int      buildingRotation;
    protected String   structureName;
    protected String   md5;
    protected boolean  cleared;
    protected String   workOrderName;
    protected int      amountOfRes;
    protected boolean levelUp = false;
    protected boolean hasSentMessageForThisWorkOrder = false;
    private   boolean requested;

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
        final StructureName sn = new StructureName(structureName);
        this.structureName = sn.toString();
        this.workOrderName = workOrderName;
        this.buildingRotation = rotation;
        this.buildingLocation = location;
        this.cleared = false;
        this.isBuildingMirrored = mirror;
        this.requested = false;
    }

    /**
     * Make a decoration level up with this.
     */
    public void setLevelUp()
    {
        this.levelUp = true;
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
     * Read the WorkOrder data from the CompoundNBT.
     *
     * @param compound NBT Tag compound.
     */
    @Override
    public void readFromNBT(@NotNull final CompoundNBT compound, final IWorkManager manager)
    {
        super.readFromNBT(compound, manager);
        final StructureName sn = new StructureName(compound.getString(TAG_SCHEMATIC_NAME));
        structureName = sn.toString();
        workOrderName = compound.getString(TAG_WORKORDER_NAME);
        cleared = compound.getBoolean(TAG_IS_CLEARED);
        md5 = compound.getString(TAG_SCHEMATIC_MD5);
        if (!Structures.hasMD5(structureName))
        {
            // If the schematic move we can use the MD5 hash to find it
            final StructureName newSN = Structures.getStructureNameByMD5(md5);
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

        buildingRotation = compound.getInt(TAG_BUILDING_ROTATION);
        requested = compound.getBoolean(TAG_IS_REQUESTED);
        isBuildingMirrored = compound.getBoolean(TAG_IS_MIRRORED);
        amountOfRes = compound.getInt(TAG_AMOUNT_OF_RES);
        levelUp = compound.getBoolean(TAG_LEVEL);
    }

    /**
     * Save the Work Order to an CompoundNBT.
     *
     * @param compound NBT tag compound.
     */
    @Override
    public void writeToNBT(@NotNull final CompoundNBT compound)
    {
        super.writeToNBT(compound);
        if (workOrderName != null)
        {
            compound.putString(TAG_WORKORDER_NAME, workOrderName);
        }
        compound.putBoolean(TAG_IS_CLEARED, cleared);
        if (md5 != null)
        {
            compound.putString(TAG_SCHEMATIC_MD5, md5);
        }
        if (structureName == null)
        {
            Log.getLogger().error("WorkOrderBuild.writeToNBT: structureName should not be null!!!");
        }
        else
        {
            compound.putString(TAG_SCHEMATIC_NAME, structureName);
        }
        compound.putInt(TAG_BUILDING_ROTATION, buildingRotation);
        compound.putBoolean(TAG_IS_REQUESTED, requested);
        compound.putBoolean(TAG_IS_MIRRORED, isBuildingMirrored);
        compound.putInt(TAG_AMOUNT_OF_RES, amountOfRes);
        compound.putBoolean(TAG_LEVEL, levelUp);
    }

    @Override
    public boolean isValid(final IColony colony)
    {
        return true;
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
    @SuppressWarnings(UNUSED_METHOD_PARAMETERS_SHOULD_BE_REMOVED)
    protected boolean canBuild(@NotNull final ICitizenData citizen)
    {
        return true;
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
    @SuppressWarnings(UNUSED_METHOD_PARAMETERS_SHOULD_BE_REMOVED)
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

    @Override
    public void onAdded(final IColony colony, final boolean readingFromNbt)
    {
        super.onAdded(colony, readingFromNbt);
        if (!readingFromNbt && colony != null && colony.getWorld() != null)
        {
            ConstructionTapeHelper.placeConstructionTape(this, colony.getWorld());
        }
    }

    @Override
    public void onCompleted(final IColony colony)
    {
        super.onCompleted(colony);

        if (this.levelUp)
        {
            final TileEntity tileEntity = colony.getWorld().getTileEntity(buildingLocation);
            if (tileEntity instanceof TileEntityDecorationController)
            {
                ((TileEntityDecorationController) tileEntity).setLevel(((TileEntityDecorationController) tileEntity).getLevel() + 1);
            }
        }
    }

    @Override
    public void onRemoved(final IColony colony)
    {
        super.onRemoved(colony);
        ConstructionTapeHelper.removeConstructionTape(this, colony.getWorld());
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
    @SuppressWarnings(UNUSED_METHOD_PARAMETERS_SHOULD_BE_REMOVED)
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
        return isBuildingMirrored;
    }

    /**
     * Set the amount of resources this building requires.
     * @param amountOfRes the amount.
     */
    public void setAmountOfRes(final int amountOfRes)
    {
        this.amountOfRes = amountOfRes;
    }

    /**
     * Amount of resources this building requires.
     * @return the amount.
     */
    public int getAmountOfRes()
    {
        return amountOfRes;
    }
}
