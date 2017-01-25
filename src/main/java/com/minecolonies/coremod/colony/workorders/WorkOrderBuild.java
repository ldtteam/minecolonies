package com.minecolonies.coremod.colony.workorders;

import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.BuildingBuilder;
import com.minecolonies.coremod.colony.jobs.JobBuilder;
import com.minecolonies.coremod.lib.Constants;
import com.minecolonies.coremod.util.BlockPosUtil;
import com.minecolonies.coremod.util.LanguageHandler;
import com.minecolonies.coremod.util.Log;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Represents one building order to complete.
 * Has his onw structure for the building.
 */
public class WorkOrderBuild extends AbstractWorkOrder
{
    private static final String TAG_BUILDING      = "building";
    private static final String TAG_UPGRADE_LEVEL = "upgradeLevel";
    private static final String TAG_UPGRADE_NAME  = "upgrade";
    private static final String TAG_IS_CLEARED    = "cleared";
    private static final String TAG_IS_REQUESTED  = "requested";

    private static final String TAG_SCHEMATIC_NAME    = "structureName";
    private static final String TAG_BUILDING_ROTATION = "buildingRotation";

    private static final String DEFAULT_STYLE = "wooden";

    protected BlockPos buildingLocation;
    protected int      buildingRotation;
    protected String   structureName;
    protected boolean  cleared;
    private   int      upgradeLevel;
    private   String   upgradeName;
    private boolean hasSentMessageForThisWorkOrder = false;
    private boolean requested;

    /**
     * Unused constructor for reflection.
     */
    public WorkOrderBuild()
    {
        super();
    }

    /**
     * Create a new WorkOrder.
     *
     * @param building the building to build.
     * @param level    the level it should have.
     */
    public WorkOrderBuild(@NotNull final AbstractBuilding building, final int level)
    {
        super();
        this.buildingLocation = building.getID();
        this.upgradeLevel = level;
        this.upgradeName = building.getSchematicName() + level;
        this.buildingRotation = building.getRotation();
        this.cleared = level > 1;
        this.requested = false;

        if (MinecraftServer.class.getResourceAsStream("/assets/" + Constants.MOD_ID + "/schematics/" + building.getStyle() + '/' + this.getUpgradeName() + ".nbt") == null)
        {
            Log.getLogger().warn(String.format("StructureProxy in Style (%s) does not exist - switching to default", building.getStyle()));
            this.structureName = DEFAULT_STYLE + '/' + this.getUpgradeName();
            return;
        }
        this.structureName = building.getStyle() + '/' + this.getUpgradeName();
    }

    /**
     * Returns the name after upgrade.
     *
     * @return Name after upgrade.
     */
    private String getUpgradeName()
    {
        return upgradeName;
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
        if (!(this instanceof WorkOrderBuildDecoration))
        {
            upgradeLevel = compound.getInteger(TAG_UPGRADE_LEVEL);
            upgradeName = compound.getString(TAG_UPGRADE_NAME);
        }
        cleared = compound.getBoolean(TAG_IS_CLEARED);
        structureName = compound.getString(TAG_SCHEMATIC_NAME);
        buildingRotation = compound.getInteger(TAG_BUILDING_ROTATION);
        requested = compound.getBoolean(TAG_IS_REQUESTED);
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
        if (!(this instanceof WorkOrderBuildDecoration))
        {
            compound.setInteger(TAG_UPGRADE_LEVEL, upgradeLevel);
            compound.setString(TAG_UPGRADE_NAME, upgradeName);
        }
        compound.setBoolean(TAG_IS_CLEARED, cleared);
        compound.setString(TAG_SCHEMATIC_NAME, structureName);
        compound.setInteger(TAG_BUILDING_ROTATION, buildingRotation);
        compound.setBoolean(TAG_IS_REQUESTED, requested);
    }

    /**
     * Is this WorkOrder still valid?  If not, it will be deleted.
     *
     * @param colony The colony that owns the Work Order.
     * @return True if the building for this work order still exists.
     */
    @Override
    public boolean isValid(@NotNull final Colony colony)
    {
        return colony.getBuilding(buildingLocation) != null;
    }

    /**
     * Attempt to fulfill the Work Order.
     * Override this with an implementation for the Work Order to find a Citizen to perform the job
     * <p>
     * finds the first suitable builder for this job.
     *
     * @param colony The colony that owns the Work Order.
     */
    @Override
    public void attemptToFulfill(@NotNull final Colony colony)
    {
        boolean sendMessage = true;
        boolean hasBuilder = false;

        for (@NotNull final CitizenData citizen : colony.getCitizens().values())
        {
            final JobBuilder job = citizen.getJob(JobBuilder.class);

            if (job == null)
            {
                continue;
            }

            hasBuilder = true;

            final int builderLevel = citizen.getWorkBuilding().getBuildingLevel();

            // don't send a message if we have a valid worker that is busy.
            if (canBuildHut(builderLevel, citizen, colony))
            {
                sendMessage = false;
            }

            if (job.hasWorkOrder())
            {
                continue;
            }

            //  A Build WorkOrder may be fulfilled by a Builder as long as any ONE of the following is true:
            //  - The Builder's Work AbstractBuilding is built
            //  - OR the WorkOrder is for the Builder's Work AbstractBuilding
            //  - OR the WorkOrder is for the TownHall
            if (canBuildHut(builderLevel, citizen, colony))
            {
                job.setWorkOrder(this);
                this.setClaimedBy(citizen);
                return;
            }
        }

        sendBuilderMessage(colony, hasBuilder, sendMessage);
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
        return upgradeName;
    }

    /**
     * Checks if a builder may accept this workOrder.
     *
     * @param builderLevel the builder level.
     * @return true if he is able to.
     */
    private boolean canBuildHut(final int builderLevel, @NotNull final CitizenData citizen, @NotNull final Colony colony)
    {
        return builderLevel >= upgradeLevel || builderLevel == BuildingBuilder.MAX_BUILDING_LEVEL
                 || (citizen.getWorkBuilding() != null && citizen.getWorkBuilding().getID().equals(buildingLocation))
                 || isLocationTownhall(colony, buildingLocation);
    }

    private void sendBuilderMessage(@NotNull final Colony colony, final boolean hasBuilder, final boolean sendMessage)
    {
        if (hasSentMessageForThisWorkOrder)
        {
            return;
        }

        if (hasBuilder && sendMessage)
        {
            hasSentMessageForThisWorkOrder = true;
            LanguageHandler.sendPlayersLocalizedMessage(colony.getMessageEntityPlayers(),
              "entity.builder.messageBuilderNecessary", Integer.toString(this.upgradeLevel));
        }

        if (!hasBuilder)
        {
            hasSentMessageForThisWorkOrder = true;
            LanguageHandler.sendPlayersLocalizedMessage(colony.getMessageEntityPlayers(),
              "entity.builder.messageNoBuilder");
        }
    }

    private static boolean isLocationTownhall(@NotNull final Colony colony, final BlockPos buildingLocation)
    {
        return colony.hasTownHall() && colony.getTownHall() != null && colony.getTownHall().getID().equals(buildingLocation);
    }

    /**
     * Returns the level up level of the building.
     *
     * @return Level after upgrade.
     */
    public int getUpgradeLevel()
    {
        return upgradeLevel;
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
        return this.structureName;
    }

    /**
     * Gets how many times this structure should be rotated.
     *
     * @return building rotation.
     */
    public int getRotation()
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
}
