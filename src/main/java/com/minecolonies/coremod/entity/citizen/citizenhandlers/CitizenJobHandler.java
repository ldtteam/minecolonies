package com.minecolonies.coremod.entity.citizen.citizenhandlers;

import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.ai.DesiredActivity;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenJobHandler;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.util.AdvancementUtils;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

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
     *
     * @param citizen the citizen owning the handler.
     */
    public CitizenJobHandler(final AbstractEntityCitizen citizen)
    {
        this.citizen = citizen;
    }

    /**
     * Set Model depending on job.
     *
     * @param job the new job.
     */
    @Override
    public void setModelDependingOnJob(@Nullable final IJob<?> job)
    {
        if (citizen.isBaby())
        {
            citizen.setModelId(ModModelTypes.child);
            citizen.getEntityData().set(DATA_MODEL, citizen.getModelType().getName().toString());
            citizen.setRenderMetadata("");
            return;
        }

        if (job == null)
        {
            if (citizen.getCitizenColonyHandler().getHomeBuilding() != null)
            {
                switch (citizen.getCitizenColonyHandler().getHomeBuilding().getBuildingLevel())
                {
                    case 3:
                        citizen.setModelId(ModModelTypes.citizen);
                        break;
                    case 4:
                        citizen.setModelId(ModModelTypes.noble);
                        break;
                    case 5:
                        citizen.setModelId(ModModelTypes.aristocrat);
                        break;
                    default:
                        citizen.setModelId(ModModelTypes.settler);
                        break;
                }
            }
            else
            {
                citizen.setModelId(ModModelTypes.settler);
            }
        }
        else
        {
            citizen.setModelId(job.getModel());
        }

        citizen.getEntityData().set(DATA_MODEL, citizen.getModelType().getName().toString());
        citizen.setRenderMetadata("");
    }

    /**
     * Defines job changes and state changes of the citizen.
     *
     * @param job the set job.
     */
    @Override
    public void onJobChanged(@Nullable final IJob<?> job)
    {
        //  Model
        setModelDependingOnJob(job);

        //  AI Tasks
        for (@NotNull final PrioritizedGoal task : new ArrayList<>(citizen.getTasks().availableGoals))
        {
            if (task.getGoal() instanceof AbstractAISkeleton)
            {
                citizen.getTasks().removeGoal(task.getGoal());
            }
        }

        citizen.getCitizenData().setIdleAtJob(false);

        if (job != null)
        {
            job.addWorkerAIToTaskList(citizen.getTasks());
            if (citizen.getTicksExisted() > 0 && citizen.getCitizenColonyHandler().getWorkBuilding() != null && citizen.getDesiredActivity() == DesiredActivity.WORK)
            {
                BlockPosUtil.tryMoveBaseCitizenEntityToXYZ(citizen, citizen.getCitizenColonyHandler().getWorkBuilding().getPosition());
            }

            // Calculate the number of guards for some advancements
            if (job instanceof AbstractJobGuard)
            {
                IColony colony = citizen.getCitizenColonyHandler().getColony();
                int guards = ((int) colony.getCitizenManager().getCitizens()
                        .stream()
                        .filter(citizen -> citizen.getJob() instanceof AbstractJobGuard)
                        .count());
                AdvancementUtils.TriggerAdvancementPlayersForColony(citizen.getCitizenColonyHandler().getColony(),
                        player -> AdvancementTriggers.ARMY_POPULATION.trigger(player, guards));
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
    public <J extends IJob<?>> J getColonyJob(@NotNull final Class<J> type)
    {
        return citizen.getCitizenData() == null ? null : citizen.getCitizenData().getJob(type);
    }

    /**
     * Gets the job of the entity.
     *
     * @return the job or els enull.
     */
    @Override
    @Nullable
    public IJob<?> getColonyJob()
    {
        return citizen.getCitizenData() == null ? null : citizen.getCitizenData().getJob();
    }

    @Override
    public boolean shouldRunAvoidance()
    {
        return getColonyJob() == null || getColonyJob().allowsAvoidance();
    }
}
