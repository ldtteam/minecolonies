package com.minecolonies.coremod.entity.ai.citizen.research;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.research.ILocalResearch;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingUniversity;
import com.minecolonies.coremod.colony.jobs.JobResearch;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;

public class EntityAIWorkResearcher extends AbstractEntityAIInteract<JobResearch, BuildingUniversity>
{
    /**
     * Delay for each subject study.
     */
    public static final int STUDY_DELAY = 1200;

    /**
     * base XP gained per study position
     */
    private static final double XP_PER_STUDYPOS = 2;

    /**
     * The current pos to study at.
     */
    private BlockPos studyPos = null;

    /**
     * Constructor for the AI
     *
     * @param job the job to fulfill
     */
    public EntityAIWorkResearcher(@NotNull final JobResearch job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING, 1),
          new AITarget(START_WORKING, this::startWorkingAtOwnBuilding, TICKS_SECOND),
          new AITarget(STUDY, this::study, STUDY_DELAY)
        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class<BuildingUniversity> getExpectedBuildingClass()
    {
        return BuildingUniversity.class;
    }

    /**
     * The AI task for the student to study. For this he should walk between the different bookcase hit them once and then stand around for a while.
     *
     * @return the next IAIState.
     */
    private IAIState study()
    {
        if (studyPos == null)
        {
            studyPos = building.getRandomBookShelf();
        }

        if (walkToBlock(studyPos))
        {
            return getState();
        }

        final IColony colony = building.getColony();
        final List<ILocalResearch> inProgress = colony.getResearchManager().getResearchTree().getResearchInProgress();
        if (!inProgress.isEmpty() && job.getCurrentMana() > 0)
        {
            final ILocalResearch research = inProgress.get(worker.getRandom().nextInt(inProgress.size()));

            if (colony.getResearchManager()
                  .getResearchTree()
                  .getResearch(research.getBranch(), research.getId())
                  .research(colony.getResearchManager().getResearchEffects(), colony.getResearchManager().getResearchTree()))
            {
                building.onSuccess(research);
            }
            colony.getResearchManager().markDirty();
            job.reduceCurrentMana();
        }

        worker.decreaseSaturationForContinuousAction();
        worker.getCitizenExperienceHandler().addExperience(XP_PER_STUDYPOS);
        studyPos = null;
        worker.queueSound(SoundEvents.BOOK_PAGE_TURN, worker.blockPosition().above(), 80, 15, 0.25f, 1.5f);
        return START_WORKING;
    }

    /**
     * Redirects the student to his library.
     *
     * @return the next state.
     */
    private IAIState startWorkingAtOwnBuilding()
    {
        if (walkToBuilding())
        {
            return getState();
        }

        if (studyPos == null)
        {
            studyPos = building.getRandomBookShelf();
        }

        return STUDY;
    }
}
