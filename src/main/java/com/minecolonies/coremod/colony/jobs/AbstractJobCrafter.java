package com.minecolonies.coremod.colony.jobs;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.data.IRequestSystemCrafterJobDataStore;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.client.render.RenderBipedCitizen;
import com.minecolonies.coremod.colony.CitizenData;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import static com.minecolonies.api.util.constant.Suppression.UNCHECKED;

/**
 * Class of the crafter job.
 */
public abstract class AbstractJobCrafter extends AbstractJob
{
    /**
     * The Token of the data store which belongs to this job.
     */
    private IToken<?> rsDataStoreToken;

    /**
     * Instantiates the job for the crafter.
     *
     * @param entity the citizen who becomes a Sawmill
     */
    public AbstractJobCrafter(final CitizenData entity)
    {
        super(entity);
        setupRsDataStore();
    }

    /**
     * Data store setup.
     */
    private void setupRsDataStore()
    {
        rsDataStoreToken = this.getCitizen()
                             .getColony()
                             .getRequestManager()
                             .getDataStoreManager()
                             .get(
                               StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
                               TypeConstants.REQUEST_SYSTEM_CRAFTER_JOB_DATA_STORE
                             )
                             .getId();
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        if(compound.hasKey(NbtTagConstants.TAG_RS_DMANJOB_DATASTORE))
        {
            rsDataStoreToken = StandardFactoryController.getInstance().deserialize(compound.getCompoundTag(NbtTagConstants.TAG_RS_DMANJOB_DATASTORE));
        }
        else
        {
            setupRsDataStore();
        }
    }

    @NotNull
    @Override
    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.CRAFTER;
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setTag(NbtTagConstants.TAG_RS_DMANJOB_DATASTORE, StandardFactoryController.getInstance().serialize(rsDataStoreToken));
    }

    /**
     * Getter for the data store which belongs to this job.
     * @return the crafter data store.
     */
    private IRequestSystemCrafterJobDataStore getDataStore()
    {
        return getCitizen().getColony().getRequestManager().getDataStoreManager().get(rsDataStoreToken, TypeConstants.REQUEST_SYSTEM_CRAFTER_JOB_DATA_STORE);
    }

    /**
     * Retrieve the task queue from the data store.
     * @return the linked queue.
     */
    private LinkedList<IToken<?>> getTaskQueueFromDataStore()
    {
        return getDataStore().getQueue();
    }

    public List<IToken<?>> getAssignedTasksFromDataStore()
    {
        return getDataStore().getAssignedTasks();
    }

    /**
     * Returns whether or not the job has a currentTask.
     *
     * @return true if has currentTask, otherwise false.
     */
    public boolean hasTask()
    {
        return !getTaskQueueFromDataStore().isEmpty();
    }

    /**
     * Returns the {@link IRequest} of the current Task.
     *
     * @return {@link IRequest} of the current Task.
     */
    @SuppressWarnings(UNCHECKED)
    public IRequest<? extends PublicCrafting>  getCurrentTask()
    {
        if (getTaskQueueFromDataStore().isEmpty())
        {
            return null;
        }

        return (IRequest<? extends PublicCrafting>) getColony().getRequestManager().getRequestForToken(getTaskQueueFromDataStore().peekFirst());
    }

    /**
     * Method used to add a request to the queue
     *
     * @param token The token of the requests to add.
     */
    public void addRequest(@NotNull final IToken<?> token)
    {
        getTaskQueueFromDataStore().add(token);
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

        final IToken<?> current = getTaskQueueFromDataStore().removeFirst();

        getColony().getRequestManager().updateRequestState(current, successful ? RequestState.COMPLETED : RequestState.CANCELLED);
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
        else if (getAssignedTasksFromDataStore().contains(token))
        {
            getAssignedTasksFromDataStore().remove(token);
        }
    }

    public void onTaskBeingScheduled(@NotNull final IToken<?> token)
    {
        getAssignedTasksFromDataStore().add(token);
    }

    public void onTaskBeingResolved(@NotNull final IToken<?> token)
    {
        onTaskDeletion(token);
        addRequest(token);
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

    public List<IToken<?>> getAssignedTasks()
    {
        return ImmutableList.copyOf(getAssignedTasksFromDataStore());
    }

}
