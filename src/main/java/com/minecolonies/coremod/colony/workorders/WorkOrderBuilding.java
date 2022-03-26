package com.minecolonies.coremod.colony.workorders;

import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.workorders.IWorkManager;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.coremod.entity.ai.citizen.builder.ConstructionTapeHelper;
import com.minecolonies.coremod.util.AdvancementUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

/**
 * Represents one building order to complete. Has his own structure for the building.
 */
public abstract class WorkOrderBuilding extends AbstractWorkOrder
{
    private static final String TAG_UPGRADE_LEVEL = "upgradeLevel";
    private static final String TAG_SCHEMATIC_NAME = "upgrade";
    private static final String TAG_DISP_NAME     = "displayname";

    /**
     * Max distance a builder can have from the building site.
     */
    private static final double MAX_DISTANCE_SQ = 100 * 100;

    /**
     * The level the building should be upgraded to
     */
    private int currentLevel;

    /**
     * The level the building should be upgraded to
     */
    private int upgradeLevel;

    /**
     * The schematic this building is using
     */
    private String schematicName;

    /**
     * The name of the structure
     */
    private String structureName;

    /**
     * The resource key of the building
     */
    private String buildingNameResourceKey;

    /**
     * The building its custom name
     */
    private String customBuildingName;

    private BlockPos buildingLocation;

    private int buildingRotation;

    private boolean isBuildingMirrored;

    /**
     * Unused constructor for reflection.
     */
    public WorkOrderBuilding()
    {
        super();
    }

    /**
     * Create a new WorkOrder.
     *
     * @param building the building to build.
     * @param level    the level it should have.
     */
    public WorkOrderBuilding(@NotNull final IBuilding building, final int level)
    {
        super();
        this.currentLevel = building.getBuildingLevel();
        this.upgradeLevel = level;
        this.buildingNameResourceKey = building.getBuildingType().getTranslationKey();
        this.buildingLocation = building.getID();
        this.buildingRotation = building.getRotation();
        this.isBuildingMirrored = building.getTileEntity() == null ? building.isMirrored() : building.getTileEntity().isMirrored();

        final TileEntity buildingTE = building.getColony().getWorld().getBlockEntity(building.getID());
        if (buildingTE instanceof AbstractTileEntityColonyBuilding)
        {
            if (!((AbstractTileEntityColonyBuilding) buildingTE).getSchematicName().isEmpty())
            {
                this.schematicName = ((AbstractTileEntityColonyBuilding) buildingTE).getSchematicName()
                        .replaceAll("\\d$", "") + level;
            }
            else
            {
                this.schematicName = building.getSchematicName() + level;
            }
        }
        else
        {
            this.schematicName = building.getSchematicName() + level;
        }

        //normalize the structureName
        this.structureName = new StructureName(Structures.SCHEMATICS_PREFIX, building.getStyle(), this.schematicName)
                .toString();

        this.customBuildingName = "";
        if (building.hasParent())
        {
            final IBuilding parentBuilding = building.getColony().getBuildingManager().getBuilding(building.getParent());
            if (parentBuilding != null)
            {
                this.customBuildingName = parentBuilding.getCustomBuildingName() + " / ";
            }
        }

        this.customBuildingName += building.getCustomBuildingName();
    }

    @Override
    protected String getSchematicName()
    {
        return schematicName;
    }

    @Override
    public String getWorkOrderName()
    {
        return buildingNameResourceKey;
    }

    @Override
    protected String getCustomName()
    {
        return customBuildingName;
    }

    @Override
    protected BlockPos getLocation()
    {
        return buildingLocation;
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
        schematicName = compound.getString(TAG_SCHEMATIC_NAME);
        customBuildingName = compound.getString(TAG_DISP_NAME);
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
        compound.putString(TAG_SCHEMATIC_NAME, schematicName);
        compound.putString(TAG_DISP_NAME, customBuildingName);
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
        return canBuildIgnoringDistance(building.getPosition(), building.getBuildingLevel())
                 && citizen.getWorkBuilding().getPosition().distSqr(getLocation()) <= MAX_DISTANCE_SQ;
    }

    /**
     * Checks if a builder may accept this workOrder while ignoring the distance to the builder.
     * @param builderLocation position of the builders own hut.
     * @param builderLevel level of the builders hut.
     * @return true if so.
     */
    private boolean canBuildIgnoringDistance(@NotNull final BlockPos builderLocation, final int builderLevel)
    {
        //  A Build WorkOrder may be fulfilled by a Builder as long as any ONE of the following is true:
        //  - The Builder's Work AbstractBuilding is built
        //  - OR the WorkOrder is for the Builder's Work AbstractBuilding

        return (builderLevel >= upgradeLevel || builderLevel == BuildingBuilder.MAX_BUILDING_LEVEL || (builderLocation.equals(getLocation())));
    }

    @Override
    public boolean tooFarFromAnyBuilder(final IColony colony, final int level)
    {
        return colony.getBuildingManager()
                 .getBuildings()
                 .values()
                 .stream()
                 .noneMatch(building -> building instanceof BuildingBuilder && !building.getAllAssignedCitizen().isEmpty()
                                          && building.getPosition().distSqr(getLocation()) <= MAX_DISTANCE_SQ);
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
        return super.isValid(colony) && colony.getBuildingManager().getBuilding(getLocation()) != null;
    }

    @Override
    public ITextComponent getCustomBuildingName()
    {
        if (customBuildingName.isEmpty())
        {
            return new TranslationTextComponent(schematicName);
        }

        return new TranslationTextComponent(customBuildingName);
    }

    @Override
    public void onCompleted(final IColony colony, ICitizenData citizen)
    {
        super.onCompleted(colony, citizen);

        final StructureName structureName = new StructureName(getStructureName());
        if (this instanceof WorkOrderBuildingBuild)
        {
            final int level = this.getUpgradeLevel();
            AdvancementUtils.TriggerAdvancementPlayersForColony(colony, player ->
                    AdvancementTriggers.COMPLETE_BUILD_REQUEST.trigger(player, structureName, level));
        }
        else
        {
            AdvancementUtils.TriggerAdvancementPlayersForColony(colony, player ->
                    AdvancementTriggers.COMPLETE_BUILD_REQUEST.trigger(player, structureName, 0));
        }
    }

    @Override
    public void onAdded(final IColony colony, final boolean readingFromNbt)
    {
        if (!readingFromNbt && colony != null && colony.getWorld() != null)
        {
            final IBuilding building = colony.getBuildingManager().getBuilding(getLocation());
            if (building != null)
            {
                ConstructionTapeHelper.placeConstructionTape(building.getCorners(), colony.getWorld());
            }
        }
    }

    @Override
    public void onRemoved(final IColony colony)
    {
        final IBuilding building = colony.getBuildingManager().getBuilding(getLocation());
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
