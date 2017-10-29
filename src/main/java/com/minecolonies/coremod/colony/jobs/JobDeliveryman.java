package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.colony.requestsystem.RequestState;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.coremod.client.render.RenderBipedCitizen;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.deliveryman.EntityAIWorkDeliveryman;
import com.minecolonies.coremod.sounds.DeliverymanSounds;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class of the deliveryman job.
 */
public class JobDeliveryman extends AbstractJob
{
    private static final String TAG_CURRENT_TASK = "currentTask";
    private static final String TAG_RETURNING = "returning";

    private IToken currentTask;

    private boolean returning;

    /**
     * Instantiates the job for the deliveryman.
     *
     * @param entity the citizen who becomes a deliveryman
     */
    public JobDeliveryman(final CitizenData entity)
    {
        super(entity);
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        if (compound.hasKey(TAG_CURRENT_TASK))
        {
            currentTask = StandardFactoryController.getInstance().deserialize(compound.getCompoundTag(TAG_CURRENT_TASK));
        }
    }

    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.Deliveryman";
    }

    @NotNull
    @Override
    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.DELIVERYMAN;
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        if (hasTask())
        {
            compound.setTag(TAG_CURRENT_TASK, StandardFactoryController.getInstance().serialize(currentTask));
        }
    }

    @Nullable
    @Override
    public SoundEvent getBadWeatherSound()
    {
        if (getCitizen() != null)
        {
            return getCitizen().isFemale() ? DeliverymanSounds.Female.badWeather : null;
        }
        return null;
    }

    @Nullable
    @Override
    public SoundEvent getMoveAwaySound()
    {
        if (getCitizen() != null)
        {
            return getCitizen().isFemale() ? DeliverymanSounds.Female.hostile : null;
        }
        return null;
    }

    @Override
    public SoundEvent getBedTimeSound()
    {
        if (getCitizen() != null)
        {
            return getCitizen().isFemale() ? DeliverymanSounds.Female.offToBed : null;
        }
        return null;
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

    /**
     * Returns whether or not the job has a currentTask.
     *
     * @return true if has currentTask, otherwise false.
     */
    public boolean hasTask()
    {
        return currentTask != null || returning;
    }

    /**
     * Returns the {@link IRequest} of the current Task.
     *
     * @return {@link IRequest} of the current Task.
     */
    public IRequest getCurrentTask()
    {
        if(currentTask == null)
        {
            return null;
        }
        return getColony().getRequestManager().getRequestForToken(currentTask);
    }

    /**
     * Sets the result of the currenttask.
     *
     * @param state the resultstate to set {@link RequestState}.
     */
    public void setRequestState(@NotNull final RequestState state)
    {
        getColony().getRequestManager().getRequestForToken(currentTask).setState(getColony().getRequestManager(), state);
    }

    /**
     * Sets the current task of the job.
     *
     * @param currentTask {@link IToken} of the current task.
     */
    public void setCurrentTask(final IToken currentTask)
    {
        this.currentTask = currentTask;
    }

    /**
     * Method used to check if this DMan is trying to return to the warehouse to clean up.
     * @return True when this DMan is returning the warehouse to clean his inventory.
     */
    public boolean getReturning()
    {
        return returning;
    }

    /**
     * Method used to set if this DMan needs to return and clear his inventory.
     * A set task is preferred over the returning flag.
     * @param returning True to return the DMan to the warehouse and clean, false not to.
     */
    public void setReturning(final boolean returning)
    {
        this.returning = returning;
    }
}
