package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.coremod.entity.ai.citizen.guard.EntityAIWitch;
import com.minecolonies.coremod.util.AttributeModifierUtils;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.util.ResourceLocation;

import static com.minecolonies.api.util.constant.CitizenConstants.GUARD_HEALTH_MOD_LEVEL_NAME;

/**
 * The Witch's Job class
 *
 */
public class JobWitch extends AbstractJobGuard<JobWitch>
{
    /**
     * Initialize citizen data.
     *
     * @param entity the citizen data.
     */
    public JobWitch(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public EntityAIWitch generateGuardAI()
    {
        return new EntityAIWitch(this);
    }

    @Override
    public void onLevelUp()
    {
        // Bonus Health for archers(gets reset upon Firing)
        if (getCitizen().getEntity().isPresent())
        {
            final AbstractEntityCitizen citizen = getCitizen().getEntity().get();

            // +1 Heart every 2 level
            final AttributeModifier healthModLevel =
              new AttributeModifier(GUARD_HEALTH_MOD_LEVEL_NAME,
                getCitizen().getCitizenSkillHandler().getLevel(Skill.Focus),
                AttributeModifier.Operation.ADDITION);
            AttributeModifierUtils.addHealthModifier(citizen, healthModLevel);
        }
    }

    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.WITCH_ID;
    }
}
