package com.minecolonies.core.colony.workorders;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.storage.StructurePacks;
import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.workorders.IBuilderWorkOrder;
import com.minecolonies.api.colony.workorders.IServerWorkOrder;
import com.minecolonies.api.colony.workorders.IWorkManager;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.api.util.ColonyUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.core.colony.Colony;
import com.minecolonies.core.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.core.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.core.colony.buildings.modules.settings.StringSetting;
import com.minecolonies.core.colony.jobs.AbstractJobStructure;
import com.minecolonies.core.colony.jobs.JobBuilder;
import com.minecolonies.core.util.AdvancementUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.TranslationConstants.OUT_OF_COLONY;
import static com.minecolonies.core.colony.buildings.workerbuildings.BuildingBuilder.MANUAL_SETTING;
import static com.minecolonies.core.colony.buildings.workerbuildings.BuildingBuilder.MODE;

/**
 * Handles work orders for a colony.
 */
public class WorkManager implements IWorkManager
{
    /**
     * Workorder NBT tags.
     */
    private static final String TAG_WORK_ORDERS = "workOrders";
    private static final String TAG_NEW_SYSTEM  = "newsystem";

    //  Once a second
    //private static final int    WORK_ORDER_FULFILL_INCREMENT = 1 * 20;
    /**
     * The Colony the workManager takes part of.
     */
    private final Colony                         colony;
    @NotNull
    private final Map<Integer, IServerWorkOrder> workOrders     = new LinkedHashMap<>();
    private       int                            topWorkOrderId = 0;
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
     * @param order {@link IWorkOrder} to remove.
     */
    @Override
    public void removeWorkOrder(@NotNull final IServerWorkOrder order)
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
            dirty = true;
            workOrders.remove(orderId);
            colony.removeWorkOrderInView(orderId);
            if (workOrder instanceof IBuilderWorkOrder builderWorkOrder)
            {
                builderWorkOrder.onRemoved(colony);
            }
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
    public <W extends IServerWorkOrder> W getWorkOrder(final int id, @NotNull final Class<W> type)
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
    public IServerWorkOrder getWorkOrder(final int id)
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
    public <W extends IServerWorkOrder> W getUnassignedWorkOrder(@NotNull final Class<W> type)
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
    public <W extends IServerWorkOrder> List<W> getWorkOrdersOfType(@NotNull final Class<W> type)
    {
        return workOrders.values().stream().filter(type::isInstance).map(type::cast).collect(Collectors.toList());
    }

    /**
     * Get all work orders.
     *
     * @return a list of all work orders.
     */
    @Override
    public @NotNull Map<Integer, IServerWorkOrder> getWorkOrders()
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
        if (citizen.getWorkBuilding() != null)
        {
            for (final IWorkOrder workOrder : workOrders.values())
            {
                if (citizen.getWorkBuilding().getPosition().equals(workOrder.getClaimedBy()))
                {
                    workOrder.setClaimedBy(null);
                }
            }
        }
    }

    /**
     * Save the Work Manager.
     *
     * @param compound Compound to save to.
     */
    @Override
    public void write(@NotNull final CompoundTag compound)
    {
        //  Work Orders
        @NotNull final ListTag list = new ListTag();
        for (@NotNull final IServerWorkOrder o : workOrders.values())
        {
            @NotNull final CompoundTag orderCompound = new CompoundTag();
            o.write(orderCompound);
            list.add(orderCompound);
        }
        compound.put(TAG_WORK_ORDERS, list);
        compound.putBoolean(TAG_NEW_SYSTEM, true);
    }

    /**
     * Restore the Work Manager.
     *
     * @param compound Compound to read from.
     */
    @Override
    public void read(@NotNull final CompoundTag compound)
    {
        workOrders.clear();

        if (!compound.contains(TAG_NEW_SYSTEM))
        {
            // On the new system, we drop all current workorders to avoid any issues.
            return;
        }

        //  Work Orders
        final ListTag list = compound.getList(TAG_WORK_ORDERS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); ++i)
        {
            final CompoundTag orderCompound = list.getCompound(i);
            @Nullable final IServerWorkOrder o = AbstractWorkOrder.createFromNBT(orderCompound, this);
            if (o != null)
            {
                addWorkOrder(o, true);

                //  If this Work Order is claimed, and the Citizen who claimed it no longer exists
                //  then clear the Claimed status
                //  This is just a failsafe cleanup; this should not happen under normal circumstances
                if (o.isClaimed() && colony.getBuildingManager().getBuilding(o.getClaimedBy()) == null)
                {
                    o.setClaimedBy(null);
                }

                topWorkOrderId = Math.max(topWorkOrderId, o.getID());
            }
        }
    }

    /**
     * Adds work order to the work manager.
     *
     * @param order          Order adding.
     * @param readingFromNbt if being read from NBT.
     */
    @Override
    public void addWorkOrder(@NotNull final IServerWorkOrder order, final boolean readingFromNbt)
    {
        dirty = true;

        if (!(order instanceof WorkOrderMiner))
        {
            for (final IServerWorkOrder or : workOrders.values())
            {
                if (or.getLocation().equals(order.getLocation()) && or.getStructurePath().equals(order.getStructurePath()) && or.getStructurePack().equals(order.getStructurePack()))
                {
                    Log.getLogger().warn("Avoiding adding duplicate workOrder");
                    removeWorkOrder(or);
                    break;
                }
            }
            if (!readingFromNbt && !isWorkOrderWithinColony(order))
            {
                MessageUtils.format(OUT_OF_COLONY, order.getDisplayName(), order.getLocation().getX(), order.getLocation().getZ()).sendTo(colony).forAllPlayers();
                return;
            }
        }

        if (order.getID() == 0)
        {
            topWorkOrderId++;
            order.setID(topWorkOrderId);
        }

        final int level = order.getTargetLevel();
        if (!readingFromNbt)
        {
            if (order instanceof WorkOrderBuilding buildingOrder)
            {
                final IBuilding building = colony.getBuildingManager().getBuilding(buildingOrder.getLocation());
                if (building != null)
                {
                    AdvancementUtils.TriggerAdvancementPlayersForColony(colony,
                            player -> AdvancementTriggers.CREATE_BUILD_REQUEST.trigger(player, building.getBuildingType().getRegistryName().getPath(), level));
                }
            }
            else if (order instanceof WorkOrderDecoration)
            {
                AdvancementUtils.TriggerAdvancementPlayersForColony(colony,
                  player -> AdvancementTriggers.CREATE_BUILD_REQUEST.trigger(player, order.getFileName().replace(String.valueOf(level), ""), level));
            }
        }

        workOrders.put(order.getID(), order);
        order.onAdded(colony, readingFromNbt);
    }

    /**
     * Check if the workOrder is within a colony.
     *
     * @param order the workorder to check.
     * @return true if so.
     */
    private boolean isWorkOrderWithinColony(final IWorkOrder order)
    {
        final Level world = colony.getWorld();
        final Blueprint blueprint = StructurePacks.getBlueprint(order.getStructurePack(), order.getStructurePath());
        final Tuple<BlockPos, BlockPos> corners
          = ColonyUtils.calculateCorners(order.getLocation(),
          world,
          blueprint,
          order.getRotation(),
          order.isMirrored());

        Set<ChunkPos> chunks = new HashSet<>();
        final int minX = Math.min(corners.getA().getX(), corners.getB().getX()) + 1;
        final int maxX = Math.max(corners.getA().getX(), corners.getB().getX());

        final int minZ = Math.min(corners.getA().getZ(), corners.getB().getZ()) + 1;
        final int maxZ = Math.max(corners.getA().getZ(), corners.getB().getZ());

        for (int x = minX; x < maxX; x += 16)
        {
            for (int z = minZ; z < maxZ; z += 16)
            {
                final int chunkX = x >> 4;
                final int chunkZ = z >> 4;
                final ChunkPos pos = new ChunkPos(chunkX, chunkZ);
                if (!chunks.contains(pos))
                {
                    chunks.add(pos);
                    if (ColonyUtils.getOwningColony(world.getChunk(pos.x, pos.z)) != colony.getID())
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
        @NotNull final Iterator<IServerWorkOrder> iter = workOrders.values().iterator();
        while (iter.hasNext())
        {
            final IServerWorkOrder order = iter.next();
            if (!order.isValid(this.colony))
            {
                iter.remove();
                dirty = true;
                continue;
            }
            else if (order.isDirty())
            {
                dirty = true;
                order.resetChange();
            }

            tryAssignWorkOrder(order, (b) -> order.getClaimedBy().equals(b.getPosition()));
        }

        for (final IServerWorkOrder wo : colony.getWorkManager().getWorkOrders().values())
        {
            tryAssignWorkOrder(wo, wo::canBuild);
        }
    }

    /**
     * Try assign a workorder to a structure builder (miner, builder, etc).
     * @param order the workorder to assign.
     * @param predicate checks to execute before assignment.
     */
    private void tryAssignWorkOrder(final IServerWorkOrder order, @Nullable Predicate<IBuilding> predicate)
    {
        for (IBuilding building : colony.getBuildingManager().getBuildings().values())
        {
            if (building instanceof AbstractBuildingStructureBuilder)
            {
                final @Nullable ICitizenData citizen = building.getFirstModuleOccurance(WorkerBuildingModule.class).getFirstCitizen();
                if (citizen == null)
                {
                    return;
                }

                if (citizen.getJob() instanceof AbstractJobStructure<?,?> abstractJobStructure)
                {
                    if (abstractJobStructure.hasWorkOrder())
                    {
                        continue;
                    }

                    if (order.getClaimedBy().equals(building.getPosition()))
                    {
                        abstractJobStructure.setWorkOrder(order);
                        order.setClaimedBy(building.getID());
                        return;
                    }

                    final StringSetting setting = building.getSetting(MODE);
                    if (setting != null && setting.getValue().equals(MANUAL_SETTING))
                    {
                        continue;
                    }

                    if (predicate.test(building))
                    {
                        abstractJobStructure.setWorkOrder(order);
                        order.setClaimedBy(building.getID());
                        return;
                    }
                }
            }
        }
    }

    /**
     * Get an ordered list by priority of the work orders.
     *
     * @param builder the builder wanting to claim it.
     * @param type    the type of workOrder which is required.
     * @param <W>     the type.
     * @return the list.
     */
    @Override
    public <W extends IServerWorkOrder> List<W> getOrderedList(Class<W> type, BlockPos builder)
    {
        return getOrderedList(type::isInstance, builder)
          .stream()
          .map(m -> (W) m)
          .collect(Collectors.toList());
    }

    /**
     * Get an ordered list by priority of the work orders.
     *
     * @param builder   the builder wanting to claim it.
     * @param predicate a predicate to check each item against
     * @return the list.
     */
    @Override
    public List<IServerWorkOrder> getOrderedList(@NotNull Predicate<IServerWorkOrder> predicate, final BlockPos builder)
    {
        return workOrders.values().stream()
          .filter(o -> (!o.isClaimed() || o.getClaimedBy().equals(builder)))
          .filter(predicate)
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
