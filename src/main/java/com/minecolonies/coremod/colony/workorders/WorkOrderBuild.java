package com.minecolonies.coremod.colony.workorders;

import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.workorders.IWorkManager;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.coremod.entity.ai.citizen.builder.ConstructionTapeHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Represents one building order to complete. Has his own structure for the building.
 */
public class WorkOrderBuild extends WorkOrderBuildDecoration
{
    private static final String TAG_UPGRADE_LEVEL = "upgradeLevel";
    private static final String TAG_UPGRADE_NAME  = "upgrade";
    private static final String TAG_DISP_NAME     = "displayname";

    /**
     * Max distance a builder can have from the building site.
     */
    private static final double MAX_DISTANCE_SQ = 100 * 100;

    private int    upgradeLevel;
    private String upgradeName;

    /**
     * The displayed name of the workorder
     */
    private String displayName = "";

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

        final TileEntity buildingTE = building.getColony().getWorld().getBlockEntity(buildingLocation);
        if (buildingTE instanceof AbstractTileEntityColonyBuilding)
        {
            if (!((AbstractTileEntityColonyBuilding) buildingTE).getSchematicName().isEmpty())
            {
                this.upgradeName = ((AbstractTileEntityColonyBuilding) buildingTE).getSchematicName().replaceAll("\\d$", "") + level;
            }
            else
            {
                this.upgradeName = building.getSchematicName() + level;
            }
        }
        else
        {
            this.upgradeName = building.getSchematicName() + level;
        }
        this.buildingRotation = building.getRotation();
        this.isBuildingMirrored = building.getTileEntity() == null ? building.isMirrored() : building.getTileEntity().isMirrored();
        this.cleared = level > 1;

        //normalize the structureName
        StructureName sn = new StructureName(Structures.SCHEMATICS_PREFIX, building.getStyle(), this.getUpgradeName());
        this.structureName = sn.toString();
        this.workOrderName = this.structureName;

        if (building.hasParent())
        {
            final IBuilding parentBuilding = building.getColony().getBuildingManager().getBuilding(building.getParent());
            if (parentBuilding != null)
            {
                displayName = parentBuilding.getCustomBuildingName() + "/";
            }
        }

        displayName += building.getCustomBuildingName() + level;
    }

    @Override
    public void serializeViewNetworkData(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(id);
        buf.writeInt(getPriority());
        buf.writeBlockPos(getClaimedBy() == null ? BlockPos.ZERO : getClaimedBy());
        buf.writeInt(getType().ordinal());
        buf.writeUtf(upgradeName);
        buf.writeUtf(getDisplayName());
        buf.writeBlockPos(buildingLocation == null ? BlockPos.ZERO : buildingLocation);
        buf.writeInt(upgradeLevel);
        //value is upgradeName and upgradeLevel for workOrderBuild
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
     *
     * @param compound NBT Tag compound.
     * @param manager  the work manager.
     */
    @Override
    public void read(@NotNull final CompoundNBT compound, final IWorkManager manager)
    {
        super.read(compound, manager);
        upgradeLevel = compound.getInt(TAG_UPGRADE_LEVEL);
        upgradeName = compound.getString(TAG_UPGRADE_NAME);
        displayName = compound.getString(TAG_DISP_NAME);
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
        compound.putString(TAG_DISP_NAME, displayName);
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

        final IBuilding building = citizen.getWorkBuilding();
        return canBuildIngoringDistance(building.getPosition(), building.getBuildingLevel())
                 && citizen.getWorkBuilding().getPosition().distSqr(this.getSchematicLocation()) <= MAX_DISTANCE_SQ;
    }

    /**
     * Checks if a builder may accept this workOrder while ignoring the distance to the builder.
     * @param builderLocation position of the builders own hut.
     * @param builderLevel level of the builders hut.
     * @return true if so.
     */
    public boolean canBuildIngoringDistance(@NotNull final BlockPos builderLocation, final int builderLevel)
    {
        //  A Build WorkOrder may be fulfilled by a Builder as long as any ONE of the following is true:
        //  - The Builder's Work AbstractBuilding is built
        //  - OR the WorkOrder is for the Builder's Work AbstractBuilding

        return (builderLevel >= upgradeLevel || builderLevel == BuildingBuilder.MAX_BUILDING_LEVEL || (builderLocation.equals(buildingLocation)));
    }

    @Override
    public boolean tooFarFromAnyBuilder(final IColony colony, final int level)
    {
        return colony.getBuildingManager()
                 .getBuildings()
                 .values()
                 .stream()
                 .noneMatch(building -> building instanceof BuildingBuilder && !building.getAllAssignedCitizen().isEmpty()
                                          && building.getPosition().distSqr(this.getSchematicLocation()) <= MAX_DISTANCE_SQ);
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
    public String getDisplayName()
    {
        if (displayName.isEmpty())
        {
            return upgradeName;
        }

        return displayName;
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
            final IBuilding building = colony.getBuildingManager().getBuilding(this.getSchematicLocation());
            if (building != null)
            {
                ConstructionTapeHelper.placeConstructionTape(building.getCorners(), colony.getWorld());
            }
        }
    }

    @Override
    public void onRemoved(final IColony colony)
    {
        final IBuilding building = colony.getBuildingManager().getBuilding(getSchematicLocation());
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
