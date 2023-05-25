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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;

/**
 * The AI behind the {@link JobCowboy} for Breeding, Killing and Milking Cows.
 */
public class EntityAIWorkCowboy extends AbstractEntityAIHerder<JobCowboy, BuildingCowboy>
{
    /**
     * Bucket metadata.
     */
    public static final String RENDER_META_BUCKET = "bucket";

    /**
     * Herd cow icon
     */
    private final static VisibleCitizenStatus HERD_COW               =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/cowboy.png"), "com.minecolonies.gui.visiblestatus.cowboy");

    /**
     * Min wait between failed milking attempts.
     */
    private static final int MILK_COOL_DOWN = 10;

    private int milkCoolDown;

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
    protected void updateRenderMetaData()
    {
        String renderMeta = getState() == IDLE ? "" : RENDER_META_WORKING;
        if (worker.getCitizenInventoryHandler().hasItemInInventory(Items.BUCKET))
        {
            renderMeta += RENDER_META_BUCKET;
        }
        worker.setRenderMetadata(renderMeta);
    }

    @Override
    public Class<BuildingCowboy> getExpectedBuildingClass()
    {
        return BuildingCowboy.class;
    }

    @Override
    public IAIState decideWhatToDo()
    {
        final IAIState result = super.decideWhatToDo();
        if (milkCoolDown > 0)
        {
            --milkCoolDown;
        }
        else if (building != null && building.getFirstModuleOccurance(BuildingCowboy.MilkingModule.class).canTryToMilk() && result.equals(START_WORKING))
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
        if (building != null && building.getFirstModuleOccurance(BuildingCowboy.MilkingModule.class).canTryToMilk())
        {
            list.add(new ItemStack(Items.BUCKET));
        }
        return list;
    }

    /**
     * Makes the Cowboy "Milk" the cows (Honestly all he does is swap an empty bucket for a milk bucket, there's no actual "Milk" method in {@link Cow}
     *
     * @return The next {@link IAIState}
     */
    private IAIState milkCows()
    {
        worker.getCitizenStatusHandler().setLatestStatus(Component.translatable(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_COWBOY_MILKING));
        worker.getCitizenData().setVisibleStatus(HERD_COW);

        if (!worker.getCitizenInventoryHandler().hasItemInInventory(Items.BUCKET))
        {
            if (InventoryUtils.hasBuildingEnoughElseCount(building, new ItemStorage(new ItemStack(Items.BUCKET, 1)), 1) > 0
                    && !walkToBuilding())
            {
                checkAndTransferFromHut(new ItemStack(Items.BUCKET, 1));
            }
            else
            {
                milkCoolDown = MILK_COOL_DOWN;
                return DECIDE;
            }
        }

        final Cow cow = searchForAnimals(Cow.class).stream().filter(c -> !c.isBaby()).findFirst().orElse(null);

        if (cow == null)
        {
            milkCoolDown = MILK_COOL_DOWN;
            return DECIDE;
        }

        if (equipItem(InteractionHand.MAIN_HAND, Collections.singletonList(new ItemStack(Items.BUCKET))) && !walkingToAnimal(cow))
        {
            if (InventoryUtils.addItemStackToItemHandler(worker.getInventoryCitizen(), new ItemStack(Items.MILK_BUCKET)))
            {
                building.getFirstModuleOccurance(BuildingCowboy.MilkingModule.class).onMilked();
                worker.getCitizenItemHandler().removeHeldItem();
                equipItem(InteractionHand.MAIN_HAND, Collections.singletonList(new ItemStack(Items.MILK_BUCKET)));
                InventoryUtils.tryRemoveStackFromItemHandler(worker.getInventoryCitizen(), new ItemStack(Items.BUCKET, 1));
            }

            incrementActionsDoneAndDecSaturation();
            worker.getCitizenExperienceHandler().addExperience(1.0);
            return INVENTORY_FULL;
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
