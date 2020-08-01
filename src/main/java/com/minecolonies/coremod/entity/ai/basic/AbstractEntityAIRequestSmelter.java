package com.minecolonies.coremod.entity.ai.basic;

import com.google.common.reflect.TypeToken;
import com.ldtteam.blockout.Log;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.colony.requestsystem.requestable.StackList;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
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
          new AITarget(RETRIEVING_END_PRODUCT_FROM_FURNACE, this::retrieveSmeltableFromFurnace, 1));
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
    
    @NotNull
    @Override
    protected List<ItemStack> itemsNiceToHave()
    {
        return getOwnBuilding().getAllowedFuel();
    }

    @Override
    protected int getExtendedOutputCount(final ItemStack primaryOutput)
    {
        if(currentRecipeStorage != null && currentRecipeStorage.getIntermediate() == Blocks.FURNACE)
        {
            return job.getProgress();
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

        final BlockPos furnacePos = getPositionOfOvenToRetrieveFrom();
        if (furnacePos != null)
        {
            currentRequest = currentTask;
            walkTo = furnacePos;
            return RETRIEVING_END_PRODUCT_FROM_FURNACE;
        }

        for (final BlockPos pos : getOwnBuilding().getFurnaces())
        {
            final TileEntity entity = world.getTileEntity(pos);
            if (entity instanceof FurnaceTileEntity)
            {
                final FurnaceTileEntity furnace = (FurnaceTileEntity) entity;
                if (furnace.isBurning() || !isEmpty(furnace.getStackInSlot(RESULT_SLOT)) || !isEmpty(furnace.getStackInSlot(SMELTABLE_SLOT)))
                {
                    return CRAFT;
                }
            }
        }

        return super.getRecipe();
    }

    /**
     * Check to see if any furnaces are still processing
     * @return
     */
    private boolean checkIfAnyFurnaceIsBurning()
    {
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
                        return true;
                    }
                }
            }
        }
        return false;
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
        for (final ItemStorage inputStorage : input)
        {
            final Predicate<ItemStack> predicate = stack -> !ItemStackUtils.isEmpty(stack) && new Stack(stack).matches(inputStorage.getItemStack());
            if (InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), predicate) + (job.getCraftCounter() * inputStorage.getAmount())
                  < inputStorage.getAmount() * job.getMaxCraftingCount())
            {
                if (InventoryUtils.hasItemInProvider(getOwnBuilding(), predicate))
                {
                    needsCurrently = new Tuple<>(predicate, inputStorage.getAmount() * job.getMaxCraftingCount());
                    return GATHERING_REQUIRED_MATERIALS;
                }
                else
                {
                    for (final BlockPos pos : getOwnBuilding().getFurnaces())
                    {
                        final TileEntity entity = world.getTileEntity(pos);
                        if (entity instanceof FurnaceTileEntity)
                        {
                            final FurnaceTileEntity furnace = (FurnaceTileEntity) entity;
                            if (furnace.getStackInSlot(RESULT_SLOT).isItemEqual(storage.getPrimaryOutput()) ||
                                  furnace.getStackInSlot(SMELTABLE_SLOT).isItemEqual(storage.getCleanedInput().get(0).getItemStack()))
                            {
                                //TODO: Scalable delay
                                return CRAFT;
                            }
                        }
                    }
                }
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

        if (walkToBlock(walkTo))
        {
            setDelay(2);
            return getState();
        }

        final TileEntity entity = world.getTileEntity(walkTo);
        if (!(entity instanceof FurnaceTileEntity) || (isEmpty(((FurnaceTileEntity) entity).getStackInSlot(RESULT_SLOT))))
        {
            walkTo = null;
            return START_WORKING;
        }

        walkTo = null;

        final int preExtractCount = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), stack -> currentRequest.getRequest().getStack().isItemEqual(stack));

        extractFromFurnace((FurnaceTileEntity) entity);
        //Do we have the requested item in the inventory now?
        final int resultCount = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), stack -> currentRequest.getRequest().getStack().isItemEqual(stack)) - preExtractCount;
        if (resultCount > 0)
        {
            final ItemStack stack = currentRequest.getRequest().getStack().copy();
            stack.setCount(resultCount);
            currentRequest.addDelivery(stack);

            job.setCraftCounter(job.getCraftCounter() + resultCount);
            job.setProgress(job.getProgress() - resultCount);
            if(job.getCraftCounter() >= job.getMaxCraftingCount())
            {
                job.finishRequest(true);
                resetValues();
                currentRecipeStorage = null;
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
        this.incrementActionsDoneAndDecSaturation();
    }

    /**
     * Checks if the worker has enough fuel and/or smeltable to start smelting.
     *
     * @param amountOfFuel the total amount of fuel.
     * @return START_USING_FURNACE if enough, else check for additional worker specific jobs.
     */
    private IAIState checkIfAbleToSmelt(final int amountOfFuel, final boolean checkSmeltables)
    {
        boolean furnaceBurning = false;
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
                else 
                {
                    furnaceBurning = true;
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

        if(furnaceBurning)
        {
            //Todo: Scalable Delay 
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
                worker.getCitizenData()
                  .triggerInteraction(new StandardInteraction(new TranslationTextComponent(BAKER_HAS_NO_FURNACES_MESSAGE), ChatPriority.BLOCKING));
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

        if (walkToBlock(walkTo))
        {
            setDelay(2);
            return getState();
        }

        final TileEntity entity = world.getTileEntity(walkTo);
        if (entity instanceof FurnaceTileEntity)
        {
            final FurnaceTileEntity furnace = (FurnaceTileEntity) entity;
            final List<ItemStack> possibleFuels = getOwnBuilding().getAllowedFuel();

            //Stoke the furnaces
            if (InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(), item -> FurnaceTileEntity.isFuel(item) && possibleFuels.stream().anyMatch(candidate -> item.isItemEqual(candidate)))
                  && (hasSmeltableInFurnaceAndNoFuel(furnace) || hasNeitherFuelNorSmeltAble(furnace)))
            {
                InventoryUtils.transferXOfFirstSlotInItemHandlerWithIntoInItemHandler(
                  worker.getInventoryCitizen(), item -> FurnaceTileEntity.isFuel(item) && possibleFuels.stream().anyMatch(candidate -> item.isItemEqual(candidate)), STACKSIZE,
                  new InvWrapper(furnace), FUEL_SLOT);
            }

            if(currentRecipeStorage != null)
            {
                final Predicate<ItemStack> smeltable = stack -> currentRecipeStorage.getCleanedInput().get(0).getItemStack().isItemEqual(stack);
                worker.setHeldItem(Hand.MAIN_HAND, currentRecipeStorage.getCleanedInput().get(0).getItemStack().copy());
                final int amountOfSmeltableInBuilding = InventoryUtils.getItemCountInProvider(getOwnBuilding(), smeltable);
                final int amountOfSmeltableInInv = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), smeltable);
                if (InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(), smeltable))
                {
                    if (hasFuelInFurnaceAndNoSmeltable(furnace) || hasNeitherFuelNorSmeltAble(furnace))
                    {
                        final int toTransfer = Math.min(STACKSIZE, job.getMaxCraftingCount() - (job.getProgress() + job.getCraftCounter()));
                        if(toTransfer > 0)
                        {
                            job.setProgress(job.getProgress() + toTransfer);
                            InventoryUtils.transferXOfFirstSlotInItemHandlerWithIntoInItemHandler(
                            worker.getInventoryCitizen(), smeltable, toTransfer,
                            new InvWrapper(furnace), SMELTABLE_SLOT);
                        }
                    }
                }
                else if(amountOfSmeltableInInv < currentRequest.getRequest().getCount() 
                && amountOfSmeltableInBuilding >= (currentRequest.getRequest().getCount() - amountOfSmeltableInInv)
                && currentRecipeStorage.getIntermediate() == Blocks.FURNACE && currentRequest != null)
                {
                    needsCurrently = new Tuple<>(smeltable, currentRequest.getRequest().getCount());
                    return GATHERING_REQUIRED_MATERIALS;
                } 
                else if (!checkIfAnyFurnaceIsBurning())
                {
                    //This is a safety net for the AI getting way out of sync with it's tracking. It shouldn't happen. 
                    job.finishRequest(false);
                    resetValues();
                    return IDLE;
                }
            }   
        }
        else
        {
            if (!(world.getBlockState(walkTo).getBlock() instanceof FurnaceBlock))
            {
                getOwnBuilding().removeFromFurnaces(walkTo);
            }
        }
        walkTo = null;
        setDelay(STANDARD_DELAY);
        return START_WORKING;
    }

    @Override
    protected IAIState craft()
    {

        final List<ItemStack> possibleFuels = getOwnBuilding().getAllowedFuel();

        final int amountOfFuelInBuilding = InventoryUtils.getItemCountInProvider(getOwnBuilding(), item -> FurnaceTileEntity.isFuel(item) && possibleFuels.stream().anyMatch(candidate -> item.isItemEqual(candidate)));
        final int amountOfFuelInInv = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), item -> FurnaceTileEntity.isFuel(item) && possibleFuels.stream().anyMatch(candidate -> item.isItemEqual(candidate)));

        if (amountOfFuelInBuilding + amountOfFuelInInv <= 0 && !getOwnBuilding().hasWorkerOpenRequestsOfType(worker.getCitizenData(), TypeToken.of(StackList.class)))
        {
            worker.getCitizenData().createRequestAsync(new StackList(possibleFuels, COM_MINECOLONIES_REQUESTS_BURNABLE, STACKSIZE, 1));
        }

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
        
        currentRequest = job.getCurrentTask();
        if (currentRecipeStorage.getIntermediate() != Blocks.FURNACE)
        {
            return super.craft();
        }

        if (getOwnBuilding().getCopyOfAllowedItems().isEmpty())
        {
            if (worker.getCitizenData() != null)
            {
                worker.getCitizenData().triggerInteraction(new StandardInteraction(new TranslationTextComponent(FURNACE_USER_NO_FUEL), ChatPriority.BLOCKING));
            }
            return getState();
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
            needsCurrently = new Tuple<>(item -> FurnaceTileEntity.isFuel(item) && possibleFuels.stream().anyMatch(candidate -> item.isItemEqual(candidate)), STACKSIZE);
            return GATHERING_REQUIRED_MATERIALS;
        }

        // Safety net, should get caught removing things from the furnace.
        if(job.getMaxCraftingCount() > 0 && job.getCraftCounter() >= job.getMaxCraftingCount())
        {
            job.finishRequest(true);
            currentRecipeStorage = null;
            currentRequest = null;
            resetValues();
            return START_WORKING;
        }

        if (currentRequest != null && (currentRequest.getState() == RequestState.CANCELLED || currentRequest.getState() == RequestState.FAILED))
        {
            currentRequest = null;
            incrementActionsDone(getActionRewardForCraftingSuccess());
            currentRecipeStorage = null;
            return START_WORKING;
        }

        return checkIfAbleToSmelt(amountOfFuelInBuilding + amountOfFuelInInv, true);
    }
}
