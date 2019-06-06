package com.minecolonies.coremod.entity.ai.citizen.herders;

import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingChickenHerder;
import com.minecolonies.coremod.colony.jobs.JobChickenHerder;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * The AI behind the {@link JobChickenHerder} for Breeding and Killing Chickens.
 */
public class EntityAIWorkChickenHerder extends AbstractEntityAIHerder<JobChickenHerder, EntityChicken>
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
    public EntityAIWorkChickenHerder(@NotNull final JobChickenHerder job)
    {
        super(job);
        worker.getCitizenExperienceHandler().setSkillModifier(2 * worker.getCitizenData().getDexterity() + worker.getCitizenData().getStrength());
    }

    @Override
    public Class getExpectedBuildingClass()
    {
        return BuildingChickenHerder.class;
    }

    @Override
    public ItemStack getBreedingItem()
    {
        final ItemStack stack = new ItemStack(Items.WHEAT_SEEDS);
        stack.setCount(2);
        return stack;
    }

    @Override
    public int getMaxAnimalMultiplier()
    {
        return MAX_ANIMALS_PER_LEVEL;
    }

    @Override
    public Class<EntityChicken> getAnimalClass()
    {
        return EntityChicken.class;
    }
}
