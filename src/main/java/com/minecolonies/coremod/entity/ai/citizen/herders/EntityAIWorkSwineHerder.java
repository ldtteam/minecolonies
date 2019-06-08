package com.minecolonies.coremod.entity.ai.citizen.herders;

import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingSwineHerder;
import com.minecolonies.coremod.colony.jobs.JobSwineHerder;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * The AI behind the {@link JobSwineHerder} for Breeding and Killing Pigs.
 */
public class EntityAIWorkSwineHerder extends AbstractEntityAIHerder<JobSwineHerder, EntityPig>
{
    /**
     * Max amount of animals per Hut Level.
     */
    private static final int MAX_ANIMALS_PER_LEVEL = 2;

    /**
     * Creates the abstract part of the AI.
     * Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAIWorkSwineHerder(@NotNull final JobSwineHerder job)
    {
        super(job);
        worker.getCitizenExperienceHandler().setSkillModifier(2 * worker.getCitizenData().getDexterity() + worker.getCitizenData().getStrength());
    }

    @Override
    public Class getExpectedBuildingClass()
    {
        return BuildingSwineHerder.class;
    }

    @Override
    public ItemStack getBreedingItem()
    {
        final ItemStack stack = new ItemStack(Items.CARROT);
        stack.setCount(2);
        return stack;
    }

    @Override
    public int getMaxAnimalMultiplier()
    {
        return MAX_ANIMALS_PER_LEVEL;
    }

    @Override
    public Class<EntityPig> getAnimalClass()
    {
        return EntityPig.class;
    }
}
