package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.entity.ai.citizen.guard.AbstractEntityAIGuard;
import com.minecolonies.coremod.util.AttributeModifierUtils;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.util.DamageSource;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.GUARD_SLEEP;
import static com.minecolonies.api.util.constant.CitizenConstants.GUARD_HEALTH_MOD_BUILDING_NAME;
import static com.minecolonies.api.util.constant.CitizenConstants.GUARD_HEALTH_MOD_CONFIG_NAME;

/**
 * Abstract Class for Guard Jobs.
 */
public abstract class AbstractJobGuard<J extends AbstractJobGuard<J>> extends AbstractJob<AbstractEntityAIGuard<J, ? extends AbstractBuildingGuards>, J>
{
    /**
     * Initialize citizen data.
     *
     * @param entity the citizen data.
     */
    public AbstractJobGuard(final ICitizenData entity)
    {
        super(entity);
    }

    protected abstract AbstractEntityAIGuard<J, ? extends AbstractBuildingGuards> generateGuardAI();

    @Override
    public AbstractEntityAIGuard<J, ? extends AbstractBuildingGuards> generateAI()
    {
        return generateGuardAI();
    }

    @Override
    public void triggerDeathAchievement(final DamageSource source, final AbstractEntityCitizen citizen)
    {
        super.triggerDeathAchievement(source, citizen);
    }

    @Override
    public boolean allowsAvoidance()
    {
        return false;
    }

    /**
     * Whether the guard is asleep.
     *
     * @return true if sleeping
     */
    public boolean isAsleep()
    {
        return getWorkerAI() != null && getWorkerAI().getState() == GUARD_SLEEP;
    }

    @Override
    public void initEntityValues(AbstractEntityCitizen citizen)
    {
        super.initEntityValues(citizen);

        final IBuilding workBuilding = citizen.getCitizenData().getWorkBuilding();
        if (workBuilding instanceof AbstractBuildingGuards)
        {
            AttributeModifierUtils.addHealthModifier(citizen,
              new AttributeModifier(GUARD_HEALTH_MOD_BUILDING_NAME, ((AbstractBuildingGuards) workBuilding).getBonusHealth(), AttributeModifier.Operation.ADDITION));
            AttributeModifierUtils.addHealthModifier(citizen,
              new AttributeModifier(GUARD_HEALTH_MOD_CONFIG_NAME,
                MineColonies.getConfig().getServer().guardHealthMult.get() - 1.0,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }
}
