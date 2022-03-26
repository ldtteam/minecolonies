package com.minecolonies.coremod.colony.workorders;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.workorders.IWorkManager;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import com.minecolonies.api.colony.workorders.WorkOrderView;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.minecolonies.api.util.constant.Suppression.UNUSED_METHOD_PARAMETERS_SHOULD_BE_REMOVED;

/**
 * General information between WorkOrders.
 */
public abstract class AbstractWorkOrder implements IWorkOrder
{
    /**
     * NBT for storage.
     */
    private static final String TAG_TYPE = "type";
    private static final String TAG_TH_PRIORITY = "priority";
    private static final String TAG_ID = "id";
    private static final String TAG_CLAIMED_BY = "claimedBy";
    private static final String TAG_CLAIMED_BY_BUILDING = "claimedByBuilding";
    private static final String TAG_ITERATOR = "iterator";

    /**
     * Bimap of workOrder from string to class.
     */
    @NotNull
    private static final BiMap<String, Class<? extends IWorkOrder>> nameToClassBiMap = HashBiMap.create();

    /*
     * WorkOrder registry.
     */
    static
    {
        addMapping("building", WorkOrderBuilding.class);
        addMapping("decoration", WorkOrderDecoration.class);
        addMapping("miner", WorkOrderMiner.class);
    }

    /**
     * Add a given Work Order mapping.
     *
     * @param name       name of work order
     * @param orderClass class of work order
     */
    private static void addMapping(final String name, @NotNull final Class<? extends IWorkOrder> orderClass)
    {
        if (nameToClassBiMap.containsKey(name))
        {
            throw new IllegalArgumentException("Duplicate type '" + name + "' when adding Work Order class mapping");
        }

        try
        {
            if (orderClass.getDeclaredConstructor() != null)
            {
                nameToClassBiMap.put(name, orderClass);
                nameToClassBiMap.inverse().put(orderClass, name);
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
            String type = compound.getString(TAG_TYPE);
            if (type.equals("removal"))
            {
                oclass = WorkOrderBuilding.class;
            }
            else
            {
                oclass = nameToClassBiMap.get(type);
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
            if (compound.getAllKeys().contains(TAG_TH_PRIORITY))
            {
                order.setPriority(compound.getInt(TAG_TH_PRIORITY));
            }
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
    public static WorkOrderView createWorkOrderView(final PacketBuffer buf)
    {
        @Nullable WorkOrderView workOrderView = new WorkOrderView();

        try
        {
            workOrderView.deserialize(buf);
        }
        catch (final RuntimeException ex)
        {
            Log.getLogger().error(String.format("A WorkOrder.View for #%d has thrown an exception during loading, its state cannot be restored. Report this to the mod author",
                    workOrderView.getId()), ex);
            workOrderView = null;
        }

        return workOrderView;
    }

    private int id;
    private int priority;
    private BlockPos claimedBy;
    private String structureName;

    private String workOrderName;
    private WorkOrderType workOrderType;

    private BlockPos location;
    private int rotation;
    private boolean isMirrored;

    private int currentLevel;
    private int targetLevel;

    private int amountOfResources;
    private String iteratorType;

    private boolean cleared;
    private boolean requested;
    private boolean changed = false;

    /**
     * Default constructor; we also start with a new id and replace it during loading; this greatly simplifies creating subclasses.
     */
    public AbstractWorkOrder()
    {
        this.iteratorType = "";
        this.changed = false;
    }

    protected AbstractWorkOrder(String structureName,
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
    public String getDisplayName()
    {
        return workOrderName;
    }

    /**
     * Whether the work order can be built or not.
     *
     * @param citizen the citizen attempting to perform the work order
     * @return true if it can be built
     */
    protected abstract boolean canBuild(@NotNull final ICitizenData citizen);

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
        id = compound.getInt(TAG_ID);
        if (compound.getAllKeys().contains(TAG_TH_PRIORITY))
        {
            priority = compound.getInt(TAG_TH_PRIORITY);
        }

        if (compound.getAllKeys().contains(TAG_CLAIMED_BY))
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
        else if (compound.getAllKeys().contains(TAG_CLAIMED_BY_BUILDING))
        {
            claimedBy = BlockPosUtil.read(compound, TAG_CLAIMED_BY_BUILDING);
        }
        iteratorType = compound.getString(TAG_ITERATOR);
    }

    /**
     * Save the Work Order to an CompoundNBT.
     *
     * @param compound NBT tag compount
     */
    @Override
    public void write(@NotNull final CompoundNBT compound)
    {
        final String s = nameToClassBiMap.inverse().get(this.getClass());

        if (s == null)
        {
            throw new IllegalStateException(this.getClass() + " is missing a mapping! This is a bug!");
        }

        compound.putInt(TAG_TH_PRIORITY, priority);
        compound.putString(TAG_TYPE, s);
        compound.putInt(TAG_ID, id);
        if (claimedBy != null)
        {
            BlockPosUtil.write(compound, TAG_CLAIMED_BY_BUILDING, claimedBy);
        }
        compound.putString(TAG_ITERATOR, iteratorType);
    }

    /**
     * Writes the workOrders data to a byte buf for transition.
     *
     * @param buf Buffer to write to
     */
    @Override
    public void serializeViewNetworkData(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(id);
        buf.writeInt(priority);
        buf.writeBlockPos(claimedBy == null ? BlockPos.ZERO : claimedBy);
        buf.writeUtf(workOrderName);
        buf.writeInt(workOrderType.ordinal());
        buf.writeUtf(structureName);
        buf.writeInt(0);
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
}
