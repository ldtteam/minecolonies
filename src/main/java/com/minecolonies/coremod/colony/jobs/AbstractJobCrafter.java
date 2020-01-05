package com.minecolonies.coremod.colony.jobs;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.client.render.modeltype.IModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.data.IRequestSystemCrafterJobDataStore;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import static com.minecolonies.api.util.constant.Suppression.UNCHECKED;

/**
 * Class of the crafter job.
 */
public abstract class AbstractJobCrafter<AI extends AbstractEntityAICrafting<J>, J extends AbstractJobCrafter<AI, J>> extends AbstractJob
{
    /**
     * The Token of the data store which belongs to this job.
     */
    private IToken<?> rsDataStoreToken;

    /**
     * Max crafting count for current recipe.
     */
    private int maxCraftingCount = 0;

    /**
     * Count of already executed recipes.
     */
    private int craftCounter = 0;

    /**
     * Progress of hitting the block.
     */
    private int progress = 0;

    /**
     * Instantiates the job for the crafter.
     *
     * @param entity the citizen who becomes a Sawmill
     */
    public AbstractJobCrafter(final ICitizenData entity)
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

    @NotNull
    @Override
    public IModelType getModel()
    {
        return BipedModelType.CRAFTER;
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        final NBTTagCompound compound = super.serializeNBT();
        compound.setTag(NbtTagConstants.TAG_RS_DMANJOB_DATASTORE, StandardFactoryController.getInstance().serialize(rsDataStoreToken));

        compound.setInteger(NbtTagConstants.TAG_PROGRESS, progress);
        compound.setInteger(NbtTagConstants.TAG_MAX_COUNTER, maxCraftingCount);
        compound.setInteger(NbtTagConstants.TAG_CRAFT_COUNTER, craftCounter);
        return compound;
    }

    @Override
    public void deserializeNBT(final NBTTagCompound compound)
    {
        super.deserializeNBT(compound);

        if(compound.hasKey(NbtTagConstants.TAG_RS_DMANJOB_DATASTORE))
        {
            rsDataStoreToken = StandardFactoryController.getInstance().deserialize(compound.getCompoundTag(NbtTagConstants.TAG_RS_DMANJOB_DATASTORE));
        }
        else
        {
            setupRsDataStore();
        }

        if (compound.hasKey(NbtTagConstants.TAG_PROGRESS))
        {
            this.progress = compound.getInteger(NbtTagConstants.TAG_PROGRESS);
        }

        if (compound.hasKey(NbtTagConstants.TAG_MAX_COUNTER))
        {
            this.progress = compound.getInteger(NbtTagConstants.TAG_MAX_COUNTER);
        }

        if (compound.hasKey(NbtTagConstants.TAG_CRAFT_COUNTER))
        {
            this.progress = compound.getInteger(NbtTagConstants.TAG_CRAFT_COUNTER);
        }
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
    public <R extends PublicCrafting> IRequest<R> getCurrentTask()
    {
        if (getTaskQueueFromDataStore().isEmpty())
        {
            return null;
        }

        //This cleans up the state after something went wrong.
        IRequest<R> request = (IRequest<R>) getColony().getRequestManager().getRequestForToken(getTaskQueueFromDataStore().peekFirst());
        while(request == null)
        {
            getTaskQueueFromDataStore().remove(getTaskQueueFromDataStore().peekFirst());
            request = (IRequest<R>) getColony().getRequestManager().getRequestForToken(getTaskQueueFromDataStore().peekFirst());
        }

        return request;
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
    public void finishRequest(final boolean successful)
    {
        if (getTaskQueueFromDataStore().isEmpty())
        {
            return;
        }

        final IToken<?> current = getTaskQueueFromDataStore().getFirst();

        getColony().getRequestManager().updateRequestState(current, successful ? RequestState.RESOLVED : RequestState.CANCELLED);
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

    /**
     * Get the max crafting count for the current recipe.
     * @return the count.
     */
    public int getMaxCraftingCount()
    {
        return maxCraftingCount;
    }

    /**
     * Set the max crafting count for the current recipe.
     * @param maxCraftingCount the count to set.
     */
    public void setMaxCraftingCount(final int maxCraftingCount)
    {
        this.maxCraftingCount = maxCraftingCount;
    }

    /**
     * Get the current craft counter.
     * @return the counter.
     */
    public int getCraftCounter()
    {
        return craftCounter;
    }

    /**
     * Set the current craft counter.
     * @param craftCounter the counter to set.
     */
    public void setCraftCounter(final int craftCounter)
    {
        this.craftCounter = craftCounter;
    }

    /**
     * Get the crafting progress.
     * @return the current progress.
     */
    public int getProgress()
    {
        return progress;
    }

    /**
     * Set the crafting progress.
     * @param progress the current progress.
     */
    public void setProgress(final int progress)
    {
        this.progress = progress;
    }
}
