package com.minecolonies.coremod.colony.workorders;

import com.ldtteam.structures.helpers.Structure;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.util.LanguageHandler;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.colony.workorders.IWorkManager;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.util.AdvancementUtils;
import com.minecolonies.coremod.util.ColonyUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.TranslationConstants.OUT_OF_COLONY;
import static com.minecolonies.coremod.MineColonies.CLOSE_COLONY_CAP;

/**
 * Handles work orders for a colony.
 */
public class WorkManager implements IWorkManager
{
    private static final String                   TAG_WORK_ORDERS = "workOrders";
    //  Once a second
    //private static final int    WORK_ORDER_FULFILL_INCREMENT = 1 * 20;
    /**
     * The Colony the workManager takes part of.
     */
    private final        Colony                   colony;
    @NotNull
    private final        Map<Integer, IWorkOrder> workOrders      = new LinkedHashMap<>();
    private              int                      topWorkOrderId  = 0;
    /**
     * Checks if there has been changes.
     */
    private              boolean                  dirty           = false;

    /**
     * Constructor, saves reference to the colony.
     *
     * @param c Colony the work manager is for.
     */
    public WorkManager(final Colony c)
    {
        colony = c;
    }

    /**
     * Removes a work order from the work manager.
     *
     * @param order {@link AbstractWorkOrder} to remove.
     */
    @Override
    public void removeWorkOrder(@NotNull final IWorkOrder order)
    {
        removeWorkOrder(order.getID());
    }

    /**
     * Removes a work order from the work manager.
     *
     * @param orderId ID of the order to remove
     */
    @Override
    public void removeWorkOrder(final int orderId)
    {
        final IWorkOrder workOrder = workOrders.get(orderId);
        if (workOrder != null)
        {
            workOrders.remove(orderId);
            colony.removeWorkOrderInView(orderId);
            workOrder.onRemoved(colony);
            colony.markDirty();
        }
    }

    /**
     * Get a work order of the specified id, as a specific type.
     *
     * @param id   the id of the work order.
     * @param type the class of the expected type of the work order.
     * @param <W>  the type of work order to return.
     * @return the work order of the specified id, or null if it was not found or is of an incompatible type.
     */
    @Override
    @Nullable
    public <W extends IWorkOrder> W getWorkOrder(final int id, @NotNull final Class<W> type)
    {
        final IWorkOrder workOrder = getWorkOrder(id);
        if (type.isInstance(workOrder))
        {
            return type.cast(workOrder);
        }

        return null;
    }

    /**
     * Get a work order of the specified id.
     *
     * @param id the id of the work order.
     * @return the work order of the specified id, or null.
     */
    @Override
    public IWorkOrder getWorkOrder(final int id)
    {
        return workOrders.get(id);
    }

    /**
     * Get an unclaimed work order of a specified type.
     *
     * @param type the class of the type of work order to find.
     * @param <W>  the type of work order to return.
     * @return an unclaimed work order of the given type, or null if no unclaimed work order of the type was found.
     */
    @Override
    @Nullable
    public <W extends IWorkOrder> W getUnassignedWorkOrder(@NotNull final Class<W> type)
    {
        for (@NotNull final IWorkOrder o : workOrders.values())
        {
            if (!o.isClaimed() && type.isInstance(o))
            {
                return type.cast(o);
            }
        }

        return null;
    }

    /**
     * Get all work orders of a specified type.
     *
     * @param type the class of the type of work order to find.
     * @param <W>  the type of work order to return.
     * @return a list of all work orders of the given type.
     */
    @Override
    public <W extends IWorkOrder> List<W> getWorkOrdersOfType(@NotNull final Class<W> type)
    {
        return workOrders.values().stream().filter(type::isInstance).map(type::cast).collect(Collectors.toList());
    }

    /**
     * Get all work orders.
     *
     * @return a list of all work orders.
     */
    @Override
    @NotNull
    public Map<Integer, IWorkOrder> getWorkOrders()
    {
        return workOrders;
    }

    /**
     * When a citizen is removed, unclaim any Work Orders that were claimed by that citizen.
     *
     * @param citizen Citizen to unclaim work for.
     */
    @Override
    public void clearWorkForCitizen(@NotNull final ICitizenData citizen)
    {
        dirty = true;
        workOrders.values().stream().filter(o -> o != null && o.isClaimedBy(citizen)).forEach(IWorkOrder::clearClaimedBy);
    }

    /**
     * Save the Work Manager.
     *
     * @param compound Compound to save to.
     */
    @Override
    public void write(@NotNull final CompoundNBT compound)
    {
        //  Work Orders
        @NotNull final ListNBT list = new ListNBT();
        for (@NotNull final IWorkOrder o : workOrders.values())
        {
            @NotNull final CompoundNBT orderCompound = new CompoundNBT();
            o.write(orderCompound);
            list.add(orderCompound);
        }
        compound.put(TAG_WORK_ORDERS, list);
    }

    /**
     * Restore the Work Manager.
     *
     * @param compound Compound to read from.
     */
    @Override
    public void read(@NotNull final CompoundNBT compound)
    {
        workOrders.clear();
        //  Work Orders
        final ListNBT list = compound.getList(TAG_WORK_ORDERS, NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); ++i)
        {
            final CompoundNBT orderCompound = list.getCompound(i);
            @Nullable final AbstractWorkOrder o = AbstractWorkOrder.createFromNBT(orderCompound, this);
            if (o != null)
            {
                addWorkOrder(o, true);

                //  If this Work Order is claimed, and the Citizen who claimed it no longer exists
                //  then clear the Claimed status
                //  This is just a failsafe cleanup; this should not happen under normal circumstances
                if (o.isClaimed() && colony.getBuildingManager().getBuilding(o.getClaimedBy()) == null)
                {
                    o.clearClaimedBy();
                }

                topWorkOrderId = Math.max(topWorkOrderId, o.getID());
            }
        }
    }

    /**
     * Adds work order to the work manager.
     *
     * @param order          Order to add.
     * @param readingFromNbt if being read from NBT.
     */
    @Override
    public void addWorkOrder(@NotNull final IWorkOrder order, final boolean readingFromNbt)
    {
        dirty = true;

        if (order instanceof WorkOrderBuildDecoration && !(order instanceof WorkOrderBuildMiner))
        {
            for (final IWorkOrder or : workOrders.values())
            {
                if (or instanceof WorkOrderBuildDecoration)
                {
                    if (((WorkOrderBuildDecoration) or).getBuildingLocation().equals(((WorkOrderBuildDecoration) order).buildingLocation)
                          && ((WorkOrderBuildDecoration) or).getStructureName().equals(((WorkOrderBuildDecoration) order).getStructureName()))
                    {
                        Log.getLogger().warn("Avoiding adding duplicate workOrder");
                        removeWorkOrder(or);
                        break;
                    }
                }
            }
            if (!readingFromNbt && !isWorkOrderWithinColony((WorkOrderBuildDecoration) order))
            {
                LanguageHandler.sendPlayersMessage(colony.getMessagePlayerEntities(), OUT_OF_COLONY, ((WorkOrderBuildDecoration) order).getName(), ((WorkOrderBuildDecoration) order).getBuildingLocation().getX(), ((WorkOrderBuildDecoration) order).getBuildingLocation().getZ());
                return;
            }
        }


        if (order.getID() == 0)
        {
            topWorkOrderId++;
            order.setID(topWorkOrderId);
        }

        if (order instanceof WorkOrderBuildDecoration && !readingFromNbt)
        {
            final StructureName structureName = new StructureName(((WorkOrderBuildDecoration) order).getStructureName());
            if (order instanceof WorkOrderBuildBuilding)
            {
                final int level = ((WorkOrderBuildBuilding) order).getUpgradeLevel();
                AdvancementUtils.TriggerAdvancementPlayersForColony(colony, player ->
                        AdvancementTriggers.CREATE_BUILD_REQUEST.trigger(player, structureName, level));
            }
            else
            {
                AdvancementUtils.TriggerAdvancementPlayersForColony(colony, player ->
                        AdvancementTriggers.CREATE_BUILD_REQUEST.trigger(player, structureName, 0));
            }
        }

        workOrders.put(order.getID(), order);
        order.onAdded(colony, readingFromNbt);
    }

    /**
     * Check if the workOrder is within a colony.
     * @param order the workorder to check.
     * @return true if so.
     */
    private boolean isWorkOrderWithinColony(final WorkOrderBuildDecoration order)
    {
        final World world = colony.getWorld();
        final Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> corners
          = ColonyUtils.calculateCorners(order.getBuildingLocation(), world,
          new Structure(world, order.getStructureName(), new PlacementSettings()), order.getRotation(world), order.isMirrored());

        Set<ChunkPos> chunks = new HashSet<>();
        final int minX = Math.min(corners.getA().getA(), corners.getA().getB());
        final int maxX = Math.max(corners.getA().getA(), corners.getA().getB());

        final int minZ = Math.min(corners.getB().getA(), corners.getB().getB());
        final int maxZ = Math.max(corners.getB().getA(), corners.getB().getB());

        for (int x = minX; x < maxX; x++)
        {
            for (int z = minZ; z < maxZ; z++)
            {
                final int chunkX = x >> 4;
                final int chunkZ = z >> 4;
                final ChunkPos pos = new ChunkPos(chunkX, chunkZ);
                if (!chunks.contains(pos))
                {
                    chunks.add(pos);
                    final IColonyTagCapability colonyCap = world.getChunk(pos.x, pos.z).getCapability(CLOSE_COLONY_CAP, null).orElseGet(null);
                    if (colonyCap == null || colonyCap.getOwningColony() != colony.getID())
                    {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Process updates on the Colony Tick. Currently, does periodic Work Order cleanup.
     *
     * @param colony the colony being ticked.
     */
    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        @NotNull final Iterator<IWorkOrder> iter = workOrders.values().iterator();
        while (iter.hasNext())
        {
            final IWorkOrder o = iter.next();
            if (!o.isValid(this.colony))
            {
                iter.remove();
                dirty = true;
            }
            else if (o.hasChanged())
            {
                dirty = true;
                o.resetChange();
            }
        }
    }

    /**
     * Get an ordered list by priority of the work orders.
     *
     * @param type    the type of workOrder which is required.
     * @param builder the builder wanting to claim it.
     * @return the list.
     */
    @Override
    public <W extends IWorkOrder> List<W> getOrderedList(@NotNull final Class<W> type, final BlockPos builder)
    {
        return workOrders.values().stream().filter(o -> (!o.isClaimed() || o.getClaimedBy().equals(builder)) && type.isInstance(o)).map(o -> (W) o)
                 .sorted(Comparator.comparingInt(IWorkOrder::getPriority).reversed())
                 .collect(Collectors.toList());
    }

    /**
     * Checks if changes has been made.
     *
     * @return true if so.
     */
    @Override
    public boolean isDirty()
    {
        return dirty;
    }

    /**
     * Sets if changes has been made.
     *
     * @param dirty true if so. False to reset.
     */
    @Override
    public void setDirty(final boolean dirty)
    {
        this.dirty = dirty;
    }

    @Override
    public IColony getColony()
    {
        return colony;
    }
}
