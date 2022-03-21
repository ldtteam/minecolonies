package com.minecolonies.coremod.colony.jobs;

import net.minecraft.resources.ResourceLocation;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.citizen.cook.EntityAIWorkCookAssistant;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Class of the CookAssistant job.
 */
public class JobCookAssistant extends AbstractJobCrafter<EntityAIWorkCookAssistant, JobCookAssistant>
{
    /**
     * Instantiates the job for the CookAssistant.
     *
     * @param entity the citizen who becomes a CookAssistant.
     */
    public JobCookAssistant(final ICitizenData entity)
    {
        super(entity);
    }

    @NotNull
    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.COOK_ID;
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public EntityAIWorkCookAssistant generateAI()
    {
        return new EntityAIWorkCookAssistant(this);
    }

    /**
     * Whether the given stack should be dumped by the worker,
     * regardless of the items the building currently needs.
     *
     * @param stack The stack to base the decision on.
     * @return true if the stack should be dumped in the building regardless.
     */
    @Override
    public boolean shouldDumpAnyway(ItemStack stack) {
        // The assistant cook should dump everything, and not keep food or fuel in their inventory,
        // which is what the restaurant dictates to its workers.
        // They may incorrectly keep food in their inventory what they just crafted otherwise,
        // which messes up the crafting request
        return true;
    }
}
