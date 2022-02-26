package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.coremod.entity.ai.citizen.guard.EntityAIDruid;
import com.minecolonies.coremod.util.AttributeModifierUtils;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.util.ResourceLocation;

import static com.minecolonies.api.util.constant.CitizenConstants.GUARD_HEALTH_MOD_LEVEL_NAME;

/**
 * The Druid's Job class
 *
 */
public class JobDruid extends AbstractJobGuard<JobDruid>
{
    /**
     * Initialize citizen data.
     *
     * @param entity the citizen data.
     */
    public JobDruid(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public EntityAIDruid generateGuardAI()
    {
        return new EntityAIDruid(this);
    }

    @Override
    public void onLevelUp()
    {
        // Bonus Health for druids(gets reset upon Firing)
        if (getCitizen().getEntity().isPresent())
        {
            final AbstractEntityCitizen citizen = getCitizen().getEntity().get();

            // +1 Heart every 2 level
            final AttributeModifier healthModLevel =
              new AttributeModifier(GUARD_HEALTH_MOD_LEVEL_NAME,
                getCitizen().getCitizenSkillHandler().getLevel(Skill.Mana),
                AttributeModifier.Operation.ADDITION);
            AttributeModifierUtils.addHealthModifier(citizen, healthModLevel);
        }
    }

    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.DRUID_ID;
    }
}
