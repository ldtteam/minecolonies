package com.minecolonies.core.colony.jobs;

import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import net.minecraft.resources.ResourceLocation;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.core.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.core.entity.ai.workers.service.EntityAIWorkHealer;
import com.minecolonies.core.util.AttributeModifierUtils;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.CitizenConstants.SKILL_BONUS_ADD;

/**
 * The healer job class.
 */
public class JobHealer extends AbstractJob<EntityAIWorkHealer, JobHealer>
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

    /**
     * Get the RenderBipedCitizen.Model to use when the Citizen performs this job role.
     *
     * @return Model of the citizen.
     */
    @NotNull
    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.HEALER_ID;
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
            final AttributeModifier speedModifier = new AttributeModifier(SKILL_BONUS_ADD, getCitizen().getCitizenSkillHandler().getLevel(getCitizen().getWorkBuilding().getModule(
              BuildingModules.HEALER_WORK).getPrimarySkill()) * BONUS_SPEED_PER_LEVEL, AttributeModifier.Operation.ADDITION);
            AttributeModifierUtils.addModifier(worker, speedModifier, Attributes.MOVEMENT_SPEED);
        }
    }

    @Override
    public double getDiseaseModifier()
    {
        return 0;
    }
}
