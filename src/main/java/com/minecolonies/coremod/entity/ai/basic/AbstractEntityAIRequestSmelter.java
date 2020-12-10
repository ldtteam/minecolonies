package com.minecolonies.coremod.entity.ai.basic;

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
import com.minecolonies.coremod.colony.buildings.AbstractBuildingSmelterCrafter;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteraction;
import com.minecolonies.coremod.colony.jobs.AbstractJobCrafter;
import net.minecraft.block.Blocks;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.ItemStackUtils.*;
import static com.minecolonies.api.util.constant.CitizenConstants.TICKS_20;
import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Crafts furnace stone related block when needed.
 */
public abstract class AbstractEntityAIRequestSmelter<J extends AbstractJobCrafter<?, J>, B extends AbstractBuildingSmelterCrafter> extends AbstractEntityAICrafting<J, B>
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
          new AITarget(ADD_FUEL_TO_FURNACE, this::addFuelToFurnace, TICKS_SECOND)
        );
    }

    @Override
    protected int getExtendedCount(final ItemStack stack)
    {
        if (currentRecipeStorage != null && currentRecipeStorage.getIntermediate() == Blocks.FURNACE)
        {
            int count = 0;
            for (final BlockPos pos : getOwnBuilding().getFurnaces())
            {
                if (WorldUtil.isBlockLoaded(world, pos))
                {
                    final TileEntity entity = world.getTileEntity(pos);
                    if (entity instanceof FurnaceTileEntity)
                    {
                        final FurnaceTileEntity furnace = (FurnaceTileEntity) entity;

                        final ItemStack smeltableSlot = furnace.getStackInSlot(SMELTABLE_SLOT);
                        final ItemStack resultSlot = furnace.getStackInSlot(RESULT_SLOT);
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
            worker.setHeldItem(Hand.MAIN_HAND, ItemStackUtils.EMPTY);
            return START_WORKING;
        }

        job.setMaxCraftingCount(currentTask.getRequest().getCount());
        final BlockPos furnacePos = getPositionOfOvenToRetrieveFrom();
        if (furnacePos != null)
        {
            currentRequest = currentTask;
            walkTo = furnacePos;
            return RETRIEVING_END_PRODUCT_FROM_FURNACE;
        }

        if(currentRecipeStorage != null && currentRecipeStorage.getIntermediate() == Blocks.FURNACE)
        {
            for (final BlockPos pos : getOwnBuilding().getFurnaces())
            {
                final TileEntity entity = world.getTileEntity(pos);
                if (entity instanceof FurnaceTileEntity)
                {
                    final FurnaceTileEntity furnace = (FurnaceTileEntity) entity;
                    if (furnace.isBurning() || !isEmpty(furnace.getStackInSlot(RESULT_SLOT)) || !isEmpty(furnace.getStackInSlot(SMELTABLE_SLOT)))
                    {
                        if (furnace.isBurning())
                        {
                            setDelay(TICKS_20);
                        }
                        return CRAFT;
                    }
                }
            }
        }

        final IAIState newState = super.getRecipe();

        // This should only happen in the stonesmelter, but it could potentially happen with multiple fuels. 
        if(newState == QUERY_ITEMS && currentRecipeStorage != null && !getOwnBuilding().getAllowedFuel().isEmpty() && getOwnBuilding().isAllowedFuel(currentRecipeStorage.getPrimaryOutput()))
        {
            job.setCraftCounter(0);
        }

        return newState;
    }

    /**
     * Check to see how many furnaces are still processing
     * @return the count.
     */
    private int countOfBurningFurnaces()
    {
        int count = 0;
        final World world = getOwnBuilding().getColony().getWorld();
        for (final BlockPos pos : getOwnBuilding().getFurnaces())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final TileEntity entity = world.getTileEntity(pos);
                if (entity instanceof FurnaceTileEntity)
                {
                    final FurnaceTileEntity furnace = (FurnaceTileEntity) entity;
                    if (furnace.isBurning()) 
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
        final int accelerationTicks = (worker.getCitizenData().getCitizenSkillHandler().getLevel(getOwnBuilding().getSecondarySkill()) / 10) * 2;
        final World world = getOwnBuilding().getColony().getWorld();
        for (final BlockPos pos : getOwnBuilding().getFurnaces())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final TileEntity entity = world.getTileEntity(pos);
                if (entity instanceof FurnaceTileEntity)
                {
                    final FurnaceTileEntity furnace = (FurnaceTileEntity) entity;
                    for(int i = 0; i < accelerationTicks; i++)
                    {
                        if (furnace.isBurning()) 
                        {
                            furnace.tick();
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
        final List<ItemStack> possibleFuels = getOwnBuilding().getAllowedFuel();
        if (possibleFuels.isEmpty())
        {
            if (worker.getCitizenData() != null)
            {
                worker.getCitizenData().triggerInteraction(new StandardInteraction(new TranslationTextComponent(FURNACE_USER_NO_FUEL), ChatPriority.IMPORTANT));
            }
            return ImmutableList.of();
        }

        if(currentRecipeStorage != null)
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
        for (final BlockPos pos : getOwnBuilding().getFurnaces())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final TileEntity entity = world.getTileEntity(pos);
                if(!(entity instanceof FurnaceTileEntity))
                {
                    getOwnBuilding().removeFromFurnaces(pos);
                    continue;
                }
                final FurnaceTileEntity furnace = (FurnaceTileEntity) entity;
                if (!furnace.isBurning() && (hasSmeltableInFurnaceAndNoFuel(furnace) || hasNeitherFuelNorSmeltAble(furnace)) && currentRecipeStorage != null && currentRecipeStorage.getIntermediate() == Blocks.FURNACE) 
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
        final World world = getOwnBuilding().getColony().getWorld();
        final List<ItemStack> possibleFuels = getActivePossibleFuels();

        if(!InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(),  isCorrectFuel(possibleFuels)) && !InventoryUtils.hasItemInProvider(getOwnBuilding(), isCorrectFuel(possibleFuels)) && !getOwnBuilding().hasWorkerOpenRequestsOfType(worker.getCitizenData(), TypeToken.of(StackList.class)) && currentRecipeStorage != null && currentRecipeStorage.getIntermediate() == Blocks.FURNACE )
        {
            worker.getCitizenData().createRequestAsync(new StackList(possibleFuels, COM_MINECOLONIES_REQUESTS_BURNABLE, STACKSIZE * getOwnBuilding().getFurnaces().size(), 1));
            return getState();
        }

        for (final BlockPos pos : getOwnBuilding().getFurnaces())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final TileEntity entity = world.getTileEntity(pos);
                if (entity instanceof FurnaceTileEntity)
                {
                    final FurnaceTileEntity furnace = (FurnaceTileEntity) entity;
                    if (!furnace.isBurning() && (hasSmeltableInFurnaceAndNoFuel(furnace) || hasNeitherFuelNorSmeltAble(furnace)) && currentRecipeStorage != null && currentRecipeStorage.getIntermediate() == Blocks.FURNACE) 
                    {
                        if (!InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(),  isCorrectFuel(possibleFuels)))
                        {
                            if(InventoryUtils.hasItemInProvider(getOwnBuilding(), isCorrectFuel(possibleFuels)))
                            {
                                needsCurrently = new Tuple<>(isCorrectFuel(possibleFuels), STACKSIZE);
                                walkTo = null; // This could be set to a furnace at this point, and gathering requires it to be null, to find the right rack
                                return GATHERING_REQUIRED_MATERIALS;
                            }
                            //We need to wait for Fuel to arrive
                            return getState();
                        }

                        fuelPos = pos;
                        if(preFuelState == null)
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
     * @return
     */
    private IAIState addFuelToFurnace()
    {
        final List<ItemStack> possibleFuels = getActivePossibleFuels();

        if(!InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(),  isCorrectFuel(possibleFuels)))
        {
            if (InventoryUtils.hasItemInProvider(getOwnBuilding(), isCorrectFuel(possibleFuels)))
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
            final TileEntity entity = world.getTileEntity(fuelPos);
            if (entity instanceof FurnaceTileEntity)
            {
                final FurnaceTileEntity furnace = (FurnaceTileEntity) entity;
                //Stoke the furnaces
                if (InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(), isCorrectFuel(possibleFuels))
                        && (hasSmeltableInFurnaceAndNoFuel(furnace) || hasNeitherFuelNorSmeltAble(furnace)))
                {
                    InventoryUtils.transferXOfFirstSlotInItemHandlerWithIntoInItemHandler(
                        worker.getInventoryCitizen(), isCorrectFuel(possibleFuels), STACKSIZE,
                        new InvWrapper(furnace), FUEL_SLOT);
                    if(preFuelState != null && preFuelState != ADD_FUEL_TO_FURNACE)
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
        final int maxSkillFurnaces = (worker.getCitizenData().getCitizenSkillHandler().getLevel(getOwnBuilding().getPrimarySkill()) / 10) + 1;
        return Math.min(maxSkillFurnaces, getOwnBuilding().getFurnaces().size());
    }

    /**
     * Get the furnace which has finished smeltables. For this check each furnace which has been registered to the building. Check if the furnace is turned off and has something in
     * the result slot or check if the furnace has more than x results.
     *
     * @return the position of the furnace.
     */
    private BlockPos getPositionOfOvenToRetrieveFrom()
    {
        for (final BlockPos pos : getOwnBuilding().getFurnaces())
        {
            final TileEntity entity = world.getTileEntity(pos);
            if (entity instanceof FurnaceTileEntity)
            {
                final FurnaceTileEntity furnace = (FurnaceTileEntity) entity;
                int countInResultSlot = 0;
                boolean fullResult = false;
                if (!isEmpty(furnace.getStackInSlot(RESULT_SLOT)))
                {
                    countInResultSlot = furnace.getStackInSlot(RESULT_SLOT).getCount();
                    fullResult = countInResultSlot >= furnace.getStackInSlot(RESULT_SLOT).getMaxStackSize();
                }

                if (fullResult || (!furnace.isBurning() && countInResultSlot > 0 && isEmpty(furnace.getStackInSlot(SMELTABLE_SLOT))))
                {
                    worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent(COM_MINECOLONIES_COREMOD_STATUS_RETRIEVING));
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
        int outputInInv = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, storage.getPrimaryOutput()));

        for (final ItemStorage inputStorage : input)
        {
            final Predicate<ItemStack> predicate = stack -> !ItemStackUtils.isEmpty(stack) && ItemStackUtils.compareItemStacksIgnoreStackSize(stack, inputStorage.getItemStack());
            int inputInFurnace = getExtendedCount(inputStorage.getItemStack());
            int inputInInv = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), predicate);

            if (countInFurnaces + inputInFurnace + inputInInv + outputInInv < inputStorage.getAmount() * job.getMaxCraftingCount())
            {
                if (InventoryUtils.hasItemInProvider(getOwnBuilding(), predicate))
                {
                    needsCurrently = new Tuple<>(predicate, inputStorage.getAmount() * (job.getMaxCraftingCount() - countInFurnaces - inputInFurnace));
                    return GATHERING_REQUIRED_MATERIALS;
                }
            }

            //if we don't have enough at all, cancel
            int countOfInput = inputInInv + InventoryUtils.getCountFromBuilding(getOwnBuilding(), predicate) + countInFurnaces + inputInFurnace + outputInInv;
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
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent(COM_MINECOLONIES_COREMOD_STATUS_RETRIEVING));

        if (walkTo == null || currentRequest == null)
        {
            return START_WORKING;
        }

        final TileEntity entity = world.getTileEntity(walkTo);
        if (!(entity instanceof FurnaceTileEntity) || (isEmpty(((FurnaceTileEntity) entity).getStackInSlot(RESULT_SLOT))))
        {
            walkTo = null;
            return START_WORKING;
        }

        if (walkToBlock(walkTo))
        {
            return getState();
        }
        walkTo = null;

        final int preExtractCount = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(currentRequest.getRequest().getStack(), stack));

        extractFromFurnace((FurnaceTileEntity) entity);
        //Do we have the requested item in the inventory now?
        final int resultCount = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(currentRequest.getRequest().getStack(), stack)) - preExtractCount;
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
            if(job.getCraftCounter() >= job.getMaxCraftingCount() && job.getProgress() <= 0)
            {
                job.finishRequest(true);
                resetValues();
                currentRecipeStorage = null;
                incrementActionsDoneAndDecSaturation();
                return INVENTORY_FULL;
            }
        }

        setDelay(STANDARD_DELAY);
        return START_WORKING;
    }

    /**
     * Very simple action, straightly extract it from the furnace.
     *
     * @param furnace the furnace to retrieve from.
     */
    private void extractFromFurnace(final FurnaceTileEntity furnace)
    {
        InventoryUtils.transferItemStackIntoNextFreeSlotInItemHandler(
          new InvWrapper(furnace), RESULT_SLOT,
          worker.getInventoryCitizen());
        worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
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
        if(burning > 0 && (burning >= getMaxUsableFurnaces() || (job.getCraftCounter() + job.getProgress() ) >= job.getMaxCraftingCount()))
        {
            setDelay(TICKS_SECOND);
            return getState();            
        }

        for (final BlockPos pos : getOwnBuilding().getFurnaces())
        {
            final TileEntity entity = world.getTileEntity(pos);

            if (entity instanceof FurnaceTileEntity)
            {
                if (!((FurnaceTileEntity) entity).isBurning())
                {
                    walkTo = pos;
                    return START_USING_FURNACE;
                }
            }
            else
            {
                if (!(world.getBlockState(pos).getBlock() instanceof FurnaceBlock))
                {
                    getOwnBuilding().removeFromFurnaces(pos);
                }
            }
        }

        if(burning > 0)
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
        if (getOwnBuilding().getFurnaces().isEmpty())
        {
            if (worker.getCitizenData() != null)
            {
                worker.getCitizenData().triggerInteraction(new StandardInteraction(new TranslationTextComponent(BAKER_HAS_NO_FURNACES_MESSAGE), ChatPriority.BLOCKING));
            }
            setDelay(STANDARD_DELAY);
            return START_WORKING;
        }

        if (walkTo == null || world.getBlockState(walkTo).getBlock() != Blocks.FURNACE)
        {
            walkTo = null;
            setDelay(STANDARD_DELAY);
            return START_WORKING;
        }

        final int burningCount = countOfBurningFurnaces();
        final TileEntity entity = world.getTileEntity(walkTo);
        if (entity instanceof FurnaceTileEntity && currentRecipeStorage != null)
        {
            final FurnaceTileEntity furnace = (FurnaceTileEntity) entity;
            final int maxFurnaces = getMaxUsableFurnaces();
            final Predicate<ItemStack> smeltable = stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(currentRecipeStorage.getCleanedInput().get(0).getItemStack(), stack);
            final int smeltableInFurnaces = getExtendedCount(currentRecipeStorage.getCleanedInput().get(0).getItemStack());
            final int resultInFurnaces = getExtendedCount(currentRecipeStorage.getPrimaryOutput());
            final int resultInCitizenInv = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, currentRecipeStorage.getPrimaryOutput()));

            final int targetCount = currentRequest.getRequest().getCount() - smeltableInFurnaces - resultInFurnaces - resultInCitizenInv;

            if (targetCount <= 0)
            {
                return START_WORKING;
            }
            final int amountOfSmeltableInBuilding = InventoryUtils.getCountFromBuilding(getOwnBuilding(), smeltable);
            final int amountOfSmeltableInInv = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), smeltable);

            if (worker.getHeldItem(Hand.MAIN_HAND).isEmpty())
            {
                worker.setHeldItem(Hand.MAIN_HAND, currentRecipeStorage.getCleanedInput().get(0).getItemStack().copy());
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
                        worker.getCitizenItemHandler().hitBlockWithToolInHand(walkTo);
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
            getOwnBuilding().removeFromFurnaces(walkTo);
        }
        walkTo = null;
        setDelay(STANDARD_DELAY);
        return START_WORKING;
    }

    @Override
    protected IAIState craft()
    {
        final List<ItemStack> possibleFuels = getOwnBuilding().getAllowedFuel();
        if (possibleFuels.isEmpty())
        {
            if (worker.getCitizenData() != null)
            {
                worker.getCitizenData().triggerInteraction(new StandardInteraction(new TranslationTextComponent(FURNACE_USER_NO_FUEL), ChatPriority.BLOCKING));
            }
            return getState();
        }

        if(currentRecipeStorage != null)
        {
            possibleFuels.removeIf(stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, currentRecipeStorage.getPrimaryOutput()));
            // There is always only one input.
            possibleFuels.removeIf(stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, currentRecipeStorage.getCleanedInput().get(0).getItemStack()));
        }

        if (walkToBuilding())
        {
            setDelay(STANDARD_DELAY);
            return getState();
        }
        
        if(currentRecipeStorage != null && currentRequest == null)
        {
            currentRequest = job.getCurrentTask();
        }

        if (currentRecipeStorage != null && currentRecipeStorage.getIntermediate() != Blocks.FURNACE)
        {
            return super.craft();
        }

        if (getOwnBuilding().getFurnaces().isEmpty())
        {
            if (worker.getCitizenData() != null)
            {
                worker.getCitizenData()
                  .triggerInteraction(new StandardInteraction(new TranslationTextComponent(BAKER_HAS_NO_FURNACES_MESSAGE), ChatPriority.BLOCKING));
            }
            setDelay(STANDARD_DELAY);
            return START_WORKING;
        }

        final BlockPos posOfOven = getPositionOfOvenToRetrieveFrom();
        if (posOfOven != null)
        {
            walkTo = posOfOven;
            worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.retrieving"));
            return RETRIEVING_END_PRODUCT_FROM_FURNACE;
        }

        // Safety net, should get caught removing things from the furnace.
        if(currentRequest != null && job.getMaxCraftingCount() > 0 && job.getCraftCounter() >= job.getMaxCraftingCount())
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
