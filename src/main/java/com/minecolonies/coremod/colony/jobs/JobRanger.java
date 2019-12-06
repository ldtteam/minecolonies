package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.entity.ai.citizen.guard.AbstractEntityAIGuard;
import com.minecolonies.coremod.entity.ai.citizen.guard.EntityAIRanger;
import com.minecolonies.coremod.util.AttributeModifierUtils;
import net.minecraft.entity.ai.attributes.AttributeModifier;

import static com.minecolonies.api.util.constant.CitizenConstants.GUARD_HEALTH_MOD_LEVEL_NAME;

/**
 * The Ranger's Job class
 *
 * @author Asherslab
 */
public class JobRanger extends AbstractJobGuard
{
    /**
     * The name associated with the job.
     */
    public static final String DESC = "com.minecolonies.coremod.job.Ranger";

    /**
     * Initialize citizen data.
     *
     * @param entity the citizen data.
     */
    public JobRanger(final ICitizenData entity)
    {
        super(entity);
    }

    /**
     * Generates the {@link AbstractEntityAIGuard} job for our ranger.
     *
     * @return The AI.
     */
    @Override
    public AbstractEntityAIGuard generateGuardAI()
    {
        return new EntityAIRanger(this);
    }

    /**
     * Custom Action on Levelup, increases Guard HP
     */
    @Override
    public void onLevelUp(final int newLevel)
    {
        // Bonus Health for guards(gets reset upon Firing)
        if (getCitizen().getCitizenEntity().isPresent())
        {
            final AbstractEntityCitizen citizen = getCitizen().getCitizenEntity().get();

            // +1 Heart on levels 6,12,18,25,34,43,54 ...
            final AttributeModifier healthModLevel =
              new AttributeModifier(GUARD_HEALTH_MOD_LEVEL_NAME, (int) (getCitizen().getLevel() / (5.0 + getCitizen().getLevel() / 20.0) * 2), 0);
            AttributeModifierUtils.addHealthModifier(citizen, healthModLevel);
        }
    }

    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.ranger;
    }

    /**
     * Gets the name of our ranger.
     *
     * @return The name.
     */
    @Override
    public String getName()
    {
        return DESC;
    }

    /**
     * Gets the {@link BipedModelType} to use for our ranger.
     *
     * @return The model to use.
     */
    @Override
    public BipedModelType getModel()
    {
        return BipedModelType.ARCHER_GUARD;
    }
}
