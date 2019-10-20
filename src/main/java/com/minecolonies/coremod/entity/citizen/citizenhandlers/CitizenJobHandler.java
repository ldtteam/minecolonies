package com.minecolonies.coremod.entity.citizen.citizenhandlers;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenJobHandler;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import net.minecraft.entity.ai.EntityAITasks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.entity.citizen.AbstractEntityCitizen.DATA_MODEL;

/**
 * Handles the citizen job methods.
 */
public class CitizenJobHandler implements ICitizenJobHandler
{
    /**
     * The citizen assigned to this manager.
     */
    private final AbstractEntityCitizen citizen;

    /**
     * Constructor for the experience handler.
     * @param citizen the citizen owning the handler.
     */
    public CitizenJobHandler(final AbstractEntityCitizen citizen)
    {
        this.citizen = citizen;
    }

    /**
     * Set Model depending on job.
     * @param job the new job.
     */
    @Override
    public void setModelDependingOnJob(@Nullable final IJob job)
    {
        if (job == null)
        {
            if (citizen.getCitizenColonyHandler().getHomeBuilding() != null)
            {
                switch (citizen.getCitizenColonyHandler().getHomeBuilding().getBuildingLevel())
                {
                    case 3:
                        citizen.setModelId(BipedModelType.CITIZEN);
                        break;
                    case 4:
                        citizen.setModelId(BipedModelType.NOBLE);
                        break;
                    case 5:
                        citizen.setModelId(BipedModelType.ARISTOCRAT);
                        break;
                    default:
                        citizen.setModelId(BipedModelType.SETTLER);
                        break;
                }
            }
            else
            {
                citizen.setModelId(BipedModelType.SETTLER);
            }
        }
        else
        {
            citizen.setModelId(job.getModel());
        }

        citizen.getDataManager().set(DATA_MODEL, citizen.getModelType().getName());
        citizen.setRenderMetadata("");
    }

    /**
     * Defines job changes and state changes of the citizen.
     *
     * @param job the set job.
     */
    @Override
    public void onJobChanged(@Nullable final IJob job)
    {
        //  Model
        setModelDependingOnJob(job);

        //  AI Tasks
        @NotNull final Object[] currentTasks = citizen.getTasks().taskEntries.toArray();
        for (@NotNull final Object task : currentTasks)
        {
            if (((EntityAITasks.EntityAITaskEntry) task).action instanceof AbstractEntityAIInteract)
            {
                citizen.getTasks().removeTask(((EntityAITasks.EntityAITaskEntry) task).action);
            }
        }

        if (job != null)
        {
            job.addWorkerAIToTaskList(citizen.getTasks());
            if (citizen.getTicksExisted() > 0 && citizen.getCitizenColonyHandler().getWorkBuilding() != null)
            {
                BlockPosUtil.tryMoveBaseCitizenEntityToXYZ(citizen, citizen.getCitizenColonyHandler().getWorkBuilding().getPosition());
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
    @Override
    @Nullable
    public <J extends IJob> J getColonyJob(@NotNull final Class<J> type)
    {
        return citizen.getCitizenData() == null ? null : citizen.getCitizenData().getJob(type);
    }

    /**
     * Gets the job of the entity.
     * @return the job or els enull.
     */
    @Override
    @Nullable
    public IJob getColonyJob()
    {
        return citizen.getCitizenData() == null ? null : citizen.getCitizenData().getJob();
    }

    @Override
    public boolean shouldRunAvoidance()
    {
        return getColonyJob() != null && getColonyJob().allowsAvoidance();
    }
}
