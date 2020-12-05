package com.minecolonies.coremod.entity.ai.basic;

import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.colony.buildings.workerbuildings.IBuildingPublicCrafter;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.jobs.AbstractJobCrafter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;

/**
 * Abstract class for the principal crafting AIs.
 */
public abstract class AbstractEntityAICrafting<J extends AbstractJobCrafter<?, J>, B extends AbstractBuildingWorker> extends AbstractEntityAIInteract<J, B>
{
    /**
     * Time the worker delays until the next hit.
     */
    protected static final int HIT_DELAY = 10;

    /**
     * Increase this value to make the product creation progress way slower.
     */
    public static final int PROGRESS_MULTIPLIER = 10;

    /**
     * Max level which should have an effect on the speed of the worker.
     */
    protected static final int MAX_LEVEL = 50;

    /**
     * Times the product needs to be hit.
     */
    private static final int HITTING_TIME = 3;

    /**
     * The current request that is being crafted;
     */
    public IRequest<? extends PublicCrafting> currentRequest;

    /**
     * The current recipe that is being crafted.
     */
    protected IRecipeStorage currentRecipeStorage;

    /**
     * The number of actions a crafting "success" is worth. By default, that's 1 action for 1 crafting success. Override this in your subclass to make crafting recipes worth more
     * actions :-)
     *
     * @return The number of actions a crafting "success" is worth.
     */
    protected int getActionRewardForCraftingSuccess()
    {
        return 1;
    }

    /**
     * Initialize the crafter job and add all his tasks.
     *
     * @param job the job he has.
     */
    public AbstractEntityAICrafting(@NotNull final J job)
    {
        super(job);
        super.registerTargets(
          /*
           * Check if tasks should be executed.
           */
          new AITarget(IDLE, () -> START_WORKING, 1),
          new AITarget(START_WORKING, this::decide, STANDARD_DELAY),
          new AITarget(QUERY_ITEMS, this::queryItems, STANDARD_DELAY),
          new AITarget(GET_RECIPE, this::getRecipe, STANDARD_DELAY),
          new AITarget(CRAFT, this::craft, HIT_DELAY)
        );
        worker.setCanPickUpLoot(true);
    }

    /**
     * Main method to decide on what to do.
     *
     * @return the next state to go to.
     */
    protected IAIState decide()
    {
        worker.getCitizenData().setVisibleStatus(VisibleCitizenStatus.WORKING);
        if (job.getTaskQueue().isEmpty())
        {
            return START_WORKING;
        }

        if (job.getCurrentTask() == null)
        {
            return START_WORKING;
        }

        if (walkToBuilding())
        {
            return START_WORKING;
        }

        if (job.getActionsDone() >= getActionsDoneUntilDumping())
        {
            // Wait to dump before continuing.
            return getState();
        }

        return getNextCraftingState();
    }

    /**
     * Gets the next crafting state required, if a task exists.
     *
     * @return next state
     */
    protected IAIState getNextCraftingState()
    {
        if (job.getCurrentTask() == null)
        {
            return getState();
        }

        if (currentRequest != null && currentRecipeStorage != null)
        {
            return QUERY_ITEMS;
        }

        return GET_RECIPE;
    }

    /**
     * Query the IRecipeStorage of the first request in the queue.
     *
     * @return the next state to go to.
     */
    protected IAIState getRecipe()
    {
        final IRequest<? extends PublicCrafting> currentTask = job.getCurrentTask();

        if (currentTask == null)
        {
            return START_WORKING;
        }
        final IBuildingWorker buildingWorker = getOwnBuilding();
        currentRecipeStorage = buildingWorker.getFirstFullFillableRecipe(stack -> stack.isItemEqual(currentTask.getRequest().getStack()), 1, false);
        if (currentRecipeStorage == null)
        {
            job.finishRequest(false);
            incrementActionsDone(getActionRewardForCraftingSuccess());
            return START_WORKING;
        }

        currentRequest = currentTask;
        job.setMaxCraftingCount(currentRequest.getRequest().getCount());
        final int currentCount = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), stack -> stack.isItemEqual(currentRecipeStorage.getPrimaryOutput()));
        final int inProgressCount = getExtendedCount(currentRecipeStorage.getPrimaryOutput());

        final int countPerIteration = currentRecipeStorage.getPrimaryOutput().getCount();
        final int doneOpsCount = currentCount / countPerIteration;
        final int progressOpsCount = inProgressCount / countPerIteration;

        final int remainingOpsCount = currentRequest.getRequest().getCount() - doneOpsCount - progressOpsCount;

        final List<ItemStorage> input = currentRecipeStorage.getCleanedInput();
        for (final ItemStorage inputStorage : input)
        {
            final ItemStack container = inputStorage.getItem().getContainerItem(inputStorage.getItemStack());
            final int remaining;
            if(!currentRecipeStorage.getSecondaryOutputs().isEmpty() && ItemStackUtils.compareItemStackListIgnoreStackSize(currentRecipeStorage.getSecondaryOutputs(), inputStorage.getItemStack(), false, true))
            {
                remaining = inputStorage.getAmount();
            }
            else if (!ItemStackUtils.isEmpty(container) && ItemStackUtils.compareItemStacksIgnoreStackSize(inputStorage.getItemStack(), container , false, true))
            {
                remaining = inputStorage.getAmount();
            }
            else
            {
                remaining = inputStorage.getAmount() * remainingOpsCount;
            }
            if (InventoryUtils.getCountFromBuilding(getOwnBuilding(), itemStack -> itemStack.isItemEqual(inputStorage.getItemStack()))
                  + InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), itemStack -> itemStack.isItemEqual(inputStorage.getItemStack()))
                  + getExtendedCount(inputStorage.getItemStack())
                  < remaining)
            {
                job.finishRequest(false);
                incrementActionsDone(getActionRewardForCraftingSuccess());
                return START_WORKING;
            }
        }

        job.setCraftCounter(doneOpsCount);
        return QUERY_ITEMS;
    }

    /**
     * Get an extended count that can be overriden.
     *
     * @param stack the stack to add.
     * @return the additional quantities (for example in a furnace).
     */
    protected int getExtendedCount(final ItemStack stack)
    {
        return 0;
    }

    @Override
    public IAIState getStateAfterPickUp()
    {
        return GET_RECIPE;
    }

    /**
     * Query the required items to take them in the inventory to craft.
     *
     * @return the next state to go to.
     */
    private IAIState queryItems()
    {
        if (currentRecipeStorage == null)
        {
            return START_WORKING;
        }

        return checkForItems(currentRecipeStorage);
    }

    /**
     * Check for all items of the required recipe.
     *
     * @param storage the recipe storage.
     * @return the next state to go to.
     */
    protected IAIState checkForItems(@NotNull final IRecipeStorage storage)
    {
        final int inProgressCount = getExtendedCount(currentRecipeStorage.getPrimaryOutput());
        final int countPerIteration = currentRecipeStorage.getPrimaryOutput().getCount();
        final int progressOpsCount = inProgressCount / countPerIteration;

        final List<ItemStorage> input = storage.getCleanedInput();
        for (final ItemStorage inputStorage : input)
        {
            final Predicate<ItemStack> predicate = stack -> !ItemStackUtils.isEmpty(stack) && new Stack(stack).matches(inputStorage.getItemStack());
            final int invCount = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), predicate);
            final ItemStack container = inputStorage.getItem().getContainerItem(inputStorage.getItemStack());
            final int remaining;
            if(!currentRecipeStorage.getSecondaryOutputs().isEmpty() && ItemStackUtils.compareItemStackListIgnoreStackSize(currentRecipeStorage.getSecondaryOutputs(), inputStorage.getItemStack(), false, true))
            {
                remaining = inputStorage.getAmount();
            }
            else if(!ItemStackUtils.isEmpty(container) && ItemStackUtils.compareItemStacksIgnoreStackSize(inputStorage.getItemStack(), container , false, true))
            {
                remaining = inputStorage.getAmount();
            }
            else
            {
                remaining = inputStorage.getAmount() * job.getMaxCraftingCount();
            }

            if (invCount <= 0 || invCount + ((job.getCraftCounter() + progressOpsCount) * inputStorage.getAmount())
                  < remaining)
            {
                if (InventoryUtils.hasItemInProvider(getOwnBuilding(), predicate))
                {
                    needsCurrently = new Tuple<>(predicate, remaining);
                    return GATHERING_REQUIRED_MATERIALS;
                }
                currentRecipeStorage = null;
                currentRequest = null;
                return GET_RECIPE;
            }
        }

        return CRAFT;
    }

    /**
     * The actual crafting logic.
     *
     * @return the next state to go to.
     */
    protected IAIState craft()
    {
        if (currentRecipeStorage == null || job.getCurrentTask() == null)
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

        if (job.getProgress() >= getRequiredProgressForMakingRawMaterial())
        {
            final IAIState check = checkForItems(currentRecipeStorage);
            if (check == CRAFT)
            {
                if (!currentRecipeStorage.fullFillRecipe(worker.getEntityWorld(), worker.getItemHandlerCitizen()))
                {
                    currentRequest = null;
                    incrementActionsDone(getActionRewardForCraftingSuccess());
                    job.finishRequest(false);
                    resetValues();
                    return START_WORKING;
                }

                currentRequest.addDelivery(currentRecipeStorage.getPrimaryOutput());
                job.setCraftCounter(job.getCraftCounter() + 1);

                if (job.getCraftCounter() >= job.getMaxCraftingCount())
                {
                    incrementActionsDone(getActionRewardForCraftingSuccess());
                    getOwnBuilding().improveRecipe(currentRecipeStorage, job.getCraftCounter(), worker.getCitizenData());
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
                else
                {
                    job.setProgress(0);
                    return GET_RECIPE;
                }
            }
            else
            {
                currentRequest = null;
                job.finishRequest(false);
                incrementActionsDoneAndDecSaturation();
                resetValues();
            }
            return START_WORKING;
        }

        return getState();
    }

    /**
     * Reset all the values.
     */
    public void resetValues()
    {
        job.setMaxCraftingCount(0);
        job.setProgress(0);
        job.setCraftCounter(0);
        worker.setHeldItem(Hand.MAIN_HAND, ItemStackUtils.EMPTY);
        worker.setHeldItem(Hand.OFF_HAND, ItemStackUtils.EMPTY);
    }

    @Override
    public IAIState afterDump()
    {
        if (job.getMaxCraftingCount() == 0 && job.getProgress() == 0 && job.getCraftCounter() == 0 && currentRequest != null)
        {
            // Fallback security blanket. Normally, the craft() method should have dealt with the request.
            if (currentRequest.getState() == RequestState.IN_PROGRESS)
            {
                job.finishRequest(true);
            }
            currentRequest = null;
        }

        resetValues();
        return super.afterDump();
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return 1;
    }

    /**
     * Get the required progress to execute a recipe.
     *
     * @return the amount of hits required.
     */
    private int getRequiredProgressForMakingRawMaterial()
    {
        final int jobModifier = worker.getCitizenData().getCitizenSkillHandler().getLevel(((IBuildingPublicCrafter) getOwnBuilding()).getCraftSpeedSkill()) / 2;
        return PROGRESS_MULTIPLIER / Math.min(jobModifier + 1, MAX_LEVEL) * HITTING_TIME;
    }

    @Override
    public boolean isAfterDumpPickupAllowed()
    {
        return currentRequest == null;
    }
}
