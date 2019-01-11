package com.minecolonies.coremod.entity.ai.citizen.blacksmith;

import com.minecolonies.coremod.colony.jobs.JobBlacksmith;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import org.jetbrains.annotations.NotNull;

/**
 * Crafts tools and armour.
 */
public class EntityAIWorkBlacksmith extends AbstractEntityAICrafting<JobBlacksmith>
{
    /**
     * Initialize the blacksmith and add all his tasks.
     *
     * @param blacksmith the job he has.
     */
    public EntityAIWorkBlacksmith(@NotNull final JobBlacksmith blacksmith)
    {
        super(blacksmith);
        worker.getCitizenExperienceHandler().setSkillModifier(2 * worker.getCitizenData().getDexterity() + worker.getCitizenData().getStrength());
    }
}
