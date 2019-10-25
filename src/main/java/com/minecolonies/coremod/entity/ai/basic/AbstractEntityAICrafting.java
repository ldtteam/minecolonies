package com.minecolonies.coremod.entity.ai.basic;

import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.jobs.AbstractJobCrafter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.CitizenConstants.TICKS_20;

/**
 * Abstract class for the principal crafting AIs.
 */
public abstract class AbstractEntityAICrafting<J extends AbstractJobCrafter> extends AbstractEntityAIInteract<J>
{
    /**
     * Time the worker delays until the next hit.
     */
    protected static final int HIT_DELAY = 20;

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
    private static final int                                HITTING_TIME = 3;
    /**
     * The current request that is being crafted;
     */
    public               IRequest<? extends PublicCrafting> currentRequest;
    /**
     * The current recipe that is being crafted.
     */
    protected            IRecipeStorage                     currentRecipeStorage;

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
          new AITarget(IDLE, () -> START_WORKING),
          new AITarget(START_WORKING, this::decide),
          new AITarget(QUERY_ITEMS, this::queryItems),
          new AITarget(GET_RECIPE, this::getRecipe),
          new AITarget(CRAFT, this::craft)
        );
        worker.setCanPickUpLoot(true);
    }

    /**
     * Main method to decide on what to do.
     * @return the next state to go to.
     */
    protected IAIState decide()
    {
        if (job.getTaskQueue().isEmpty())
        {
            setDelay(TICKS_20);
            return START_WORKING;
        }

        if (job.getCurrentTask() == null)
        {
            setDelay(TICKS_20);
            return START_WORKING;
        }

        if (walkToBuilding())
        {
            setDelay(STANDARD_DELAY);
            return START_WORKING;
        }

        if (currentRequest != null && currentRecipeStorage != null)
        {
            return QUERY_ITEMS;
        }

        return GET_RECIPE;
    }

    /**
     * Query the IRecipeStorage of the first request in the queue.
     * @return the next state to go to.
     */
    private IAIState getRecipe()
    {
        final IRequest<? extends PublicCrafting> currentTask = job.getCurrentTask();

        if (currentTask == null)
        {
            return START_WORKING;
        }
        final IBuildingWorker buildingWorker = getOwnBuilding();
        currentRecipeStorage = buildingWorker.getFirstFullFillableRecipe(currentTask.getRequest().getStack());

        if (currentRecipeStorage == null)
        {
            job.finishRequest(false);
            setDelay(TICKS_20);
            return START_WORKING;
        }

        currentRequest = currentTask;
        job.setMaxCraftingCount(CraftingUtils.calculateMaxCraftingCount(currentRequest.getRequest().getCount(), currentRecipeStorage));

        setDelay(STANDARD_DELAY);
        return QUERY_ITEMS;
    }

    @Override
    public IAIState getStateAfterPickUp()
    {
        return GET_RECIPE;
    }

    /**
     * Query the required items to take them in the inventory to craft.
     * @return the next state to go to.
     */
    private IAIState queryItems()
    {
        setDelay(STANDARD_DELAY);
        if (currentRecipeStorage == null)
        {
            setDelay(TICKS_20);
            return START_WORKING;
        }

        return checkForItems(currentRecipeStorage);
    }

    /**
     * Check for all items of the required recipe.
     * @return the next state to go to.
     */
    protected IAIState checkForItems(@NotNull final IRecipeStorage storage)
    {
        final List<ItemStorage> input = storage.getCleanedInput();
        for(final ItemStorage inputStorage : input)
        {
            final Predicate<ItemStack> predicate = stack -> !ItemStackUtils.isEmpty(stack) && new Stack(stack).matches(inputStorage.getItemStack());
            if (InventoryUtils.getItemCountInItemHandler(new InvWrapper(worker.getInventoryCitizen()), predicate) < inputStorage.getAmount())
            {
                if (InventoryUtils.hasItemInProvider(getOwnBuilding(), predicate))
                {
                    needsCurrently = predicate;
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
     * @return the next state to go to.
     */
    protected IAIState craft()
    {
        if (currentRecipeStorage == null)
        {
            setDelay(TICKS_20);
            return START_WORKING;
        }

        if (currentRequest == null)
        {
            return GET_RECIPE;
        }

        if (walkToBuilding())
        {
            setDelay(STANDARD_DELAY);
            return getState();
        }

        job.setProgress(job.getProgress() + 1);

        worker.setHeldItem(EnumHand.MAIN_HAND, currentRecipeStorage.getCleanedInput().get(worker.getRandom().nextInt(currentRecipeStorage.getCleanedInput().size())).getItemStack().copy());
        worker.setHeldItem(EnumHand.OFF_HAND, currentRecipeStorage.getPrimaryOutput().copy());
        worker.getCitizenItemHandler().hitBlockWithToolInHand(getOwnBuilding().getPosition());
        setDelay(HIT_DELAY);

        currentRequest = job.getCurrentTask();

        if (currentRequest != null && currentRequest.getState() == RequestState.CANCELLED)
        {
            currentRequest = null;
            incrementActionsDone();
            job.setMaxCraftingCount(0);
            job.setProgress(0);
            job.setCraftCounter(0);
            currentRecipeStorage = null;
            worker.setHeldItem(EnumHand.MAIN_HAND, ItemStackUtils.EMPTY);
            worker.setHeldItem(EnumHand.OFF_HAND, ItemStackUtils.EMPTY);
            return START_WORKING;
        }

        if (job.getProgress() >= getRequiredProgressForMakingRawMaterial())
        {
            final IAIState check = checkForItems(currentRecipeStorage);
            if (check == CRAFT)
            {
                if (!currentRecipeStorage.fullFillRecipe(worker.getItemHandlerCitizen()))
                {
                    currentRequest = null;
                    incrementActionsDone();
                    job.finishRequest(false);
                    job.setMaxCraftingCount(0);
                    job.setProgress(0);
                    job.setCraftCounter(0);
                    setDelay(TICKS_20);
                    worker.setHeldItem(EnumHand.MAIN_HAND, ItemStackUtils.EMPTY);
                    worker.setHeldItem(EnumHand.OFF_HAND, ItemStackUtils.EMPTY);
                    return START_WORKING;
                }

                currentRequest.addDelivery(currentRecipeStorage.getPrimaryOutput());
                job.setCraftCounter(job.getCraftCounter() + 1);

                if (job.getCraftCounter() == job.getMaxCraftingCount())
                {
                    incrementActionsDone();
                    job.setMaxCraftingCount(0);
                    job.setProgress(0);
                    job.setCraftCounter(0);
                    currentRecipeStorage = null;
                    worker.setHeldItem(EnumHand.MAIN_HAND, ItemStackUtils.EMPTY);
                     worker.setHeldItem(EnumHand.OFF_HAND, ItemStackUtils.EMPTY);
                }
                else
                {
                    job.setProgress(0);
                    return GET_RECIPE;
                }

                return START_WORKING;
            }
            else
            {
                currentRequest = null;
                job.finishRequest(false);
                job.setMaxCraftingCount(0);
                job.setProgress(0);
                job.setCraftCounter(0);
                incrementActionsDoneAndDecSaturation();
                setDelay(TICKS_20);
                worker.setHeldItem(EnumHand.MAIN_HAND, ItemStackUtils.EMPTY);
                worker.setHeldItem(EnumHand.OFF_HAND, ItemStackUtils.EMPTY);
                return START_WORKING;
            }
        }

        return getState();
    }

    @Override
    public IAIState afterDump()
    {
        if (job.getMaxCraftingCount() == 0 && job.getProgress() == 0 && job.getCraftCounter() == 0 && currentRequest != null)
        {
            job.finishRequest(true);
            worker.getCitizenExperienceHandler().addExperience(currentRequest.getRequest().getCount()/2.0);
            worker.setHeldItem(EnumHand.MAIN_HAND, ItemStackUtils.EMPTY);
            worker.setHeldItem(EnumHand.OFF_HAND, ItemStackUtils.EMPTY);
            currentRequest = null;
        }

        getOwnBuilding().setPickUpPriority(1);

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
        return PROGRESS_MULTIPLIER / Math.min(worker.getCitizenExperienceHandler().getLevel() + 1, MAX_LEVEL) * HITTING_TIME;
    }
}
