package com.minecolonies.core.entity.ai.workers.crafting;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.StackList;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.statemachine.AIEventTarget;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.AIBlockingEventType;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.api.util.constant.translation.RequestSystemTranslationConstants;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.buildings.modules.FurnaceUserModule;
import com.minecolonies.core.colony.buildings.modules.ItemListModule;
import com.minecolonies.core.colony.interactionhandling.StandardInteraction;
import com.minecolonies.core.colony.jobs.AbstractJobCrafter;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIBasic;
import com.minecolonies.core.util.citizenutils.CitizenItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.ItemStackUtils.*;
import static com.minecolonies.api.util.constant.BuildingConstants.FUEL_LIST;
import static com.minecolonies.api.util.constant.CitizenConstants.TICKS_20;
import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.BAKER_HAS_NO_FURNACES_MESSAGE;
import static com.minecolonies.api.util.constant.TranslationConstants.FURNACE_USER_NO_FUEL;

/**
 * Crafts furnace stone related block when needed.
 */
public abstract class AbstractEntityAIRequestSmelter<J extends AbstractJobCrafter<?, J>, B extends AbstractBuilding> extends AbstractEntityAICrafting<J, B>
{
    /**
     * Base xp gain for the smelter.
     */
    private static final double BASE_XP_GAIN = 5;

    /**
     * Furnace to fuel
     */
    private BlockPos fuelPos = null;

    /**
     * State before we decided to fuel
     */
    private IAIState preFuelState = null;

    /**
     * Initialize the stone smeltery and add all his tasks.
     *
     * @param smelteryJob the job he has.
     */
    public AbstractEntityAIRequestSmelter(@NotNull final J smelteryJob)
    {
        super(smelteryJob);
        super.registerTargets(
          /*
           * Check if tasks should be executed.
           */
          new AIEventTarget(AIBlockingEventType.EVENT, this::isFuelNeeded, this::checkFurnaceFuel, TICKS_SECOND * 10),
          new AIEventTarget(AIBlockingEventType.EVENT, this::accelerateFurnaces, this::getState, TICKS_SECOND),
          new AITarget(START_USING_FURNACE, this::fillUpFurnace, TICKS_SECOND),
          new AITarget(RETRIEVING_END_PRODUCT_FROM_FURNACE, this::retrieveSmeltableFromFurnace, TICKS_SECOND),
          new AITarget(RETRIEVING_USED_FUEL_FROM_FURNACE, this::retrieveUsedFuel, TICKS_SECOND),
          new AITarget(ADD_FUEL_TO_FURNACE, this::addFuelToFurnace, TICKS_SECOND)
        );
    }

    @Override
    protected int getExtendedCount(final ItemStack stack)
    {
        if (currentRecipeStorage != null && currentRecipeStorage.getIntermediate() == Blocks.FURNACE)
        {
            int count = 0;
            for (final BlockPos pos : building.getFirstModuleOccurance(FurnaceUserModule.class).getFurnaces())
            {
                if (WorldUtil.isBlockLoaded(world, pos))
                {
                    final BlockEntity entity = world.getBlockEntity(pos);
                    if (entity instanceof FurnaceBlockEntity)
                    {
                        final FurnaceBlockEntity furnace = (FurnaceBlockEntity) entity;

                        final ItemStack smeltableSlot = furnace.getItem(SMELTABLE_SLOT);
                        final ItemStack resultSlot = furnace.getItem(RESULT_SLOT);
                        if (ItemStackUtils.compareItemStacksIgnoreStackSize(stack, smeltableSlot))
                        {
                            count += smeltableSlot.getCount();
                        }
                        else if (ItemStackUtils.compareItemStacksIgnoreStackSize(stack, resultSlot))
                        {
                            count += resultSlot.getCount();
                        }
                    }
                }
            }

            return count;
        }

        return 0;
    }

    @Override
    protected IAIState getRecipe()
    {
        final IRequest<? extends PublicCrafting> currentTask = job.getCurrentTask();

        if (currentTask == null)
        {
            worker.setItemInHand(InteractionHand.MAIN_HAND, ItemStackUtils.EMPTY);
            return START_WORKING;
        }

        job.setMaxCraftingCount(currentTask.getRequest().getCount());

        final BlockPos furnacePosWithUsedFuel = getPositionOfOvenToRetrieveFuelFrom();
        if (furnacePosWithUsedFuel != null)
        {
            currentRequest = currentTask;
            walkTo = furnacePosWithUsedFuel;
            return RETRIEVING_USED_FUEL_FROM_FURNACE;
        }

        final BlockPos furnacePos = getPositionOfOvenToRetrieveFrom();
        if (furnacePos != null)
        {
            currentRequest = currentTask;
            walkTo = furnacePos;
            return RETRIEVING_END_PRODUCT_FROM_FURNACE;
        }

        if (currentRecipeStorage != null && currentRecipeStorage.getIntermediate() == Blocks.FURNACE)
        {
            for (final BlockPos pos : building.getFirstModuleOccurance(FurnaceUserModule.class).getFurnaces())
            {
                final BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof FurnaceBlockEntity)
                {
                    final FurnaceBlockEntity furnace = (FurnaceBlockEntity) entity;
                    if (furnace.isLit() || !isEmpty(furnace.getItem(RESULT_SLOT)) || !isEmpty(furnace.getItem(SMELTABLE_SLOT)))
                    {
                        if (furnace.isLit())
                        {
                            setDelay(TICKS_20);
                        }
                        return CRAFT;
                    }
                }
            }
        }

        final IAIState newState = super.getRecipe();

        final ItemListModule module = building.getModuleMatching(ItemListModule.class, m -> m.getId().equals(FUEL_LIST));

        // This should only happen in the stonesmelter, but it could potentially happen with multiple fuels.
        if (newState == QUERY_ITEMS && currentRecipeStorage != null && module.isItemInList(new ItemStorage(currentRecipeStorage.getPrimaryOutput())))
        {
            job.setCraftCounter(0);
        }

        return newState;
    }

    /**
     * Check to see how many furnaces are still processing
     *
     * @return the count.
     */
    private int countOfBurningFurnaces()
    {
        int count = 0;
        final Level world = building.getColony().getWorld();
        for (final BlockPos pos : building.getFirstModuleOccurance(FurnaceUserModule.class).getFurnaces())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof FurnaceBlockEntity)
                {
                    final FurnaceBlockEntity furnace = (FurnaceBlockEntity) entity;
                    if (furnace.isLit())
                    {
                        count += 1;
                    }
                }
            }
        }
        return count;
    }

    /**
     * Actually accelerate the furnaces
     */
    private boolean accelerateFurnaces()
    {
        final int accelerationTicks = (worker.getCitizenData().getCitizenSkillHandler().getLevel(getModuleForJob().getSecondarySkill()) / 10) * 2;
        final Level world = building.getColony().getWorld();
        for (final BlockPos pos : building.getFirstModuleOccurance(FurnaceUserModule.class).getFurnaces())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof FurnaceBlockEntity)
                {
                    final FurnaceBlockEntity furnace = (FurnaceBlockEntity) entity;
                    for (int i = 0; i < accelerationTicks; i++)
                    {
                        if (furnace.isLit())
                        {
                            FurnaceBlockEntity.serverTick(entity.getLevel(), entity.getBlockPos(), entity.getBlockState(), furnace);
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Get the list of possible fuels, adjusted for any inputs/outputs of the current recipe to avoid interference
     */
    private List<ItemStack> getActivePossibleFuels()
    {
        final List<ItemStack> possibleFuels = getAllowedFuel();
        if (possibleFuels.isEmpty())
        {
            if (worker.getCitizenData() != null)
            {
                worker.getCitizenData().triggerInteraction(new StandardInteraction(Component.translatable(FURNACE_USER_NO_FUEL), ChatPriority.IMPORTANT));
            }
            return ImmutableList.of();
        }

        if (currentRecipeStorage != null)
        {
            possibleFuels.removeIf(stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, currentRecipeStorage.getPrimaryOutput()));
            // There is always only one input.
            possibleFuels.removeIf(stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, currentRecipeStorage.getCleanedInput().get(0).getItemStack()));
        }
        return possibleFuels;
    }

    /**
     * Quick check to see if there is a furnace that needs fuel
     */
    private boolean isFuelNeeded()
    {
        final FurnaceUserModule module = building.getFirstModuleOccurance(FurnaceUserModule.class);
        for (final BlockPos pos : module.getFurnaces())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final BlockEntity entity = world.getBlockEntity(pos);
                if (!(entity instanceof FurnaceBlockEntity))
                {
                    module.removeFromFurnaces(pos);
                    continue;
                }
                final FurnaceBlockEntity furnace = (FurnaceBlockEntity) entity;
                if (!furnace.isLit() && (hasSmeltableInFurnaceAndNoFuel(furnace) || hasNeitherFuelNorSmeltAble(furnace)) && currentRecipeStorage != null
                      && currentRecipeStorage.getIntermediate() == Blocks.FURNACE)
                {
                    //We only want to return true if we're not already gathering materials.
                    return getState() != GATHERING_REQUIRED_MATERIALS;
                }
            }
        }
        return false;
    }

    /**
     * Predicate for checking fuel in inventories
     */
    private static Predicate<ItemStack> isCorrectFuel(final List<ItemStack> possibleFuels)
    {
        return item -> ItemStackUtils.compareItemStackListIgnoreStackSize(possibleFuels, item);
    }

    /**
     * Check Fuel levels in the furnace
     */
    private IAIState checkFurnaceFuel()
    {
        final Level world = building.getColony().getWorld();
        final List<ItemStack> possibleFuels = getActivePossibleFuels();

        final FurnaceUserModule module = building.getFirstModuleOccurance(FurnaceUserModule.class);
        if (!InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(), isCorrectFuel(possibleFuels)) && !InventoryUtils.hasItemInProvider(building,
          isCorrectFuel(possibleFuels)) && !building.hasWorkerOpenRequestsOfType(worker.getCitizenData().getId(), TypeToken.of(StackList.class)) && currentRecipeStorage != null
              && currentRecipeStorage.getIntermediate() == Blocks.FURNACE)
        {
            worker.getCitizenData()
              .createRequestAsync(new StackList(possibleFuels, RequestSystemTranslationConstants.REQUESTS_TYPE_BURNABLE, STACKSIZE * module.getFurnaces().size(), 1));
            return getState();
        }

        for (final BlockPos pos : module.getFurnaces())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof FurnaceBlockEntity)
                {
                    final FurnaceBlockEntity furnace = (FurnaceBlockEntity) entity;
                    if (!furnace.isLit() && (hasSmeltableInFurnaceAndNoFuel(furnace) || hasNeitherFuelNorSmeltAble(furnace)) && currentRecipeStorage != null
                          && currentRecipeStorage.getIntermediate() == Blocks.FURNACE)
                    {
                        if (!InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(), isCorrectFuel(possibleFuels)))
                        {
                            if (InventoryUtils.hasItemInProvider(building, isCorrectFuel(possibleFuels)))
                            {
                                needsCurrently = new Tuple<>(isCorrectFuel(possibleFuels), STACKSIZE);
                                walkTo = null; // This could be set to a furnace at this point, and gathering requires it to be null, to find the right rack
                                return GATHERING_REQUIRED_MATERIALS;
                            }
                            //We need to wait for Fuel to arrive
                            return getState();
                        }

                        fuelPos = pos;
                        if (preFuelState == null)
                        {
                            preFuelState = getState();
                        }
                        return ADD_FUEL_TO_FURNACE;
                    }
                }
            }
        }
        return getState();
    }

    /**
     * Add furnace fuel when necessary
     *
     * @return
     */
    private IAIState addFuelToFurnace()
    {
        final List<ItemStack> possibleFuels = getActivePossibleFuels();

        if (!InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(), isCorrectFuel(possibleFuels)))
        {
            if (InventoryUtils.hasItemInProvider(building, isCorrectFuel(possibleFuels)))
            {
                needsCurrently = new Tuple<>(isCorrectFuel(possibleFuels), STACKSIZE);
                return GATHERING_REQUIRED_MATERIALS;
            }
            //We shouldn't get here, unless something changed between the checkFurnaceFuel and the addFueltoFurnace calls
            preFuelState = null;
            fuelPos = null;
            return START_WORKING;
        }

        if (fuelPos == null || walkToBlock(fuelPos))
        {
            return getState();
        }

        if (WorldUtil.isBlockLoaded(world, fuelPos))
        {
            final BlockEntity entity = world.getBlockEntity(fuelPos);
            if (entity instanceof FurnaceBlockEntity)
            {
                final FurnaceBlockEntity furnace = (FurnaceBlockEntity) entity;
                //Stoke the furnaces
                if (InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(), isCorrectFuel(possibleFuels))
                      && (hasSmeltableInFurnaceAndNoFuel(furnace) || hasNeitherFuelNorSmeltAble(furnace)))
                {
                    InventoryUtils.transferXOfFirstSlotInItemHandlerWithIntoInItemHandler(
                      worker.getInventoryCitizen(), isCorrectFuel(possibleFuels), STACKSIZE,
                      new InvWrapper(furnace), FUEL_SLOT);
                    if (preFuelState != null && preFuelState != ADD_FUEL_TO_FURNACE)
                    {
                        IAIState returnState = preFuelState;
                        preFuelState = null;
                        fuelPos = null;
                        return returnState;
                    }
                }
            }
        }

        //Fueling is confused, start over. 
        preFuelState = null;
        fuelPos = null;
        return START_WORKING;
    }

    private int getMaxUsableFurnaces()
    {
        final int maxSkillFurnaces = (worker.getCitizenData().getCitizenSkillHandler().getLevel(getModuleForJob().getPrimarySkill()) / 10) + 1;
        return Math.min(maxSkillFurnaces, building.getFirstModuleOccurance(FurnaceUserModule.class).getFurnaces().size());
    }

    /**
     * Get the furnace which has finished smeltables. For this check each furnace which has been registered to the building. Check if the furnace is turned off and has something in
     * the result slot or check if the furnace has more than x results.
     *
     * @return the position of the furnace.
     */
    private BlockPos getPositionOfOvenToRetrieveFrom()
    {
        for (final BlockPos pos : building.getFirstModuleOccurance(FurnaceUserModule.class).getFurnaces())
        {
            final BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof FurnaceBlockEntity)
            {
                final FurnaceBlockEntity furnace = (FurnaceBlockEntity) entity;
                int countInResultSlot = 0;
                boolean fullResult = false;
                if (!isEmpty(furnace.getItem(RESULT_SLOT)))
                {
                    countInResultSlot = furnace.getItem(RESULT_SLOT).getCount();
                    fullResult = countInResultSlot >= furnace.getItem(RESULT_SLOT).getMaxStackSize();
                }

                if (fullResult || (!furnace.isLit() && countInResultSlot > 0 && isEmpty(furnace.getItem(SMELTABLE_SLOT))))
                {
                    return pos;
                }
            }
        }
        return null;
    }

    /**
     * Get the furnace which has used fuel. For this check each furnace which has been registered to the building. Check if the furnace has used fuel in the fuel slot.
     *
     * @return the position of the furnace.
     */
    protected BlockPos getPositionOfOvenToRetrieveFuelFrom()
    {
        for (final BlockPos pos : building.getFirstModuleOccurance(FurnaceUserModule.class).getFurnaces())
        {
            final BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof FurnaceBlockEntity)
            {
                final FurnaceBlockEntity furnace = (FurnaceBlockEntity) entity;

                if (!furnace.getItem(FUEL_SLOT).isEmpty() && !compareItemStackListIgnoreStackSize(getAllowedFuel(), furnace.getItem(FUEL_SLOT), false, false))
                {
                    return pos;
                }
            }
        }
        return null;
    }

    @Override
    protected IAIState checkForItems(@NotNull final IRecipeStorage storage)
    {
        if (storage.getIntermediate() != Blocks.FURNACE)
        {
            return super.checkForItems(storage);
        }

        final List<ItemStorage> input = storage.getCleanedInput();
        final int countInFurnaces = getExtendedCount(storage.getPrimaryOutput());
        int outputInInv =
          InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, storage.getPrimaryOutput()));

        for (final ItemStorage inputStorage : input)
        {
            final Predicate<ItemStack> predicate = stack -> !ItemStackUtils.isEmpty(stack) && ItemStackUtils.compareItemStacksIgnoreStackSize(stack, inputStorage.getItemStack());
            int inputInFurnace = getExtendedCount(inputStorage.getItemStack());
            int inputInInv = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), predicate);

            if (countInFurnaces + inputInFurnace + inputInInv + outputInInv < inputStorage.getAmount() * job.getMaxCraftingCount())
            {
                if (InventoryUtils.hasItemInProvider(building, predicate))
                {
                    needsCurrently = new Tuple<>(predicate, inputStorage.getAmount() * (job.getMaxCraftingCount() - countInFurnaces - inputInFurnace));
                    return GATHERING_REQUIRED_MATERIALS;
                }
            }

            //if we don't have enough at all, cancel
            int countOfInput = inputInInv + InventoryUtils.getCountFromBuilding(building, predicate) + countInFurnaces + inputInFurnace + outputInInv;
            if (countOfInput < inputStorage.getAmount() * job.getMaxCraftingCount())
            {
                job.finishRequest(false);
                resetValues();
            }
        }

        return CRAFT;
    }

    /**
     * Retrieve ready bars from the furnaces. If no position has been set return. Else navigate to the position of the furnace. On arrival execute the extract method of the
     * specialized worker.
     *
     * @return the next state to go to.
     */
    private IAIState retrieveSmeltableFromFurnace()
    {
        if (walkTo == null || currentRequest == null)
        {
            return START_WORKING;
        }

        final BlockEntity entity = world.getBlockEntity(walkTo);
        if (!(entity instanceof FurnaceBlockEntity) || (isEmpty(((FurnaceBlockEntity) entity).getItem(RESULT_SLOT))))
        {
            walkTo = null;
            return START_WORKING;
        }

        if (walkToBlock(walkTo))
        {
            return getState();
        }
        walkTo = null;

        final int preExtractCount = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(),
          stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(currentRequest.getRequest().getStack(), stack));

        extractFromFurnaceSlot((FurnaceBlockEntity) entity, RESULT_SLOT);
        //Do we have the requested item in the inventory now?
        final int resultCount = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(),
          stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(currentRequest.getRequest().getStack(), stack)) - preExtractCount;
        if (resultCount > 0)
        {
            final ItemStack stack = currentRequest.getRequest().getStack().copy();
            stack.setCount(resultCount);
            currentRequest.addDelivery(stack);

            job.setCraftCounter(job.getCraftCounter() + resultCount);
            job.setProgress(job.getProgress() - resultCount);
            if (job.getMaxCraftingCount() == 0)
            {
                job.setMaxCraftingCount(currentRequest.getRequest().getCount());
            }
            if (job.getCraftCounter() >= job.getMaxCraftingCount() && job.getProgress() <= 0)
            {
                job.finishRequest(true);
                resetValues();
                currentRecipeStorage = null;
                incrementActionsDoneAndDecSaturation();
                return INVENTORY_FULL;
            }
        }

        setDelay(AbstractEntityAIBasic.STANDARD_DELAY);
        return START_WORKING;
    }

    /**
     * Retrieve used fuel from the furnaces. If no position has been set return. Else navigate to the position of the furnace. On arrival execute the extract method of the
     * specialized worker.
     *
     * @return the next state to go to.
     */
    private IAIState retrieveUsedFuel()
    {
        if (walkTo == null)
        {
            return START_WORKING;
        }

        if (walkToBlock(walkTo))
        {
            return getState();
        }

        final BlockEntity entity = world.getBlockEntity(walkTo);
        if (!(entity instanceof FurnaceBlockEntity)
              || (ItemStackUtils.isEmpty(((FurnaceBlockEntity) entity).getItem(FUEL_SLOT))))
        {
            walkTo = null;
            return START_WORKING;
        }

        walkTo = null;

        extractFromFurnaceSlot((FurnaceBlockEntity) entity, FUEL_SLOT);
        return START_WORKING;
    }

    /**
     * Very simple action, straightly extract from a furnace slot.
     *
     * @param furnace the furnace to retrieve from.
     */
    private void extractFromFurnaceSlot(final FurnaceBlockEntity furnace, final int slot)
    {
        InventoryUtils.transferItemStackIntoNextFreeSlotInItemHandler(
          new InvWrapper(furnace), slot,
          worker.getInventoryCitizen());
        if (slot == RESULT_SLOT)
        {
            worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
        }
    }

    /**
     * Checks if the furnaces are ready to start smelting.
     *
     * @return START_USING_FURNACE if enough, else check for additional worker specific jobs.
     */
    private IAIState checkIfAbleToSmelt()
    {
        // We're fully committed currently, try again later.
        final int burning = countOfBurningFurnaces();
        if (burning > 0 && (burning >= getMaxUsableFurnaces() || (job.getCraftCounter() + job.getProgress()) >= job.getMaxCraftingCount()))
        {
            setDelay(TICKS_SECOND);
            return getState();
        }

        final FurnaceUserModule module = building.getFirstModuleOccurance(FurnaceUserModule.class);
        for (final BlockPos pos : module.getFurnaces())
        {
            final BlockEntity entity = world.getBlockEntity(pos);

            if (entity instanceof FurnaceBlockEntity)
            {
                if (isEmpty(((FurnaceBlockEntity) entity).getItem(SMELTABLE_SLOT)))
                {
                    walkTo = pos;
                    return START_USING_FURNACE;
                }
            }
            else
            {
                if (!(world.getBlockState(pos).getBlock() instanceof FurnaceBlock))
                {
                    module.removeFromFurnaces(pos);
                }
            }
        }

        if (burning > 0)
        {
            setDelay(TICKS_SECOND);
        }

        return getState();
    }

    /**
     * Smelt the smeltable after the required items are in the inv.
     *
     * @return the next state to go to.
     */
    private IAIState fillUpFurnace()
    {
        final FurnaceUserModule module = building.getFirstModuleOccurance(FurnaceUserModule.class);
        if (module.getFurnaces().isEmpty())
        {
            if (worker.getCitizenData() != null)
            {
                worker.getCitizenData().triggerInteraction(new StandardInteraction(Component.translatable(BAKER_HAS_NO_FURNACES_MESSAGE), ChatPriority.BLOCKING));
            }
            setDelay(AbstractEntityAIBasic.STANDARD_DELAY);
            return START_WORKING;
        }

        if (walkTo == null || world.getBlockState(walkTo).getBlock() != Blocks.FURNACE)
        {
            walkTo = null;
            setDelay(AbstractEntityAIBasic.STANDARD_DELAY);
            return START_WORKING;
        }

        final int burningCount = countOfBurningFurnaces();
        final BlockEntity entity = world.getBlockEntity(walkTo);
        if (entity instanceof FurnaceBlockEntity && currentRecipeStorage != null)
        {
            final FurnaceBlockEntity furnace = (FurnaceBlockEntity) entity;
            final int maxFurnaces = getMaxUsableFurnaces();
            final Predicate<ItemStack> smeltable = stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(currentRecipeStorage.getCleanedInput().get(0).getItemStack(), stack);
            final int smeltableInFurnaces = getExtendedCount(currentRecipeStorage.getCleanedInput().get(0).getItemStack());
            final int resultInFurnaces = getExtendedCount(currentRecipeStorage.getPrimaryOutput());
            final int resultInCitizenInv = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(),
              stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, currentRecipeStorage.getPrimaryOutput()));

            final int targetCount = currentRequest.getRequest().getCount() - smeltableInFurnaces - resultInFurnaces - resultInCitizenInv;

            if (targetCount <= 0)
            {
                return START_WORKING;
            }
            final int amountOfSmeltableInBuilding = InventoryUtils.getCountFromBuilding(building, smeltable);
            final int amountOfSmeltableInInv = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), smeltable);

            if (worker.getItemInHand(InteractionHand.MAIN_HAND).isEmpty())
            {
                worker.setItemInHand(InteractionHand.MAIN_HAND, currentRecipeStorage.getCleanedInput().get(0).getItemStack().copy());
            }
            if (amountOfSmeltableInInv > 0)
            {
                if (hasFuelInFurnaceAndNoSmeltable(furnace) || hasNeitherFuelNorSmeltAble(furnace))
                {
                    int toTransfer = 0;
                    if (burningCount < maxFurnaces)
                    {
                        final int availableFurnaces = maxFurnaces - burningCount;

                        if (targetCount > STACKSIZE * availableFurnaces)
                        {
                            toTransfer = STACKSIZE;
                        }
                        else
                        {
                            //We need to split stacks and spread them across furnaces for best performance
                            //We will front-load the remainder
                            toTransfer = Math.min((targetCount / availableFurnaces) + (targetCount % availableFurnaces), STACKSIZE);
                        }
                    }
                    if (toTransfer > 0)
                    {
                        if (walkToBlock(walkTo))
                        {
                            return getState();
                        }
                        CitizenItemUtils.hitBlockWithToolInHand(worker, walkTo);
                        InventoryUtils.transferXInItemHandlerIntoSlotInItemHandler(
                          worker.getInventoryCitizen(),
                          smeltable,
                          toTransfer,
                          new InvWrapper(furnace),
                          SMELTABLE_SLOT);
                    }
                }
            }
            else if (amountOfSmeltableInBuilding >= targetCount - amountOfSmeltableInInv
                       && currentRecipeStorage.getIntermediate() == Blocks.FURNACE)
            {
                needsCurrently = new Tuple<>(smeltable, targetCount);
                return GATHERING_REQUIRED_MATERIALS;
            }
            else
            {
                //This is a safety net for the AI getting way out of sync with it's tracking. It shouldn't happen.
                job.finishRequest(false);
                resetValues();
                walkTo = null;
                return IDLE;
            }
        }
        else if (!(world.getBlockState(walkTo).getBlock() instanceof FurnaceBlock))
        {
            module.removeFromFurnaces(walkTo);
        }
        walkTo = null;
        setDelay(AbstractEntityAIBasic.STANDARD_DELAY);
        return START_WORKING;
    }

    /**
     * Get a copy of the list of allowed fuel.
     *
     * @return the list.
     */
    private List<ItemStack> getAllowedFuel()
    {
        final List<ItemStack> list = new ArrayList<>();
        for (final ItemStorage storage : building.getModuleMatching(ItemListModule.class, m -> m.getId().equals(FUEL_LIST)).getList())
        {
            final ItemStack stack = storage.getItemStack().copy();
            stack.setCount(stack.getMaxStackSize());
            list.add(stack);
        }
        return list;
    }

    @Override
    protected IAIState craft()
    {
        final FurnaceUserModule module = building.getFirstModuleOccurance(FurnaceUserModule.class);
        final List<ItemStack> possibleFuels = getAllowedFuel();
        if (possibleFuels.isEmpty())
        {
            if (worker.getCitizenData() != null)
            {
                worker.getCitizenData().triggerInteraction(new StandardInteraction(Component.translatable(FURNACE_USER_NO_FUEL), ChatPriority.BLOCKING));
            }
            return getState();
        }

        if (currentRecipeStorage != null)
        {
            possibleFuels.removeIf(stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, currentRecipeStorage.getPrimaryOutput()));
            // There is always only one input.
            possibleFuels.removeIf(stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, currentRecipeStorage.getCleanedInput().get(0).getItemStack()));
        }

        if (walkToBuilding())
        {
            setDelay(AbstractEntityAIBasic.STANDARD_DELAY);
            return getState();
        }

        if (currentRecipeStorage != null && currentRequest == null)
        {
            currentRequest = job.getCurrentTask();
        }

        if (currentRecipeStorage != null && currentRecipeStorage.getIntermediate() != Blocks.FURNACE)
        {
            return super.craft();
        }

        if (module.getFurnaces().isEmpty())
        {
            if (worker.getCitizenData() != null)
            {
                worker.getCitizenData()
                  .triggerInteraction(new StandardInteraction(Component.translatable(BAKER_HAS_NO_FURNACES_MESSAGE), ChatPriority.BLOCKING));
            }
            setDelay(AbstractEntityAIBasic.STANDARD_DELAY);
            return START_WORKING;
        }

        final BlockPos furnacePosWithUsedFuel = getPositionOfOvenToRetrieveFuelFrom();
        if (furnacePosWithUsedFuel != null)
        {
            walkTo = furnacePosWithUsedFuel;
            return RETRIEVING_USED_FUEL_FROM_FURNACE;
        }

        final BlockPos posOfOven = getPositionOfOvenToRetrieveFrom();
        if (posOfOven != null)
        {
            walkTo = posOfOven;
            return RETRIEVING_END_PRODUCT_FROM_FURNACE;
        }

        // Safety net, should get caught removing things from the furnace.
        if (currentRequest != null && job.getMaxCraftingCount() > 0 && job.getCraftCounter() >= job.getMaxCraftingCount())
        {
            job.finishRequest(true);
            currentRecipeStorage = null;
            currentRequest = null;
            resetValues();
            return INVENTORY_FULL;
        }

        if (currentRequest != null && (currentRequest.getState() == RequestState.CANCELLED || currentRequest.getState() == RequestState.FAILED))
        {
            incrementActionsDone(getActionRewardForCraftingSuccess());
            currentRecipeStorage = null;
            currentRequest = null;
            resetValues();
            return START_WORKING;
        }

        return checkIfAbleToSmelt();
    }
}
