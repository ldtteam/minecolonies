package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.IAffectsWalkingSpeed;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.entity.ai.citizen.healer.EntityAIWorkHealer;
import net.minecraft.entity.SharedMonsterAttributes;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.CitizenConstants.BASE_MOVEMENT_SPEED;

/**
 * The healer job class.
 */
public class JobHealer extends AbstractJob<EntityAIWorkHealer, JobHealer> implements IAffectsWalkingSpeed
{
    /**
     * Walking speed bonus per level
     */
    public static final double BONUS_SPEED_PER_LEVEL = 0.003;

    /**
     * Create a healer job.
     *
     * @param entity the healer.
     */
    public JobHealer(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.healer;
    }

    /**
     * Return a Localization textContent for the Job.
     *
     * @return localization textContent String.
     */
    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.healer";
    }

    /**
     * Get the RenderBipedCitizen.Model to use when the Citizen performs this job role.
     *
     * @return Model of the citizen.
     */
    @NotNull
    @Override
    public BipedModelType getModel()
    {
        return BipedModelType.HEALER;
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public EntityAIWorkHealer generateAI()
    {
        return new EntityAIWorkHealer(this);
    }

    @Override
    public void onLevelUp()
    {
        if (getCitizen().getEntity().isPresent())
        {
            final AbstractEntityCitizen worker = getCitizen().getEntity().get();
            worker.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(getWalkingSpeed());
        }
    }

    @Override
    public double getWalkingSpeed()
    {
        return BASE_MOVEMENT_SPEED + (getCitizen().getCitizenSkillHandler().getLevel(getCitizen().getWorkBuilding().getPrimarySkill())) * BONUS_SPEED_PER_LEVEL;
    }

    @Override
    public int getDiseaseModifier()
    {
        return 0;
    }
}
