package com.minecolonies.api.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.citizen.Skill;

/**
 * Interface for all the buildings of workers that do public crafting.
 */
public interface IBuildingPublicCrafter extends IBuilding
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
