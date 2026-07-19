package com.minecolonies.core.colony.jobs;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import com.minecolonies.core.colony.buildings.modules.WarehouseRequestQueueModule;
import com.minecolonies.core.colony.buildings.modules.WorkerBuildingModule;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.workerbuildings.IWareHouse;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.data.IRequestSystemDeliveryManJobDataStore;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.AbstractDeliverymanRequestable;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Delivery;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.IDeliverymanRequestable;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Pickup;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.core.colony.buildings.modules.CourierAssignmentModule;
import com.minecolonies.core.colony.requestsystem.requests.StandardRequests;
import com.minecolonies.core.entity.ai.workers.service.EntityAIWorkDeliveryman;
import com.minecolonies.core.util.AttributeModifierUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.minecolonies.api.util.constant.BuildingConstants.TAG_ONGOING;
import static com.minecolonies.api.util.constant.CitizenConstants.SKILL_BONUS_ADD_NAME;
import static com.minecolonies.api.util.constant.Suppression.UNCHECKED;

/**
 * Class of the deliveryman job.
 */
public class JobDeliveryman extends AbstractJob<EntityAIWorkDeliveryman, JobDeliveryman>
{
    private IToken<?> rsDataStoreToken;

    /**
     * Walking speed bonus per level
     */
    public static final double BONUS_SPEED_PER_LEVEL = 0.003;

    /**
     * Old field for backwards compatibility.
     */
    private int ongoingDeliveries;

    /**
     * Instantiates the job for the deliveryman.
     *
     * @param entity the citizen who becomes a deliveryman
     */
    public JobDeliveryman(final ICitizenData entity)
    {
        super(entity);
        if (entity != null)
        {
            setupRsDataStore();
        }
    }

    private void setupRsDataStore()
    {
        rsDataStoreToken = this.getCitizen()
                             .getColony()
                             .getRequestManager()
                             .getDataStoreManager()
                             .get(
                               StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
                               TypeConstants.REQUEST_SYSTEM_DELIVERY_MAN_JOB_DATA_STORE
                             )
                             .getId();
    }

    @Override
    public void onLevelUp()
    {
        if (getCitizen().getEntity().isPresent())
        {
            final AbstractEntityCitizen worker = getCitizen().getEntity().get();
            final AttributeModifier speedModifier = new AttributeModifier(SKILL_BONUS_ADD_NAME, getCitizen().getCitizenSkillHandler().getLevel(getCitizen().getWorkBuilding().getModule(
              BuildingModules.COURIER_WORK).getPrimarySkill()) * BONUS_SPEED_PER_LEVEL, AttributeModifier.Operation.ADD_VALUE);
            AttributeModifierUtils.addModifier(worker, speedModifier, Attributes.MOVEMENT_SPEED);
        }
    }

    @NotNull
    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.COURIER_ID;
    }

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider)
    {
        final CompoundTag compound = super.serializeNBT(provider);
        compound.put(NbtTagConstants.TAG_RS_DMANJOB_DATASTORE, StandardFactoryController.getInstance().serializeTag(provider, rsDataStoreToken));
        return compound;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, final CompoundTag compound)
    {
        super.deserializeNBT(provider, compound);

        if (compound.contains(NbtTagConstants.TAG_RS_DMANJOB_DATASTORE))
        {
            rsDataStoreToken = StandardFactoryController.getInstance().deserializeTag(provider, compound.getCompound(NbtTagConstants.TAG_RS_DMANJOB_DATASTORE));
        }
        else
        {
            setupRsDataStore();
        }
        this.ongoingDeliveries = compound.getInt(TAG_ONGOING);
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public EntityAIWorkDeliveryman generateAI()
    {
        return new EntityAIWorkDeliveryman(this);
    }

    private IRequestSystemDeliveryManJobDataStore getDataStore()
    {
        return getCitizen().getColony().getRequestManager().getDataStoreManager().get(rsDataStoreToken, TypeConstants.REQUEST_SYSTEM_DELIVERY_MAN_JOB_DATA_STORE);
    }

    @Override
    public void serializeToView(final RegistryFriendlyByteBuf buffer)
    {
        super.serializeToView(buffer);
        StandardFactoryController.getInstance().serialize(buffer, rsDataStoreToken);
    }

    private LinkedList<IToken<?>> getTaskQueueFromDataStore()
    {
        return getDataStore().getQueue();
    }

    @Override
    public int getInactivityLimit()
    {
        return 60 * 10;
    }

    @Override
    public void triggerActivityChangeAction(final boolean newState)
    {
        try
        {
            if (newState)
            {
                getColony().getRequestManager().onColonyUpdate(request -> request.getRequest() instanceof Delivery || request.getRequest() instanceof Pickup);
            }
            else
            {
                cancelAssignedRequests();
            }
        }
        catch (final Exception ex)
        {
            Log.getLogger().warn("Active Triggered resulted in exception", ex);
        }
    }

    private int getRequestPriority(final IToken<?> token, final List<IToken<?>> mutableRequestList)
    {
        final IRequest<?> req = getColony().getRequestManager().getRequestForToken(token);
        int priority = 1;
        if (!WorldUtil.isBlockLoaded(getColony().getWorld(), getTarget(req)))
        {
            priority -= 1000;
        }
        if (req != null && req.getRequest() instanceof AbstractDeliverymanRequestable requestable)
        {
            priority = requestable.getPriority();
            if (requestable instanceof Pickup pickup && pickup.getDay() > getColony().getDay())
            {
                priority -= 100;
            }
        }

        priority += mutableRequestList.size() - mutableRequestList.indexOf(token);
        final int distance = (int) Math.sqrt(getSource(req).distManhattan(getTarget(req)));
        return priority - distance;
    }

    /**
     * Returns the {@link IRequest} of the current Task.
     *
     * @return {@link IRequest} of the current Task.
     */
    @SuppressWarnings(UNCHECKED)
    // TODO: Rework logic to account for partially unloaded colonies, skipping tasks who's location is unloaded temporarily
    public IRequest<IDeliverymanRequestable> getCurrentTask()
    {
        final IToken<?> currentRequest = getTaskQueueFromDataStore().peekFirst();
        if (currentRequest != null)
        {
            return (IRequest<IDeliverymanRequestable>) getColony().getRequestManager().getRequestForToken(currentRequest);
        }

        IBuilding wareHouse = findWareHouse();
        if (wareHouse == null)
        {
            return null;
        }

        final WarehouseRequestQueueModule wareHouseModule = wareHouse.getModule(BuildingModules.WAREHOUSE_REQUEST_QUEUE);
        if (wareHouseModule.getMutableRequestList().isEmpty())
        {
            return null;
        }

        final List<IToken<?>> reqsToRemove = new ArrayList<>();

        IToken<?> resultRequestId = null;
        int priority = Integer.MIN_VALUE;
        for (final IToken<?> reqId : wareHouseModule.getMutableRequestList())
        {
            final int localPriority = getRequestPriority(reqId, wareHouseModule.getMutableRequestList());
            if (localPriority > priority)
            {
                priority = localPriority;
                resultRequestId = reqId;
            }
        }

        if (resultRequestId == null)
        {
            return null;
        }

        final int resultIndex = wareHouseModule.getMutableRequestList().indexOf(resultRequestId);
        reqsToRemove.add(resultRequestId);

        final IRequest<?> resultRequest = getColony().getRequestManager().getRequestForToken(resultRequestId);
        if (resultRequest instanceof StandardRequests.DeliveryRequest)
        {
            getTaskQueueFromDataStore().add(resultRequestId);
        }
        int index = 0;
        int extendedReqs = 1;
        for (final IToken<?> reqId : wareHouseModule.getMutableRequestList())
        {
            final IRequest<?> localRequest = getColony().getRequestManager().getRequestForToken(reqId);
            if (localRequest == null)
            {
                reqsToRemove.add(reqId);
                index++;
                continue;
            }

            // If we skipped this, we should add this
            if (index < resultIndex)
            {
                if (localRequest.getRequest() instanceof AbstractDeliverymanRequestable requestable)
                {
                    requestable.incrementPriorityDueToAging();
                }
            }
            else if (index == resultIndex)
            {
                index++;
                continue;
            }

            if (getTarget(localRequest).equals(getTarget(resultRequest)))
            {
                getTaskQueueFromDataStore().add(reqId);
                extendedReqs++;
                reqsToRemove.add(reqId);
            }

            index++;
            if (extendedReqs >= getMaxParallelDeliveries())
            {
                break;
            }
        }

        if (resultRequest instanceof StandardRequests.PickupRequest)
        {
            getTaskQueueFromDataStore().add(resultRequestId);
        }

        wareHouseModule.getMutableRequestList().removeAll(reqsToRemove);
        wareHouseModule.markDirty();

        return (IRequest<IDeliverymanRequestable>) resultRequest;
    }

    /**
     * Method called to mark the current request as finished.
     *
     * @param successful True when the processing was successful, false when not.
     */
    public void finishRequest(final boolean successful)
    {
        if (getTaskQueueFromDataStore().isEmpty())
        {
            return;
        }

        final IToken<?> current = getTaskQueueFromDataStore().getFirst();

        final IRequest<?> request = getColony().getRequestManager().getRequestForToken(current);

        if (request == null)
        {
            if (!getTaskQueueFromDataStore().isEmpty() && current == getTaskQueueFromDataStore().getFirst())
            {
                getTaskQueueFromDataStore().removeFirst();
            }
            return;
        }
        else if (request.getRequest() instanceof Delivery)
        {
            final List<IRequest<? extends Delivery>> taskList = getTaskListWithSameDestination((IRequest<? extends Delivery>) request);
            if (ongoingDeliveries != 0)
            {
                for (int i = 0; i < Math.max(1, Math.min(ongoingDeliveries, taskList.size())); i++)
                {
                    final IRequest<? extends Delivery> req = taskList.get(i);
                    if (req.getState() == RequestState.IN_PROGRESS)
                    {
                        getColony().getRequestManager().updateRequestState(req.getId(), successful ? RequestState.RESOLVED : RequestState.FAILED);
                    }
                    getTaskQueueFromDataStore().remove(req.getId());
                }
            }
            else
            {
                for (final IToken<?> token : new ArrayList<>(getDataStore().getOngoingDeliveries()))
                {
                    final IRequest<?> req = getColony().getRequestManager().getRequestForToken(token);
                    if (req != null && req.getState() == RequestState.IN_PROGRESS)
                    {
                        getColony().getRequestManager().updateRequestState(req.getId(), successful ? RequestState.RESOLVED : RequestState.FAILED);
                    }
                    getTaskQueueFromDataStore().remove(token);
                    getDataStore().getOngoingDeliveries().remove(token);
                }
            }
        }
        else if (request.getRequest() instanceof Pickup)
        {
            getTaskQueueFromDataStore().remove(request.getId());
            getColony().getRequestManager().updateRequestState(current, successful ? RequestState.RESOLVED : RequestState.FAILED);
        }
        else
        {
            getColony().getRequestManager().updateRequestState(current, successful ? RequestState.RESOLVED : RequestState.FAILED);

            //Just to be sure lets delete them!
            if (!getTaskQueueFromDataStore().isEmpty() && current == getTaskQueueFromDataStore().getFirst())
            {
                getTaskQueueFromDataStore().removeFirst();
            }
        }

        getCitizen().getWorkBuilding().markDirty();
    }

    /**
     * Called when a task that is being scheduled is being canceled.
     *
     * @param token token of the task to be deleted.
     */
    public void onTaskDeletion(@NotNull final IToken<?> token)
    {
        if (getTaskQueueFromDataStore().contains(token))
        {
            getTaskQueueFromDataStore().remove(token);
        }

        if (getCitizen().getWorkBuilding() != null)
        {
            getCitizen().getWorkBuilding().markDirty();
        }
    }

    /**
     * Method to get the task queue of this job.
     *
     * @return The task queue.
     */
    public List<IToken<?>> getTaskQueue()
    {
        return ImmutableList.copyOf(getTaskQueueFromDataStore());
    }

    private void cancelAssignedRequests()
    {
        for (final IToken<?> t : getTaskQueue())
        {
            final IRequest<?> r = getColony().getRequestManager().getRequestForToken(t);
            if (r != null)
            {
                getColony().getRequestManager().updateRequestState(t, RequestState.FAILED);
            }
            else
            {
                Log.getLogger().warn("Oops, the request with ID: " + t.toString() + " couldn't be cancelled by the deliveryman because it doesn't exist");
            }
            getTaskQueueFromDataStore().remove(t);
        }
    }

    @Override
    public void onRemoval()
    {
        getCitizen().setWorking(false);
        try
        {
            cancelAssignedRequests();
        }
        catch (final Exception ex)
        {
            Log.getLogger().warn("Active Triggered resulted in exception", ex);
        }
        super.onRemoval();
        getColony().getRequestManager().getDataStoreManager().remove(this.rsDataStoreToken);
    }

    /**
     * Check if two deliveries have the same source and destination.
     *
     * @param requestA the first request.
     * @param requestB the second request.
     * @return true if so.
     */
    private boolean haveTasksSameSourceAndDest(@NotNull final Delivery requestA, @NotNull final Delivery requestB)
    {
        if (requestA.getTarget().equals(requestB.getTarget()))
        {
            if (requestA.getStart().equals(requestB.getStart()))
            {
                return true;
            }
            for (final IWareHouse wareHouse : getColony().getServerBuildingManager().getWareHouses())
            {
                if (wareHouse.hasContainerPosition(requestA.getStart().getInDimensionLocation()) && wareHouse.hasContainerPosition(requestB.getStart().getInDimensionLocation()))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Build a list of all requests that have the same source/dest pair.
     *
     * @param request the first request.
     * @return a list.
     */
    public List<IRequest<? extends Delivery>> getTaskListWithSameDestination(final IRequest<? extends Delivery> request)
    {
        final List<IRequest<? extends Delivery>> deliveryList = new ArrayList<>();
        deliveryList.add(request);
        for (final IToken<?> requestToken : getTaskQueue())
        {
            if (!requestToken.equals(request.getId()))
            {
                final IRequest<?> compareRequest = getColony().getRequestManager().getRequestForToken(requestToken);
                if (compareRequest != null && compareRequest.getRequest() instanceof Delivery)
                {
                    final Delivery current = (Delivery) compareRequest.getRequest();
                    final Delivery newDev = request.getRequest();
                    if (haveTasksSameSourceAndDest(current, newDev))
                    {
                        deliveryList.add((IRequest<? extends Delivery>) compareRequest);
                    }
                }
            }
        }
        return deliveryList;
    }

    /**
     * Calculate the max parallel deliveries the courier can do.
     * @return the max.
     */
    public int getMaxParallelDeliveries()
    {
        if (getWorkModule().getAssignedCitizen().isEmpty())
        {
            return 1;
        }
        return 1 + (getWorkModule().getAssignedCitizen().get(0).getCitizenSkillHandler().getLevel(((WorkerBuildingModule) getWorkModule()).getSecondarySkill()) / 5);
    }

    /**
     * Gets the source position of a request, pickups are reversed
     *
     * @param request
     * @return
     */
    private BlockPos getSource(final IRequest<?> request)
    {
        if (request.getRequest() instanceof Delivery)
        {
            return ((Delivery) request.getRequest()).getStart().getInDimensionLocation();
        }

        if (request.getRequest() instanceof Pickup)
        {
            final IWareHouse wareHouse = findWareHouse();
            if (wareHouse != null)
            {
                return wareHouse.getID();
            }
        }

        return null;
    }

    /**
     * Gets the target position of a request, pickups are reversed
     *
     * @param request
     * @return
     */
    private BlockPos getTarget(final IRequest<?> request)
    {
        if (request.getRequest() instanceof Delivery)
        {
            return ((Delivery) request.getRequest()).getTarget().getInDimensionLocation();
        }

        if (request.getRequest() instanceof Pickup)
        {
            return request.getRequester().getLocation().getInDimensionLocation();
        }

        return null;
    }

    /**
     * Finds the warehouse our dman is assigned to
     *
     * @return warehouse building or null
     */
    public IWareHouse findWareHouse()
    {
        for (final IWareHouse building : getColony().getServerBuildingManager().getWareHouses())
        {
            if (building.getFirstModuleOccurance(CourierAssignmentModule.class).hasAssignedCitizen(getCitizen()))
            {
                return building;
            }
        }

        return null;
    }

    /**
     * Add a concurrent delivery that is going on.
     * @param requestToken the token of the request.
     */
    public void addConcurrentDelivery(final IToken<?> requestToken)
    {
        getDataStore().getOngoingDeliveries().add(requestToken);
    }

    /**
     * Remove a concurrent delivery that is going on.
     * @param requestToken the token of the request.
     */
    public void removeConcurrentDelivery(final IToken<?> requestToken)
    {
        getDataStore().getOngoingDeliveries().remove(requestToken);
    }

    @Override
    public double getSaturationFactor()
    {
        return 1.2;
    }
}
