package com.minecolonies.coremod.entity.ai.citizen.herders;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCowboy;
import com.minecolonies.coremod.colony.jobs.JobCowboy;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;

/**
 * The AI behind the {@link JobCowboy} for Breeding, Killing and Milking Cows.
 */
public class EntityAIWorkCowboy extends AbstractEntityAIHerder<JobCowboy, BuildingCowboy, CowEntity>
{
    /**
     * Max amount of animals per Hut Level.
     */
    private static final int MAX_ANIMALS_PER_LEVEL = 2;

    /**
     * Herd cow icon
     */
    private final static VisibleCitizenStatus HERD_COW               =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/cowboy.png"), "com.minecolonies.gui.visiblestatus.cowboy");

    /**
     * Creates the abstract part of the AI. Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAIWorkCowboy(@NotNull final JobCowboy job)
    {
        super(job);
        super.registerTargets(
          new AITarget(COWBOY_MILK, this::milkCows, 1)
        );
    }

    @Override
    public Class<BuildingCowboy> getExpectedBuildingClass()
    {
        return BuildingCowboy.class;
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
    public Class<CowEntity> getAnimalClass()
    {
        return CowEntity.class;
    }

    @Override
    public IAIState decideWhatToDo()
    {
        final IAIState result = super.decideWhatToDo();
        final boolean hasBucket = InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(), Items.BUCKET);
        if (building != null && building.getSetting(BuildingCowboy.MILKING).getValue() && result.equals(START_WORKING) && hasBucket)
        {
            return COWBOY_MILK;
        }
        return result;
    }

    @NotNull
    @Override
    public List<ItemStack> getExtraItemsNeeded()
    {
        final List<ItemStack> list = super.getExtraItemsNeeded();
        if (building.getSetting(BuildingCowboy.MILKING).getValue())
        {
            list.add(new ItemStack(Items.BUCKET));
        }
        return list;
    }

    /**
     * Makes the Cowboy "Milk" the cows (Honestly all he does is swap an empty bucket for a milk bucket, there's no actual "Milk" method in {@link CowEntity}
     *
     * @return The next {@link IAIState}
     */
    private IAIState milkCows()
    {
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_COWBOY_MILKING));
        worker.getCitizenData().setVisibleStatus(HERD_COW);

        if (!worker.getCitizenInventoryHandler().hasItemInInventory(getBreedingItem().getItem()) && InventoryUtils.hasBuildingEnoughElseCount(building, new ItemStorage(new ItemStack(Items.BUCKET, 1)), 2) > 1)
        {
            if (!walkToBuilding())
            {
                checkAndTransferFromHut(new ItemStack(Items.BUCKET, 1));
            }
            else
            {
                return DECIDE;
            }
        }

        final CowEntity cow = searchForAnimals().stream().findFirst().orElse(null);

        if (cow == null)
        {
            return DECIDE;
        }

        if (!walkingToAnimal(cow) && equipItem(Hand.MAIN_HAND, new ItemStack(Items.BUCKET, 1)))
        {
            if (InventoryUtils.addItemStackToItemHandler(worker.getInventoryCitizen(), new ItemStack(Items.MILK_BUCKET)))
            {
                worker.getCitizenItemHandler().removeHeldItem();
                equipItem(Hand.MAIN_HAND, new ItemStack(Items.MILK_BUCKET));
                InventoryUtils.tryRemoveStackFromItemHandler(worker.getInventoryCitizen(), new ItemStack(Items.BUCKET, 1));
            }

            incrementActionsDoneAndDecSaturation();
            worker.getCitizenExperienceHandler().addExperience(1.0);
        }

        return DECIDE;
    }

    @Override
    public double getButcheringAttackDamage()
    {
        return Math.max(1.0, getPrimarySkillLevel() / 10.0);
    }

    @Override
    protected IAIState breedAnimals()
    {
        worker.getCitizenData().setVisibleStatus(HERD_COW);
        return super.breedAnimals();
    }

    @Override
    protected IAIState butcherAnimals()
    {
        worker.getCitizenData().setVisibleStatus(HERD_COW);
        return super.butcherAnimals();
    }
}
