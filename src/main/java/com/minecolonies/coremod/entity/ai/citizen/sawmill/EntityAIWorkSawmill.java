package com.minecolonies.coremod.entity.ai.citizen.sawmill;

import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.jobs.JobSawmill;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.CitizenConstants.TICKS_20;
import static com.minecolonies.coremod.entity.ai.util.AIState.*;

/**
 * Crafts wood related block when needed.
 */
public class EntityAIWorkSawmill extends AbstractEntityAIInteract<JobSawmill>
{
    /**
     * Time the worker delays until the next hit.
     */
    private static final int HIT_DELAY = 20;

    /**
     * Increase this value to make the product creation progress way slower.
     */
    private static final int PROGRESS_MULTIPLIER = 50;

    /**
     * Max level which should have an effect on the speed of the worker.
     */
    private static final int MAX_LEVEL = 50;

    /**
     * Times the dough needs to be kneaded.
     */
    private static final int HITTING_TIME = 5;

    /**
     * The recipe storage he is currently working on.
     */
    private IRecipeStorage currentRecipeStorage;

    /**
     * Max crafting count for current recipe.
     */
    private int maxCraftingCount = 0;

    /**
     * Count of already executed recipes.
     */
    private int craftCounter = 0;

    /**
     * Progress of hitting the block.
     */
    private int progress = 0;

    /**
     * The current request.
     */
    private IRequest<? extends PublicCrafting> currentRequest;

    /**
     * Initialize the sawmill and add all his tasks.
     *
     * @param sawmill the job he has.
     */
    public EntityAIWorkSawmill(@NotNull final JobSawmill sawmill)
    {
        super(sawmill);
        super.registerTargets(
          /*
           * Check if tasks should be executed.
           */
          new AITarget(IDLE, true, () -> START_WORKING),
          new AITarget(START_WORKING, true, this::decide),
          new AITarget(QUERY_ITEMS, true, this::queryItems),
          new AITarget(GET_RECIPE, true, this::getRecipe),
          new AITarget(CRAFT, true, this::craft)
        );
        worker.getCitizenExperienceHandler().setSkillModifier(2 * worker.getCitizenData().getEndurance() + worker.getCitizenData().getCharisma());
        worker.setCanPickUpLoot(true);
    }

    /**
     * Main method to decide on what to do.
     * @return the next state to go to.
     */
    private AIState decide()
    {
        if (job.getTaskQueue().isEmpty())
        {
            setDelay(TICKS_20);
            return START_WORKING;
        }

        if (walkToBuilding())
        {
            setDelay(STANDARD_DELAY);
            return START_WORKING;
        }

        if (currentRecipeStorage != null)
        {
            return QUERY_ITEMS;
        }

        return GET_RECIPE;
    }

    /**
     * Query the IRecipeStorage of the first request in the queue.
     * @return the next state to go to.
     */
    private AIState getRecipe()
    {
        final IRequest<? extends PublicCrafting> currentTask = job.getCurrentTask();
        final AbstractBuildingWorker buildingWorker = getOwnBuilding();
        currentRecipeStorage = buildingWorker.getFirstFullFillableRecipe(currentTask.getRequest().getStack());

        if (currentRecipeStorage == null)
        {
            worker.getCitizenColonyHandler().getColony().getRequestManager().updateRequestState(currentTask.getToken(), RequestState.CANCELLED);
            setDelay(TICKS_20);
            return START_WORKING;
        }
        setDelay(STANDARD_DELAY);
        return QUERY_ITEMS;
    }

    @Override
    public AIState getStateAfterPickUp()
    {
        return GET_RECIPE;
    }

    /**
     * Query the required items to take them in the inventory to craft.
     * @return the next state to go to.
     */
    private AIState queryItems()
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
    private AIState checkForItems(final IRecipeStorage storage)
    {
        final List<ItemStorage> input = storage.getCleanedInput();
        for(final ItemStorage inputStorage : input)
        {
            final Predicate<ItemStack> predicate = stack -> !ItemStackUtils.isEmpty(stack) && new Stack(stack).matches(inputStorage.getItemStack());
            if (!InventoryUtils.hasItemInItemHandler(new InvWrapper(worker.getInventoryCitizen()), predicate))
            {
                if (InventoryUtils.hasItemInProvider(getOwnBuilding(), predicate))
                {
                    needsCurrently = predicate;
                    return GATHERING_REQUIRED_MATERIALS;
                }
                currentRecipeStorage = null;
                return GET_RECIPE;
            }
        }

        return CRAFT;
    }

    /**
     * The actual crafting logic.
     * @return the next state to go to.
     */
    private AIState craft()
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
            craftCounter = CraftingUtils.calculateMaxCraftingCount(job.getCurrentTask().getRequest().getStack(), currentRecipeStorage);
        }

        if (craftCounter == 0)
        {
            getOwnBuilding().getColony().getRequestManager().updateRequestState(job.getCurrentTask().getToken(), RequestState.CANCELLED);
            maxCraftingCount = 0;
            progress = 0;
            craftCounter = 0;
            return START_WORKING;
        }

        progress++;
        worker.getCitizenItemHandler().hitBlockWithToolInHand(getOwnBuilding().getLocation());
        setDelay(HIT_DELAY);
        if (progress >= 10) //TODO set up afterwards again!
        {
            final AIState check = checkForItems(currentRecipeStorage);
            if (check == CRAFT)
            {
                getOwnBuilding().fullFillRecipe(currentRecipeStorage);
                progress = 0;
                craftCounter++;

                if (craftCounter >= maxCraftingCount)
                {
                    incrementActionsDoneAndDecSaturation();
                    maxCraftingCount = 0;
                    progress = 0;
                    craftCounter = 0;
                    currentRecipeStorage = null;
                    currentRequest = job.getCurrentTask();
                    return START_WORKING;
                }
            }
            else
            {
                return check;
            }
        }
        return START_WORKING;
    }

    @Override
    public AIState afterDump()
    {
        if (maxCraftingCount == 0 && progress == 0 && craftCounter == 0 && currentRequest != null)
        {
            getOwnBuilding().getColony().getRequestManager().updateRequestState(currentRequest.getToken(), RequestState.COMPLETED);
            currentRequest = null;
        }
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
