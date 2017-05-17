package com.minecolonies.coremod.colony.workorders;

import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.Structures;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.BuildingBuilder;
import com.minecolonies.coremod.colony.jobs.JobBuilder;
import com.minecolonies.coremod.util.BlockPosUtil;
import com.minecolonies.coremod.util.LanguageHandler;
import com.minecolonies.coremod.util.Log;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Represents one building order to complete.
 * Has his own structure for the building.
 */
public class WorkOrderBuild extends WorkOrderBuildDecoration
{
    private static final String TAG_UPGRADE_LEVEL  = "upgradeLevel";
    private static final String TAG_UPGRADE_NAME   = "upgrade";

    private   int      upgradeLevel;
    private   String   upgradeName;

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
        this.isMirrored = building.getTileEntity() == null ? building.isMirrored() : building.getTileEntity().isMirrored();
        this.cleared = level > 1;

        //normalize the structureName
        Structures.StructureName sn = new Structures.StructureName(Structures.SCHEMATICS_PREFIX, building.getStyle(), this.getUpgradeName());
        if(building.getTileEntity() != null && !building.getTileEntity().getStyle().isEmpty())
        {
            final String previousStructureName = sn.toString();
            sn = new Structures.StructureName(Structures.SCHEMATICS_PREFIX, building.getTileEntity().getStyle(), this.getUpgradeName());
            Log.getLogger().info("WorkOrderBuild at location " + this.buildingLocation + " is using " +  sn + " instead of " + previousStructureName);
        }


        this.structureName = sn.toString();
        this.workOrderName = this.structureName;
        this.md5 = Structures.getMD5(this.structureName);
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
        upgradeLevel = compound.getInteger(TAG_UPGRADE_LEVEL);
        upgradeName = compound.getString(TAG_UPGRADE_NAME);
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
        compound.setInteger(TAG_UPGRADE_LEVEL, upgradeLevel);
        compound.setString(TAG_UPGRADE_NAME, upgradeName);
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

    @Override
    protected String getValue()
    {
        return upgradeName;
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
     * Checks if a builder may accept this workOrder.
     *
     * @param builderLevel the builder level.
     * @return true if he is able to.
     */
    @Override
    protected boolean canBuild(@NotNull final CitizenData citizen, @NotNull final Colony colony)
    {
        final int builderLevel = citizen.getWorkBuilding().getBuildingLevel();
        return builderLevel >= upgradeLevel || builderLevel == BuildingBuilder.MAX_BUILDING_LEVEL
                 || (citizen.getWorkBuilding() != null && citizen.getWorkBuilding().getID().equals(buildingLocation))
                 || isLocationTownhall(colony, buildingLocation);
    }

    @Override
    protected void sendBuilderMessage(@NotNull final Colony colony, final boolean hasBuilder, final boolean sendMessage)
    {
        if (hasSentMessageForThisWorkOrder)
        {
            return;
        }

        if (hasBuilder && sendMessage)
        {
            hasSentMessageForThisWorkOrder = true;
            LanguageHandler.sendPlayersMessage(colony.getMessageEntityPlayers(),
              "entity.builder.messageBuilderNecessary", Integer.toString(this.upgradeLevel));
        }

        if (!hasBuilder)
        {
            hasSentMessageForThisWorkOrder = true;
            LanguageHandler.sendPlayersMessage(colony.getMessageEntityPlayers(),
              "entity.builder.messageNoBuilder");
        }
    }

    @Override
    public void onRemoved(final Colony colony)
    {
        super.onRemoved(colony);
        final AbstractBuilding building = colony.getBuilding(getBuildingLocation());
        if (building != null)
        {
            building.markDirty();
        }
    }
}
