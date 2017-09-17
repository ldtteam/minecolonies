package com.minecolonies.coremod.entity.ai.citizen.herders;

import com.minecolonies.coremod.colony.jobs.JobChickenHerder;
import net.minecraft.entity.passive.EntityChicken;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Asher on 16/9/17.
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
        super(job, MAX_ANIMALS_PER_LEVEL);
    }

    @Override
    public Class<EntityChicken> getAnimalClass()
    {
        return EntityChicken.class;
    }
}
