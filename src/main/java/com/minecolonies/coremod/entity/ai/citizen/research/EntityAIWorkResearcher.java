package com.minecolonies.coremod.entity.ai.citizen.research;

import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingUniversity;
import com.minecolonies.coremod.colony.jobs.JobResearch;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;

public class EntityAIWorkResearcher extends AbstractEntityAIInteract<JobResearch>
{
    /**
     * How often should intelligence factor into the researcher's skill modifier.
     */
    private static final int INTELLIGENCE_MULTIPLIER = 2;

    /**
     * How often should intelligence factor into the researcher's skill modifier.
     */
    private static final int DEXTERITY_MULTIPLIER = 1;

    /**
     * Delay for each subject study.
     */
    private static final int STUDY_DELAY = 20 * 60;

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
          new AITarget(STUDY, this::study, STANDARD_DELAY)
        );
        worker.getCitizenExperienceHandler().setSkillModifier(DEXTERITY_MULTIPLIER * worker.getCitizenData().getDexterity()
                                                                + INTELLIGENCE_MULTIPLIER * worker.getCitizenData().getIntelligence());

        worker.setCanPickUpLoot(true);

    }

    @Override
    public Class getExpectedBuildingClass()
    {
        return BuildingUniversity.class;
    }

    /**
     * The AI task for the student to study.
     * For this he should walk between the different bookcase hit them once and then stand around for a while.
     *
     * @return the next IAIState.
     */
    private IAIState study()
    {
        if (studyPos == null)
        {
            studyPos = getOwnBuilding(BuildingUniversity.class).getRandomBookShelf();
        }

        if (walkToBlock(studyPos))
        {
            setDelay(WALK_DELAY);
            return getState();
        }

        worker.decreaseSaturationForAction();

        studyPos = null;
        setDelay(STUDY_DELAY);
        return getState();
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
        return STUDY;
    }
}
