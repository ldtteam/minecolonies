package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.*;
import com.minecolonies.coremod.blocks.*;
import com.minecolonies.coremod.colony.*;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildBuilding;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.citizen.builder.ConstructionTapeHelper;
import com.minecolonies.coremod.entity.ai.citizen.deliveryman.EntityAIWorkDeliveryman;
import com.minecolonies.coremod.inventory.api.CombinedItemHandler;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import com.minecolonies.coremod.tileentities.TileEntityRack;
import com.minecolonies.coremod.util.ColonyUtils;
import com.minecolonies.coremod.util.StructureWrapper;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.BuildingConstants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.Suppression.*;

/**
 * Base building class, has all the foundation for what a building stores and does.
 * <p>
 * We suppress the warning which warns you about referencing child classes in the parent because that's how we register the instances of the childClasses
 * to their views and blocks.
 */
@SuppressWarnings("squid:S2390")
public abstract class AbstractBuilding extends AbstractRequestingBuilding implements ICapabilityProvider
{
    /**
     * The tileEntity of the building.
     */
    private TileEntityColonyBuilding tileEntity;

    /**
     * List of workers assosiated to the building.
     */
    private final List<CitizenData> assignedCitizen = new ArrayList();

    /**
     * List of items the worker should keep.
     */
    protected final Map<Predicate<ItemStack>, Integer> keepX = new HashMap<>();

    /**
     * A list which contains the position of all containers which belong to the
     * worker building.
     */
    private final List<BlockPos> containerList = new ArrayList<>();

    /**
     * Priority of the building in the pickUpList.
     */
    private int pickUpPriority = 1;
    /**
     * Is being gathered right now
     */
    private boolean beingGathered = false;

    /**
     * Constructor for a AbstractBuilding.
     *
     * @param colony Colony the building belongs to.
     * @param pos    Location of the building (it's Hut Block).
     */
    protected AbstractBuilding(@NotNull final Colony colony, final BlockPos pos)
    {
        super(pos, colony);
    }

    /**
     * Load data from NBT compound.
     * Writes to {@link #buildingLevel}, {@link #rotation} and {@link #style}.
     *
     * @param compound {@link net.minecraft.nbt.NBTTagCompound} to read data from.
     */
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        final NBTTagList containerTagList = compound.getTagList(TAG_CONTAINERS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < containerTagList.tagCount(); ++i)
        {
            final NBTTagCompound containerCompound = containerTagList.getCompoundTagAt(i);
            containerList.add(NBTUtil.getPosFromTag(containerCompound));
        }

        if (compound.hasKey(TAG_PRIO))
        {
            this.pickUpPriority = compound.getInteger(TAG_PRIO);
        }

        assignedCitizen.clear();
    }

    /**
     * executed when a new day start.
     */
    public void onWakeUp()
    {
        /**
         * Buildings override this if required.
         */
    }

    /**
     * Checks if a block matches the current object.
     *
     * @param block Block you want to know whether it matches this class or not.
     * @return True if the block matches this class, otherwise false.
     */
    public boolean isMatchingBlock(@NotNull final Block block)
    {
        final Class<?> c = BuildingRegistry.blockClassToBuildingClassMap.get(block.getClass());
        return getClass().equals(c);
    }

    /**
     * Save data to NBT compound.
     * Writes the {@link #buildingLevel}, {@link #rotation}, {@link #style}, {@link #location}, and {@link #getClass()} value.
     *
     * @param compound {@link net.minecraft.nbt.NBTTagCompound} to write data to.
     */
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        @NotNull final NBTTagList containerTagList = new NBTTagList();
        for (@NotNull final BlockPos pos : containerList)
        {
            containerTagList.appendTag(NBTUtil.createPosTag(pos));
        }
        compound.setTag(TAG_CONTAINERS, containerTagList);

        compound.setInteger(TAG_PRIO, this.pickUpPriority);
    }

    /**
     * Destroys the block.
     * Calls {@link #onDestroyed()}.
     */
    public final void destroy()
    {
        onDestroyed();
        colony.getBuildingManager().removeBuilding(this, colony.getPackageManager().getSubscribers());
    }

    /**
     * Method to do things when a block is destroyed.
     */
    public void onDestroyed()
    {
        final TileEntityColonyBuilding tileEntityNew = this.getTileEntity();
        final World world = colony.getWorld();
        final Block block = world.getBlockState(this.getLocation()).getBlock();

        if (tileEntityNew != null)
        {
            InventoryHelper.dropInventoryItems(world, this.getLocation(), (IInventory) tileEntityNew);
            world.updateComparatorOutputLevel(this.getLocation(), block);
        }

        ConstructionTapeHelper.removeConstructionTape(getCorners(), world);

        if (hasAssignedCitizen())
        {
            // EntityCitizen will detect the workplace is gone and fix up it's
            // Entity properly
            assignedCitizen.clear();
        }
    }

    /**
     * Returns the tile entity that belongs to the colony building.
     *
     * @return {@link TileEntityColonyBuilding} object of the building.
     */
    public TileEntityColonyBuilding getTileEntity()
    {
        if ((tileEntity == null || tileEntity.isInvalid())
              && colony != null
              && colony.getWorld() != null
              && getLocation() != null
              && colony.getWorld().getBlockState(getLocation())
                   != null && colony.getWorld().getBlockState(this.getLocation()).getBlock() instanceof AbstractBlockHut)
        {
            final TileEntity te = getColony().getWorld().getTileEntity(getLocation());
            if (te instanceof TileEntityColonyBuilding)
            {
                tileEntity = (TileEntityColonyBuilding) te;
                if (tileEntity.getBuilding() == null)
                {
                    tileEntity.setColony(colony);
                    tileEntity.setBuilding(this);
                }
            }
        }

        return tileEntity;
    }

    /**
     * Sets the tile entity for the building.
     *
     * @param te {@link TileEntityColonyBuilding} that will fill the {@link #tileEntity} field.
     */
    public void setTileEntity(final TileEntityColonyBuilding te)
    {
        tileEntity = te;
    }

    /**
     * On tick of the server.
     *
     * @param event {@link net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent}
     */
    public void onServerTick(final TickEvent.ServerTickEvent event)
    {
        // Can be overridden by other buildings.
    }

    /**
     * On tick of the world.
     *
     * @param event {@link net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent}
     */
    public void onWorldTick(final TickEvent.WorldTickEvent event)
    {
        // Can be overridden by other buildings.
    }

    /**
     * Adds work orders to the {@link Colony#workManager}.
     *
     * @param level Desired level.
     */
    protected void requestWorkOrder(final int level)
    {
        for (@NotNull final WorkOrderBuildBuilding o : colony.getWorkManager().getWorkOrdersOfType(WorkOrderBuildBuilding.class))
        {
            if (o.getBuildingLocation().equals(getID()))
            {
                return;
            }
        }

        colony.getWorkManager().addWorkOrder(new WorkOrderBuildBuilding(this, level), false);
        LanguageHandler.sendPlayersMessage(colony.getMessageEntityPlayers(), "com.minecolonies.coremod.workOrderAdded");
        markDirty();
    }

    @Override
    public final void markDirty()
    {
        super.markDirty();
        if (colony != null)
        {
            colony.getBuildingManager().markBuildingsDirty();
        }
    }

    /**
     * Checks if this building have a work order.
     *
     * @return true if the building is building, upgrading or repairing.
     */
    public boolean hasWorkOrder()
    {
        return getCurrentWorkOrderLevel() != NO_WORK_ORDER;
    }

    /**
     * Get the current level of the work order.
     *
     * @return NO_WORK_ORDER if not current work otherwise the level requested.
     */
    private int getCurrentWorkOrderLevel()
    {
        for (@NotNull final WorkOrderBuildBuilding o : colony.getWorkManager().getWorkOrdersOfType(WorkOrderBuildBuilding.class))
        {
            if (o.getBuildingLocation().equals(getID()))
            {
                return o.getUpgradeLevel();
            }
        }

        return NO_WORK_ORDER;
    }

    /**
     * Remove the work order for the building.
     * <p>
     * Remove either the upgrade or repair work order
     */
    public void removeWorkOrder()
    {
        for (@NotNull final WorkOrderBuildBuilding o : colony.getWorkManager().getWorkOrdersOfType(WorkOrderBuildBuilding.class))
        {
            if (o.getBuildingLocation().equals(getID()))
            {
                colony.getWorkManager().removeWorkOrder(o.getID());
                markDirty();

                final int citizenThatIsBuilding = o.getClaimedBy();
                final CitizenData data = colony.getCitizenManager().getCitizen(citizenThatIsBuilding);
                if (data != null && data.getWorkBuilding() != null)
                {
                    data.getWorkBuilding().cancelAllRequestsOfCitizen(data);
                }
                return;
            }
        }
    }

    /**
     * Serializes to view.
     * Sends 3 integers.
     * 1) hashcode of the name of the class.
     * 2) building level.
     * 3) max building level.
     *
     * @param buf ByteBuf to write to.
     */
    public void serializeToView(@NotNull final ByteBuf buf)
    {
        buf.writeInt(this.getClass().getName().hashCode());
        buf.writeInt(getBuildingLevel());
        buf.writeInt(getMaxBuildingLevel());
        buf.writeInt(getPickUpPriority());
        buf.writeInt(getCurrentWorkOrderLevel());
        ByteBufUtils.writeUTF8String(buf, getStyle());
        ByteBufUtils.writeUTF8String(buf, this.getSchematicName());
        buf.writeInt(getRotation());
        buf.writeBoolean(isMirrored());
        final NBTTagCompound requestSystemCompound = new NBTTagCompound();
        writeRequestSystemToNBT(requestSystemCompound);

        ByteBufUtils.writeTag(buf, requestSystemCompound);
    }

    /**
     * Get the pick up priority of the building.
     *
     * @return the priority, an integer.
     */
    public int getPickUpPriority()
    {
        return this.pickUpPriority;
    }

    /**
     * Register a blockState and position.
     * We suppress this warning since this parameter will be used in child classes which override this method.
     *
     * @param blockState to be registered
     * @param pos   of the blockState
     */
    public void registerBlockPosition(@NotNull final IBlockState blockState, @NotNull final BlockPos pos, @NotNull final World world)
    {
        registerBlockPosition(blockState.getBlock(), pos, world);
    }

    /**
     * Register a block and position.
     * We suppress this warning since this parameter will be used in child classes which override this method.
     *
     * @param block to be registered
     * @param pos   of the block
     */
    @SuppressWarnings("squid:S1172")
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos, @NotNull final World world)
    {
        if (block instanceof BlockContainer || block instanceof BlockMinecoloniesRack)
        {
            addContainerPosition(pos);
        }
    }

    /**
     * Add a new container to the building.
     *
     * @param pos position to add.
     */
    public void addContainerPosition(@NotNull final BlockPos pos)
    {
        if (!containerList.contains(pos))
        {
            containerList.add(pos);
        }
    }

    /**
     * Remove a container from the building.
     *
     * @param pos position to remove.
     */
    public void removeContainerPosition(final BlockPos pos)
    {
        containerList.remove(pos);
    }

    /**
     * Get all additional containers which belong to the building.
     *
     * @return a copy of the list to avoid currentModification exception.
     */
    public List<BlockPos> getAdditionalCountainers()
    {
        return new ArrayList<>(containerList);
    }

    /**
     * Increase or decrease the current pickup priority.
     *
     * @param value the new prio to add to.
     */
    public void alterPickUpPriority(final int value)
    {
        if (this.pickUpPriority + value < 1)
        {
            this.pickUpPriority = 1;
        }
        else if (this.pickUpPriority + value > MAX_PRIO)
        {
            this.pickUpPriority = MAX_PRIO;
        }
        else
        {
            this.pickUpPriority += value;
        }
    }

    /**
     * Check if a building is being gathered.
     *
     * @return true if so.
     */
    public boolean isBeingGathered()
    {
        return this.beingGathered;
    }

    /**
     * Set if a building is being gathered.
     *
     * @param gathering value to set.
     */
    public void setBeingGathered(final boolean gathering)
    {
        this.beingGathered = gathering;
    }

    /**
     * Requests an upgrade for the current building.
     *
     * @param player the requesting player.
     */
    public void requestUpgrade(final EntityPlayer player)
    {
        if (getBuildingLevel() < getMaxBuildingLevel())
        {
            requestWorkOrder(getBuildingLevel() + 1);
        }
        else
        {
            player.sendMessage(new TextComponentTranslation("com.minecolonies.coremod.worker.noUpgrade"));
        }
    }

    /**
     * Requests a repair for the current building.
     */
    public void requestRepair()
    {
        if (getBuildingLevel() > 0)
        {
            requestWorkOrder(getBuildingLevel());
        }
    }

    /**
     * Deconstruct the building on destroyed.
     */
    public void deconstruct()
    {
        final Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> tuple = getCorners();
        for (int x = tuple.getFirst().getFirst(); x < tuple.getFirst().getSecond(); x++)
        {
            for (int z = tuple.getSecond().getFirst(); z < tuple.getSecond().getSecond(); z++)
            {
                for (int y = getLocation().getY() - 1; y < getLocation().getY() + this.getHeight(); y++)
                {
                    getColony().getWorld().destroyBlock(new BlockPos(x, y, z), false);
                }
            }
        }
    }

    /**
     * Called upon completion of an upgrade process.
     * We suppress this warning since this parameter will be used in child classes which override this method.
     *
     * @param newLevel The new level.
     */
    @SuppressWarnings("squid:S1172")
    public void onUpgradeComplete(final int newLevel)
    {
        final WorkOrderBuildBuilding workOrder = new WorkOrderBuildBuilding(this, newLevel);
        final StructureWrapper wrapper = new StructureWrapper(colony.getWorld(), workOrder.getStructureName());
        final Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> corners
                = ColonyUtils.calculateCorners(this.getLocation(),
                colony.getWorld(),
                wrapper,
                workOrder.getRotation(colony.getWorld()),
                workOrder.isMirrored());
        this.setHeight(wrapper.getHeight());
        this.setCorners(corners.getFirst().getFirst(), corners.getFirst().getSecond(), corners.getSecond().getFirst(), corners.getSecond().getSecond());
    }

    //------------------------- Starting Required Tools/Item handling -------------------------//

    @Override
    public int hashCode()
    {
        return (int) (31 * this.getID().toLong());
    }

    @Override
    public boolean equals(final Object o)
    {
        return o instanceof AbstractBuilding && ((AbstractBuilding) o).getID().equals(this.getID());
    }

    /**
     * Try to transfer a stack to one of the inventories of the building.
     *
     * @param stack the stack to transfer.
     * @param world the world to do it in.
     * @return The {@link ItemStack} as that is left over, might be {@link ItemStackUtils#EMPTY} if the stack was completely accepted
     */
    public ItemStack transferStack(@NotNull final ItemStack stack, @NotNull final World world)
    {
        if (tileEntity == null || InventoryUtils.isProviderFull(tileEntity))
        {
            final Iterator<BlockPos> posIterator = containerList.iterator();
            @NotNull ItemStack resultStack = stack.copy();

            while (posIterator.hasNext() && !ItemStackUtils.isEmpty(resultStack))
            {
                final BlockPos pos = posIterator.next();
                final TileEntity tempTileEntity = world.getTileEntity(pos);
                if (tempTileEntity instanceof TileEntityChest && !InventoryUtils.isProviderFull(tempTileEntity))
                {
                    resultStack = InventoryUtils.addItemStackToProviderWithResult(tempTileEntity, stack);
                }
            }

            return resultStack;
        }
        else
        {
            return InventoryUtils.addItemStackToProviderWithResult(tileEntity, stack);
        }
    }

    /**
     * Check if the worker requires a certain amount of that item and the alreadykept list contains it.
     * Always leave one stack behind if the worker requires a certain amount of it. Just to be sure.
     *
     * @param stack            the stack to check it with.
     * @param localAlreadyKept already kept items.
     * @return true if it should be leave it behind.
     */
    public boolean buildingRequiresCertainAmountOfItem(final ItemStack stack, final List<ItemStorage> localAlreadyKept)
    {
        for (final Map.Entry<Predicate<ItemStack>, Integer> entry : getRequiredItemsAndAmount().entrySet())
        {
            if (entry.getKey().test(stack))
            {
                final ItemStorage kept = ItemStorage.getItemStackOfListMatchingPredicate(localAlreadyKept, entry.getKey());
                if (kept != null)
                {
                    if (kept.getAmount() >= entry.getValue())
                    {
                        return false;
                    }

                    localAlreadyKept.remove(kept);
                    kept.setAmount(kept.getAmount() + ItemStackUtils.getSize(stack));
                    localAlreadyKept.add(kept);
                    return true;
                }

                localAlreadyKept.add(new ItemStorage(stack));
                return true;
            }
        }
        return false;
    }

    /**
     * Override this method if you want to keep an amount of items in inventory.
     * When the inventory is full, everything get's dumped into the building chest.
     * But you can use this method to hold some stacks back.
     *
     * @return a list of objects which should be kept.
     */
    public Map<Predicate<ItemStack>, Integer> getRequiredItemsAndAmount()
    {
        final Map<Predicate<ItemStack>, Integer> toKeep = new HashMap<>();
        toKeep.putAll(keepX);
        final IRequestManager manager = colony.getRequestManager();
        toKeep.put(stack -> this.getOpenRequestsByCitizen().values().stream()
                .anyMatch(list -> list.stream()
                        .anyMatch(token -> manager.getRequestForToken(token).getRequest() instanceof IDeliverable
                                && ((IDeliverable) manager.getRequestForToken(token).getRequest()).matches(stack))), Integer.MAX_VALUE);

        return toKeep;
    }

    /**
     * Try to transfer a stack to one of the inventories of the building and force the transfer.
     *
     * @param stack the stack to transfer.
     * @param world the world to do it in.
     * @return the itemStack which has been replaced or the itemStack which could not be transfered
     */
    @Nullable
    public ItemStack forceTransferStack(final ItemStack stack, final World world)
    {
        if (tileEntity == null)
        {
            for (final BlockPos pos : containerList)
            {
                final TileEntity tempTileEntity = world.getTileEntity(pos);
                if (tempTileEntity instanceof TileEntityChest && !InventoryUtils.isProviderFull(tempTileEntity))
                {
                    return forceItemStackToProvider(tempTileEntity, stack);
                }
            }
        }
        else
        {
            return forceItemStackToProvider(tileEntity, stack);
        }
        return stack;
    }

    @Nullable
    private ItemStack forceItemStackToProvider(@NotNull final ICapabilityProvider provider, @NotNull final ItemStack itemStack)
    {
        final List<ItemStorage> localAlreadyKept = new ArrayList<>();
        return InventoryUtils.forceItemStackToProvider(provider, itemStack, (ItemStack stack) -> EntityAIWorkDeliveryman.workerRequiresItem(this, stack, localAlreadyKept));
    }

    //------------------------- Ending Required Tools/Item handling -------------------------//

    //------------------------- Starting Assigned Citizen handling -------------------------//

    /**
     * Get the main worker of the building (the first in the list).
     *
     * @return the matching CitizenData.
     */
    public CitizenData getMainCitizen()
    {
        if (assignedCitizen.isEmpty())
        {
            return null;
        }
        return assignedCitizen.get(0);
    }

    /**
     * Returns the worker of the current building.
     *
     * @return {@link CitizenData} of the current building
     */
    public List<CitizenData> getAssignedCitizen()
    {
        return new ArrayList<>(assignedCitizen);
    }

    /**
     * Method to remove a citizen.
     *
     * @param citizen Citizen to be removed.
     */
    public void removeCitizen(final CitizenData citizen)
    {
        if (isCitizenAssigned(citizen))
        {
            assignedCitizen.remove(citizen);
            citizen.setWorkBuilding(null);
            markDirty();
        }
    }

    /**
     * Returns if the {@link CitizenData} is the same as the worker.
     *
     * @param citizen {@link CitizenData} you want to compare
     * @return true if same citizen, otherwise false
     */
    public boolean isCitizenAssigned(final CitizenData citizen)
    {
        return assignedCitizen.contains(citizen);
    }

    /**
     * Returns the first worker in the list.
     *
     * @return the EntityCitizen of that worker.
     */
    public Optional<EntityCitizen> getMainCitizenEntity()
    {
        if (assignedCitizen.isEmpty())
        {
            return Optional.empty();
        }
        return assignedCitizen.get(0).getCitizenEntity();
    }

    /**
     * Returns whether or not the building has a worker.
     *
     * @return true if building has worker, otherwise false.
     */
    public boolean hasAssignedCitizen()
    {
        return !assignedCitizen.isEmpty();
    }

    /**
     * Returns the {@link net.minecraft.entity.Entity} of the worker.
     *
     * @return {@link net.minecraft.entity.Entity} of the worker
     */
    @Nullable
    public List<Optional<EntityCitizen>> getAssignedEntities()
    {
        return assignedCitizen.stream().filter(Objects::nonNull).map(CitizenData::getCitizenEntity).collect(Collectors.toList());
    }

    /**
     * Assign the citizen to the current building.
     *
     * @param citizen {@link CitizenData} of the worker
     */
    public boolean assignCitizen(final CitizenData citizen)
    {
        if (assignedCitizen.contains(citizen))
        {
            return false;
        }

        // If we set a worker, inform it of such
        if (citizen != null)
        {
            assignedCitizen.add(citizen);
        }

        markDirty();
        return true;
    }

    /**
     * Assign the citizen after loading it from NBT.
     * @param data the citizen data.
     */
    public void assignCitizenFromNBtAction(final CitizenData data)
    {
        /**
         * Specific classes will override this.
         */
    }

    /**
     * Returns whether the citizen has this as home or not.
     *
     * @param citizen Citizen to check.
     * @return True if citizen lives here, otherwise false.
     */
    public boolean hasAssignedCitizen(final CitizenData citizen)
    {
        return assignedCitizen.contains(citizen);
    }


    /**
     * Checks if the building is full.
     *
     * @return true if so.
     */
    public boolean isFull()
    {
        return assignedCitizen.size() >= getMaxInhabitants();
    }

    /**
     * Returns the max amount of inhabitants.
     *
     * @return Max inhabitants.
     */
    public int getMaxInhabitants()
    {
        return 1;
    }


    //------------------------- Ending Assigned Citizen handling -------------------------//
    //------------------------- !Start! Capabilities handling for minecolonies buildings -------------------------//

    @Override
    public boolean hasCapability(
      @Nonnull final Capability<?> capability, @Nullable final EnumFacing facing)
    {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing == null;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing == null)
        {
            final Set<ICapabilityProvider> providers = new HashSet<>();

            //Add myself
            providers.add(getTileEntity());

            //Add additional containers
            providers.addAll(getAdditionalCountainers().stream()
                               .map(getTileEntity().getWorld()::getTileEntity)
                               .filter(entity -> (entity instanceof TileEntityChest) || (entity instanceof TileEntityRack))
                               .collect(Collectors.toSet()));
            providers.removeIf(Objects::isNull);

            //Map all providers to IItemHandlers.
            final Set<IItemHandlerModifiable> modifiables = providers
                                                              .stream()
                                                              .flatMap(provider -> InventoryUtils.getItemHandlersFromProvider(provider).stream())
                                                              .filter(handler -> handler instanceof IItemHandlerModifiable)
                                                              .map(handler -> (IItemHandlerModifiable) handler)
                                                              .collect(Collectors.toSet());

            return (T) new CombinedItemHandler(getSchematicName(), modifiables.toArray(new IItemHandlerModifiable[modifiables.size()]));
        }

        return null;
    }

    //------------------------- !End! Capabilities handling for minecolonies buildings -------------------------//
}
