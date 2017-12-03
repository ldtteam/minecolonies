package com.minecolonies.coremod.entity.ai.citizen.herders;

import com.minecolonies.coremod.colony.jobs.JobCowboy;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.coremod.entity.ai.util.AIState.*;

/**
 * The AI behind the {@link JobCowboy} for Breeding, Killing and Milking Cows.
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

        final boolean hasBreedingItem = worker.hasItemInInventory(getBreedingItem(), 0);
        final boolean hasBucket = worker.hasItemInInventory(Items.BUCKET, 0);

        if (!searchForItemsInArea().isEmpty())
        {
            return HERDER_PICKUP;
        }
        else if (maxAnimals())
        {
            return HERDER_BUTCHER;
        }
        else if (numOfBreedableAnimals >= NUM_OF_ANIMALS_TO_BREED && hasBreedingItem)
        {
            return HERDER_BREED;
        }
        else if (hasBucket)
        {
            return COWBOY_MILK;
        }
        return START_WORKING;
    }

    @NotNull
    @Override
    protected List<ItemStack> itemsNiceToHave()
    {
        final List<ItemStack> list = super.itemsNiceToHave();
        list.add(new ItemStack(Items.BUCKET, 1));
        return list;
    }

    /**
     * Makes the Cowboy "Milk" the cows (Honestly all he does is swap an empty
     * bucket for a milk bucket, there's no actual "Milk" method in {@link EntityCow}
     *
     * @return The next {@link AIState}
     */
    private AIState milkCows()
    {
        if (!worker.hasItemInInventory(getBreedingItem(), 0) && isInHut(new ItemStack(Items.BUCKET, 1)))
        {
            if (!walkToBuilding() && getOwnBuilding() != null)
            {
                isInTileEntity(getOwnBuilding().getTileEntity(), new ItemStack(Items.BUCKET, 1));
            }
            else
            {
                return HERDER_DECIDE;
            }
        }

        final EntityCow cow = searchForAnimals().stream().findFirst().orElse(null);

        if (cow == null)
        {
            return HERDER_DECIDE;
        }

        if (!walkingToAnimal(cow) && equipItem(new ItemStack(Items.BUCKET, 1)))
        {

            if (worker.getInventoryCitizen().addItemStackToInventory(new ItemStack(Items.MILK_BUCKET)))
            {
                worker.removeHeldItem();
                equipItem(new ItemStack(Items.MILK_BUCKET));
                new InvWrapper(getInventory()).extractItem(getItemSlot(Items.BUCKET), 1, false);
            }

            incrementActionsDone();
        }

        return HERDER_DECIDE;
    }
}
