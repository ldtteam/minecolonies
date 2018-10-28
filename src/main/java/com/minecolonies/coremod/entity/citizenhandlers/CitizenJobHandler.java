package com.minecolonies.coremod.entity.citizenhandlers;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.client.render.RenderBipedCitizen;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIBasic;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.entity.ai.util.AITarget;

import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.coremod.entity.AbstractEntityCitizen.DATA_MODEL;

import java.util.function.Predicate;

/**
 * Handles the citizen job methods.
 */
public class CitizenJobHandler
{
    /**
     * The citizen assigned to this manager.
     */
    private final EntityCitizen citizen;

    /**
     * The last job of the citizen.
     */
    private String lastJob = "";

    /**
     * Constructor for the experience handler.
     * @param citizen the citizen owning the handler.
     */
    public CitizenJobHandler(final EntityCitizen citizen)
    {
        this.citizen = citizen;
    }

    /**
     * Getter for the last job.
     *
     * @return the last job he had.
     */
    @NotNull
    public String getLastJob()
    {
        return this.lastJob;
    }

    /**
     * Sets the last job of the citizen.
     *
     * @param jobName the job he last had.
     */
    public void setLastJob(@NotNull final String jobName)
    {
        this.lastJob = jobName;
    }

    /**
     * Set Model depending on job.
     * @param job the new job.
     */
    public void setModelDependingOnJob(@Nullable final AbstractJob job)
    {
        if (job == null)
        {
            if (citizen.getCitizenColonyHandler().getHomeBuilding() != null)
            {
                switch (citizen.getCitizenColonyHandler().getHomeBuilding().getBuildingLevel())
                {
                    case 3:
                        citizen.setModelId(RenderBipedCitizen.Model.CITIZEN);
                        break;
                    case 4:
                        citizen.setModelId(RenderBipedCitizen.Model.NOBLE);
                        break;
                    case 5:
                        citizen.setModelId(RenderBipedCitizen.Model.ARISTOCRAT);
                        break;
                    default:
                        citizen.setModelId(RenderBipedCitizen.Model.SETTLER);
                        break;
                }
            }
            else
            {
                citizen.setModelId(RenderBipedCitizen.Model.SETTLER);
            }
        }
        else
        {
            citizen.setModelId(job.getModel());
        }

        citizen.getDataManager().set(DATA_MODEL, citizen.getModelID().name());
        citizen.setRenderMetadata("");
    }

    /**
     * Defines job changes and state changes of the citizen.
     *
     * @param job the set job.
     */
    public void onJobChanged(@Nullable final AbstractJob job)
    {
        //  Model
        setModelDependingOnJob(job);

        //  AI Tasks
        @NotNull final Object[] currentTasks = citizen.tasks.taskEntries.toArray();
        for (@NotNull final Object task : currentTasks)
        {
            if (((EntityAITasks.EntityAITaskEntry) task).action instanceof AbstractEntityAIInteract)
            {
                citizen.tasks.removeTask(((EntityAITasks.EntityAITaskEntry) task).action);
            }
        }

        if (job != null)
        {
            job.addTasks(citizen.tasks);
            if (citizen.ticksExisted > 0 && citizen.getCitizenColonyHandler().getWorkBuilding() != null)
            {
                BlockPosUtil.tryMoveLivingToXYZ(citizen, citizen.getCitizenColonyHandler().getWorkBuilding().getLocation());
            }
        }
    }

    /**
     * Get the job of the citizen.
     *
     * @param type of the type.
     * @param <J>  wildcard.
     * @return the job.
     */
    @Nullable
    public <J extends AbstractJob> J getColonyJob(@NotNull final Class<J> type)
    {
        return citizen.getCitizenData() == null ? null : citizen.getCitizenData().getJob(type);
    }

    /**
     * Gets the job of the entity.
     * @return the job or els enull.
     */
    @Nullable
    public AbstractJob getColonyJob()
    {
        return citizen.getCitizenData() == null ? null : citizen.getCitizenData().getJob();
    }

    /**
     * Check if a certain item is in the hut of the citizen.
     * @param predicate the predicate to check.
     * @return true if so.
     */
    public boolean isInHut(@Nullable final Predicate<ItemStack> predicate)
    {
        final AbstractEntityAIBasic job = getEntityAI();
        if (job != null)
        {
            return job.isInHut(predicate);
        }
        
        return false;
    }

    /**
     * Get the entityAI via the job of the entity.
     * @return the AbstractEntityAIBasic AI.
     */
    private AbstractEntityAIBasic getEntityAI()
    {
        return citizen.tasks.taskEntries
                .stream()
                .filter(t -> t.action instanceof AbstractEntityAIBasic)
                .map(t -> (AbstractEntityAIBasic) t.action)
                .findAny() 
                .orElse(null);

    }
}
