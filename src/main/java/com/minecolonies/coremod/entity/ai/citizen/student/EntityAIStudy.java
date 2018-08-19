package com.minecolonies.coremod.entity.ai.citizen.student;

import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingLibrary;
import com.minecolonies.coremod.colony.jobs.JobStudent;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAISkill;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.coremod.entity.ai.util.AIState.*;

/**
 * The Entity AI study class.
 */
public class EntityAIStudy extends AbstractEntityAISkill<JobStudent>
{
    /**
     * Delay for each subject study.
     */
    private static final int STUDY_DELAY = 20*60;

    /**
     * Delay for walking.
     */
    private static final int WALK_DELAY = 20;

    /**
     * The current pos to study at.
     */
    private BlockPos studyPos = null;

    /**
     * Constructor for the student.
     * Defines the tasks the student executes.
     *
     * @param job a student job to use.
     */
    public EntityAIStudy(@NotNull final JobStudent job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING, true),
          new AITarget(START_WORKING, true, this::startWorkingAtOwnBuilding),
          new AITarget(STUDY, false, this::study)
        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class getExpectedBuildingClass()
    {
        return BuildingLibrary.class;
    }

    /**
     * The AI task for the student to study.
     * For this he should walk between the different bookcase hit them once and then stand around for a while.
     *
     * @return the next AIState.
     */
    private AIState study()
    {
        final CitizenData data = worker.getCitizenData();
        if (data == null)
        {
            setDelay(STUDY_DELAY);
            return getState();
        }

        if (studyPos == null)
        {
            studyPos = getOwnBuilding(BuildingLibrary.class).getRandomBookShelf();
        }

        if (walkToBlock(studyPos))
        {
            setDelay(WALK_DELAY);
            return getState();
        }

        data.tryRandomLevelUp(world.rand);

        studyPos = null;
        setDelay(STUDY_DELAY);
        return getState();
    }

    /**
     * Redirects the student to his library.
     *
     * @return the next state.
     */
    private AIState startWorkingAtOwnBuilding()
    {
        if (walkToBuilding())
        {
            return getState();
        }
        return STUDY;
    }
}
