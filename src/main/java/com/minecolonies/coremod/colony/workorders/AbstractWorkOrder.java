package com.minecolonies.coremod.colony.workorders;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.workorders.IWorkManager;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.api.colony.workorders.IWorkOrderView;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.coremod.colony.workorders.view.AbstractWorkOrderView;
import com.minecolonies.coremod.colony.workorders.view.WorkOrderBuildingView;
import com.minecolonies.coremod.colony.workorders.view.WorkOrderDecorationView;
import com.minecolonies.coremod.colony.workorders.view.WorkOrderMinerView;
import com.minecolonies.coremod.tileentities.TileEntityDecorationController;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;

import static com.minecolonies.api.util.constant.Suppression.UNUSED_METHOD_PARAMETERS_SHOULD_BE_REMOVED;

/**
 * General information between WorkOrders.
 */
public abstract class AbstractWorkOrder implements IWorkOrder
{
    /**
     * NBT for storage.
     */
    private static final String TAG_TYPE                = "type";
    private static final String TAG_ID                  = "id";
    private static final String TAG_TH_PRIORITY         = "priority";
    private static final String TAG_CLAIMED_BY          = "claimedBy";
    private static final String TAG_CLAIMED_BY_BUILDING = "claimedByBuilding";
    private static final String TAG_STRUCTURE_NAME      = "structureName";
    private static final String TAG_WO_NAME             = "workOrderName";
    private static final String TAG_WO_TYPE             = "workOrderType";
    private static final String TAG_LOCATION            = "location";
    private static final String TAG_ROTATION            = "rotation";
    private static final String TAG_IS_MIRRORED         = "isMirrored";
    private static final String TAG_CURRENT_LEVEL       = "currentLevel";
    private static final String TAG_TARGET_LEVEL        = "targetLevel";
    private static final String TAG_AMOUNT_OF_RESOURCES = "amountOfResources";
    private static final String TAG_ITERATOR            = "iterator";
    private static final String TAG_IS_CLEARED          = "cleared";
    private static final String TAG_IS_REQUESTED        = "requested";

    /**
     * Old NBT tags for storage
     */
    private static final String TAG_BUILDING_OLD            = "building";
    private static final String TAG_BUILDING_ROTATION_OLD   = "buildingRotation";
    private static final String TAG_IS_MIRRORED_OLD         = "mirrored";
    private static final String TAG_AMOUNT_OF_RESOURCES_OLD = "resQuantity";
    private static final String TAG_UPGRADE_LEVEL_OLD       = "upgradeLevel";

    /**
     * Bimap of workOrder from string to class.
     */
    @NotNull
    private static final BiMap<String, Tuple<Class<? extends IWorkOrder>, Class<? extends IWorkOrderView>>> nameToClassBiMap = HashBiMap.create();
    /*
     * WorkOrder registry.
     */
    static
    {
        addMapping("building", WorkOrderBuilding.class, WorkOrderBuildingView.class);
        addMapping("decoration", WorkOrderDecoration.class, WorkOrderDecorationView.class);
        addMapping("miner", WorkOrderMiner.class, WorkOrderMinerView.class);
    }
    /**
     * Add a given Work Order mapping.
     *
     * @param name       name of work order
     * @param orderClass class of work order
     */
    private static void addMapping(
      final String name,
      @NotNull final Class<? extends IWorkOrder> orderClass,
      @NotNull final Class<? extends IWorkOrderView> viewClass)
    {
        if (nameToClassBiMap.containsKey(name))
        {
            throw new IllegalArgumentException("Duplicate type '" + name + "' when adding Work Order class mapping");
        }

        try
        {
            if (orderClass.getDeclaredConstructor() != null)
            {
                nameToClassBiMap.put(name, new Tuple<>(orderClass, viewClass));
                nameToClassBiMap.inverse().put(new Tuple<>(orderClass, viewClass), name);
            }
        }
        catch (final NoSuchMethodException exception)
        {
            throw new IllegalArgumentException("Missing constructor for type '" + name + "' when adding Work Order class mapping", exception);
        }
    }

    /**
     * Create a Work Order from a saved CompoundNBT.
     *
     * @param compound the compound that contains the data for the Work Order
     * @param manager  the work manager.
     * @return {@link AbstractWorkOrder} from the NBT
     */
    public static AbstractWorkOrder createFromNBT(@NotNull final CompoundNBT compound, final WorkManager manager)
    {
        @Nullable AbstractWorkOrder order = null;
        @Nullable Class<? extends IWorkOrder> oclass = null;

        try
        {
            // TODO: In 1.19 remove this check as this is purely for backwards compatibility with old class mappings
            String type = compound.getString(TAG_TYPE);
            if (type.equals("removal"))
            {
                oclass = WorkOrderBuilding.class;
            }
            else
            {
                oclass = nameToClassBiMap.get(type).getA();
            }

            if (oclass != null)
            {
                final Constructor<?> constructor = oclass.getDeclaredConstructor();
                order = (AbstractWorkOrder) constructor.newInstance();
            }
        }
        catch (@NotNull NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e)
        {
            Log.getLogger().trace(e);
        }

        if (order == null)
        {
            Log.getLogger().warn(String.format("Unknown WorkOrder type '%s' or missing constructor of proper format.", compound.getString(TAG_TYPE)));
            return null;
        }

        try
        {
            order.read(compound, manager);
        }
        catch (final RuntimeException ex)
        {
            Log.getLogger().error(String.format("A WorkOrder %s(%s) has thrown an exception during loading, its state cannot be restored. Report this to the mod author",
              compound.getString(TAG_TYPE), oclass.getName()), ex);
            return null;
        }

        return order;
    }

    /**
     * Create a WorkOrder View from a buffer.
     *
     * @param buf The network data
     * @return View object of the workOrder
     */
    @Nullable
    public static AbstractWorkOrderView createWorkOrderView(final PacketBuffer buf)
    {
        @Nullable AbstractWorkOrderView orderView = null;
        String mappingName = buf.readUtf(32767);

        try
        {
            @Nullable Class<? extends IWorkOrderView> oclass = nameToClassBiMap.get(mappingName).getB();

            if (oclass != null)
            {
                final Constructor<?> constructor = oclass.getDeclaredConstructor();
                orderView = (AbstractWorkOrderView) constructor.newInstance();
            }
        }
        catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e)
        {
            Log.getLogger().trace(e);
        }

        if (orderView == null)
        {
            Log.getLogger().warn(String.format("Unknown WorkOrder type '%s' or missing constructor of proper format.", mappingName));
            return null;
        }
        try
        {
            orderView.deserialize(buf);
        }
        catch (final RuntimeException ex)
        {
            Log.getLogger().error(String.format("A WorkOrder.View for #%d has thrown an exception during loading, its state cannot be restored. Report this to the mod author",
              orderView.getId()), ex);
            return null;
        }

        return orderView;
    }

    /**
     * The ID of the work order.
     */
    private int id;

    /**
     * The priority of the work order.
     */
    private int priority;

    /**
     * Which building has claimed this work order.
     */
    private BlockPos claimedBy;

    /**
     * The structurize schematic name.
     */
    private String structureName;

    /**
     * The work order name.
     */
    private String workOrderName;

    /**
     * The work order type.
     */
    private WorkOrderType workOrderType;

    /**
     * The location of this work order its structure.
     */
    private BlockPos location;

    /**
     * The rotation of this work order its structure.
     */
    private int rotation;

    /**
     * Whether the work order its structure is mirrored or not.
     */
    private boolean isMirrored;

    /**
     * The current level of the work order its structure.
     */
    private int currentLevel;

    /**
     * The target level of the work order its structure.
     */
    private int targetLevel;

    /**
     * The amount of resources the work order its structure still requires.
     */
    private int amountOfResources;

    /**
     * The iterator type (building method) of this work order.
     */
    private String iteratorType;

    /**
     * Whether the work order area is cleared.
     */
    private boolean cleared;

    /**
     * Whether the resources for the work order have been requested.
     */
    private boolean requested;

    /**
     * Internal flag to see if anything has been changed.
     */
    private boolean changed;

    /**
     * Default constructor; we also start with a new id and replace it during loading; this greatly simplifies creating subclasses.
     */
    public AbstractWorkOrder()
    {
        this.iteratorType = "";
        this.changed = false;
    }

    protected AbstractWorkOrder(
      String structureName,
      String workOrderName,
      WorkOrderType workOrderType,
      BlockPos location,
      int rotation,
      boolean isMirrored,
      int currentLevel,
      int targetLevel)
    {
        this();
        this.structureName = structureName;
        this.workOrderName = workOrderName;
        this.workOrderType = workOrderType;
        this.location = location;
        this.rotation = rotation;
        this.isMirrored = isMirrored;
        this.currentLevel = currentLevel;
        this.targetLevel = targetLevel;
    }

    @Override
    public final int getID()
    {
        return id;
    }

    @Override
    public final void setID(int id)
    {
        this.id = id;
    }

    @Override
    public final int getPriority()
    {
        return priority;
    }

    @Override
    public final void setPriority(int priority)
    {
        this.priority = priority;
    }

    @Override
    public final BlockPos getClaimedBy()
    {
        return claimedBy;
    }

    @Override
    public final void setClaimedBy(BlockPos claimedBy)
    {
        this.claimedBy = claimedBy;
    }

    @Override
    public final void setClaimedBy(@Nullable ICitizenData citizen)
    {
        changed = true;
        claimedBy = (citizen != null && citizen.getWorkBuilding() != null) ? citizen.getWorkBuilding().getPosition() : null;
    }

    @Override
    public final boolean isClaimed()
    {
        return claimedBy != null;
    }

    @Override
    public final boolean isClaimedBy(@NotNull ICitizenData citizen)
    {
        if (citizen.getWorkBuilding() != null)
        {
            return citizen.getWorkBuilding().getPosition().equals(claimedBy);
        }
        return false;
    }

    @Override
    public final void clearClaimedBy()
    {
        changed = true;
        claimedBy = null;
    }

    @Override
    public final String getStructureName()
    {
        return structureName;
    }

    @Override
    public final String getWorkOrderName()
    {
        return workOrderName;
    }

    @Override
    public final WorkOrderType getWorkOrderType()
    {
        return workOrderType;
    }

    @Override
    public final BlockPos getLocation()
    {
        return location;
    }

    @Override
    public final int getRotation()
    {
        return rotation;
    }

    @Override
    public final boolean isMirrored()
    {
        return isMirrored;
    }

    @Override
    public final int getCurrentLevel()
    {
        return currentLevel;
    }

    @Override
    public final int getTargetLevel()
    {
        return targetLevel;
    }

    @Override
    public final int getAmountOfResources()
    {
        return amountOfResources;
    }

    @Override
    public final void setAmountOfResources(int newQuantity)
    {
        this.amountOfResources = newQuantity;
    }

    @Override
    public final String getIteratorType()
    {
        return iteratorType;
    }

    @Override
    public final void setIteratorType(String iteratorType)
    {
        this.iteratorType = iteratorType;
    }

    @Override
    public final boolean isCleared()
    {
        return cleared;
    }

    @Override
    public final void setCleared(boolean cleared)
    {
        this.cleared = cleared;
    }

    @Override
    public final boolean isRequested()
    {
        return requested;
    }

    @Override
    public final void setRequested(final boolean requested)
    {
        this.requested = requested;
    }

    @Override
    public final boolean isChanged()
    {
        return changed;
    }

    @Override
    public final void resetChange()
    {
        this.changed = false;
    }

    /**
     * Get the name of the work order, provides the custom name or the work order name when no custom name is given
     *
     * @return the display name for the work order
     */
    @Override
    public ITextComponent getDisplayName()
    {
        return new TranslationTextComponent(workOrderName);
    }

    /**
     * Whether this work order can be made by a builder.
     *
     * @param job
     * @return a boolean.
     */
    public abstract boolean canBeMadeBy(final IJob<?> job);

    /**
     * Whether the work order can be built or not.
     *
     * @param citizen the citizen attempting to perform the work order
     * @return true if it can be built
     */
    public abstract boolean canBuild(@NotNull final ICitizenData citizen);

    /**
     * Is this WorkOrder still valid?  If not, it will be deleted.
     * <p>
     * Suppressing Sonar Rule squid:S1172 This rule does " Unused method parameters should be removed" But in this case extending class may need to use the colony parameter
     *
     * @param colony The colony that owns the Work Order
     * @return True if the WorkOrder is still valid, or False if it should be deleted
     */
    @Override
    @SuppressWarnings(UNUSED_METHOD_PARAMETERS_SHOULD_BE_REMOVED)
    public boolean isValid(final IColony colony)
    {
        return true;
    }

    /**
     * Read the WorkOrder data from the CompoundNBT.
     *
     * @param compound NBT Tag compound
     * @param manager  the workManager calling this method.
     */
    @Override
    public void read(@NotNull final CompoundNBT compound, final IWorkManager manager)
    {
        // TODO: In 1.19 remove this method call as this is purely for backwards compatibility with old class mappings
        migrateOldNbt(compound, manager);

        id = compound.getInt(TAG_ID);
        if (compound.contains(TAG_TH_PRIORITY))
        {
            priority = compound.getInt(TAG_TH_PRIORITY);
        }

        if (compound.contains(TAG_CLAIMED_BY))
        {
            final int citizenId = compound.getInt(TAG_CLAIMED_BY);
            if (manager.getColony() != null)
            {
                final ICitizenData data = manager.getColony().getCitizenManager().getCivilian(citizenId);
                if (data != null && data.getWorkBuilding() != null)
                {
                    claimedBy = data.getWorkBuilding().getPosition();
                }
            }
        }
        else if (compound.contains(TAG_CLAIMED_BY_BUILDING))
        {
            claimedBy = BlockPosUtil.read(compound, TAG_CLAIMED_BY_BUILDING);
        }
        structureName = compound.getString(TAG_STRUCTURE_NAME);
        workOrderName = compound.getString(TAG_WO_NAME);
        workOrderType = WorkOrderType.values()[compound.getInt(TAG_WO_TYPE)];
        location = BlockPosUtil.read(compound, TAG_LOCATION);
        rotation = compound.getInt(TAG_ROTATION);
        isMirrored = compound.getBoolean(TAG_IS_MIRRORED);
        currentLevel = compound.getInt(TAG_CURRENT_LEVEL);
        targetLevel = compound.getInt(TAG_TARGET_LEVEL);
        amountOfResources = compound.getInt(TAG_AMOUNT_OF_RESOURCES);
        iteratorType = compound.getString(TAG_ITERATOR);
        cleared = compound.getBoolean(TAG_IS_CLEARED);
        requested = compound.getBoolean(TAG_IS_REQUESTED);
    }

    /**
     * Save the Work Order to an CompoundNBT.
     *
     * @param compound NBT tag compount
     */
    @Override
    public void write(@NotNull final CompoundNBT compound)
    {
        compound.putInt(TAG_TH_PRIORITY, priority);
        compound.putString(TAG_TYPE, getMappingName());
        compound.putInt(TAG_ID, id);
        if (claimedBy != null)
        {
            BlockPosUtil.write(compound, TAG_CLAIMED_BY_BUILDING, claimedBy);
        }
        compound.putString(TAG_STRUCTURE_NAME, structureName);
        compound.putString(TAG_WO_NAME, workOrderName);
        compound.putInt(TAG_WO_TYPE, workOrderType.ordinal());
        BlockPosUtil.write(compound, TAG_LOCATION, location);
        compound.putInt(TAG_ROTATION, rotation);
        compound.putBoolean(TAG_IS_MIRRORED, isMirrored);
        compound.putInt(TAG_CURRENT_LEVEL, currentLevel);
        compound.putInt(TAG_TARGET_LEVEL, targetLevel);
        compound.putInt(TAG_AMOUNT_OF_RESOURCES, amountOfResources);
        compound.putString(TAG_ITERATOR, iteratorType);
        compound.putBoolean(TAG_IS_CLEARED, cleared);
        compound.putBoolean(TAG_IS_REQUESTED, requested);
    }

    /**
     * Writes the workOrders data to a byte buf for transition.
     *
     * @param buf Buffer to write to
     */
    @Override
    public void serializeViewNetworkData(@NotNull final PacketBuffer buf)
    {
        buf.writeUtf(getMappingName());
        buf.writeInt(id);
        buf.writeInt(priority);
        buf.writeBlockPos(claimedBy == null ? BlockPos.ZERO : claimedBy);
        buf.writeUtf(structureName);
        buf.writeUtf(workOrderName);
        buf.writeInt(workOrderType.ordinal());
        buf.writeBlockPos(location);
        buf.writeInt(rotation);
        buf.writeBoolean(isMirrored);
        buf.writeInt(currentLevel);
        buf.writeInt(targetLevel);
        buf.writeInt(amountOfResources);
        buf.writeUtf(iteratorType);
        buf.writeBoolean(cleared);
        buf.writeBoolean(requested);
    }

    private String getMappingName()
    {
        final Optional<String> s = nameToClassBiMap.entrySet().stream()
          .filter(f -> this.getClass().equals(f.getValue().getA()))
          .map(Map.Entry::getKey)
          .findFirst();

        if (!s.isPresent())
        {
            throw new IllegalStateException(this.getClass() + " is missing a mapping! This is a bug!");
        }

        return s.get();
    }

    /**
     * Executed when a work order is added.
     * <p>
     * Override this when something need to be done when the work order is added
     *
     * @param colony         in which the work order exist
     * @param readingFromNbt if being read from NBT.
     */
    @Override
    public void onAdded(final IColony colony, final boolean readingFromNbt)
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Executed when a work order is completed.
     * <p>
     * Override this when something need to be done when the work order is completed
     *
     * @param colony  in which the work order exist
     * @param citizen citizen that completed the work order
     */
    @Override
    public void onCompleted(final IColony colony, ICitizenData citizen)
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Executed when a work order is removed.
     * <p>
     * Override this when something need to be done when the work order is removed
     *
     * @param colony in which the work order exist
     */
    @Override
    public void onRemoved(final IColony colony)
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Check if this workOrder can be resolved by an existing builder.
     *
     * @param colony the colony to check in.
     * @param level  the new level of the building.
     * @return true if so.
     */
    @Override
    public boolean canBeResolved(final IColony colony, final int level)
    {
        return colony.getBuildingManager()
          .getBuildings()
          .values()
          .stream()
          .anyMatch(building -> building instanceof BuildingBuilder && !building.getAllAssignedCitizen().isEmpty() && building.getBuildingLevel() >= level);
    }

    /**
     * Check if this workOrder can be resolved by an existing builder by distance.
     *
     * @param colony the colony to check in.
     * @param level  the new level of the building.
     * @return true if so.
     */
    @Override
    public boolean tooFarFromAnyBuilder(final IColony colony, final int level)
    {
        return false;
    }

    // TODO: In 1.19 remove this method as this is purely for backwards compatibility with old class mappings
    private static void migrateOldNbt(final CompoundNBT compound, IWorkManager manager)
    {
        // When the old TAG_BUILDING tag no longer exists in the compound NBT, it means the work order has already migrated
        if (!compound.contains(TAG_BUILDING_OLD) && !compound.getString(TAG_TYPE).equals("removal"))
        {
            return;
        }

        // Migrate tags
        // Re-write the location
        BlockPosUtil.write(compound, TAG_LOCATION, BlockPosUtil.read(compound, TAG_BUILDING_OLD));
        // Re-write the rotation
        compound.putInt(TAG_ROTATION, compound.getInt(TAG_BUILDING_ROTATION_OLD));
        // Re-write the mirrored state
        compound.putBoolean(TAG_ROTATION, compound.getBoolean(TAG_BUILDING_ROTATION_OLD));
        // Re-write the mirrored state
        compound.putInt(TAG_AMOUNT_OF_RESOURCES, compound.getInt(TAG_AMOUNT_OF_RESOURCES_OLD));

        // Re-write the current and upgrade level
        int targetLevel = compound.getInt(TAG_UPGRADE_LEVEL_OLD);

        // If the tag is removal it's indicating an old removal request
        if (compound.getString(TAG_TYPE).equals("removal"))
        {
            compound.putString(TAG_TYPE, "building");
            compound.putInt(TAG_WO_TYPE, WorkOrderType.REMOVE.ordinal());
            compound.putInt(TAG_CURRENT_LEVEL, targetLevel);
            compound.putInt(TAG_TARGET_LEVEL, 0);
        }
        else if (targetLevel == 1)
        {
            compound.putInt(TAG_WO_TYPE, WorkOrderType.BUILD.ordinal());
            compound.putInt(TAG_CURRENT_LEVEL, 0);
            compound.putInt(TAG_TARGET_LEVEL, targetLevel);
        }
        else if (targetLevel > 1)
        {
            IBuilding building = manager.getColony().getBuildingManager().getBuilding(BlockPosUtil.read(compound, TAG_LOCATION));
            TileEntity entity = manager.getColony().getWorld().getBlockEntity(BlockPosUtil.read(compound, TAG_LOCATION));

            if (building != null)
            {
                if (building.getBuildingLevel() == targetLevel)
                {
                    compound.putInt(TAG_WO_TYPE, WorkOrderType.REPAIR.ordinal());
                }
                else
                {
                    compound.putInt(TAG_WO_TYPE, WorkOrderType.UPGRADE.ordinal());
                }
            }
            else if (entity instanceof TileEntityDecorationController)
            {
                TileEntityDecorationController dEntity = (TileEntityDecorationController) entity;
                if (dEntity.getTier() == targetLevel)
                {
                    compound.putInt(TAG_WO_TYPE, WorkOrderType.REPAIR.ordinal());
                }
                else
                {
                    compound.putInt(TAG_WO_TYPE, WorkOrderType.UPGRADE.ordinal());
                }
            }
            compound.putInt(TAG_TARGET_LEVEL, targetLevel);
            compound.putInt(TAG_CURRENT_LEVEL, building.getBuildingLevel());
        }

        // Remove old NBT
        compound.remove(TAG_BUILDING_OLD);
        compound.remove(TAG_BUILDING_ROTATION_OLD);
        compound.remove(TAG_IS_MIRRORED_OLD);
        compound.remove(TAG_AMOUNT_OF_RESOURCES_OLD);
        compound.remove(TAG_UPGRADE_LEVEL_OLD);

        // Building specific info
        IBuilding building = manager.getColony().getBuildingManager().getBuilding(BlockPosUtil.read(compound, TAG_LOCATION));
        if (building != null)
        {
            final WorkOrderBuilding test = new WorkOrderBuilding();
            test.setCustomName(building);
            compound.putString("customName", test.getCustomName());
            compound.putString("customParentName", test.getCustomParentName());
            compound.putString("parentTranslationKey", test.getParentTranslationKey());
        }
    }
}
