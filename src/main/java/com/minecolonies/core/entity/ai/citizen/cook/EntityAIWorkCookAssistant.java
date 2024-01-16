package com.minecolonies.core.entity.ai.citizen.cook;

import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingCook;
import com.minecolonies.core.colony.jobs.JobCookAssistant;
import com.minecolonies.core.entity.ai.basic.AbstractEntityAIRequestSmelter;
import net.minecraft.world.level.block.Blocks;
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
        // Only mark is cooking if the current recipe is a furnace recipe, to keep the cook from messing with the furnaces
        if (job.hasTask() && !building.getIsCooking() && currentRecipeStorage != null && currentRecipeStorage.getIntermediate() == Blocks.FURNACE)
        {
            building.setIsCooking(true);
        }
        if(!job.hasTask() && building.getIsCooking())
        {
            building.setIsCooking(false);
        }
        return nextState;
    }
}
