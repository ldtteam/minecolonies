package com.minecolonies.coremod.colony.workorders;

import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.workorders.IWorkManager;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.coremod.colony.jobs.JobBuilder;
import com.minecolonies.coremod.entity.ai.citizen.builder.ConstructionTapeHelper;
import com.minecolonies.coremod.util.AdvancementUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

/**
 * Represents one building order to complete. Has his own structure for the building.
 */
public class WorkOrderBuilding extends AbstractWorkOrder
{
    private static final String TAG_CUSTOM_NAME            = "customName";
    private static final String TAG_CUSTOM_PARENT_NAME     = "customParentName";
    private static final String TAG_PARENT_TRANSLATION_KEY = "parentTranslationKey";

    /**
     * Maximum distance a builder can have from the building site.
     */
    private static final double MAX_DISTANCE_SQ = 100 * 100;

    /**
     * The custom name of the building.
     */
    private String customName;

    /**
     * The custom name of the parent building.
     */
    private String customParentName;

    /**
     * The translation key of the parent building.
     */
    private String parentTranslationKey;

    public static WorkOrderBuilding create(@NotNull final WorkOrderType type, @NotNull final IBuilding building)
    {
        int targetLevel = building.getBuildingLevel();
        switch (type)
        {
            case BUILD:
                targetLevel = 1;
                break;
            case UPGRADE:
                targetLevel++;
                break;
            case REMOVE:
                targetLevel = 0;
                break;
        }

        String schematicName;
        final int targetSchematicLevel = type == WorkOrderType.REMOVE ? building.getBuildingLevel() : targetLevel;
        final TileEntity buildingTE = building.getColony().getWorld().getBlockEntity(building.getID());
        if (buildingTE instanceof AbstractTileEntityColonyBuilding)
        {
            if (!((AbstractTileEntityColonyBuilding) buildingTE).getSchematicName().isEmpty())
            {
                schematicName = ((AbstractTileEntityColonyBuilding) buildingTE).getSchematicName()
                  .replaceAll("\\d$", "") + targetSchematicLevel;
            }
            else
            {
                schematicName = building.getSchematicName() + targetSchematicLevel;
            }
        }
        else
        {
            schematicName = building.getSchematicName() + targetSchematicLevel;
        }

        String structureName = new StructureName(Structures.SCHEMATICS_PREFIX, building.getStyle(), schematicName).toString();
        WorkOrderBuilding wo = new WorkOrderBuilding(
          structureName,
          building.getBuildingType().getTranslationKey(),
          type,
          building.getID(),
          building.getRotation(),
          building.getTileEntity() == null ? building.isMirrored() : building.getTileEntity().isMirrored(),
          building.getBuildingLevel(),
          targetLevel);
        wo.setCustomName(building);
        return wo;
    }

    /**
     * Unused constructor for reflection.
     */
    public WorkOrderBuilding()
    {
        super();
    }

    private WorkOrderBuilding(
      String structureName,
      String workOrderName,
      WorkOrderType workOrderType,
      BlockPos location,
      int rotation,
      boolean isMirrored,
      int currentLevel,
      int targetLevel)
    {
        super(structureName, workOrderName, workOrderType, location, rotation, isMirrored, currentLevel, targetLevel);
    }

    public String getCustomName()
    {
        return customName;
    }

    public String getCustomParentName()
    {
        return customParentName;
    }

    public String getParentTranslationKey()
    {
        return parentTranslationKey;
    }

    public void setCustomName(@NotNull final IBuilding building)
    {
        this.customName = building.getCustomName();
        this.customParentName = "";
        this.parentTranslationKey = "";

        if (building.hasParent())
        {
            final IBuilding parentBuilding = building.getColony().getBuildingManager().getBuilding(building.getParent());
            if (parentBuilding != null)
            {
                this.customParentName = parentBuilding.getCustomName();
                this.parentTranslationKey = parentBuilding.getBuildingType().getTranslationKey();
            }
        }
    }

    @Override
    public ITextComponent getDisplayName()
    {
        String customParentName = getCustomParentName();
        String customName = getCustomName();
        ITextComponent buildingComponent = customName.isEmpty() ? new TranslationTextComponent(getWorkOrderName()) : new StringTextComponent(customName);

        if (parentTranslationKey.isEmpty())
        {
            return buildingComponent;
        }
        else
        {
            ITextComponent parentComponent = customParentName.isEmpty() ? new TranslationTextComponent(parentTranslationKey) : new StringTextComponent(customParentName);
            return new TranslationTextComponent("%s / %s", parentComponent, buildingComponent);
        }
    }

    @Override
    public boolean canBeMadeBy(final IJob<?> job)
    {
        return job instanceof JobBuilder;
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
     *
     * @param builderLocation position of the builders own hut.
     * @param builderLevel    level of the builders hut.
     * @return true if so.
     */
    private boolean canBuildIgnoringDistance(@NotNull final BlockPos builderLocation, final int builderLevel)
    {
        //  A Build WorkOrder may be fulfilled by a Builder as long as any ONE of the following is true:
        //  - The Builder's Work AbstractBuilding is built
        //  - OR the WorkOrder is for the Builder's Work AbstractBuilding

        return (builderLevel >= this.getTargetLevel() || builderLevel == BuildingBuilder.MAX_BUILDING_LEVEL || (builderLocation.equals(getLocation())));
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
        customName = compound.getString(TAG_CUSTOM_NAME);
        customParentName = compound.getString(TAG_CUSTOM_PARENT_NAME);
        parentTranslationKey = compound.getString(TAG_PARENT_TRANSLATION_KEY);
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
        compound.putString(TAG_CUSTOM_NAME, customName);
        compound.putString(TAG_CUSTOM_PARENT_NAME, customParentName);
        compound.putString(TAG_PARENT_TRANSLATION_KEY, parentTranslationKey);
    }

    @Override
    public void serializeViewNetworkData(@NotNull PacketBuffer buf)
    {
        super.serializeViewNetworkData(buf);
        buf.writeUtf(customName);
        buf.writeUtf(customParentName);
        buf.writeUtf(parentTranslationKey);
    }

    @Override
    public void onCompleted(final IColony colony, ICitizenData citizen)
    {
        super.onCompleted(colony, citizen);

        final StructureName structureName = new StructureName(this.getStructureName());
        if (getWorkOrderType() != WorkOrderType.REMOVE)
        {
            AdvancementUtils.TriggerAdvancementPlayersForColony(colony, player ->
                                                                          AdvancementTriggers.COMPLETE_BUILD_REQUEST.trigger(player, structureName, this.getTargetLevel()));
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
}
