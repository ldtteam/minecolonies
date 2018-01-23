package com.minecolonies.coremod.colony.workorders;

import com.minecolonies.api.util.BlockUtils;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.blocks.AbstractBlockHut;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.StructureName;
import com.minecolonies.coremod.colony.Structures;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.BuildingBuilder;
import com.minecolonies.coremod.entity.ai.citizen.builder.ConstructionTapeHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Represents one building order to complete.
 * Has his own structure for the building.
 */
public class WorkOrderBuild extends WorkOrderBuildDecoration
{
    private static final String TAG_UPGRADE_LEVEL = "upgradeLevel";
    private static final String TAG_UPGRADE_NAME  = "upgrade";

    private int    upgradeLevel;
    private String upgradeName;

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
        this.isBuildingMirrored = building.getTileEntity() == null ? building.isMirrored() : building.getTileEntity().isMirrored();
        this.cleared = level > 1;

        //normalize the structureName
        StructureName sn = new StructureName(Structures.SCHEMATICS_PREFIX, building.getStyle(), this.getUpgradeName());
        if (building.getTileEntity() != null && !building.getTileEntity().getStyle().isEmpty())
        {
            final String previousStructureName = sn.toString();
            sn = new StructureName(Structures.SCHEMATICS_PREFIX, building.getTileEntity().getStyle(), this.getUpgradeName());
            Log.getLogger().info("WorkOrderBuild at location " + this.buildingLocation + " is using " + sn + " instead of " + previousStructureName);
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
    public String getUpgradeName()
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
    protected boolean canBuild(@NotNull final CitizenData citizen)
    {
        //  A Build WorkOrder may be fulfilled by a Builder as long as any ONE of the following is true:
        //  - The Builder's Work AbstractBuilding is built
        //  - OR the WorkOrder is for the Builder's Work AbstractBuilding
        //  - OR the WorkOrder is for the TownHall

        final int builderLevel = citizen.getWorkBuilding().getBuildingLevel();
        return builderLevel >= upgradeLevel || builderLevel == BuildingBuilder.MAX_BUILDING_LEVEL
                 || (citizen.getWorkBuilding() != null && citizen.getWorkBuilding().getID().equals(buildingLocation))
                 || isLocationTownhall(citizen.getColony(), buildingLocation);
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
        return colony.getBuildingManager().getBuilding(buildingLocation) != null;
    }

    @Override
    protected String getValue()
    {
        return upgradeName;
    }

    @Override
    public int getRotation(final World world)
    {
        if (buildingRotation == 0 && world != null)
        {
            final IBlockState blockState = world.getBlockState(buildingLocation);
            if (blockState.getBlock() instanceof AbstractBlockHut)
            {
                return BlockUtils.getRotationFromFacing(blockState.getValue(AbstractBlockHut.FACING));
            }
        }
        return buildingRotation;
    }

    @Override
    public void onAdded(final Colony colony)
    {
        if (colony != null && colony.getWorld() != null)
        {
            final AbstractBuilding building = colony.getBuildingManager().getBuilding(this.getBuildingLocation());
            if (building != null)
            {
                ConstructionTapeHelper.placeConstructionTape(building.getLocation(), building.getCorners(), colony.getWorld());
            }
        }
    }

    @Override
    public void onRemoved(final Colony colony)
    {
        final AbstractBuilding building = colony.getBuildingManager().getBuilding(getBuildingLocation());
        if (building != null)
        {
            building.markDirty();
            ConstructionTapeHelper.removeConstructionTape(building.getCorners(), colony.getWorld());
        }
    }

    private static boolean isLocationTownhall(@NotNull final Colony colony, final BlockPos buildingLocation)
    {
        return colony.hasTownHall() && colony.getBuildingManager().getTownHall() != null && colony.getBuildingManager().getTownHall().getID().equals(buildingLocation);
    }

    @Override
    public void onCompleted(final Colony colony)
    {
        final BlockPos buildingLocation = getBuildingLocation();
        final AbstractBuilding building = colony.getBuildingManager().getBuilding(buildingLocation);
        colony.onBuildingUpgradeComplete(building, getUpgradeLevel());
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
}
