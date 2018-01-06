package com.minecolonies.coremod.entity.ai.citizen.herders;

import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.jobs.JobShepherd;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.init.Items;
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
     * Constants used for sheep dying calculations.
     */
    private static final int HUNDRED_PERCENT_CHANCE      = 100;
    private static final int NUMBER_OF_DYE_POSSIBILITIES = 15;

    /**
     * Creates the abstract part of the AI.
     * Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAIWorkShepherd(@NotNull final JobShepherd job)
    {
        super(job);
        toolsNeeded.add(ToolType.SHEARS);
        super.registerTargets(
          new AITarget(SHEPHERD_SHEAR, this::shearSheep)
        );
    }

    @Override
    public ItemStack getBreedingItem()
    {
        return new ItemStack(Items.WHEAT);
    }

    @Override
    public int getMaxAnimalMultiplier()
    {
        return MAX_ANIMALS_PER_LEVEL;
    }

    @Override
    public AIState decideWhatToDo()
    {

        final AIState result = super.decideWhatToDo();

        final List<EntitySheep> animals = new ArrayList<>(searchForAnimals());
        final EntitySheep shearingSheep = animals.stream().filter(sheepie -> !sheepie.getSheared() && !sheepie.isChild()).findFirst().orElse(null);

        if (result.equals(START_WORKING) && shearingSheep != null)
        {
            return SHEPHERD_SHEAR;
        }

        return result;
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
        worker.setLatestStatus(new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_SHEPHERD_SHEARING));

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
            if (walkingToAnimal(sheep))
            {
                return getState();
            }
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

            final int rand = world.rand.nextInt(HUNDRED_PERCENT_CHANCE);

            if (rand <= chanceToDye)
            {
                final int dyeInt = world.rand.nextInt(NUMBER_OF_DYE_POSSIBILITIES);
                sheep.setFleeceColor(EnumDyeColor.byMetadata(dyeInt));
            }
        }
    }
}
