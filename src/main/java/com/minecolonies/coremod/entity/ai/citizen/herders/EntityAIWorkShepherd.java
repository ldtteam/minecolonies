package com.minecolonies.coremod.entity.ai.citizen.herders;

import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingShepherd;
import com.minecolonies.coremod.colony.jobs.JobShepherd;
import com.minecolonies.coremod.entity.ai.statemachine.AITarget;
import com.minecolonies.coremod.entity.ai.statemachine.states.IAIState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.coremod.entity.ai.statemachine.states.AIWorkerState.*;

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
        worker.getCitizenExperienceHandler().setSkillModifier(2 * worker.getCitizenData().getDexterity() + worker.getCitizenData().getStrength());

        super.registerTargets(
          new AITarget(SHEPHERD_SHEAR, this::shearSheep)
        );
    }

    @NotNull
    @Override
    public List<ToolType> getExtraToolsNeeded()
    {
        final List<ToolType> toolsNeeded = super.getExtraToolsNeeded();
        toolsNeeded.add(ToolType.SHEARS);
        return toolsNeeded;
    }

    @Override
    public Class getExpectedBuildingClass()
    {
        return BuildingShepherd.class;
    }

    @Override
    public ItemStack getBreedingItem()
    {
        final ItemStack stack = new ItemStack(Items.WHEAT);
        stack.setCount(2);
        return stack;
    }

    @Override
    public int getMaxAnimalMultiplier()
    {
        return MAX_ANIMALS_PER_LEVEL;
    }

    @Override
    public IAIState decideWhatToDo()
    {

        final IAIState result = super.decideWhatToDo();

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
     * @return The next {@link IAIState}
     */
    private IAIState shearSheep()
    {
        worker.getCitizenStatusHandler().setLatestStatus(new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_SHEPHERD_SHEARING));

        final List<EntitySheep> sheeps = searchForAnimals();

        if (sheeps.isEmpty())
        {
            return DECIDE;
        }

        if (!equipTool(EnumHand.MAIN_HAND, ToolType.SHEARS))
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
              worker.getEntityWorld(),
              worker.getPosition(),
              net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel(net.minecraft.init.Enchantments.FORTUNE, worker.getHeldItemMainhand()));

            dyeSheepChance(sheep);

            worker.getHeldItemMainhand().damageItem(1, (EntityLivingBase) worker);

            worker.getCitizenExperienceHandler().addExperience(EXP_PER_SHEEP);

            for (final ItemStack item : items)
            {
                InventoryUtils.transferItemStackIntoNextBestSlotInItemHandler(item, new InvWrapper(worker.getInventoryCitizen()));
            }
        }
        worker.getCitizenExperienceHandler().addExperience(1.0);
        incrementActionsDoneAndDecSaturation();

        return DECIDE;
    }

    /**
     * Possibly dyes a sheep based on their Worker Hut Level
     *
     * @param sheep the {@link EntitySheep} to possibly dye.
     */
    private void dyeSheepChance(final EntitySheep sheep)
    {
        if (worker.getCitizenColonyHandler().getWorkBuilding() != null && ((BuildingShepherd) worker.getCitizenColonyHandler().getWorkBuilding()).isDyeSheeps())
        {
            final int chanceToDye = worker.getCitizenColonyHandler().getWorkBuilding().getBuildingLevel();

            final int rand = world.rand.nextInt(HUNDRED_PERCENT_CHANCE);

            if (rand <= chanceToDye)
            {
                final int dyeInt = world.rand.nextInt(NUMBER_OF_DYE_POSSIBILITIES);
                sheep.setFleeceColor(EnumDyeColor.byMetadata(dyeInt));
            }
        }
    }
}
