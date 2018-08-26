package com.minecolonies.coremod.entity.ai.basic;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.requestable.Burnable;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingFurnaceUser;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.coremod.entity.ai.util.AIState.*;

/**
 * AI class for all workers which use a furnace and require fuel and a block to smelt in it.
 * @param <J>
 */
public abstract class AbstractEntityAIUsesFurnace<J extends AbstractJob> extends AbstractEntityAISkill<J>
{
    /**
     * Retrieve smeltable if more than a certain amount.
     */
    private static final int RETRIEVE_SMELTABLE_IF_MORE_THAN = 10;

    /**
     * Wait this amount of ticks after requesting a burnable material.
     */
    protected static final int WAIT_AFTER_REQUEST = 50;

    /**
     * The standard delay after each terminated action.
     */
    protected static final int STANDARD_DELAY = 5;

    /**
     * What he currently might be needing.
     */
    protected Predicate<ItemStack> needsCurrently = null;

    /**
     * The current position the worker should walk to.
     */
    protected BlockPos walkTo = null;

    /**
     * Sets up some important skeleton stuff for every ai.
     *
     * @param job the job class.
     */
    protected AbstractEntityAIUsesFurnace(@NotNull final J job)
    {
        super(job);
        super.registerTargets(
                new AITarget(IDLE, START_WORKING),
                new AITarget(START_WORKING, this::startWorking),
                new AITarget(GATHERING_REQUIRED_MATERIALS, this::getNeededItem),
                new AITarget(START_USING_FURNACE, this::fillUpFurnace),
                new AITarget(RETRIEVING_END_PRODUCT_FROM_FURNACE, this::retrieveSmeltableFromFurnace));
    }

    @Override
    public Class getExpectedBuildingClass()
    {
        return AbstractBuildingFurnaceUser.class;
    }

    /**
     * Method called to extract things from the furnace after it has been reached already.
     * Has to be overwritten by the exact class.
     * @param furnace the furnace to retrieveSmeltableFromFurnace from.
     */
    protected abstract void extractFromFurnace(final TileEntityFurnace furnace);

    /**
     * Method called to detect if a certain stack is of the type we want to be put in the furnace.
     * @param stack the stack to check.
     * @return true if so.
     */
    protected abstract boolean isSmeltable(final ItemStack stack);

    /**
     * If the worker reached his max amount.
     * @return true if so.
     */
    protected boolean reachedMaxToKeep()
    {
        return false;
    }

    /**
     * Get the furnace which has finished smeltables.
     * For this check each furnace which has been registered to the building.
     * Check if the furnace is turned off and has something in the result slot
     * or check if the furnace has more than x results.
     * @return the position of the furnace.
     */
    protected BlockPos getPositionOfOvenToRetrieveFrom()
    {
        for (final BlockPos pos : ((AbstractBuildingFurnaceUser) getOwnBuilding()).getFurnaces())
        {
            final TileEntity entity = world.getTileEntity(pos);
            if (entity instanceof TileEntityFurnace)
            {
                final TileEntityFurnace furnace = (TileEntityFurnace) entity;
                final int countInResultSlot =
                        ItemStackUtils.isEmpty(furnace.getStackInSlot(RESULT_SLOT)) ? 0 : furnace.getStackInSlot(RESULT_SLOT).getCount();
                if ((!furnace.isBurning() && countInResultSlot > 0)
                        || countInResultSlot > RETRIEVE_SMELTABLE_IF_MORE_THAN)
                {
                    worker.getCitizenStatusHandler().setLatestStatus(new TextComponentTranslation(COM_MINECOLONIES_COREMOD_STATUS_RETRIEVING));
                    return pos;
                }
            }
        }
        return null;
    }

    /**
     * Central method of the furnace user, he decides about what to do next from here.
     * First check if any of the workers has important tasks to handle first.
     * If not check if there is an oven with an item which has to be retrieved.
     * If not check if fuel and smeltable are available and request if necessary and get into inventory.
     * Then check if able to smelt already.
     * @return the next state to go to.
     */
    private AIState startWorking()
    {
        worker.getCitizenStatusHandler().setLatestStatus(new TextComponentTranslation(COM_MINECOLONIES_COREMOD_STATUS_DECIDING));

        final AIState nextState = checkForImportantJobs();
        if(nextState != START_WORKING)
        {
            return nextState;
        }

        final BlockPos posOfOven = getPositionOfOvenToRetrieveFrom();
        if (posOfOven != null)
        {
            walkTo = posOfOven;
            worker.getCitizenStatusHandler().setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.retrieving"));
            return RETRIEVING_END_PRODUCT_FROM_FURNACE;
        }

        final int amountOfSmeltableInBuilding = InventoryUtils.getItemCountInProvider(getOwnBuilding(), this::isSmeltable);
        final int amountOfSmeltableInInv = InventoryUtils.getItemCountInItemHandler(new InvWrapper(worker.getInventoryCitizen()), this::isSmeltable);

        final int amountOfFuelInBuilding = InventoryUtils.getItemCountInProvider(getOwnBuilding(), TileEntityFurnace::isItemFuel);
        final int amountOfFuelInInv = InventoryUtils.getItemCountInItemHandler(new InvWrapper(worker.getInventoryCitizen()), TileEntityFurnace::isItemFuel);

        if (amountOfSmeltableInBuilding + amountOfSmeltableInInv <= 0
                && !getOwnBuilding().hasWorkerOpenRequestsOfType(worker.getCitizenData(), TypeToken.of(getSmeltAbleClass().getClass())) && !reachedMaxToKeep())
        {
            worker.getCitizenData().createRequestAsync(getSmeltAbleClass());
        }
        else if (amountOfFuelInBuilding + amountOfFuelInInv <= 0 && !getOwnBuilding().hasWorkerOpenRequestsOfType(worker.getCitizenData(), TypeToken.of(Burnable.class)))
        {
            worker.getCitizenData().createRequestAsync(new Burnable(STACKSIZE));
        }

        if(amountOfSmeltableInBuilding > 0 && amountOfSmeltableInInv == 0)
        {
            needsCurrently = this::isSmeltable;
            return GATHERING_REQUIRED_MATERIALS;
        }
        else if(amountOfFuelInBuilding > 0 && amountOfFuelInInv == 0)
        {
            needsCurrently = TileEntityFurnace::isItemFuel;
            return GATHERING_REQUIRED_MATERIALS;
        }

        return checkIfAbleToSmelt(amountOfFuelInBuilding + amountOfFuelInInv, amountOfSmeltableInBuilding + amountOfSmeltableInInv);
    }

    /**
     * Checks if the worker has enough fuel and/or smeltable to start smelting.
     * @param amountOfFuel the total amount of fuel.
     * @param amountOfSmeltable the total amount of smeltables.
     * @return START_USING_FURNACE if enough, else check for additional worker specific jobs.
     */
    private AIState checkIfAbleToSmelt(final int amountOfFuel, final int amountOfSmeltable)
    {
        for (final BlockPos pos : ((AbstractBuildingFurnaceUser) getOwnBuilding()).getFurnaces())
        {
            final TileEntity entity = world.getTileEntity(pos);

            if(entity instanceof TileEntityFurnace && !((TileEntityFurnace) entity).isBurning())
            {
                final TileEntityFurnace furnace = (TileEntityFurnace) entity;
                if ((amountOfFuel > 0 && hasSmeltableInFurnaceAndNoFuel(furnace))
                        || (amountOfSmeltable > 0 && hasFuelInFurnaceAndNoSmeltable(furnace))
                        || (amountOfFuel > 0 && amountOfSmeltable > 0 && hasNeitherFuelNorSmeltAble(furnace)))
                {
                    walkTo = pos;
                    return START_USING_FURNACE;
                }
            }
        }

        return checkForAdditionalJobs();
    }

    /**
     * Check for additional jobs to execute after the traditional furnace user jobs have been handled.
     * @return the next AIState to go to.
     */
    protected AIState checkForAdditionalJobs()
    {
        worker.getCitizenStatusHandler().setLatestStatus(new TextComponentTranslation(COM_MINECOLONIES_COREMOD_STATUS_IDLING));
        setDelay(WAIT_AFTER_REQUEST);
        walkToBuilding();
        return START_WORKING;
    }

    /**
     * Check for important jobs to execute before the traditional furnace user jobs are handled.
     * @return the next AIState to go to.
     */
    protected AIState checkForImportantJobs()
    {
        return START_WORKING;
    }

    /**
     * Specify that we dump inventory after every action.
     * @see com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIBasic#getActionsDoneUntilDumping()
     * @return 1 to indicate that we dump inventory after every action
     */
    @Override
    protected int getActionsDoneUntilDumping()
    {
        return 1;
    }

    /**
     * Retrieve burnable material from the building to get to start smelting.
     * For this go to the building if no position has been set.
     * Then check for the chest with the required material and set the position and return.
     *
     * If the position has been set navigate to it.
     * On arrival transfer to inventory and return to StartWorking.
     *
     * @return the next state to transfer to.
     */
    private AIState getNeededItem()
    {
        worker.getCitizenStatusHandler().setLatestStatus(new TextComponentTranslation(COM_MINECOLONIES_COREMOD_STATUS_GATHERING));

        if (walkTo == null && walkToBuilding())
        {
            return getState();
        }

        if (needsCurrently == null || !InventoryUtils.hasItemInProvider(getOwnBuilding(), needsCurrently))
        {
            setDelay(STANDARD_DELAY);
            return START_WORKING;
        }
        else
        {
            if (walkTo == null)
            {
                final BlockPos pos = getOwnBuilding().getTileEntity().getPositionOfChestWithItemStack(needsCurrently);
                if (pos == null)
                {
                    setDelay(STANDARD_DELAY);
                    return START_WORKING;
                }
                walkTo = pos;
            }

            if (walkToBlock(walkTo))
            {
                setDelay(2);
                return getState();
            }

            final boolean transfered = tryTransferFromPosToWorker(walkTo, needsCurrently);
            if (!transfered)
            {
                walkTo = null;
                return START_WORKING;
            }
            walkTo = null;
        }

        setDelay(STANDARD_DELAY);
        return START_WORKING;
    }

    /**
     * Retrieve ready bars from the furnaces.
     * If no position has been set return.
     * Else navigate to the position of the furnace.
     * On arrival execute the extract method of the specialized worker.
     * @return the next state to go to.
     */
    private AIState retrieveSmeltableFromFurnace()
    {
        worker.getCitizenStatusHandler().setLatestStatus(new TextComponentTranslation(COM_MINECOLONIES_COREMOD_STATUS_RETRIEVING));

        if (walkTo == null)
        {
            return START_WORKING;
        }

        if (walkToBlock(walkTo))
        {
            setDelay(2);
            return getState();
        }

        final TileEntity entity = world.getTileEntity(walkTo);
        if (!(entity instanceof TileEntityFurnace)
                || (ItemStackUtils.isEmpty(((TileEntityFurnace) entity).getStackInSlot(RESULT_SLOT))))
        {
            walkTo = null;
            return START_WORKING;
        }

        walkTo = null;

        extractFromFurnace((TileEntityFurnace) entity);
        incrementActionsDoneAndDecSaturation();
        setDelay(STANDARD_DELAY);
        return START_WORKING;
    }

    /**
     * Smelt the smeltable after the required items are in the inv.
     * @return the next state to go to.
     */
    private AIState fillUpFurnace()
    {
        if (((AbstractBuildingFurnaceUser) getOwnBuilding()).getFurnaces().isEmpty())
        {
            chatSpamFilter.talkWithoutSpam(COM_MINECOLONIES_COREMOD_STATUS_COOKING);
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
        if (entity instanceof TileEntityFurnace)
        {
            final TileEntityFurnace furnace = (TileEntityFurnace) entity;

            if (InventoryUtils.hasItemInItemHandler(new InvWrapper(worker.getInventoryCitizen()), this::isSmeltable)
                    && (hasFuelInFurnaceAndNoSmeltable(furnace) || hasNeitherFuelNorSmeltAble(furnace)))
            {
                InventoryUtils.transferXOfFirstSlotInItemHandlerWithIntoInItemHandler(
                        new InvWrapper(worker.getInventoryCitizen()), this::isSmeltable, STACKSIZE,
                        new InvWrapper(furnace), SMELTABLE_SLOT);
            }

            if (InventoryUtils.hasItemInItemHandler(new InvWrapper(worker.getInventoryCitizen()), TileEntityFurnace::isItemFuel)
                    && (hasSmeltableInFurnaceAndNoFuel(furnace) || hasNeitherFuelNorSmeltAble(furnace)))
            {
                InventoryUtils.transferXOfFirstSlotInItemHandlerWithIntoInItemHandler(
                        new InvWrapper(worker.getInventoryCitizen()), TileEntityFurnace::isItemFuel, STACKSIZE,
                        new InvWrapper(furnace), FUEL_SLOT);
            }
        }
        walkTo = null;
        setDelay(STANDARD_DELAY);
        return START_WORKING;
    }

    /**
     * Check if the furnace has smeltable in it and fuel empty.
     * @param entity the furnace.
     * @return true if so.
     */
    private static boolean hasSmeltableInFurnaceAndNoFuel(final TileEntityFurnace entity)
    {
        return !ItemStackUtils.isEmpty(entity.getStackInSlot(SMELTABLE_SLOT))
                && ItemStackUtils.isEmpty(entity.getStackInSlot(FUEL_SLOT));
    }

    /**
     * Check if the furnace has smeltable in it and fuel empty.
     * @param entity the furnace.
     * @return true if so.
     */
    private static boolean hasNeitherFuelNorSmeltAble(final TileEntityFurnace entity)
    {
        return ItemStackUtils.isEmpty(entity.getStackInSlot(SMELTABLE_SLOT))
                && ItemStackUtils.isEmpty(entity.getStackInSlot(FUEL_SLOT));
    }

    /**
     * Check if the furnace has fuel in it and smeltable empty.
     * @param entity the furnace.
     * @return true if so.
     */
    private static boolean hasFuelInFurnaceAndNoSmeltable(final TileEntityFurnace entity)
    {
        return ItemStackUtils.isEmpty(entity.getStackInSlot(SMELTABLE_SLOT))
                && !ItemStackUtils.isEmpty(entity.getStackInSlot(FUEL_SLOT));
    }

    /**
     * Smeltabel the worker requires.
     * Each worker has to override this.
     * @return the type of it.
     */
    protected abstract IRequestable getSmeltAbleClass();
}
