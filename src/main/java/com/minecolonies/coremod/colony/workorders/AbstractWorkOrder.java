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
import com.minecolonies.coremod.colony.CitizenData;
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
    private static final String TAG_BUILDING       = "building";
    private static final String TAG_TYPE        = "type";
    private static final String TAG_TH_PRIORITY = "priority";
    private static final String TAG_ID          = "id";
    private static final String TAG_CLAIMED_BY  = "claimedBy";
    private static final String TAG_CLAIMED_BY_BUILDING  = "claimedByBuilding";

    /**
     * Bimap of workOrder from string to class.
     */
    @NotNull
    private static final BiMap<String, Class<? extends IWorkOrder>> nameToClassBiMap = HashBiMap.create();

    /**
     * WorkOrder registry.
     */
    static
    {
        addMapping("build", WorkOrderBuild.class);
        addMapping("decoration", WorkOrderBuildDecoration.class);
        addMapping("removal", WorkOrderBuildRemoval.class);
        addMapping("building", WorkOrderBuildBuilding.class);
        addMapping("miner", WorkOrderBuildMiner.class);
    }

    /**
     * The id of the workOrder.
     */
    protected int      id;

    /**
     * The position of the worker building claiming this workOrder.
     */
    private   BlockPos claimedBy;

    /**
     * The priority of the workOrder.
     */
    private   int      priority;

    /**
     * If the workOrder changed.
     */
    private   boolean  changed = false;

    /**
     * The location to built at.
     */
    protected BlockPos buildingLocation;

    /**
     * Default constructor; we also start with a new id and replace it during loading;
     * this greatly simplifies creating subclasses.
     */
    public AbstractWorkOrder()
    {
        //Should be overridden
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
     * @param manager the work manager.
     * @return {@link AbstractWorkOrder} from the NBT
     */
    public static AbstractWorkOrder createFromNBT(@NotNull final CompoundNBT compound, final WorkManager manager)
    {
        @Nullable AbstractWorkOrder order = null;
        @Nullable Class<? extends IWorkOrder> oclass = null;

        try
        {
            oclass = nameToClassBiMap.get(compound.getString(TAG_TYPE));

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
            if (compound.keySet().contains(TAG_TH_PRIORITY))
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
     * Read the WorkOrder data from the CompoundNBT.
     *  @param compound NBT Tag compound
     * @param manager the workManager calling this method.
     */
    @Override
    public void read(@NotNull final CompoundNBT compound, final IWorkManager manager)
    {
        id = compound.getInt(TAG_ID);
        if (compound.keySet().contains(TAG_TH_PRIORITY))
        {
            priority = compound.getInt(TAG_TH_PRIORITY);
        }

        if (compound.keySet().contains(TAG_CLAIMED_BY))
        {
            final int citizenId = compound.getInt(TAG_CLAIMED_BY);
            if (manager.getColony() != null)
            {
                final ICitizenData data = manager.getColony().getCitizenManager().getCitizen(citizenId);
                if (data != null && data.getWorkBuilding() != null)
                {
                    claimedBy = data.getWorkBuilding().getPosition();
                }
            }
        }
        else if (compound.keySet().contains(TAG_CLAIMED_BY_BUILDING))
        {
            claimedBy = BlockPosUtil.read(compound, TAG_CLAIMED_BY_BUILDING);
        }
        buildingLocation = BlockPosUtil.read(compound, TAG_BUILDING);
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

    /**
     * Getter for the priority.
     *
     * @return the priority of the work order.
     */
    @Override
    public int getPriority()
    {
        return this.priority;
    }

    /**
     * Setter for the priority.
     *
     * @param priority the new priority.
     */
    @Override
    public void setPriority(final int priority)
    {
        this.priority = priority;
    }

    /**
     * Checks if the workOrder has changed.
     *
     * @return true if so.
     */
    @Override
    public boolean hasChanged()
    {
        return changed;
    }

    /**
     * Resets the changed variable.
     */
    @Override
    public void resetChange()
    {
        changed = false;
    }

    /**
     * Get the ID of the Work Order.
     *
     * @return ID of the work order
     */
    @Override
    public int getID()
    {
        return id;
    }

    @Override
    public void setID(final int id)
    {
        this.id = id;
    }

    /**
     * Is the Work Order claimed?
     *
     * @return true if the Work Order has been claimed
     */
    @Override
    public boolean isClaimed()
    {
        return claimedBy != null;
    }

    /**
     * Is the Work Order claimed by the given citizen?
     *
     * @param citizen The citizen to check
     * @return true if the Work Order is claimed by this Citizen
     */
    @Override
    public boolean isClaimedBy(@NotNull final ICitizenData citizen)
    {
        if (citizen.getWorkBuilding() != null)
        {
            return citizen.getWorkBuilding().getPosition().equals(claimedBy);
        }
        return false;
    }

    /**
     * Get the ID of the Citizen that the Work Order is claimed by.
     *
     * @return ID of citizen the Work Order has been claimed by, or null
     */
    @Override
    public BlockPos getClaimedBy()
    {
        return claimedBy;
    }

    /**
     * Set the Work Order as claimed by the given Citizen.
     *
     * @param citizen {@link CitizenData}
     */
    @Override
    public void setClaimedBy(@Nullable final ICitizenData citizen)
    {
        changed = true;
        claimedBy = (citizen != null && citizen.getWorkBuilding() != null) ? citizen.getWorkBuilding().getPosition() : null;
    }

    /**
     * Set the Work order as claimed by a given building.
     * @param builder the building position.
     */
    @Override
    public void setClaimedBy(final BlockPos builder)
    {
        claimedBy = builder;
    }

    /**
     * Clear the Claimed By status of the Work Order.
     */
    @Override
    public void clearClaimedBy()
    {
        changed = true;
        claimedBy = null;
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
        BlockPosUtil.write(compound, TAG_BUILDING, buildingLocation);
    }

    /**
     * Is this WorkOrder still valid?  If not, it will be deleted.
     * <p>
     * Suppressing Sonar Rule squid:S1172
     * This rule does " Unused method parameters should be removed"
     * But in this case extending class may need to use the colony parameter
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
        buf.writeInt(getType().ordinal());
        buf.writeString(get());
        buf.writeBlockPos(buildingLocation == null ? BlockPos.ZERO : buildingLocation);

        //value is upgradeName and upgradeLevel for workOrderBuild
    }

    /**
     * Gets of the WorkOrder Type. Overwrite this for the different implementations.
     *
     * @return the type.
     */
    @NotNull
    protected abstract WorkOrderType getType();

    /**
     * Gets the value of the WorkOrder. Overwrite this in every subclass.
     *
     * @return a description string.
     */
    protected abstract String get();

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
     * @param colony in which the work order exist
     */
    @Override
    public void onCompleted(final IColony colony)
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
                 .anyMatch(building -> building instanceof BuildingBuilder && building.getMainCitizen() != null && building.getBuildingLevel() >= level);
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
