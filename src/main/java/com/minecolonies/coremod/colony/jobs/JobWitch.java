package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.entity.ai.citizen.guard.AbstractEntityAIGuard;
import com.minecolonies.coremod.entity.ai.citizen.guard.EntityAIWitch;
import com.minecolonies.coremod.util.AttributeModifierUtils;
import net.minecraft.entity.ai.attributes.AttributeModifier;

import static com.minecolonies.api.util.constant.CitizenConstants.GUARD_HEALTH_MOD_LEVEL_NAME;

//TODO
public class JobWitch extends AbstractJobGuard<JobWitch>
{
    /**
     * Desc of witch job.
     */
    public static final String DESC = "com.minecolonies.coremod.job.Witch";

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
    protected AbstractEntityAIGuard<JobWitch, ? extends AbstractBuildingGuards> generateGuardAI()
    {
        return new EntityAIWitch(this);
    }

    /**
     * The {@link JobEntry} for this job.
     *
     * @return The {@link JobEntry}.
     */
    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.witch;
    }

    /**
     * Custom Action on Levelup, increases Guard HP
     */
    @Override
    public void onLevelUp()
    {
        // Bonus Health for guards(gets reset upon Firing)
        if (getCitizen().getCitizenEntity().isPresent())
        {
            final AbstractEntityCitizen citizen = getCitizen().getCitizenEntity().get();

            // +1 half heart every 7 level
            final AttributeModifier healthModLevel =
              new AttributeModifier(GUARD_HEALTH_MOD_LEVEL_NAME, getCitizen().getJobModifier() / 7, AttributeModifier.Operation.ADDITION);
            AttributeModifierUtils.addHealthModifier(citizen, healthModLevel);
        }
    }

    /**
     * Return a Localization textContent for the Job.
     *
     * @return localization textContent String.
     */
    @Override
    public String getName()
    {
        return DESC;
    }
}
