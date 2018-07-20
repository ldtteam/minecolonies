package com.minecolonies.coremod.entity.ai.citizen.composter;

import com.minecolonies.blockout.Log;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingComposter;
import com.minecolonies.coremod.colony.jobs.JobComposter;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.coremod.entity.ai.util.AIState.*;

public class EntityAIWorkComposter extends AbstractEntityAIInteract<JobComposter>
{

    /**
     * How often should strength factor into the composter's skill modifier.
     */
    private static final int STRENGTH_MULTIPLIER = 2;

    /**
     * How often should intelligence factor into the composter's skill modifier.
     */
    private static final int INTELLIGENCE_MULTIPLIER = 1;

    /**
     * Base xp gain for the composter.
     */
    private static final double BASE_XP_GAIN = 5;

    private  BlockPos currentTarget;

    /**
     * Constructor for the AI
     *
     * @param job the job to fulfill
     */
    public EntityAIWorkComposter(@NotNull final JobComposter job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING),
          new AITarget(START_WORKING, this::decideWhatToDo),
          new AITarget(COMPOSTER_FILL, this::fillBarrels),
          new AITarget(COMPOSTER_HARVEST, this::harvestBarrels)
        );
        worker.getCitizenExperienceHandler().setSkillModifier(STRENGTH_MULTIPLIER * worker.getCitizenData().getStrength()
                                                                + INTELLIGENCE_MULTIPLIER * worker.getCitizenData().getIntelligence());

        worker.setCanPickUpLoot(true);
    }

    private AIState decideWhatToDo()
    {
        return COMPOSTER_FILL;
    }

    private AIState fillBarrels()
    {
        if(currentTarget == null)
        {
            BuildingComposter building = this.getOwnBuilding();
            currentTarget = building.getBarrels().get(building.getBarrels().size()-1);
        }
        if (walkToBlock(currentTarget))
        {
            setDelay(2);
            return getState();
        }

        return COMPOSTER_HARVEST;
    }

    private AIState harvestBarrels()
    {
        if (walkToBuilding())
        {
            setDelay(2);
            return getState();
        }
        currentTarget = null;
        return START_WORKING;
    }
}
