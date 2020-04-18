package com.minecolonies.coremod.entity.ai.citizen.cook;

import com.google.common.reflect.TypeToken;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.Food;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCook;
import com.minecolonies.coremod.colony.jobs.JobCook;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIUsesFurnace;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.Tuple;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.CitizenConstants.TICKS_20;
import static com.minecolonies.api.util.constant.Constants.*;

/**
 * Cook AI class.
 */
public class EntityAIWorkCook extends AbstractEntityAIUsesFurnace<JobCook>
{
    /**
     * The amount of food which should be served to the worker.
     */
    public static final int AMOUNT_OF_FOOD_TO_SERVE = 2;

    /**
     * Delay between each serving.
     */
    private static final int SERVE_DELAY = 30;

    /**
     * Level at which the cook should give some food to the player.
     */
    private static final int LEVEL_TO_FEED_PLAYER = 10;

    /**
     * The citizen the worker is currently trying to serve.
     */
    private final List<AbstractEntityCitizen> citizenToServe = new ArrayList<>();

    /**
     * The citizen the worker is currently trying to serve.
     */
    private final List<PlayerEntity> playerToServe = new ArrayList<>();

    /**
     * The building range the cook should search for clients.
     */
    private AxisAlignedBB range = null;

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
     * Constructor for the Cook. Defines the tasks the cook executes.
     *
     * @param job a cook job to use.
     */
    public EntityAIWorkCook(@NotNull final JobCook job)
    {
        super(job);
        super.registerTargets(
          new AITarget(COOK_SERVE_FOOD_TO_CITIZEN, this::serveFoodToCitizen, SERVE_DELAY),
          new AITarget(QUERY_ITEMS, this::queryItems, STANDARD_DELAY),
          new AITarget(GET_RECIPE, this::getRecipe, STANDARD_DELAY),
          new AITarget(CRAFT, this::craft, HIT_DELAY)
        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class getExpectedBuildingClass()
    {
        return BuildingCook.class;
    }

    /**
     * Very simple action, cook straightly extract it from the furnace.
     *
     * @param furnace the furnace to retrieve from.
     */
    @Override
    protected void extractFromFurnace(final FurnaceTileEntity furnace)
    {
        InventoryUtils.transferItemStackIntoNextFreeSlotInItemHandler(
          new InvWrapper(furnace), RESULT_SLOT,
          worker.getInventoryCitizen());
        worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
        this.incrementActionsDoneAndDecSaturation();
    }

    @Override
    protected boolean isSmeltable(final ItemStack stack)
    {
        return ItemStackUtils.ISCOOKABLE.test(stack);
    }

    @Override
    protected boolean reachedMaxToKeep()
    {
        return InventoryUtils.getItemCountInProvider(getOwnBuilding(), ItemStackUtils.ISFOOD)
                 > Math.max(1, getOwnBuilding().getBuildingLevel() * getOwnBuilding().getBuildingLevel()) * SLOT_PER_LINE;
    }

    @Override
    public void requestSmeltable()
    {
        if (!getOwnBuilding().hasWorkerOpenRequestsOfType(worker.getCitizenData(), TypeToken.of(getSmeltAbleClass().getClass())))
        {
            worker.getCitizenData().createRequestAsync(getSmeltAbleClass());
        }
    }

    /**
     * Serve food to customer
     * <p>
     * If no customer, transition to START_WORKING. If we need to walk to the customer, repeat this state with tiny delay. If the customer has a full inventory, report and remove
     * customer, delay and repeat this state. If we have food, then COOK_SERVE. If no food in the building, transition to START_WORKING. If we were able to get the stored food,
     * then COOK_SERVE. If food is no longer available, delay and transition to START_WORKING. Otherwise, give the customer some food, then delay and repeat this state.
     *
     * @return next IAIState
     */
    private IAIState serveFoodToCitizen()
    {
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_SERVING));

        if (citizenToServe.isEmpty() && playerToServe.isEmpty())
        {
            return START_WORKING;
        }

        final Entity living = citizenToServe.isEmpty() ? playerToServe.get(0) : citizenToServe.get(0);

        if (range == null)
        {
            range = getOwnBuilding().getTargetableArea(world);
        }

        if (!range.contains(new Vec3d(living.getPosition())))
        {
            worker.getNavigator().clearPath();
            removeFromQueue();
            return START_WORKING;
        }

        if (walkToBlock(living.getPosition()))
        {
            if (worker.getCitizenStuckHandler().isStuck())
            {
                worker.getNavigator().clearPath();
                removeFromQueue();
            }
            setDelay(2);
            return getState();
        }

        final IItemHandler handler = citizenToServe.isEmpty() ? new InvWrapper(playerToServe.get(0).inventory) : citizenToServe.get(0).getInventoryCitizen();

        if (InventoryUtils.isItemHandlerFull(handler))
        {
            if (!citizenToServe.isEmpty())
            {
                final int foodSlot = InventoryUtils.findFirstSlotInItemHandlerWith(worker.getInventoryCitizen(), ItemStackUtils.CAN_EAT);
                if (foodSlot != -1)
                {
                    final ItemStack stack = worker.getInventoryCitizen().extractItem(foodSlot, 1, false);
                    if (stack.getItem().isFood())
                    {
                        citizenToServe.get(0).getCitizenData().increaseSaturation(stack.getItem().getFood().getHealing() / 2.0);
                    }
                }
            }

            removeFromQueue();
            setDelay(SERVE_DELAY);
            return getState();
        }
        InventoryUtils.transferXOfFirstSlotInItemHandlerWithIntoNextFreeSlotInItemHandler(
          worker.getInventoryCitizen(),
          ItemStackUtils.CAN_EAT,
          getOwnBuilding().getBuildingLevel() * AMOUNT_OF_FOOD_TO_SERVE, handler
        );

        if (!citizenToServe.isEmpty() && citizenToServe.get(0).getCitizenData() != null)
        {
            citizenToServe.get(0).getCitizenData().setJustAte(true);
        }

        if (citizenToServe.isEmpty() && living instanceof PlayerEntity)
        {
            LanguageHandler.sendPlayerMessage((PlayerEntity) living, "com.minecolonies.coremod.cook.serve.player", worker.getName());
        }
        removeFromQueue();

        setDelay(SERVE_DELAY);
        worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
        this.incrementActionsDoneAndDecSaturation();
        return START_WORKING;
    }

    /**
     * Remove the last citizen or player from the queue.
     */
    private void removeFromQueue()
    {
        if (citizenToServe.isEmpty())
        {
            playerToServe.remove(0);
        }
        else
        {
            citizenToServe.remove(0);
        }
    }

    /**
     * Checks if the cook has anything important to do before going to the default furnace user jobs. First calculate the building range if not cached yet. Then check for citizens
     * around the building. If no citizen around switch to default jobs. If citizens around check if food in inventory, if not, switch to gather job. If food in inventory switch to
     * serve job.
     *
     * @return the next IAIState to transfer to.
     */
    @Override
    protected IAIState checkForImportantJobs()
    {
        if (range == null)
        {
            range = getOwnBuilding().getTargetableArea(world);
        }

        citizenToServe.clear();
        final List<AbstractEntityCitizen> citizenList = world.getEntitiesWithinAABB(Entity.class, range)
                                                          .stream()
                                                          .filter(e -> e instanceof AbstractEntityCitizen)
                                                          .map(e -> (AbstractEntityCitizen) e)
                                                          .filter(cit -> !(cit.getCitizenJobHandler().getColonyJob() instanceof JobCook) && cit.shouldBeFed())
                                                          .sorted(Comparator.comparingInt(a -> (a.getCitizenJobHandler().getColonyJob() == null ? 1 : 0)))
                                                          .collect(Collectors.toList());

        final List<PlayerEntity> playerList = world.getEntitiesWithinAABB(PlayerEntity.class,
          range, player -> player != null && player.getFoodStats().getFoodLevel() < LEVEL_TO_FEED_PLAYER);

        if (!citizenList.isEmpty() || !playerList.isEmpty())
        {
            citizenToServe.addAll(citizenList);
            playerToServe.addAll(playerList);

            if (InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(), ItemStackUtils.CAN_EAT))
            {
                return COOK_SERVE_FOOD_TO_CITIZEN;
            }
            else if (!InventoryUtils.hasItemInProvider(getOwnBuilding(), ItemStackUtils.CAN_EAT))
            {
                return START_WORKING;
            }

            needsCurrently = new Tuple<>(ItemStackUtils.CAN_EAT, STACKSIZE);
            return GATHERING_REQUIRED_MATERIALS;
        }

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

        if (job.getActionsDone() > 0)
        {
            // Wait to dump before continuing.
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
        job.setMaxCraftingCount(currentRequest.getRequest().getCount());

        return QUERY_ITEMS;
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return 1;
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
            setDelay(TICKS_20);
            return START_WORKING;
        }

        return checkForItems(currentRecipeStorage);
    }

    /**
     * Check for all items of the required recipe.
     *
     * @return the next state to go to.
     */
    protected IAIState checkForItems(@NotNull final IRecipeStorage storage)
    {
        final List<ItemStorage> input = storage.getCleanedInput();
        for (final ItemStorage inputStorage : input)
        {
            final Predicate<ItemStack> predicate = stack -> !ItemStackUtils.isEmpty(stack) && new Stack(stack).matches(inputStorage.getItemStack());
            if (InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), predicate) < inputStorage.getAmount())
            {
                if (InventoryUtils.hasItemInProvider(getOwnBuilding(), predicate))
                {
                    needsCurrently = new Tuple<>(predicate, Constants.STACKSIZE);
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
        if (currentRecipeStorage == null)
        {
            return START_WORKING;
        }

        if (currentRequest == null)
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
            incrementActionsDone();
            currentRecipeStorage = null;
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
                    resetValues();
                    return START_WORKING;
                }

                currentRequest.addDelivery(currentRecipeStorage.getPrimaryOutput());
                job.setCraftCounter(job.getCraftCounter() + 1);

                if (job.getCraftCounter() >= job.getMaxCraftingCount())
                {
                    incrementActionsDone();
                    currentRecipeStorage = null;
                    resetValues();
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
            job.finishRequest(true);
            worker.getCitizenExperienceHandler().addExperience(currentRequest.getRequest().getCount() / 2.0);
            currentRequest = null;
        }

        resetValues();
        getOwnBuilding().setPickUpPriority(1);
        return super.afterDump();
    }

    @Override
    protected IRequestable getSmeltAbleClass()
    {
        return new Food(STACKSIZE);
    }

    /**
     * Get the required progress to execute a recipe.
     *
     * @return the amount of hits required.
     */
    private int getRequiredProgressForMakingRawMaterial()
    {
        return PROGRESS_MULTIPLIER / Math.min(worker.getCitizenData().getJobModifier() + 1, MAX_LEVEL) * HITTING_TIME;
    }
}
