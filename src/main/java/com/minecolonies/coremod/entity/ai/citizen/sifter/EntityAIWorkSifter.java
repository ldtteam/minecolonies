package com.minecolonies.coremod.entity.ai.citizen.sifter;

import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingSifter;
import com.minecolonies.coremod.colony.jobs.JobSifter;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import com.minecolonies.coremod.network.messages.client.LocalizedParticleEffectMessage;
import com.minecolonies.coremod.util.WorkerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.items.IItemHandler;

import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.Constants.ONE_HUNDRED_PERCENT;

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

        currentRecipeStorage = sifterBuilding.getFirstFullFillableRecipe(item -> ItemStackUtils.isEmpty(item), 1, false);

        if (currentRecipeStorage == null)
        {
            progress = 0;
            return START_WORKING;
        }

        ItemStack meshItem = ItemStack.EMPTY;
        for (final IItemHandler handler : InventoryUtils.getItemHandlersFromProvider(sifterBuilding))
        {
            final int foundSlot = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(handler, item -> ItemStackUtils.compareItemStackListIgnoreStackSize(currentRecipeStorage.getCraftingTools(), item, false, true));
            if (foundSlot > -1)
            {
                meshItem = handler.getStackInSlot(foundSlot).copy();
            }
        }

        if(!meshItem.isEmpty() && (ItemStackUtils.isEmpty(worker.getHeldItemMainhand()) || ItemStackUtils.compareItemStacksIgnoreStackSize(worker.getHeldItemMainhand(), meshItem, false, true)))
        {
            worker.setHeldItem(Hand.MAIN_HAND, meshItem);
        }

        WorkerUtil.faceBlock(getOwnBuilding().getPosition(), worker);

        progress++;
       
        if (progress > MAX_LEVEL - (getEffectiveSkillLevel(getSecondarySkillLevel()) / 2))
        {
            progress = 0;
            sifterBuilding.setCurrentDailyQuantity(sifterBuilding.getCurrentDailyQuantity() + 1);
            if (sifterBuilding.getCurrentDailyQuantity() >= sifterBuilding.getDailyQuantity() || worker.getRandom().nextInt(ONE_HUNDRED_PERCENT) < CHANCE_TO_DUMP_INV)
            {
                incrementActionsDoneAndDecSaturation();
            } 
            currentRecipeStorage.fullfillRecipe(getLootContext(), sifterBuilding.getHandlers());

            worker.decreaseSaturationForContinuousAction();
            worker.getCitizenExperienceHandler().addExperience(0.2);
        }

        Network.getNetwork()
            .sendToTrackingEntity(new LocalizedParticleEffectMessage(meshItem, sifterBuilding.getID()), worker);
        Network.getNetwork()
            .sendToTrackingEntity(new LocalizedParticleEffectMessage(currentRecipeStorage.getCleanedInput().get(0).getItemStack().copy(), sifterBuilding.getID().down()), worker);
        
        worker.swingArm(Hand.MAIN_HAND);
        SoundUtils.playSoundAtCitizen(world, getOwnBuilding().getID(), SoundEvents.ENTITY_LEASH_KNOT_BREAK);
        return getState();
    }
}
