package com.minecolonies.coremod.entity.ai.citizen.crusher;

import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCrusher;
import com.minecolonies.coremod.colony.jobs.JobCrusher;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import com.minecolonies.coremod.network.messages.LocalizedParticleEffectMessage;
import com.minecolonies.coremod.util.WorkerUtil;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.CitizenConstants.TICKS_20;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Crusher AI class.
 */
public class EntityAIWorkCrusher<J extends JobCrusher> extends AbstractEntityAICrafting<J>
{
    /**
     * How often should strength factor into the crusher's skill modifier.
     */
    private static final int STRENGTH_MULTIPLIER = 2;

    /**
     * How often should endurance factor into the crusher's skill modifier.
     */
    private static final int ENDURANCE_MULTIPLIER = 1;

    /**
     * Delay for each of the craftings.
     */
    private static final int TICK_DELAY = 5;

    /**
     * Constructor for the crusher.
     * Defines the tasks the cook executes.
     *
     * @param job a crusher job to use.
     */
    public EntityAIWorkCrusher(@NotNull final J job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING),
          new AITarget(CRUSH, this::crush)
        );
        worker.getCitizenExperienceHandler().setSkillModifier(STRENGTH_MULTIPLIER * worker.getCitizenData().getStrength()
                                                                + ENDURANCE_MULTIPLIER * worker.getCitizenData().getEndurance());
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class getExpectedBuildingClass()
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
        WorkerUtil.faceBlock(getOwnBuilding().getPosition(), worker);

        setDelay(TICK_DELAY);
        progress++;

        final BuildingCrusher crusherBuilding = getOwnBuilding(BuildingCrusher.class);
        if (currentRecipeStorage == null)
        {
            currentRecipeStorage = crusherBuilding.getCurrentRecipe();
        }

        if ((getState() != CRAFT && crusherBuilding.getCurrentDailyQuantity() >= crusherBuilding.getCrusherMode().getSecond()) || currentRecipeStorage == null)
        {
            return START_WORKING;
        }

        final IAIState check = checkForItems(currentRecipeStorage);
        if (progress > MAX_LEVEL - Math.min(worker.getCitizenExperienceHandler().getLevel() + 1, MAX_LEVEL))
        {
            progress = 0;

            if (check == CRAFT)
            {
                if (getState() != CRAFT)
                {
                    crusherBuilding.setCurrentDailyQuantity(crusherBuilding.getCurrentDailyQuantity() + 1);
                    if (crusherBuilding.getCurrentDailyQuantity() >= crusherBuilding.getCrusherMode().getSecond())
                    {
                        incrementActionsDoneAndDecSaturation();
                    }
                }

                craftCounter++;
                currentRecipeStorage.fullFillRecipe(worker.getItemHandlerCitizen());
                worker.decreaseSaturationForContinuousAction();
                worker.getCitizenExperienceHandler().addExperience(0.1);
            }
            else if (getState() != CRAFT)
            {
                currentRecipeStorage = crusherBuilding.getCurrentRecipe();
                final int requestQty = Math.min((crusherBuilding.getCrusherMode().getSecond() - crusherBuilding.getCurrentDailyQuantity()) * 2, STACKSIZE);
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
            MineColonies.getNetwork().sendToAllTracking(new LocalizedParticleEffectMessage(currentRecipeStorage.getInput().get(0).copy(), crusherBuilding.getID()), worker);
            MineColonies.getNetwork().sendToAllTracking(new LocalizedParticleEffectMessage(currentRecipeStorage.getPrimaryOutput().copy(), crusherBuilding.getID().down()),
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
    protected IAIState craft()
    {
        if (currentRecipeStorage == null)
        {
            setDelay(TICKS_20);
            return START_WORKING;
        }

        if (walkToBuilding())
        {
            setDelay(STANDARD_DELAY);
            return getState();
        }

        if (maxCraftingCount == 0)
        {
            PublicCrafting crafting = (PublicCrafting) job.getCurrentTask().getRequest();
            maxCraftingCount = CraftingUtils.calculateMaxCraftingCount(crafting.getCount(), currentRecipeStorage);
        }

        if (maxCraftingCount == 0)
        {
            getOwnBuilding().getColony().getRequestManager().updateRequestState(job.getCurrentTask().getId(), RequestState.CANCELLED);
            maxCraftingCount = 0;
            craftCounter = 0;
            setDelay(TICKS_20);
            return START_WORKING;
        }

        currentRequest = job.getCurrentTask();
        final IAIState nextState = crush();
        if (nextState == getState())
        {
            if (craftCounter >= maxCraftingCount)
            {
                final ItemStack primaryOutput = currentRecipeStorage.getPrimaryOutput();
                primaryOutput.setCount(currentRequest.getRequest().getCount());
                currentRequest.addDelivery(primaryOutput);
                incrementActionsDoneAndDecSaturation();
                maxCraftingCount = 0;
                craftCounter = 0;
                currentRecipeStorage = null;
                return START_WORKING;
            }
        }
        else
        {
            maxCraftingCount = 0;
            craftCounter = 0;
            return START_WORKING;
        }
        return getState();
    }
}
