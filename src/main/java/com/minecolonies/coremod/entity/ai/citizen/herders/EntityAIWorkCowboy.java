package com.minecolonies.coremod.entity.ai.citizen.herders;

import com.minecolonies.api.colony.interactionhandling.TranslationTextComponent;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCowboy;
import com.minecolonies.coremod.colony.jobs.JobCowboy;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;

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
        super(job);
        worker.getCitizenExperienceHandler().setSkillModifier(2 * worker.getCitizenData().getDexterity() + worker.getCitizenData().getStrength());
        super.registerTargets(
          new AITarget(COWBOY_MILK, this::milkCows, 1)
        );
    }

    @Override
    public Class getExpectedBuildingClass()
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
    public Class<EntityCow> getAnimalClass()
    {
        return EntityCow.class;
    }

    @Override
    public IAIState decideWhatToDo()
    {
        final IAIState result = super.decideWhatToDo();
        final BuildingCowboy building = getOwnBuilding();

        final boolean hasBucket = InventoryUtils.hasItemInItemHandler(new InvWrapper(worker.getInventoryCitizen()), Items.BUCKET, 0);
        if (building != null && building.isMilkingCows() && result.equals(START_WORKING) && hasBucket)
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
        if (getOwnBuilding(BuildingCowboy.class).isMilkingCows())
        {
            list.add(new ItemStack(Items.BUCKET));
        }
        return list;
    }

    @NotNull
    @Override
    protected List<ItemStack> itemsNiceToHave()
    {
        return getExtraItemsNeeded();
    }

    /**
     * Makes the Cowboy "Milk" the cows (Honestly all he does is swap an empty
     * bucket for a milk bucket, there's no actual "Milk" method in {@link EntityCow}
     *
     * @return The next {@link IAIState}
     */
    private IAIState milkCows()
    {
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_COWBOY_MILKING));

        if (!worker.getCitizenInventoryHandler().hasItemInInventory(getBreedingItem().getItem(), 0) && isInHut(new ItemStack(Items.BUCKET, 1)))
        {
            if (!walkToBuilding() && getOwnBuilding() != null)
            {
                isInTileEntity(getOwnBuilding().getTileEntity(), new ItemStack(Items.BUCKET, 1));
            }
            else
            {
                return DECIDE;
            }
        }

        final EntityCow cow = searchForAnimals().stream().findFirst().orElse(null);

        if (cow == null)
        {
            return DECIDE;
        }

        if (!walkingToAnimal(cow) && equipItem(EnumHand.MAIN_HAND, new ItemStack(Items.BUCKET, 1)))
        {

            if (!worker.getInventoryCitizen().addItemStackToInventory(new ItemStack(Items.MILK_BUCKET)))
            {
                worker.getCitizenItemHandler().removeHeldItem();
                equipItem(EnumHand.MAIN_HAND, new ItemStack(Items.MILK_BUCKET));
                InventoryUtils.removeStackFromItemHandler(new InvWrapper(worker.getInventoryCitizen()), new ItemStack(Items.BUCKET, 1));
            }

            incrementActionsDoneAndDecSaturation();
            worker.getCitizenExperienceHandler().addExperience(1.0);
        }

        return DECIDE;
    }
}
