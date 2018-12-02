package com.minecolonies.coremod.entity.ai.citizen.sawmill.deliveryman;

import com.minecolonies.coremod.colony.buildings.*;
import com.minecolonies.coremod.colony.jobs.JobSawmill;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.coremod.entity.ai.util.AIState.*;

/**
 * Crafts wood related block when needed.
 */
public class EntityAIWorkSawmill extends AbstractEntityAIInteract<JobSawmill>
{
    /**
     * Initialize the sawmill and add all his tasks.
     *
     * @param sawmill the job he has.
     */
    public EntityAIWorkSawmill(@NotNull final JobSawmill sawmill)
    {
        super(sawmill);
        super.registerTargets(
          /**
           * Check if tasks should be executed.
           */
          new AITarget(IDLE, () -> START_WORKING)
        );
        worker.setSkillModifier(2 * worker.getCitizenData().getEndurance() + worker.getCitizenData().getCharisma());
        worker.setCanPickUpLoot(true);
    }
}
