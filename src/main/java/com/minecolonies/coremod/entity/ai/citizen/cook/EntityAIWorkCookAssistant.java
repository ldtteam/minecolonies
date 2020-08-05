package com.minecolonies.coremod.entity.ai.citizen.cook;

import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCook;
import com.minecolonies.coremod.colony.jobs.JobCookAssistant;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIRequestSmelter;
import org.jetbrains.annotations.NotNull;

/**
 * Crafts food related things.
 */
public class EntityAIWorkCookAssistant extends AbstractEntityAIRequestSmelter<JobCookAssistant, BuildingCook>
{
    /**
     * Initialize the Cook Assistant.
     *
     * @param cookAssistant the job he has.
     */
    public EntityAIWorkCookAssistant(@NotNull final JobCookAssistant cookAssistant)
    {
        super(cookAssistant);
    }

    @Override
    public Class<BuildingCook> getExpectedBuildingClass()
    {
        return BuildingCook.class;
    }

    /**
     * Main method to decide on what to do.
     *
     * @return the next state to go to.
     */
    protected IAIState decide()
    {
        IAIState nextState = super.decide();
        if (job.hasTask() && !getOwnBuilding().getIsCooking())
        {
            getOwnBuilding().setIsCooking(true);
        }
        if(!job.hasTask() && getOwnBuilding().getIsCooking())
        {
            getOwnBuilding().setIsCooking(false);
        }
        return nextState;
    }
}
