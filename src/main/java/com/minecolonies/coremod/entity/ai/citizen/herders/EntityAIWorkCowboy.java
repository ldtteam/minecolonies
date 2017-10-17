package com.minecolonies.coremod.entity.ai.citizen.herders;

import com.minecolonies.coremod.colony.jobs.JobCowboy;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.coremod.entity.ai.util.AIState.*;

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
        itemsNeeded.add(new ItemStack(Items.BUCKET));
        super.registerTargets(
          new AITarget(COWBOY_MILK, this::milkCows)
        );
    }

    @Override
    public Class<EntityCow> getAnimalClass()
    {
        return EntityCow.class;
    }

    @Override
    public AIState decideWhatToDo()
    {
        setDelay(DELAY_FOURTY);

        final List<EntityCow> animals = new ArrayList<>(searchForAnimals());

        if (animals.isEmpty())
        {
            setDelay(DELAY_ONE_HUNDRED);
            return HERDER_DECIDE;
        }

        worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.herder.deciding"));

        final int numOfBreedableAnimals = animals.stream().filter(animal -> animal.getGrowingAge() == 0).toArray().length;

        if (!searchForItemsInArea().isEmpty())
        {
            return HERDER_PICKUP;
        }
        else if (maxAnimals())
        {
            return HERDER_BUTCHER;
        }
        else if (numOfBreedableAnimals >= NUM_OF_ANIMALS_TO_BREED)
        {
            return HERDER_BREED;
        }
        else if (!checkOrRequestItemsAsynch(false, new ItemStack(Items.BUCKET)))
        {
            return COWBOY_MILK;
        }
        return HERDER_DECIDE;
    }

    private AIState milkCows()
    {
        System.out.println("Debug");

        return HERDER_DECIDE;
    }
}
