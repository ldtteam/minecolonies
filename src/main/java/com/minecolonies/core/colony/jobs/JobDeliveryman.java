package com.minecolonies.core.colony.jobs;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import com.minecolonies.core.colony.buildings.modules.WarehouseRequestQueueModule;
import net.minecraft.resources.ResourceLocation;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.workerbuildings.IWareHouse;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.data.IRequestSystemDeliveryManJobDataStore;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.AbstractDeliverymanRequestable;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Delivery;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.IDeliverymanRequestable;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Pickup;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.core.colony.buildings.modules.CourierAssignmentModule;
import com.minecolonies.core.colony.requestsystem.requests.StandardRequests;
import com.minecolonies.core.entity.ai.workers.service.EntityAIWorkDeliveryman;
import com.minecolonies.core.util.AttributeModifierUtils;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.minecolonies.api.util.constant.BuildingConstants.TAG_ONGOING;
import static com.minecolonies.api.util.constant.CitizenConstants.SKILL_BONUS_ADD;
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
              BuildingModules.COURIER_WORK).getPrimarySkill()) * BONUS_SPEED_PER_LEVEL, AttributeModifier.Operation.ADDITION);
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
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = super.serializeNBT();
        compound.put(NbtTagConstants.TAG_RS_DMANJOB_DATASTORE, StandardFactoryController.getInstance().serialize(rsDataStoreToken));
        return compound;
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);

        if (compound.contains(NbtTagConstants.TAG_RS_DMANJOB_DATASTORE))
        {
            rsDataStoreToken = StandardFactoryController.getInstance().deserialize(compound.getCompound(NbtTagConstants.TAG_RS_DMANJOB_DATASTORE));
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
    public void serializeToView(final FriendlyByteBuf buffer)
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

    /**
     * Returns the {@link IRequest} of the current Task.
     *
     * @return {@link IRequest} of the current Task.
     */
    @SuppressWarnings(UNCHECKED)
    public IRequest<IDeliverymanRequestable> getCurrentTask()
    {
        IToken<?> request = getTaskQueueFromDataStore().peekFirst();
        if (request == null)
        {
            IBuilding wareHouse = findWareHouse();
            if (wareHouse == null)
            {
                return null;
            }

            final WarehouseRequestQueueModule module = wareHouse.getModule(BuildingModules.WAREHOUSE_REQUEST_QUEUE);
            if (module.getMutableRequestList().isEmpty())
            {
                return null;
            }

            final List<IToken<?>> reqsToRemove = new ArrayList<>();
            int extendedReqs = 0;
            for (final IToken<?> reqId : module.getMutableRequestList())
            {
                final IRequest localRequest = getColony().getRequestManager().getRequestForToken(reqId);
                if (localRequest == null)
                {
                    reqsToRemove.add(reqId);
                    continue;
                }

                if (request == null)
                {
                    addRequest(reqId, 0);
                    request = reqId;
                    reqsToRemove.add(reqId);
                }
                else if (localRequest instanceof StandardRequests.DeliveryRequest && hasSameDestinationDelivery(localRequest))
                {
                    addRequest(reqId, 0);
                    extendedReqs++;
                    reqsToRemove.add(reqId);
                }

                if (extendedReqs > 5)
                {
                    break;
                }

            }

            module.getMutableRequestList().removeAll(reqsToRemove);
            module.markDirty();

            if (request == null)
            {
                return null;
            }

        }

        return (IRequest<IDeliverymanRequestable>) getColony().getRequestManager().getRequestForToken(request);
    }

    /**
     * Method used to add a request to the queue
     *
     * @param token The token of the requests to add.
     */
    public void addRequest(@NotNull final IToken<?> token, final int insertionIndex)
    {
        final IRequestManager requestManager = getColony().getRequestManager();
        IRequest<? extends IDeliverymanRequestable> newRequest = (IRequest<? extends IDeliverymanRequestable>) (requestManager.getRequestForToken(token));

        LinkedList<IToken<?>> taskQueue = getTaskQueueFromDataStore();

        int offset = 0;
        for (int i = insertionIndex; i < taskQueue.size(); i++)
        {
            final IToken theToken = taskQueue.get(i);
            final IRequest<? extends IDeliverymanRequestable> request = (IRequest<? extends IDeliverymanRequestable>) (requestManager.getRequestForToken(theToken));
            if (request == null || request.getState() == RequestState.COMPLETED)
            {
                taskQueue.remove(theToken);
                i--;
                offset--;
            }
            else
            {
                request.getRequest().incrementPriorityDueToAging();
            }
        }

        getTaskQueueFromDataStore().add(Math.max(0, insertionIndex + offset), token);
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
     * Check if the dman has the same destination request.
     *
     * @param request the incoming request.
     * @return 0 if so, and 1 if not.
     */
    public boolean hasSameDestinationDelivery(@NotNull final IRequest<? extends Delivery> request)
    {
        for (final IToken<?> requestToken : getTaskQueue())
        {
            final IRequest<?> compareRequest = getColony().getRequestManager().getRequestForToken(requestToken);
            if (compareRequest != null && compareRequest.getRequest() instanceof Delivery)
            {
                final Delivery current = (Delivery) compareRequest.getRequest();
                final Delivery newDev = request.getRequest();
                if (haveTasksSameSourceAndDest(current, newDev))
                {
                    return true;
                }
            }
        }

        return false;
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
            for (final IWareHouse wareHouse : getColony().getBuildingManager().getWareHouses())
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
     * Calculates a score an position for a delivery, the bigger the score the worse the request fits.
     *
     * @param newRequest to check
     * @return tuple of score and index to place at.
     */
    @NotNull
    public Tuple<Double, Integer> getScoreForDelivery(final IRequest<?> newRequest)
    {
        final List<IToken<?>> requestTokens = getTaskQueueFromDataStore();

        double totalScore = 10000;
        int bestRequestIndex = Math.max(0, requestTokens.size());

        if (requestTokens.isEmpty())
        {
            // No task, compare with dman pos
            totalScore = getClosenessFactorTo(getSource(newRequest),
              getTarget(newRequest),
              getCitizen().getLastPosition(),
              getTarget(newRequest));

            totalScore -= ((AbstractDeliverymanRequestable) newRequest.getRequest()).getPriority();
        }

        for (int i = 0; i < requestTokens.size(); i++)
        {
            final IRequest<?> compareRequest = getColony().getRequestManager().getRequestForToken(requestTokens.get(i));
            if (compareRequest == null)
            {
                continue;
            }

            if (compareRequest.getRequest() instanceof AbstractDeliverymanRequestable)
            {
                double score = getScoreOfRequestComparedTo(newRequest, compareRequest, i);

                if (score <= totalScore)
                {
                    bestRequestIndex = i + getPickupOrRequestOffset(newRequest, compareRequest);
                    totalScore = score;
                }
            }
        }

        totalScore += bestRequestIndex;

        return new Tuple<>(totalScore, bestRequestIndex);
    }

    /**
     * Calculates a score between two requesting making them compareable in many aspects.
     *
     * @param source         source request
     * @param comparing      comparing request
     * @param comparingIndex index of the comparing request in our taskque. Use taskque size when comparing a request not on the que.
     * @return compare score of the two requests, lower is better.
     */
    public double getScoreOfRequestComparedTo(final IRequest<?> source, final IRequest<?> comparing, final int comparingIndex)
    {
        if (!(comparing != null && comparing.getRequest() instanceof AbstractDeliverymanRequestable && source != null
                && source.getRequest() instanceof AbstractDeliverymanRequestable))
        {
            return 100;
        }

        // Closeness compared to the existing request
        double score = getClosenessFactorTo(getSource(source), getTarget(source), getSource(comparing), getTarget(comparing));
        // Priority of the existing request in diff to priority of the newly incomming one
        score += (((AbstractDeliverymanRequestable) comparing.getRequest()).getPriority() - ((AbstractDeliverymanRequestable) source.getRequest()).getPriority()) * 0.5;

        // Additional score for alternating between pickup and delivery
        score += getPickUpRequestScore(source, comparing);

        // Worse score the more requests we have to overtake
        score += getTaskQueue().size() - comparingIndex;

        return score;
    }

    /**
     * Gets the right task insertion order for pickups, if a new request fitting an existing request is added and the existing is a pickup and the new is a delivery it should be
     * infront.
     *
     * @param newRequest the new request to add
     * @param existing   the existing request
     * @return 1 for inserting after existing, 0 for infront
     */
    private static int getPickupOrRequestOffset(final IRequest<?> newRequest, final IRequest<?> existing)
    {
        if (newRequest.getRequest() instanceof Delivery && existing.getRequest() instanceof Pickup)
        {
            return 0;
        }

        return 1;
    }

    /**
     * Score for how nicely pickups and deliveries alternate
     *
     * @param newRequest the new request
     * @param existing   existing request
     * @return better score for when alternating deliveries and pickups nicely
     */
    private static int getPickUpRequestScore(final IRequest<?> newRequest, final IRequest<?> existing)
    {
        if (newRequest.getRequest() instanceof Pickup && existing.getRequest() instanceof Delivery
              || newRequest.getRequest() instanceof Delivery && existing.getRequest() instanceof Pickup)
        {
            return 0;
        }

        return 3;
    }

    /**
     * Calculates the closeness factor for two different delivery vectors
     *
     * @param source1 source of first request
     * @param target1 target of first request
     * @param source2 source of second request
     * @param target2 target of second request
     * @return closeness factor, representing how close these positions are to eachother. The lower the closer they are.
     */
    public static double getClosenessFactorTo(final BlockPos source1, final BlockPos target1, final BlockPos source2, final BlockPos target2)
    {
        final double newLength = BlockPosUtil.getDistance(target1, source1);
        if (newLength <= 0)
        {
            // Return a relatively high value(bad) when the distance is bad.
            return 10;
        }

        final double targetCloseness = BlockPosUtil.getDistance(target1, target2) / newLength;
        final double sourceCloseness = BlockPosUtil.getDistance(source1, source2) / newLength;

        return (targetCloseness + sourceCloseness) * 5;
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
        for (final IWareHouse building : getColony().getBuildingManager().getWareHouses())
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
}
