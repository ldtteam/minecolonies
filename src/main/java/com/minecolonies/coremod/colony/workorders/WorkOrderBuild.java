package com.minecolonies.coremod.colony.workorders;

import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.workorders.IWorkManager;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.coremod.entity.ai.citizen.builder.ConstructionTapeHelper;
import net.minecraft.nbt.CompoundNBT;
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

    /**
     * Max distance a builder can have from the building site.
     */
    private static final double MAX_DISTANCE_SQ = 100*100;

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
    public WorkOrderBuild(@NotNull final IBuilding building, final int level)
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
     * Read the WorkOrder data from the CompoundNBT.
     *  @param compound NBT Tag compound.
     * @param manager
     */
    @Override
    public void read(@NotNull final CompoundNBT compound, final IWorkManager manager)
    {
        super.read(compound, manager);
        upgradeLevel = compound.getInt(TAG_UPGRADE_LEVEL);
        upgradeName = compound.getString(TAG_UPGRADE_NAME);
    }

    /**
     * Save the Work Order to an CompoundNBT.
     *
     * @param compound NBT tag compound.
     */
    @Override
    public void write(@NotNull final CompoundNBT compound)
    {
        super.write(compound);
        compound.putInt(TAG_UPGRADE_LEVEL, upgradeLevel);
        compound.putString(TAG_UPGRADE_NAME, upgradeName);
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
            LanguageHandler.sendPlayersMessage(colony.getMessagePlayerEntities(),
              "entity.builder.messageBuilderNecessary", Integer.toString(this.upgradeLevel));
        }

        if (!hasBuilder)
        {
            hasSentMessageForThisWorkOrder = true;
            LanguageHandler.sendPlayersMessage(colony.getMessagePlayerEntities(),
              "entity.builder.messageNoBuilder");
        }
    }

    @Override
    public boolean canBuild(@NotNull final ICitizenData citizen)
    {
        //  A Build WorkOrder may be fulfilled by a Builder as long as any ONE of the following is true:
        //  - The Builder's Work AbstractBuilding is built
        //  - OR the WorkOrder is for the Builder's Work AbstractBuilding
        //  - OR the WorkOrder is for the TownHall
        //  - OR the WorkOrder is not farther away than 100 blocks from any builder

        final int builderLevel = citizen.getWorkBuilding().getBuildingLevel();
        return (builderLevel >= upgradeLevel || builderLevel == BuildingBuilder.MAX_BUILDING_LEVEL
                  || (citizen.getWorkBuilding() != null && citizen.getWorkBuilding().getID().equals(buildingLocation))
                  || isLocationTownhall(citizen.getColony(), buildingLocation)
                       && citizen.getWorkBuilding().getPosition().distanceSq(this.getBuildingLocation()) <= MAX_DISTANCE_SQ);
    }

    @Override
    public boolean tooFarFromAnyBuilder(final IColony colony, final int level)
    {
        return colony.getBuildingManager().getBuildings().values().stream().noneMatch(building -> building instanceof BuildingBuilder && building.getMainCitizen() != null && building.getPosition().distanceSq(this.getBuildingLocation()) <= MAX_DISTANCE_SQ);
    }

    /**
     * Is this WorkOrder still valid?  If not, it will be deleted.
     *
     * @param colony The colony that owns the Work Order.
     * @return True if the building for this work order still exists.
     */
    @Override
    public boolean isValid(@NotNull final IColony colony)
    {
        return super.isValid(colony) && colony.getBuildingManager().getBuilding(buildingLocation) != null;
    }

    @Override
    protected String get()
    {
        return upgradeName;
    }

    @Override
    public int getRotation(final World world)
    {
        return buildingRotation;
    }

    @Override
    public void onAdded(final IColony colony, final boolean readingFromNbt)
    {
        if (!readingFromNbt && colony != null && colony.getWorld() != null)
        {
            final IBuilding building = colony.getBuildingManager().getBuilding(this.getBuildingLocation());
            if (building != null)
            {
                ConstructionTapeHelper.placeConstructionTape(building.getPosition(), building.getCorners(), colony.getWorld());
            }
        }
    }

    @Override
    public void onRemoved(final IColony colony)
    {
        final IBuilding building = colony.getBuildingManager().getBuilding(getBuildingLocation());
        if (building != null)
        {
            building.markDirty();
            ConstructionTapeHelper.removeConstructionTape(building.getCorners(), colony.getWorld());
        }
    }

    private static boolean isLocationTownhall(@NotNull final IColony colony, final BlockPos buildingLocation)
    {
        return colony.hasTownHall() && colony.getBuildingManager().getTownHall() != null && colony.getBuildingManager().getTownHall().getID().equals(buildingLocation);
    }

    @Override
    public void onCompleted(final IColony colony)
    {
        super.onCompleted(colony);
        final BlockPos buildingLocation = getBuildingLocation();
        final IBuilding building = colony.getBuildingManager().getBuilding(buildingLocation);
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
