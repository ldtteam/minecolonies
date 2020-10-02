package com.minecolonies.coremod.colony.jobs;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.workerbuildings.IWareHouse;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.data.IRequestSystemDeliveryManJobDataStore;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Delivery;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.IDeliverymanRequestable;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Pickup;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.requestsystem.requests.StandardRequests;
import com.minecolonies.coremod.entity.ai.citizen.deliveryman.EntityAIWorkDeliveryman;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static com.minecolonies.api.colony.requestsystem.requestable.deliveryman.AbstractDeliverymanRequestable.getPlayerActionPriority;
import static com.minecolonies.api.util.constant.BuildingConstants.TAG_ACTIVE;
import static com.minecolonies.api.util.constant.BuildingConstants.TAG_ONGOING;
import static com.minecolonies.api.util.constant.CitizenConstants.BASE_MOVEMENT_SPEED;
import static com.minecolonies.api.util.constant.Suppression.UNCHECKED;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_ENTITY_DELIVERYMAN_FORCEPICKUP;

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
     * If the dman is currently active.
     */
    private boolean active = false;

    /**
     * How many deliveries are ongoing in parallel.
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
        setupRsDataStore();
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
            worker.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
              .setBaseValue(
                BASE_MOVEMENT_SPEED + (getCitizen().getCitizenSkillHandler().getLevel(getCitizen().getWorkBuilding().getPrimarySkill())) * BONUS_SPEED_PER_LEVEL);
        }
    }

    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.delivery;
    }

    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.Deliveryman";
    }

    @NotNull
    @Override
    public BipedModelType getModel()
    {
        return BipedModelType.DELIVERYMAN;
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();
        compound.put(NbtTagConstants.TAG_RS_DMANJOB_DATASTORE, StandardFactoryController.getInstance().serialize(rsDataStoreToken));
        compound.putBoolean(TAG_ACTIVE, this.active);
        compound.putInt(TAG_ONGOING, this.ongoingDeliveries);
        return compound;
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);

        if (compound.keySet().contains(NbtTagConstants.TAG_RS_DMANJOB_DATASTORE))
        {
            rsDataStoreToken = StandardFactoryController.getInstance().deserialize(compound.getCompound(NbtTagConstants.TAG_RS_DMANJOB_DATASTORE));
        }
        else
        {
            setupRsDataStore();
        }
        this.active = compound.getBoolean(TAG_ACTIVE);
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
    public void serializeToView(final PacketBuffer buffer)
    {
        super.serializeToView(buffer);
        StandardFactoryController.getInstance().serialize(buffer, rsDataStoreToken);
    }

    private LinkedList<IToken<?>> getTaskQueueFromDataStore()
    {
        return getDataStore().getQueue();
    }

    /**
     * Returns the {@link IRequest} of the current Task.
     *
     * @return {@link IRequest} of the current Task.
     */
    @SuppressWarnings(UNCHECKED)
    public IRequest<IDeliverymanRequestable> getCurrentTask()
    {
        final IToken<?> request = getTaskQueueFromDataStore().peekFirst();
        if (request == null)
        {
            return null;
        }

        return (IRequest<IDeliverymanRequestable>) getColony().getRequestManager().getRequestForToken(request);
    }

    /**
     * Method used to add a request to the queue
     *
     * @param token The token of the requests to add.
     */
    public void addRequest(@NotNull final IToken<?> token)
    {
        final IRequestManager requestManager = getColony().getRequestManager();
        IRequest<? extends IDeliverymanRequestable> newRequest = (IRequest<? extends IDeliverymanRequestable>) (requestManager.getRequestForToken(token));

        LinkedList<IToken<?>> taskQueue = getTaskQueueFromDataStore();
        Iterator<IToken<?>> iterator = taskQueue.descendingIterator();

        int insertionIndex = taskQueue.size();
        while (iterator.hasNext())
        {
            final IToken theToken = iterator.next();
            final IRequest<? extends IDeliverymanRequestable> request = (IRequest<? extends IDeliverymanRequestable>) (requestManager.getRequestForToken(theToken));
            if (request == null || request.getState() == RequestState.COMPLETED)
            {
                taskQueue.remove(theToken);
            }
            else
            {
                if (request.getRequest().getPriority() < newRequest.getRequest().getPriority())
                {
                    request.getRequest().incrementPriorityDueToAging();
                    insertionIndex--;
                }
                else
                {
                    break;
                }
            }
        }
        getTaskQueueFromDataStore().add(Math.max(0, insertionIndex), token);

        if (newRequest instanceof StandardRequests.PickupRequest && newRequest.getRequest().getPriority() == getPlayerActionPriority(true))
        {
            getCitizen().getEntity()
              .get()
              .getCitizenChatHandler()
              .sendLocalizedChat(COM_MINECOLONIES_COREMOD_ENTITY_DELIVERYMAN_FORCEPICKUP);
        }
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

    @Override
    public void setActive(final boolean b)
    {
        try
        {
            if (!b && active)
            {
                this.active = b;
                cancelAssignedRequests();
            }
            else if (!active && b)
            {
                this.active = b;
                getColony().getRequestManager().onColonyUpdate(request -> request.getRequest() instanceof Delivery || request.getRequest() instanceof Pickup);
            }
        }
        catch (final Exception ex)
        {
            Log.getLogger().warn("Active Triggered resulted in exception", ex);
        }
    }

    private void cancelAssignedRequests()
    {
        for (final IToken<?> t : getTaskQueue())
        {
            IRequest r = getColony().getRequestManager().getRequestForToken(t);
            if (r != null)
            {
                getColony().getRequestManager().updateRequestState(t, RequestState.FAILED);
                getTaskQueueFromDataStore().remove(t);
            }
            else
            {
                Log.getLogger().warn("Oops, the request with ID: " + t.toString() + " couldn't be cancelled by the deliveryman because it doesn't exist");
            }
        }
    }

    @Override
    public void onRemoval()
    {
        this.active = false;
        try
        {
            cancelAssignedRequests();
        }
        catch (final Exception ex)
        {
            Log.getLogger().warn("Active Triggered resulted in exception", ex);
        }
    }

    @Override
    public boolean isActive()
    {
        return this.active;
    }

    /**
     * Check if the dman has the same destination request.
     *
     * @param request the incoming request.
     * @return 0 if so, and 1 if not.
     */
    public int hasSameDestinationDelivery(@NotNull final IRequest<? extends Delivery> request)
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
                    return 0;
                }
            }
        }

        return 1;
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
     * Set how many parallel deliveries are ongoing.
     *
     * @param i the quantity.
     */
    public void setParallelDeliveries(final int i)
    {
        this.ongoingDeliveries = i;
    }
}
