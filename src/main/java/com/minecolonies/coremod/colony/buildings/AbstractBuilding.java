package com.minecolonies.coremod.colony.buildings;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.data.IRequestSystemBuildingDataStore;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolverProvider;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.*;
import com.minecolonies.coremod.colony.buildings.registry.BuildingRegistry;
import com.minecolonies.coremod.colony.requestsystem.requesters.BuildingBasedRequester;
import com.minecolonies.coremod.colony.requestsystem.resolvers.BuildingRequestResolver;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildBuilding;
import com.minecolonies.coremod.entity.ai.citizen.builder.ConstructionTapeHelper;
import com.minecolonies.coremod.entity.ai.citizen.deliveryman.EntityAIWorkDeliveryman;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import com.minecolonies.coremod.util.ColonyUtils;
import com.minecolonies.coremod.util.StructureWrapper;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
public abstract class AbstractBuilding extends AbstractBuildingContainer implements IRequestResolverProvider, IRequester
{
    /**
     * The data store id for request system related data.
     */
    @NotNull
    private IToken<?> rsDataStoreToken;

    /**
     * The ID of the building. Needed in the request system to identify it.
     */
    private IRequester requester;

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

        this.requester = StandardFactoryController.getInstance().getNewInstance(TypeToken.of(BuildingBasedRequester.class), this);
        setupRsDataStore();
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        loadRequestSystemFromNBT(compound);
    }

    /**
     * executed when a new day start.
     */
    public void onWakeUp()
    {
        /*
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
        final Class<?> c = BuildingRegistry.getBlockClassToBuildingClassMap().get(block.getClass());
        return getClass().equals(c);
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        writeRequestSystemToNBT(compound);
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

    @Override
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
        if (getTileEntity() == null)
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
            return forceItemStackToProvider(getTileEntity(), stack);
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

    //------------------------- !START! RequestSystem handling for minecolonies buildings -------------------------//

    protected void writeRequestSystemToNBT(final NBTTagCompound compound)
    {
        compound.setTag(TAG_RS_BUILDING_DATASTORE, StandardFactoryController.getInstance().serialize(rsDataStoreToken));
    }

    protected void setupRsDataStore()
    {
        this.rsDataStoreToken = colony.getRequestManager()
                .getDataStoreManager()
                .get(
                        StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
                        TypeConstants.REQUEST_SYSTEM_BUILDING_DATA_STORE
                )
                .getId();
    }

    private void loadRequestSystemFromNBT(final NBTTagCompound compound)
    {
        if (compound.hasKey(TAG_REQUESTOR_ID))
        {
            this.requester = StandardFactoryController.getInstance().deserialize(compound.getCompoundTag(TAG_REQUESTOR_ID));
        }
        else
        {
            this.requester = StandardFactoryController.getInstance().getNewInstance(TypeToken.of(BuildingBasedRequester.class), this);
        }

        if (compound.hasKey(TAG_RS_BUILDING_DATASTORE))
        {
            this.rsDataStoreToken = StandardFactoryController.getInstance().deserialize(compound.getCompoundTag(TAG_RS_BUILDING_DATASTORE));
        }
        else
        {
            setupRsDataStore();
        }
    }

    private IRequestSystemBuildingDataStore getDataStore()
    {
        return colony.getRequestManager().getDataStoreManager().get(rsDataStoreToken, TypeConstants.REQUEST_SYSTEM_BUILDING_DATA_STORE);
    }

    private Map<TypeToken<?>, Collection<IToken<?>>> getOpenRequestsByRequestableType()
    {
        return getDataStore().getOpenRequestsByRequestableType();
    }

    protected Map<Integer, Collection<IToken<?>>> getOpenRequestsByCitizen()
    {
        return getDataStore().getOpenRequestsByCitizen();
    }

    private Map<Integer, Collection<IToken<?>>> getCompletedRequestsByCitizen()
    {
        return getDataStore().getCompletedRequestsByCitizen();
    }

    private Map<IToken<?>, Integer> getCitizensByRequest()
    {
        return getDataStore().getCitizensByRequest();
    }

    /**
     * Create a request for a citizen.
     * @param citizenData the data of the citizen.
     * @param requested the request to create.
     * @param async if async or not.
     * @param <R> the type of the request.
     * @return the Token of the request.
     */
    public <R extends IRequestable> IToken<?> createRequest(@NotNull final CitizenData citizenData, @NotNull final R requested, final boolean async)
    {
        final IToken requestToken = colony.getRequestManager().createRequest(requester, requested);
        if (async)
        {
            citizenData.getJob().getAsyncRequests().add(requestToken);
        }
        addRequestToMaps(citizenData.getId(), requestToken, TypeToken.of(requested.getClass()));

        colony.getRequestManager().assignRequest(requestToken);

        markDirty();

        return requestToken;
    }

    /**
     * Internal method used to register a new Request to the request maps.
     * Helper method.
     *
     * @param citizenId    The id of the citizen.
     * @param requestToken The {@link IToken} that is used to represent the request.
     * @param requested    The class of the type that has been requested eg. {@code ItemStack.class}
     */
    private void addRequestToMaps(@NotNull final Integer citizenId, @NotNull final IToken requestToken, @NotNull final TypeToken requested)
    {
        if (!getOpenRequestsByRequestableType().containsKey(requested))
        {
            getOpenRequestsByRequestableType().put(requested, new ArrayList<>());
        }
        getOpenRequestsByRequestableType().get(requested).add(requestToken);

        getCitizensByRequest().put(requestToken, citizenId);

        if (!getOpenRequestsByCitizen().containsKey(citizenId))
        {
            getOpenRequestsByCitizen().put(citizenId, new ArrayList<>());
        }
        getOpenRequestsByCitizen().get(citizenId).add(requestToken);
    }

    public boolean hasWorkerOpenRequests(@NotNull final CitizenData citizen)
    {
        return !getOpenRequests(citizen).isEmpty();
    }

    @SuppressWarnings(RAWTYPES)
    public ImmutableList<IRequest> getOpenRequests(@NotNull final CitizenData data)
    {
        if (!getOpenRequestsByCitizen().containsKey(data.getId()))
        {
            return ImmutableList.of();
        }

        return ImmutableList.copyOf(getOpenRequestsByCitizen().get(data.getId())
                .stream()
                .map(getColony().getRequestManager()::getRequestForToken)
                .filter(Objects::nonNull)
                .iterator());
    }

    @SuppressWarnings(RAWTYPES)
    public boolean hasWorkerOpenRequestsFiltered(@NotNull final CitizenData citizen, @NotNull final Predicate<IRequest> selectionPredicate)
    {
        return getOpenRequests(citizen).stream().anyMatch(selectionPredicate);
    }

    public <R> boolean hasWorkerOpenRequestsOfType(@NotNull final CitizenData citizenData, final TypeToken<R> requestType)
    {
        return !getOpenRequestsOfType(citizenData, requestType).isEmpty();
    }

    @SuppressWarnings({GENERIC_WILDCARD, UNCHECKED, RAWTYPES})
    public <R> ImmutableList<IRequest<? extends R>> getOpenRequestsOfType(
            @NotNull final CitizenData citizenData,
            final TypeToken<R> requestType)
    {
        return ImmutableList.copyOf(getOpenRequests(citizenData).stream()
                .filter(request -> {
                    final Set<TypeToken> requestTypes = ReflectionUtils.getSuperClasses(request.getRequestType());
                    return requestTypes.contains(requestType);
                })
                .map(request -> (IRequest<? extends R>) request)
                .iterator());
    }

    public boolean hasCitizenCompletedRequests(@NotNull final CitizenData data)
    {
        return !getCompletedRequests(data).isEmpty();
    }

    @SuppressWarnings(RAWTYPES)
    public ImmutableList<IRequest> getCompletedRequests(@NotNull final CitizenData data)
    {
        if (!getCompletedRequestsByCitizen().containsKey(data.getId()))
        {
            return ImmutableList.of();
        }

        return ImmutableList.copyOf(getCompletedRequestsByCitizen().get(data.getId()).stream()
                .map(getColony().getRequestManager()::getRequestForToken).filter(Objects::nonNull).iterator());
    }

    @SuppressWarnings({GENERIC_WILDCARD, RAWTYPES, UNCHECKED})
    public <R> ImmutableList<IRequest<? extends R>> getCompletedRequestsOfType(@NotNull final CitizenData citizenData, final TypeToken<R> requestType)
    {
        return ImmutableList.copyOf(getCompletedRequests(citizenData).stream()
                .filter(request -> {
                    final Set<TypeToken> requestTypes = ReflectionUtils.getSuperClasses(request.getRequestType());
                    return requestTypes.contains(requestType);
                })
                .map(request -> (IRequest<? extends R>) request)
                .iterator());
    }

    @SuppressWarnings({GENERIC_WILDCARD, RAWTYPES, UNCHECKED})
    public <R> ImmutableList<IRequest<? extends R>> getCompletedRequestsOfTypeFiltered(
            @NotNull final CitizenData citizenData,
            final TypeToken<R> requestType,
            final Predicate<IRequest<? extends R>> filter)
    {
        return ImmutableList.copyOf(getCompletedRequests(citizenData).stream()
                .filter(request -> {
                    final Set<TypeToken> requestTypes = ReflectionUtils.getSuperClasses(request.getRequestType());
                    return requestTypes.contains(requestType);
                })
                .map(request -> (IRequest<? extends R>) request)
                .filter(filter)
                .iterator());
    }

    public void markRequestAsAccepted(@NotNull final CitizenData data, @NotNull final IToken<?> token)
    {
        if (!getCompletedRequestsByCitizen().containsKey(data.getId()) || !getCompletedRequestsByCitizen().get(data.getId()).contains(token))
        {
            throw new IllegalArgumentException("The given token " + token + " is not known as a completed request waiting for acceptance by the citizen.");
        }

        getCompletedRequestsByCitizen().get(data.getId()).remove(token);
        if (getCompletedRequestsByCitizen().get(data.getId()).isEmpty())
        {
            getCompletedRequestsByCitizen().remove(data.getId());
        }

        getColony().getRequestManager().updateRequestState(token, RequestState.RECEIVED);
        markDirty();
    }

    public void cancelAllRequestsOfCitizen(@NotNull final CitizenData data)
    {
        getOpenRequests(data).forEach(request ->
        {
            getColony().getRequestManager().updateRequestState(request.getToken(), RequestState.CANCELLED);

            if (getOpenRequestsByRequestableType().containsKey(TypeToken.of(request.getRequest().getClass())))
            {
                getOpenRequestsByRequestableType().get(TypeToken.of(request.getRequest().getClass())).remove(request.getToken());
                if (getOpenRequestsByRequestableType().get(TypeToken.of(request.getRequest().getClass())).isEmpty())
                {
                    getOpenRequestsByRequestableType().remove(TypeToken.of(request.getRequest().getClass()));
                }
            }

            getCitizensByRequest().remove(request.getToken());
        });

        getCompletedRequests(data).forEach(request -> getColony().getRequestManager().updateRequestState(request.getToken(), RequestState.RECEIVED));

        if (getOpenRequestsByCitizen().containsKey(data.getId()))
        {
            getOpenRequestsByCitizen().remove(data.getId());
        }

        if (getCompletedRequestsByCitizen().containsKey(data.getId()))
        {
            getCompletedRequestsByCitizen().remove(data.getId());
        }

        markDirty();
    }

    /**
     * Overrule the next open request with a give stack.
     * <p>
     * We squid:s135 which takes care that there are not too many continue statements in a loop since it makes sense here
     * out of performance reasons.
     *
     * @param stack the stack.
     */
    @SuppressWarnings("squid:S135")
    public void overruleNextOpenRequestWithStack(@NotNull final ItemStack stack)
    {
        if (ItemStackUtils.isEmpty(stack))
        {
            return;
        }

        for (final int citizenId : getOpenRequestsByCitizen().keySet())
        {
            final CitizenData data = getColony().getCitizenManager().getCitizen(citizenId);

            if (data == null)
            {
                continue;
            }

            final IRequest<? extends IDeliverable> target = getFirstOverullingRequestFromInputList(getOpenRequestsOfType(data, TypeConstants.DELIVERABLE), stack);

            if (target == null)
            {
                continue;
            }

            getColony().getRequestManager().overruleRequest(target.getToken(), stack.copy());
            return;
        }
    }

    @SuppressWarnings({GENERIC_WILDCARD, UNCHECKED, RAWTYPES})
    public <R> ImmutableList<IRequest<? extends R>> getOpenRequestsOfTypeFiltered(
            @NotNull final CitizenData citizenData,
            final TypeToken<R> requestType,
            final Predicate<IRequest<? extends R>> filter)
    {
        return ImmutableList.copyOf(getOpenRequests(citizenData).stream()
                .filter(request -> {
                    final Set<TypeToken> requestTypes = ReflectionUtils.getSuperClasses(request.getRequestType());
                    return requestTypes.contains(requestType);
                })
                .map(request -> (IRequest<? extends R>) request)
                .filter(filter)
                .iterator());
    }

    public boolean overruleNextOpenRequestOfCitizenWithStack(@NotNull final CitizenData citizenData, @NotNull final ItemStack stack)
    {
        if (ItemStackUtils.isEmpty(stack))
        {
            return false;
        }

        final IRequest<? extends IDeliverable> target = getFirstOverullingRequestFromInputList(getOpenRequestsOfType(citizenData, TypeConstants.DELIVERABLE),stack);

        if (target == null)
        {
            return false;
        }

        getColony().getRequestManager().overruleRequest(target.getToken(), stack.copy());
        return true;
    }

    private IRequest<? extends IDeliverable> getFirstOverullingRequestFromInputList(@NotNull final Collection<IRequest<? extends IDeliverable>> queue, @NotNull final ItemStack stack)
    {
        if (queue.isEmpty())
        {
            return null;
        }

        return queue
                .stream()
                .filter(request -> request.getRequest().matches(stack))
                .findFirst()
                .orElseGet(() ->
                        getFirstOverullingRequestFromInputList(queue
                                        .stream()
                                        .flatMap(r -> flattenDeliverableChildRequests(r).stream())
                                        .collect(Collectors.toList()),
                                stack));
    }

    private Collection<IRequest<? extends IDeliverable>> flattenDeliverableChildRequests(@NotNull final IRequest<? extends IDeliverable> request)
    {
        if (!request.hasChildren())
        {
            return ImmutableList.of();
        }

        return request.getChildren()
                .stream()
                .map(getColony().getRequestManager()::getRequestForToken)
                .filter(Objects::nonNull)
                .filter(request1 -> request1.getRequest() instanceof IDeliverable)
                .map(request1 -> (IRequest<? extends IDeliverable>) request1)
                .collect(Collectors.toList());
    }

    @Override
    public IToken<?> getRequesterId()
    {
        return getToken();
    }

    @Override
    public IToken<?> getToken()
    {
        return requester.getRequesterId();
    }

    @Override
    public ImmutableCollection<IRequestResolver<?>> getResolvers()
    {
        return ImmutableList.of(new BuildingRequestResolver(getRequester().getRequesterLocation(), getColony().getRequestManager().getFactoryController().getNewInstance(
                TypeConstants.ITOKEN)));
    }

    public IRequester getRequester()
    {
        return requester;
    }

    @NotNull
    @Override
    public ILocation getRequesterLocation()
    {
        return getRequester().getRequesterLocation();
    }

    @Override
    public void onRequestComplete(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
        final Integer citizenThatRequested = getCitizensByRequest().remove(token);
        getOpenRequestsByCitizen().get(citizenThatRequested).remove(token);

        if (getOpenRequestsByCitizen().get(citizenThatRequested).isEmpty())
        {
            getOpenRequestsByCitizen().remove(citizenThatRequested);
        }

        final IRequest<?> requestThatCompleted = getColony().getRequestManager().getRequestForToken(token);
        getOpenRequestsByRequestableType().get(TypeToken.of(requestThatCompleted.getRequest().getClass())).remove(token);

        if (getOpenRequestsByRequestableType().get(TypeToken.of(requestThatCompleted.getRequest().getClass())).isEmpty())
        {
            getOpenRequestsByRequestableType().remove(TypeToken.of(requestThatCompleted.getRequest().getClass()));
        }

        if (!getCompletedRequestsByCitizen().containsKey(citizenThatRequested))
        {
            getCompletedRequestsByCitizen().put(citizenThatRequested, new ArrayList<>());
        }
        getCompletedRequestsByCitizen().get(citizenThatRequested).add(token);

        markDirty();
    }

    @Override
    @NotNull
    public void onRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IToken token)
    {
        final int citizenThatRequested = getCitizensByRequest().remove(token);
        getOpenRequestsByCitizen().get(citizenThatRequested).remove(token);

        if (getOpenRequestsByCitizen().get(citizenThatRequested).isEmpty())
        {
            getOpenRequestsByCitizen().remove(citizenThatRequested);
        }

        final IRequest<?> requestThatCompleted = getColony().getRequestManager().getRequestForToken(token);
        if (requestThatCompleted != null && getOpenRequestsByRequestableType().containsKey(TypeToken.of(requestThatCompleted.getRequest().getClass())))
        {
            getOpenRequestsByRequestableType().get(TypeToken.of(requestThatCompleted.getRequest().getClass())).remove(token);
            if (getOpenRequestsByRequestableType().get(TypeToken.of(requestThatCompleted.getRequest().getClass())).isEmpty())
            {
                getOpenRequestsByRequestableType().remove(TypeToken.of(requestThatCompleted.getRequest().getClass()));
            }
        }

        //Check if the citizen did not die.
        if (getColony().getCitizenManager().getCitizen(citizenThatRequested) != null)
        {
            getColony().getCitizenManager().getCitizen(citizenThatRequested).onRequestCancelled(token);
        }
        markDirty();
    }

    @NotNull
    @Override
    public ITextComponent getDisplayName(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
        if (!getCitizensByRequest().containsKey(token))
        {
            return new TextComponentString("<UNKNOWN>");
        }

        final Integer citizenData = getCitizensByRequest().get(token);
        return new TextComponentString(this.getSchematicName() + " " + getColony().getCitizenManager().getCitizen(citizenData).getName());
    }

    public Optional<CitizenData> getCitizenForRequest(@NotNull final IToken token)
    {
        if (!getCitizensByRequest().containsKey(token) || getColony() == null)
        {
            return Optional.empty();
        }

        final int citizenID = getCitizensByRequest().get(token);
        if(getColony().getCitizenManager().getCitizen(citizenID) == null)
        {
            return Optional.empty();
        }

        return Optional.of(getColony().getCitizenManager().getCitizen(citizenID));
    }


    //------------------------- !END! RequestSystem handling for minecolonies buildings -------------------------//
}
