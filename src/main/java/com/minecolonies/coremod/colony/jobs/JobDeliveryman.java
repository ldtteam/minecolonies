package com.minecolonies.coremod.colony.jobs;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.data.IRequestSystemDeliveryManJobDataStore;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.IDeliverymanRequestable;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.deliveryman.EntityAIWorkDeliveryman;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.nbt.CompoundNBT;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import static com.minecolonies.api.util.constant.BuildingConstants.TAG_ACTIVE;
import static com.minecolonies.api.util.constant.CitizenConstants.BASE_MOVEMENT_SPEED;
import static com.minecolonies.api.util.constant.Suppression.UNCHECKED;

/**
 * Class of the deliveryman job.
 */
public class JobDeliveryman extends AbstractJob
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
        if (getCitizen().getCitizenEntity().isPresent())
        {
            final AbstractEntityCitizen worker = getCitizen().getCitizenEntity().get();
            worker.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
              .setBaseValue(
                BASE_MOVEMENT_SPEED + (getCitizen().getJobModifier()) * BONUS_SPEED_PER_LEVEL);
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
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public AbstractAISkeleton<JobDeliveryman> generateAI()
    {
        return new EntityAIWorkDeliveryman(this);
    }

    private IRequestSystemDeliveryManJobDataStore getDataStore()
    {
        return getCitizen().getColony().getRequestManager().getDataStoreManager().get(rsDataStoreToken, TypeConstants.REQUEST_SYSTEM_DELIVERY_MAN_JOB_DATA_STORE);
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
        getTaskQueueFromDataStore().add(token);
        getCitizen().getWorkBuilding().markDirty();
    }

    /**
     * Method called to mark the current request as finished.
     *
     * @param successful True when the processing was successful, false when not.
     */
    public void finishRequest(@NotNull final boolean successful)
    {
        if (getTaskQueueFromDataStore().isEmpty())
        {
            return;
        }

        final IToken<?> current = getTaskQueueFromDataStore().getFirst();

        getColony().getRequestManager().updateRequestState(current, successful ? RequestState.RESOLVED : RequestState.FAILED);

        //Just to be sure lets delete them!
        if (!getTaskQueueFromDataStore().isEmpty() && current == getTaskQueueFromDataStore().getFirst())
        {
            getTaskQueueFromDataStore().removeFirst();
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

        getCitizen().getWorkBuilding().markDirty();
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
                getColony().getRequestManager().onColonyUpdate(request -> true);
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
            getColony().getRequestManager().updateRequestState(t, RequestState.FAILED);
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

    /**
     * Check if the dman can currently accept requests.
     *
     * @return true if active.
     */
    public boolean isActive()
    {
        return this.active;
    }
}
