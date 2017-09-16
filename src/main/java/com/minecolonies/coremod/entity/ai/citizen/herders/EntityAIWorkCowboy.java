package com.minecolonies.coremod.entity.ai.citizen.herders;

import com.minecolonies.coremod.colony.jobs.JobCowboy;
import net.minecraft.entity.passive.EntityCow;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by Asher on 16/9/17.
 */
public class EntityAIWorkCowboy extends AbstractEntityAIHerder<JobCowboy, EntityCow>
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
    public EntityAIWorkCowboy(@NotNull final JobCowboy job)
    {
        super(job, MAX_ANIMALS_PER_LEVEL);
    }

    @Override
    public List<EntityCow> getAnimals()
    {
        return searchForAnimals(EntityCow.class);
    }
}
