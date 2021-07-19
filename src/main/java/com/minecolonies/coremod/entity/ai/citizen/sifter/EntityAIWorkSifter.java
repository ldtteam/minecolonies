package com.minecolonies.coremod.entity.ai.citizen.sifter;

import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingSifter;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteraction;
import com.minecolonies.coremod.colony.jobs.JobSifter;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import com.minecolonies.coremod.network.messages.client.LocalizedParticleEffectMessage;
import com.minecolonies.coremod.util.WorkerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.TranslationTextComponent;

import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.Constants.ONE_HUNDRED_PERCENT;
import static com.minecolonies.api.util.constant.TranslationConstants.SIFTER_NO_MESH;

/**
 * Sifter AI class.
 */
public class EntityAIWorkSifter extends AbstractEntityAICrafting<JobSifter, BuildingSifter>
{
    /**
     * Max level which should have an effect on the speed of the worker.
     */
    private static final int MAX_LEVEL = 50;

    /**
     * Delay for each of the craftings.
     */
    private static final int TICK_DELAY = 10;

    /**
     * Chance for the sifter to dump his inventory.
     */
    private static final int CHANCE_TO_DUMP_INV = 10;

    /**
     * The delay before processing again when there is no mesh in the building
     */
    private static final int NO_MESH_DELAY = 100;

    /**
     * Progress of hitting the block.
     */
    protected int progress = 0;

    /**
     * Constructor for the sifter. Defines the tasks the cook executes.
     *
     * @param job a sifter job to use.
     */
    public EntityAIWorkSifter(@NotNull final JobSifter job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING, 10),
          new AITarget(START_WORKING, SIFT, 1),
          new AITarget(SIFT, this::sift, TICK_DELAY)
        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class<BuildingSifter> getExpectedBuildingClass()
    {
        return BuildingSifter.class;
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return 1; 
    }

    /**
     * The sifting process.
     *
     * @return the next AiState to go to.
     */
    protected IAIState sift()
    {
        final BuildingSifter sifterBuilding = getOwnBuilding();

        // Go idle if we can't do any more today
        if (sifterBuilding.getCurrentDailyQuantity() >= sifterBuilding.getMaxDailyQuantity())
        {
            return IDLE;
        }

        if (walkToBuilding())
        {
            return getState();
        }

        if (InventoryUtils.isItemHandlerFull(worker.getInventoryCitizen()))
        {
            return INVENTORY_FULL;
        }

        if (currentRecipeStorage == null)
        {
            final ICraftingBuildingModule module = getOwnBuilding().getFirstModuleOccurance(BuildingSifter.CraftingModule.class);
            currentRecipeStorage = module.getFirstFulfillableRecipe(ItemStackUtils::isEmpty, 1, false);
        }

        if (currentRecipeStorage == null)
        {
            if(InventoryUtils.getCountFromBuilding(sifterBuilding, i -> ModTags.meshes.contains(i.getItem())) == 0)
            {
                if(InventoryUtils.getItemCountInProvider(worker, i -> ModTags.meshes.contains(i.getItem())) > 0)
                {
                    // We don't want the mesh in our inventory, we 'craft' out of the building
                    incrementActionsDone();
                    return INVENTORY_FULL;
                }
                if(worker.getCitizenData() != null)
                {
                    worker.getCitizenData().triggerInteraction(new StandardInteraction(new TranslationTextComponent(SIFTER_NO_MESH), ChatPriority.IMPORTANT));
                    setDelay(NO_MESH_DELAY);
                }
            }
            if (!ItemStackUtils.isEmpty(worker.getMainHandItem()))
            {
                worker.setItemInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
            }
            if (!ItemStackUtils.isEmpty(worker.getOffhandItem()))
            {
                worker.setItemInHand(Hand.OFF_HAND, ItemStack.EMPTY);
            }

            progress = 0;
            return START_WORKING;
        }

        final ItemStack meshItem = currentRecipeStorage.getCraftingTools().get(0);
        final ItemStack inputItem = currentRecipeStorage.getCleanedInput().stream()
                .map(ItemStorage::getItemStack)
                .filter(item -> !ItemStackUtils.compareItemStacksIgnoreStackSize(item, meshItem, false, true))
                .findFirst().orElse(ItemStack.EMPTY);

        if (meshItem.isEmpty() || inputItem.isEmpty())
        {
            currentRecipeStorage = null;
            return getState();
        }

        if (!inputItem.isEmpty() && (ItemStackUtils.isEmpty(worker.getMainHandItem()) || ItemStackUtils.compareItemStacksIgnoreStackSize(worker.getMainHandItem(), inputItem)))
        {
            worker.setItemInHand(Hand.MAIN_HAND, inputItem);
        }
        if (!meshItem.isEmpty() && (ItemStackUtils.isEmpty(worker.getOffhandItem()) || ItemStackUtils.compareItemStacksIgnoreStackSize(worker.getOffhandItem(), meshItem, false, true)))
        {
            worker.setItemInHand(Hand.OFF_HAND, meshItem);
        }

        WorkerUtil.faceBlock(getOwnBuilding().getPosition(), worker);

        progress++;
       
        if (progress > MAX_LEVEL - (getEffectiveSkillLevel(getSecondarySkillLevel()) / 2))
        {
            progress = 0;
            sifterBuilding.setCurrentDailyQuantity(sifterBuilding.getCurrentDailyQuantity() + 1);
            if (sifterBuilding.getCurrentDailyQuantity() >= sifterBuilding.getMaxDailyQuantity() || worker.getRandom().nextInt(ONE_HUNDRED_PERCENT) < CHANCE_TO_DUMP_INV)
            {
                incrementActionsDoneAndDecSaturation();
            } 
            if (!currentRecipeStorage.fullfillRecipe(getLootContext(), sifterBuilding.getHandlers()))
            {
                currentRecipeStorage = null;
                return getState();
            }

            worker.decreaseSaturationForContinuousAction();
            worker.getCitizenExperienceHandler().addExperience(0.2);
        }

        Network.getNetwork()
            .sendToTrackingEntity(new LocalizedParticleEffectMessage(meshItem, sifterBuilding.getID()), worker);
        Network.getNetwork()
            .sendToTrackingEntity(new LocalizedParticleEffectMessage(inputItem, sifterBuilding.getID().below()), worker);
        
        worker.swing(Hand.MAIN_HAND);
        SoundUtils.playSoundAtCitizen(world, getOwnBuilding().getID(), SoundEvents.LEASH_KNOT_BREAK);
        return getState();
    }
}
