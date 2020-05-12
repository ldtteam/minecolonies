package com.minecolonies.coremod.entity.ai.basic;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requestable.StackList;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingFurnaceUser;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteractionResponseHandler;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import net.minecraft.block.Blocks;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.ItemStackUtils.*;
import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * AI class for all workers which use a furnace and require fuel and a block to smelt in it.
 *
 * @param <J> the job it is for.
 */
public abstract class AbstractEntityAIUsesFurnace<J extends AbstractJob> extends AbstractEntityAISkill<J>
{
    /**
     * Base xp gain for the basic xp.
     */
    protected static final double BASE_XP_GAIN = 2;

    /**
     * Retrieve smeltable if more than a certain amount.
     */
    private static final int RETRIEVE_SMELTABLE_IF_MORE_THAN = 10;

    /**
     * Wait this amount of ticks after requesting a burnable material.
     */
    protected static final int WAIT_AFTER_REQUEST = 50;

    /**
     * Sets up some important skeleton stuff for every ai.
     *
     * @param job the job class.
     */
    protected AbstractEntityAIUsesFurnace(@NotNull final J job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING, STANDARD_DELAY),
          new AITarget(START_WORKING, this::startWorking, TICKS_SECOND),
          new AITarget(START_USING_FURNACE, this::fillUpFurnace, STANDARD_DELAY),
          new AITarget(RETRIEVING_END_PRODUCT_FROM_FURNACE, this::retrieveSmeltableFromFurnace, STANDARD_DELAY));
    }

    @Override
    public Class getExpectedBuildingClass()
    {
        return AbstractBuildingFurnaceUser.class;
    }

    /**
     * Method called to extract things from the furnace after it has been reached already. Has to be overwritten by the exact class.
     *
     * @param furnace the furnace to retrieveSmeltableFromFurnace from.
     */
    protected abstract void extractFromFurnace(final FurnaceTileEntity furnace);

    /**
     * Method called to detect if a certain stack is of the type we want to be put in the furnace.
     *
     * @param stack the stack to check.
     * @return true if so.
     */
    protected abstract boolean isSmeltable(final ItemStack stack);

    /**
     * If the worker reached his max amount.
     *
     * @return true if so.
     */
    protected boolean reachedMaxToKeep()
    {
        return false;
    }

    /**
     * Get the furnace which has finished smeltables. For this check each furnace which has been registered to the building. Check if the furnace is turned off and has something in
     * the result slot or check if the furnace has more than x results.
     *
     * @return the position of the furnace.
     */
    protected BlockPos getPositionOfOvenToRetrieveFrom()
    {
        for (final BlockPos pos : ((AbstractBuildingFurnaceUser) getOwnBuilding()).getFurnaces())
        {
            final TileEntity entity = world.getTileEntity(pos);
            if (entity instanceof FurnaceTileEntity)
            {
                final FurnaceTileEntity furnace = (FurnaceTileEntity) entity;
                final int countInResultSlot =
                  ItemStackUtils.isEmpty(furnace.getStackInSlot(RESULT_SLOT)) ? 0 : furnace.getStackInSlot(RESULT_SLOT).getCount();
                if ((!furnace.isBurning() && countInResultSlot > 0)
                      || countInResultSlot > RETRIEVE_SMELTABLE_IF_MORE_THAN)
                {
                    worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent(COM_MINECOLONIES_COREMOD_STATUS_RETRIEVING));
                    return pos;
                }
            }
        }
        return null;
    }

    /**
     * Central method of the furnace user, he decides about what to do next from here. First check if any of the workers has important tasks to handle first. If not check if there
     * is an oven with an item which has to be retrieved. If not check if fuel and smeltable are available and request if necessary and get into inventory. Then check if able to
     * smelt already.
     *
     * @return the next state to go to.
     */
    private IAIState startWorking()
    {
        if (walkToBuilding())
        {
            return getState();
        }

        if (getOwnBuilding(AbstractBuildingFurnaceUser.class).getAllowedFuel().isEmpty())
        {
            if (worker.getCitizenData() != null)
            {
                worker.getCitizenData().triggerInteraction(new StandardInteractionResponseHandler(new TranslationTextComponent(FURNACE_USER_NO_FUEL), ChatPriority.BLOCKING));
            }
            return getState();
        }

        if (getOwnBuilding(AbstractBuildingFurnaceUser.class).getFurnaces().isEmpty())
        {
            if (worker.getCitizenData() != null)
            {
                worker.getCitizenData()
                  .triggerInteraction(new StandardInteractionResponseHandler(new TranslationTextComponent(BAKER_HAS_NO_FURNACES_MESSAGE), ChatPriority.BLOCKING));
            }
            return getState();
        }

        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent(COM_MINECOLONIES_COREMOD_STATUS_DECIDING));

        final IAIState nextState = checkForImportantJobs();
        if (nextState != START_WORKING)
        {
            return nextState;
        }

        final BlockPos posOfOven = getPositionOfOvenToRetrieveFrom();
        if (posOfOven != null)
        {
            walkTo = posOfOven;
            worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.retrieving"));
            return RETRIEVING_END_PRODUCT_FROM_FURNACE;
        }

        final int amountOfSmeltableInBuilding = InventoryUtils.getItemCountInProvider(getOwnBuilding(), this::isSmeltable);
        final int amountOfSmeltableInInv = InventoryUtils.getItemCountInItemHandler((worker.getInventoryCitizen()), this::isSmeltable);

        final int amountOfFuelInBuilding = InventoryUtils.getItemCountInProvider(getOwnBuilding(), getOwnBuilding(AbstractBuildingFurnaceUser.class)::isAllowedFuel);
        final int amountOfFuelInInv =
          InventoryUtils.getItemCountInItemHandler((worker.getInventoryCitizen()), getOwnBuilding(AbstractBuildingFurnaceUser.class)::isAllowedFuel);

        if (amountOfSmeltableInBuilding + amountOfSmeltableInInv <= 0 && !reachedMaxToKeep())
        {
            requestSmeltable();
        }
        else if (amountOfFuelInBuilding + amountOfFuelInInv <= 0 && !getOwnBuilding().hasWorkerOpenRequestsFiltered(worker.getCitizenData(),
          req -> req.getShortDisplayString().getString().equals(
            LanguageHandler.format(COM_MINECOLONIES_REQUESTS_BURNABLE))))
        {
            worker.getCitizenData().createRequestAsync(new StackList(getOwnBuilding(AbstractBuildingFurnaceUser.class).getAllowedFuel(), COM_MINECOLONIES_REQUESTS_BURNABLE, STACKSIZE, 1));
        }

        if (amountOfSmeltableInBuilding > 0 && amountOfSmeltableInInv == 0)
        {
            needsCurrently = new Tuple<>(this::isSmeltable, STACKSIZE);
            return GATHERING_REQUIRED_MATERIALS;
        }
        else if (amountOfFuelInBuilding > 0 && amountOfFuelInInv == 0)
        {
            needsCurrently = new Tuple<>(getOwnBuilding(AbstractBuildingFurnaceUser.class)::isAllowedFuel, STACKSIZE);
            return GATHERING_REQUIRED_MATERIALS;
        }

        return checkIfAbleToSmelt(amountOfFuelInBuilding + amountOfFuelInInv, amountOfSmeltableInBuilding + amountOfSmeltableInInv);
    }

    /**
     * Request the smeltable item to the building. Specific worker has to override this.
     */
    public abstract void requestSmeltable();

    /**
     * Checks if the worker has enough fuel and/or smeltable to start smelting.
     *
     * @param amountOfFuel      the total amount of fuel.
     * @param amountOfSmeltable the total amount of smeltables.
     * @return START_USING_FURNACE if enough, else check for additional worker specific jobs.
     */
    private IAIState checkIfAbleToSmelt(final int amountOfFuel, final int amountOfSmeltable)
    {
        for (final BlockPos pos : ((AbstractBuildingFurnaceUser) getOwnBuilding()).getFurnaces())
        {
            final TileEntity entity = world.getTileEntity(pos);

            if (entity instanceof FurnaceTileEntity)
            {
                if (!((FurnaceTileEntity) entity).isBurning())
                {
                    final FurnaceTileEntity furnace = (FurnaceTileEntity) entity;
                    if ((amountOfFuel > 0 && hasSmeltableInFurnaceAndNoFuel(furnace))
                          || (amountOfSmeltable > 0 && hasFuelInFurnaceAndNoSmeltable(furnace))
                          || (amountOfFuel > 0 && amountOfSmeltable > 0 && hasNeitherFuelNorSmeltAble(furnace)))
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
                    ((AbstractBuildingFurnaceUser) getOwnBuilding()).removeFromFurnaces(pos);
                }
            }
        }

        return checkForAdditionalJobs();
    }

    /**
     * Check for additional jobs to execute after the traditional furnace user jobs have been handled.
     *
     * @return the next IAIState to go to.
     */
    protected IAIState checkForAdditionalJobs()
    {
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent(COM_MINECOLONIES_COREMOD_STATUS_IDLING));
        return START_WORKING;
    }

    /**
     * Check for important jobs to execute before the traditional furnace user jobs are handled.
     *
     * @return the next IAIState to go to.
     */
    protected IAIState checkForImportantJobs()
    {
        return START_WORKING;
    }

    /**
     * Specify that we dump inventory after every action.
     *
     * @return 1 to indicate that we dump inventory after every action
     * @see com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIBasic#getActionsDoneUntilDumping()
     */
    @Override
    protected int getActionsDoneUntilDumping()
    {
        return 1;
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

        if (walkTo == null)
        {
            return START_WORKING;
        }

        if (walkToBlock(walkTo))
        {
            return getState();
        }

        final TileEntity entity = world.getTileEntity(walkTo);
        if (!(entity instanceof FurnaceTileEntity)
              || (ItemStackUtils.isEmpty(((FurnaceTileEntity) entity).getStackInSlot(RESULT_SLOT))))
        {
            walkTo = null;
            return START_WORKING;
        }

        walkTo = null;

        extractFromFurnace((FurnaceTileEntity) entity);
        incrementActionsDoneAndDecSaturation();
        return START_WORKING;
    }

    /**
     * Smelt the smeltable after the required items are in the inv.
     *
     * @return the next state to go to.
     */
    private IAIState fillUpFurnace()
    {
        if (((AbstractBuildingFurnaceUser) getOwnBuilding()).getFurnaces().isEmpty())
        {
            if (worker.getCitizenData() != null)
            {
                worker.getCitizenData()
                  .triggerInteraction(new StandardInteractionResponseHandler(new TranslationTextComponent(BAKER_HAS_NO_FURNACES_MESSAGE), ChatPriority.BLOCKING));
            }
            return START_WORKING;
        }

        if (walkTo == null || world.getBlockState(walkTo).getBlock() != Blocks.FURNACE)
        {
            walkTo = null;
            return START_WORKING;
        }

        if (walkToBlock(walkTo))
        {
            return getState();
        }

        final TileEntity entity = world.getTileEntity(walkTo);
        if (entity instanceof FurnaceTileEntity)
        {
            final FurnaceTileEntity furnace = (FurnaceTileEntity) entity;

            if (InventoryUtils.hasItemInItemHandler((worker.getInventoryCitizen()), this::isSmeltable)
                  && (hasFuelInFurnaceAndNoSmeltable(furnace) || hasNeitherFuelNorSmeltAble(furnace)))
            {
                InventoryUtils.transferXOfFirstSlotInItemHandlerWithIntoInItemHandler(
                  (worker.getInventoryCitizen()), this::isSmeltable, STACKSIZE,
                  new InvWrapper(furnace), SMELTABLE_SLOT);
            }

            if (InventoryUtils.hasItemInItemHandler((worker.getInventoryCitizen()), getOwnBuilding(AbstractBuildingFurnaceUser.class)::isAllowedFuel)
                  && (hasSmeltableInFurnaceAndNoFuel(furnace) || hasNeitherFuelNorSmeltAble(furnace)))
            {
                InventoryUtils.transferXOfFirstSlotInItemHandlerWithIntoInItemHandler(
                  (worker.getInventoryCitizen()), getOwnBuilding(AbstractBuildingFurnaceUser.class)::isAllowedFuel, STACKSIZE,
                  new InvWrapper(furnace), FUEL_SLOT);
            }
        }
        walkTo = null;
        return START_WORKING;
    }

    /**
     * Smeltabel the worker requires. Each worker has to override this.
     *
     * @return the type of it.
     */
    protected abstract IRequestable getSmeltAbleClass();
}
