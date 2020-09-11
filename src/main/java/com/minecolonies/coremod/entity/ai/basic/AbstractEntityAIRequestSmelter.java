package com.minecolonies.coremod.entity.ai.basic;

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
import com.minecolonies.api.util.Log;
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
          new AITarget(START_USING_FURNACE, this::fillUpFurnace, 1),
          new AITarget(RETRIEVING_END_PRODUCT_FROM_FURNACE, this::retrieveSmeltableFromFurnace, 5),
          new AIEventTarget(AIBlockingEventType.AI_BLOCKING, this::accelerateFurnaces, TICKS_SECOND)
          );
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        if(currentRecipeStorage != null && currentRecipeStorage.getIntermediate() == Blocks.FURNACE)
        {
            return 32;
        }
        return super.getActionsDoneUntilDumping();
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
    private IAIState accelerateFurnaces()
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
        return getState();
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
                final int countInResultSlot = isEmpty(furnace.getStackInSlot(RESULT_SLOT)) ? 0 : furnace.getStackInSlot(RESULT_SLOT).getCount();
                if ((!furnace.isBurning() && countInResultSlot > 0 && isEmpty(furnace.getStackInSlot(SMELTABLE_SLOT))))
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
            int countOfInput = inputInInv + InventoryUtils.getItemCountInProvider(getOwnBuilding(), predicate) + countInFurnaces + inputInFurnace + outputInInv;
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
                return IDLE;
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
     * Checks if the worker has enough fuel and/or smeltable to start smelting.
     *
     * @param amountOfFuel the total amount of fuel.
     * @return START_USING_FURNACE if enough, else check for additional worker specific jobs.
     */
    private IAIState checkIfAbleToSmelt(final int amountOfFuel, final boolean checkSmeltables)
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
                    final FurnaceTileEntity furnace = (FurnaceTileEntity) entity;
                    
                    if ((amountOfFuel > 0 && hasSmeltableInFurnaceAndNoFuel(furnace))
                          || (hasFuelInFurnaceAndNoSmeltable(furnace) && checkSmeltables)
                          || (amountOfFuel > 0 && hasNeitherFuelNorSmeltAble(furnace)))
                    {
                        walkTo = pos;
                        return START_USING_FURNACE;
                    }
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
            final List<ItemStack> possibleFuels = getOwnBuilding().getAllowedFuel();

            possibleFuels.removeIf(stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, currentRecipeStorage.getPrimaryOutput()));
            // There is always only one input.
            possibleFuels.removeIf(stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, currentRecipeStorage.getCleanedInput().get(0).getItemStack()));

            //Stoke the furnaces
            if (InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(), item -> FurnaceTileEntity.isFuel(item) && possibleFuels.stream().anyMatch(stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, item)))
                  && (hasSmeltableInFurnaceAndNoFuel(furnace) || hasNeitherFuelNorSmeltAble(furnace)))
            {
                InventoryUtils.transferXOfFirstSlotInItemHandlerWithIntoInItemHandler(
                  worker.getInventoryCitizen(), item -> FurnaceTileEntity.isFuel(item) && possibleFuels.stream().anyMatch(stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, item)), STACKSIZE,
                  new InvWrapper(furnace), FUEL_SLOT);
            }

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
            final int amountOfSmeltableInBuilding = InventoryUtils.getItemCountInProvider(getOwnBuilding(), smeltable);
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

        final int amountOfFuelInBuilding = InventoryUtils.getItemCountInProvider(getOwnBuilding(), item -> FurnaceTileEntity.isFuel(item) && possibleFuels.stream().anyMatch(candidate -> ItemStackUtils.compareItemStacksIgnoreStackSize(candidate, item)));
        final int amountOfFuelInInv = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), item -> FurnaceTileEntity.isFuel(item) && possibleFuels.stream().anyMatch(candidate -> ItemStackUtils.compareItemStacksIgnoreStackSize(candidate, item)));

        if (amountOfFuelInBuilding + amountOfFuelInInv <= 0 && !getOwnBuilding().hasWorkerOpenRequestsOfType(worker.getCitizenData(), TypeToken.of(StackList.class)))
        {
            worker.getCitizenData().createRequestAsync(new StackList(possibleFuels, COM_MINECOLONIES_REQUESTS_BURNABLE, STACKSIZE, 1));
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

        if (amountOfFuelInBuilding > 0 && amountOfFuelInInv == 0)
        {
            needsCurrently = new Tuple<>(item -> FurnaceTileEntity.isFuel(item) && possibleFuels.stream().anyMatch(candidate -> ItemStackUtils.compareItemStacksIgnoreStackSize(candidate, item)), STACKSIZE);
            return GATHERING_REQUIRED_MATERIALS;
        }

        // Safety net, should get caught removing things from the furnace.
        if(currentRequest != null && job.getMaxCraftingCount() > 0 && job.getCraftCounter() >= job.getMaxCraftingCount())
        {
            job.finishRequest(true);
            currentRecipeStorage = null;
            currentRequest = null;
            resetValues();
            return START_WORKING;
        }

        if (currentRequest != null && (currentRequest.getState() == RequestState.CANCELLED || currentRequest.getState() == RequestState.FAILED))
        {
            incrementActionsDone(getActionRewardForCraftingSuccess());
            currentRecipeStorage = null;
            currentRequest = null;
            resetValues();
            return START_WORKING;
        }

        return checkIfAbleToSmelt(amountOfFuelInBuilding + amountOfFuelInInv, true);
    }
}
