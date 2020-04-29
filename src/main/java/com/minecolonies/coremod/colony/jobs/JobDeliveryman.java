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
import com.minecolonies.api.colony.requestsystem.requestable.Delivery;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
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

        if(compound.keySet().contains(NbtTagConstants.TAG_RS_DMANJOB_DATASTORE))
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
     * Returns whether or not the job has a currentTask.
     *
     * @return true if has currentTask, otherwise false.
     */
    public boolean hasTask()
    {
        return !getTaskQueueFromDataStore().isEmpty() || getDataStore().isReturning();
    }

    /**
     * Returns the {@link IRequest} of the current Task.
     *
     * @return {@link IRequest} of the current Task.
     */
    @SuppressWarnings(UNCHECKED)
    public IRequest<Delivery> getCurrentTask()
    {
        if (getTaskQueueFromDataStore().isEmpty())
        {
            return null;
        }

        return (IRequest<Delivery>) getColony().getRequestManager().getRequestForToken(getTaskQueueFromDataStore().peekFirst());
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

        this.setReturning(true);
        final IToken<?> current = getTaskQueueFromDataStore().getFirst();

        getColony().getRequestManager().updateRequestState(current, successful ? RequestState.RESOLVED : RequestState.FAILED);

        //Just to be sure lets delete them!
        if (!getTaskQueueFromDataStore().isEmpty() && current == getTaskQueueFromDataStore().getFirst())
            getTaskQueueFromDataStore().removeFirst();

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
            if (getTaskQueueFromDataStore().peek().equals(token))
            {
                this.setReturning(true);
            }

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

    /**
     * Method used to check if this DMan is trying to return to the warehouse to clean up.
     *
     * @return True when this DMan is returning the warehouse to clean his inventory.
     */
    public boolean isReturning()
    {
        return getDataStore().isReturning();
    }

    /**
     * Method used to set if this DMan needs to return and clear his inventory.
     * A set task is preferred over the returning flag.
     *
     * @param returning True to return the DMan to the warehouse and clean, false not to.
     */
    public void setReturning(final boolean returning)
    {
        getDataStore().setReturning(returning);
    }

    /**
     * Set if the dman can currently work.
     * @param b true if so.
     */
    public void setActive(final boolean b)
    {
        if (!b && active)
        {
            cancelAssignedRequests();
        }
        else if (!active && b)
        {
            this.active = b;
            final ImmutableList<IToken<?>> tokenList = getColony().getRequestManager().getPlayerResolver().getAllAssignedRequests();
            getColony().getRequestManager()
              .onColonyUpdate(request -> tokenList.contains(request.getId()));
        }
        this.active = b;
    }

    private void cancelAssignedRequests()
    {
        for (final IToken<?> t : getTaskQueue())
        {
            getColony().getRequestManager().updateRequestState(t,  RequestState.FAILED);
        }
    }

    @Override
    public void onRemoval()
    {
        cancelAssignedRequests();
    }

    /**
     * Check if the dman can currently accept requests.
     * @return true if active.
     */
    public boolean isActive()
    {
        return this.active;
    }
}
