package com.minecolonies.coremod.entity.ai.citizen.herders;

import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCowboy;
import com.minecolonies.coremod.colony.jobs.JobCowboy;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

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

    private final ItemStack bucketStack = new ItemStack(Items.BUCKET);
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
        itemsNeeded.add(bucketStack);
        super.registerTargets(
          new AITarget(COWBOY_MILK, this::milkCows)
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
        return new ItemStack(Items.WHEAT);
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
    public AIState decideWhatToDo()
    {
        final AIState result = super.decideWhatToDo();
        final BuildingCowboy building = (BuildingCowboy) worker.getCitizenColonyHandler().getWorkBuilding();

        if (!building.isMilkCows())
        {
            if (itemsNeeded.contains(bucketStack))
            {
                itemsNeeded.remove(bucketStack);
            }
            
        }
        else
        {
            if (!itemsNeeded.contains(bucketStack))
            {
                itemsNeeded.add(bucketStack);
            }
        }
        
        final boolean hasBucket = InventoryUtils.hasItemInItemHandler(new InvWrapper(worker.getInventoryCitizen()), Items.BUCKET, 0);


        if (building != null && building.isMilkCows() && result.equals(START_WORKING) && hasBucket)
        {
            return COWBOY_MILK;
        }
        return result;
    }

    @NotNull
    @Override
    protected List<ItemStack> itemsNiceToHave()
    {
        final List<ItemStack> list = super.itemsNiceToHave();
        if (! ((BuildingCowboy) worker.getCitizenColonyHandler().getWorkBuilding()).isMilkCows())
        {
            list.add(new ItemStack(Items.BUCKET, 1));
        }
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
        worker.getCitizenStatusHandler().setLatestStatus(new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_COWBOY_MILKING));

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
