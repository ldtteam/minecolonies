package com.minecolonies.api.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.entity.citizen.Skill;

/**
 * Interface for all the buildings of workers that do public crafting.
 */
public interface IBuildingPublicCrafter extends IBuildingWorker
{
    /**
     * Crafting Speed skill
     * @return the crafting speed skill
     */
    default Skill getCraftSpeedSkill()
    {
         return getPrimarySkill();
    }
}
