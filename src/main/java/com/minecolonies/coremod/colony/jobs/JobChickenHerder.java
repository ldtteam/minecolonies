package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.coremod.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.coremod.entity.ai.citizen.herders.EntityAIWorkChickenHerder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The Chicken Herder job
 */
public class JobChickenHerder extends AbstractJob<EntityAIWorkChickenHerder, JobChickenHerder>
{
    /**
     * Instantiates the placeholder job.
     *
     * @param entity the entity.
     */
    public JobChickenHerder(final ICitizenData entity)
    {
        super(entity);
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @Nullable
    @Override
    public EntityAIWorkChickenHerder generateAI()
    {
        return new EntityAIWorkChickenHerder(this);
    }

    @Override
    public boolean pickupSuccess(@NotNull final ItemStack pickedUpStack)
    {
        if (pickedUpStack.getItem() == Items.FEATHER || pickedUpStack.getItem() == Items.EGG)
        {
            return getCitizen().getRandom().nextInt((getCitizen().getCitizenSkillHandler().getLevel(getCitizen().getWorkBuilding().getModuleMatching(WorkerBuildingModule.class, m -> m.getJobEntry() == getJobRegistryEntry()).getPrimarySkill()))) > 1;
        }
        return true;
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
        return ModModelTypes.CHICKEN_FARMER_ID;
    }
}
