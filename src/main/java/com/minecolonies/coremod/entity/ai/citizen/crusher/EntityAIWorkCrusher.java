package com.minecolonies.coremod.entity.ai.citizen.crusher;

import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCrusher;
import com.minecolonies.coremod.colony.jobs.JobCrusher;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import com.minecolonies.coremod.network.messages.client.LocalizedParticleEffectMessage;
import com.minecolonies.coremod.util.WorkerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.CitizenConstants.TICKS_20;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Crusher AI class.
 */
public class EntityAIWorkCrusher extends AbstractEntityAICrafting<JobCrusher, BuildingCrusher>
{
    /**
     * Delay for each of the craftings.
     */
    private static final int TICK_DELAY = 20;

    /**
     * Constructor for the crusher.
     * Defines the tasks the crusher executes.
     *
     * @param job a crusher job to use.
     */
    public EntityAIWorkCrusher(@NotNull final JobCrusher job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING, 1),
          new AITarget(CRUSH, this::crush, TICK_DELAY)
        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class<BuildingCrusher> getExpectedBuildingClass()
    {
        return BuildingCrusher.class;
    }

    @Override
    protected IAIState decide()
    {
        final IAIState nextState = super.decide();
        if (nextState != START_WORKING)
        {
            return nextState;
        }
        return CRUSH;
    }

    /**
     * The crushing process.
     *
     * @return the next AiState to go to.
     */
    protected IAIState crush()
    {
        if (walkToBuilding())
        {
            return getState();
        }
        job.setProgress(job.getProgress() + TICK_DELAY);

        final BuildingCrusher crusherBuilding = getOwnBuilding();
        WorkerUtil.faceBlock(crusherBuilding.getPosition(), worker);
        if (currentRecipeStorage == null)
        {
            currentRecipeStorage = crusherBuilding.getCurrentRecipe();
        }

        if ((getState() != CRAFT && crusherBuilding.getCurrentDailyQuantity() >= crusherBuilding.getCrusherMode().getB()) || currentRecipeStorage == null)
        {
            return START_WORKING;
        }

        final IAIState check = checkForItems(currentRecipeStorage);
        if (job.getProgress() > MAX_LEVEL - Math.min(worker.getCitizenData().getJobModifier() + 1, MAX_LEVEL))
        {
            job.setProgress(0);

            if (check == CRAFT)
            {
                if (getState() != CRAFT)
                {
                    crusherBuilding.setCurrentDailyQuantity(crusherBuilding.getCurrentDailyQuantity() + 1);
                    if (crusherBuilding.getCurrentDailyQuantity() >= crusherBuilding.getCrusherMode().getB())
                    {
                        incrementActionsDoneAndDecSaturation();
                    }
                }
                if (currentRequest != null)
                {
                    currentRequest.addDelivery(currentRecipeStorage.getPrimaryOutput());
                }

                worker.swingArm(Hand.MAIN_HAND);
                job.setCraftCounter(job.getCraftCounter()+1);
                currentRecipeStorage.fullFillRecipe(worker.getItemHandlerCitizen());

                worker.decreaseSaturationForContinuousAction();
                worker.getCitizenExperienceHandler().addExperience(0.1);
            }
            else if (getState() != CRAFT)
            {
                currentRecipeStorage = crusherBuilding.getCurrentRecipe();
                final int requestQty = Math.min((crusherBuilding.getCrusherMode().getB() - crusherBuilding.getCurrentDailyQuantity()) * 2, STACKSIZE);
                if (requestQty <= 0)
                {
                    return START_WORKING;
                }
                final ItemStack stack = currentRecipeStorage.getInput().get(0).copy();
                stack.setCount(requestQty);
                checkIfRequestForItemExistOrCreateAsynch(stack);
                return START_WORKING;
            }
            else
            {
                return check;
            }
        }
        if (check == CRAFT)
        {
            Network.getNetwork().sendToTrackingEntity(new LocalizedParticleEffectMessage(currentRecipeStorage.getInput().get(0).copy(), crusherBuilding.getID()), worker);
            Network.getNetwork().sendToTrackingEntity(new LocalizedParticleEffectMessage(currentRecipeStorage.getPrimaryOutput().copy(), crusherBuilding.getID().down()),
              worker);
            SoundUtils.playSoundAtCitizen(world, getOwnBuilding().getID(), SoundEvents.BLOCK_STONE_BREAK);
        }
        return getState();
    }


    /**
     * The actual crafting logic.
     *
     * @return the next state to go to.
     */
    @Override
    protected IAIState craft()
    {
        if (currentRecipeStorage == null)
        {
            return START_WORKING;
        }

        if (currentRequest == null && job.getCurrentTask() != null)
        {
            return GET_RECIPE;
        }

        if (walkToBuilding())
        {
            return getState();
        }

        job.setProgress(job.getProgress() + 1);

        worker.setHeldItem(Hand.MAIN_HAND,
          currentRecipeStorage.getCleanedInput().get(worker.getRandom().nextInt(currentRecipeStorage.getCleanedInput().size())).getItemStack().copy());
        worker.setHeldItem(Hand.OFF_HAND, currentRecipeStorage.getPrimaryOutput().copy());
        worker.getCitizenItemHandler().hitBlockWithToolInHand(getOwnBuilding().getPosition());

        currentRequest = job.getCurrentTask();

        if (currentRequest != null && (currentRequest.getState() == RequestState.CANCELLED || currentRequest.getState() == RequestState.FAILED))
        {
            currentRequest = null;
            incrementActionsDone(getActionRewardForCraftingSuccess());
            currentRecipeStorage = null;
            return START_WORKING;
        }

        final IAIState check = crush();
        if (check == getState())
        {
            if (job.getCraftCounter() >= job.getMaxCraftingCount())
            {
                incrementActionsDone(getActionRewardForCraftingSuccess());
                currentRecipeStorage = null;
                resetValues();

                if (inventoryNeedsDump())
                {
                    if (job.getMaxCraftingCount() == 0 && job.getProgress() == 0 && job.getCraftCounter() == 0 && currentRequest != null)
                    {
                        job.finishRequest(true);
                        worker.getCitizenExperienceHandler().addExperience(currentRequest.getRequest().getCount() / 2.0);
                    }
                }
            }
        }
        else
        {
            currentRequest = null;
            job.finishRequest(false);
            incrementActionsDoneAndDecSaturation();
            resetValues();
        }

        return getState();
    }
}
