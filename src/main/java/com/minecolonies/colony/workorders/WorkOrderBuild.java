package com.minecolonies.colony.workorders;

import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.buildings.AbstractBuilding;
import com.minecolonies.colony.jobs.JobBuilder;
import com.minecolonies.util.BlockPosUtil;
import com.minecolonies.util.LanguageHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Represents one building order to complete.
 * Has his onw schematic for the building.
 */
public class WorkOrderBuild extends AbstractWorkOrder
{
    private static final String TAG_BUILDING          = "building";
    private static final String TAG_UPGRADE_LEVEL     = "upgradeLevel";
    private static final String TAG_UPGRADE_NAME      = "upgrade";
    private static final String TAG_IS_CLEARED        = "cleared";
    private static final String TAG_SCHEMATIC_NAME    = "schematicName";
    private static final String TAG_BUILDING_ROTATION = "buildingRotation";

    protected BlockPos buildingLocation;
    protected int      buildingRotation;
    protected String   schematicName;
    protected boolean  cleared;
    private   int      upgradeLevel;
    private   String   upgradeName;
    private boolean hasSentMessageForThisWorkOrder = false;

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
    public WorkOrderBuild(@NotNull AbstractBuilding building, int level)
    {
        super();
        this.buildingLocation = building.getID();
        this.upgradeLevel = level;
        this.upgradeName = building.getSchematicName() + level;
        this.schematicName = building.getStyle() + '/' + this.getUpgradeName();
        this.buildingRotation = building.getRotation();
        this.cleared = level > 1;
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
    public void readFromNBT(@NotNull NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        buildingLocation = BlockPosUtil.readFromNBT(compound, TAG_BUILDING);
        if (!(this instanceof WorkOrderBuildDecoration))
        {
            upgradeLevel = compound.getInteger(TAG_UPGRADE_LEVEL);
            upgradeName = compound.getString(TAG_UPGRADE_NAME);
        }
        cleared = compound.getBoolean(TAG_IS_CLEARED);
        schematicName = compound.getString(TAG_SCHEMATIC_NAME);
        buildingRotation = compound.getInteger(TAG_BUILDING_ROTATION);
    }

    /**
     * Save the Work Order to an NBTTagCompound.
     *
     * @param compound NBT tag compound.
     */
    @Override
    public void writeToNBT(@NotNull NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        BlockPosUtil.writeToNBT(compound, TAG_BUILDING, buildingLocation);
        if (!(this instanceof WorkOrderBuildDecoration))
        {
            compound.setInteger(TAG_UPGRADE_LEVEL, upgradeLevel);
            compound.setString(TAG_UPGRADE_NAME, upgradeName);
        }
        compound.setBoolean(TAG_IS_CLEARED, cleared);
        compound.setString(TAG_SCHEMATIC_NAME, schematicName);
        compound.setInteger(TAG_BUILDING_ROTATION, buildingRotation);
    }

    /**
     * Is this WorkOrder still valid?  If not, it will be deleted.
     *
     * @param colony The colony that owns the Work Order.
     * @return True if the building for this work order still exists.
     */
    @Override
    public boolean isValid(@NotNull Colony colony)
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
    public void attemptToFulfill(@NotNull Colony colony)
    {
        boolean sendMessage = true;
        boolean hasBuilder = false;

        for (@NotNull CitizenData citizen : colony.getCitizens().values())
        {
            JobBuilder job = citizen.getJob(JobBuilder.class);

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
    private boolean canBuildHut(int builderLevel, @NotNull CitizenData citizen, @NotNull Colony colony)
    {
        return builderLevel >= upgradeLevel || builderLevel == 2
                 || citizen.getWorkBuilding().getID().equals(buildingLocation)
                 || isLocationTownhall(colony, buildingLocation);
    }

    private void sendBuilderMessage(@NotNull Colony colony, boolean hasBuilder, boolean sendMessage)
    {
        if (hasSentMessageForThisWorkOrder)
        {
            return;
        }

        if (hasBuilder && sendMessage)
        {
            hasSentMessageForThisWorkOrder = true;
            LanguageHandler.sendPlayersLocalizedMessage(colony.getMessageEntityPlayers(),
              "entity.builder.messageBuilderNecessary", this.upgradeLevel);
        }

        if (!hasBuilder)
        {
            hasSentMessageForThisWorkOrder = true;
            LanguageHandler.sendPlayersLocalizedMessage(colony.getMessageEntityPlayers(),
              "entity.builder.messageNoBuilder");
        }
    }

    private boolean isLocationTownhall(@NotNull Colony colony, BlockPos buildingLocation)
    {
        return colony.hasTownHall() && colony.getTownHall().getID().equals(buildingLocation);
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
     * Get the name the schematic for this work order.
     *
     * @return the internal string for this schematic.
     */
    public String getSchematicName()
    {
        return this.schematicName;
    }

    /**
     * Gets how many times this schematic should be rotated.
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
    public void setCleared(boolean cleared)
    {
        this.cleared = cleared;
    }
}
