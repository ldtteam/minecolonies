package com.minecolonies.coremod.entity.ai.citizen.herders;

import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.jobs.JobShepherd;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.coremod.entity.ai.util.AIState.*;

/**
 * The AI behind the {@link JobShepherd} for Breeding, Killing and Shearing sheep.
 */
public class EntityAIWorkShepherd extends AbstractEntityAIHerder<JobShepherd, EntitySheep>
{
    /**
     * Experience given per sheep sheared.
     */
    protected static final double EXP_PER_SHEEP = 5.0;

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
    public EntityAIWorkShepherd(@NotNull final JobShepherd job)
    {
        super(job, MAX_ANIMALS_PER_LEVEL);
        toolsNeeded.add(ToolType.SHEARS);
        super.registerTargets(
          new AITarget(SHEPHERD_SHEAR, this::shearSheep)
        );
    }

    @Override
    public AIState decideWhatToDo()
    {
        setDelay(DELAY_FOURTY);

        final List<EntitySheep> animals = new ArrayList<>(searchForAnimals());

        if (animals.isEmpty())
        {
            setDelay(DELAY_ONE_HUNDRED);
            return HERDER_DECIDE;
        }

        worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.herder.deciding"));

        final EntitySheep shearingSheep = animals.stream().filter(sheepie -> !sheepie.getSheared() && !sheepie.isChild()).findFirst().orElse(null);

        final int numOfBreedableSheep = animals.stream().filter(sheepie -> sheepie.getGrowingAge() == 0).toArray().length;

        final boolean hasBreedingItem = worker.hasItemInInventory(getBreedingItem(), 0);

        if (!searchForItemsInArea().isEmpty())
        {
            return HERDER_PICKUP;
        }
        else if (maxAnimals())
        {
            return HERDER_BUTCHER;
        }
        else if (shearingSheep != null && !walkingToAnimal(shearingSheep))
        {
            return SHEPHERD_SHEAR;
        }
        else if (numOfBreedableSheep >= NUM_OF_ANIMALS_TO_BREED && hasBreedingItem)
        {
            return HERDER_BREED;
        }
        return PREPARING;
    }

    @Override
    public Class<EntitySheep> getAnimalClass()
    {
        return EntitySheep.class;
    }

    /**
     * Shears a sheep, with a chance of dying it!
     *
     * @return The next {@link AIState}
     */
    private AIState shearSheep()
    {
        worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.shepherd.shearing"));

        final List<EntitySheep> sheeps = searchForAnimals();

        if (sheeps.isEmpty())
        {
            return HERDER_DECIDE;
        }

        if (!equipTool(ToolType.SHEARS))
        {
            return PREPARING;
        }

        final EntitySheep sheep = sheeps.stream().filter(sheepie -> !sheepie.getSheared()).findFirst().orElse(null);

        if (worker.getHeldItemMainhand() != null && sheep != null)
        {
            worker.swingArm(EnumHand.MAIN_HAND);
            final List<ItemStack> items = sheep.onSheared(worker.getHeldItemMainhand(),
              worker.world,
              worker.getPosition(),
              net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel(net.minecraft.init.Enchantments.FORTUNE, worker.getHeldItemMainhand()));

            dyeSheepChance(sheep);

            worker.getHeldItemMainhand().damageItem(1, worker);

            worker.addExperience(EXP_PER_SHEEP);

            for (final ItemStack item : items)
            {
                worker.getInventoryCitizen().addItemStackToInventory(item);
            }
        }

        incrementActionsDone();

        return HERDER_DECIDE;
    }

    /**
     * Possibly dyes a sheep based on their Worker Hut Level
     *
     * @param sheep the {@link EntitySheep} to possibly dye.
     */
    private void dyeSheepChance(final EntitySheep sheep)
    {
        if (worker.getWorkBuilding() != null)
        {
            final int chanceToDye = worker.getWorkBuilding().getBuildingLevel();

            final int rand = world.rand.nextInt(100);

            if (rand <= chanceToDye)
            {
                final int dyeInt = world.rand.nextInt(15);
                sheep.setFleeceColor(EnumDyeColor.byMetadata(dyeInt));
            }
        }
    }
}
