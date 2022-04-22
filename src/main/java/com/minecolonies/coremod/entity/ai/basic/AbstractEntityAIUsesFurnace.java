package com.minecolonies.coremod.entity.ai.basic;

import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requestable.StackList;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.statemachine.AIEventTarget;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.AIBlockingEventType;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.api.util.constant.translation.RequestSystemTranslationConstants;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.FurnaceUserModule;
import com.minecolonies.coremod.colony.buildings.modules.ItemListModule;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteraction;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import net.minecraft.block.Blocks;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.ItemStackUtils.*;
import static com.minecolonies.api.util.constant.BuildingConstants.FUEL_LIST;
import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * AI class for all workers which use a furnace and require fuel and a block to smelt in it.
 *
 * @param <J> the job it is for.
 */
public abstract class AbstractEntityAIUsesFurnace<J extends AbstractJob<?, J>, B extends AbstractBuilding> extends AbstractEntityAISkill<J, B>
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
    /**
     * Storage buffer, slots to not fill with requests.
     */
    private static final int STORAGE_BUFFER = 3;

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
          new AITarget(START_WORKING, this::startWorking, 60),
          new AITarget(START_USING_FURNACE, this::fillUpFurnace, STANDARD_DELAY),
          new AIEventTarget(AIBlockingEventType.AI_BLOCKING, this::accelerateFurnaces, TICKS_SECOND),
          new AITarget(RETRIEVING_END_PRODUCT_FROM_FURNACE, this::retrieveSmeltableFromFurnace, STANDARD_DELAY),
          new AITarget(RETRIEVING_USED_FUEL_FROM_FURNACE, this::retrieveUsedFuel, STANDARD_DELAY));
    }

    /**
     * Method called to extract things from the furnace after it has been reached already. Has to be overwritten by the exact class.
     *
     * @param furnace the furnace to retrieveSmeltableFromFurnace from.
     */
    protected abstract void extractFromFurnace(final FurnaceTileEntity furnace);

    /**
     * Extract fuel from the furnace.
     *
     * @param furnace the furnace to retrieve from.
     */
    private void extractFuelFromFurnace(final FurnaceTileEntity furnace)
    {
        InventoryUtils.transferItemStackIntoNextFreeSlotInItemHandler(
                new InvWrapper(furnace), FUEL_SLOT,
                worker.getInventoryCitizen());
    }

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
        final int count = InventoryUtils.countEmptySlotsInBuilding(getOwnBuilding());
        return count <= STORAGE_BUFFER;
    }

    /**
     * Get the furnace which has finished smeltables. For this check each furnace which has been registered to the building. Check if the furnace is turned off and has something in
     * the result slot or check if the furnace has more than x results.
     *
     * @return the position of the furnace.
     */
    protected BlockPos getPositionOfOvenToRetrieveFrom()
    {
        for (final BlockPos pos : getOwnBuilding().getFirstModuleOccurance(FurnaceUserModule.class).getFurnaces())
        {
            final TileEntity entity = world.getBlockEntity(pos);
            if (entity instanceof FurnaceTileEntity)
            {
                final FurnaceTileEntity furnace = (FurnaceTileEntity) entity;
                final int countInResultSlot = ItemStackUtils.isEmpty(furnace.getItem(RESULT_SLOT)) ? 0 : furnace.getItem(RESULT_SLOT).getCount();
                final int countInInputSlot = ItemStackUtils.isEmpty(furnace.getItem(SMELTABLE_SLOT)) ? 0 : furnace.getItem(SMELTABLE_SLOT).getCount();

                if ((!furnace.isLit() && countInResultSlot > 0)
                      || countInResultSlot > RETRIEVE_SMELTABLE_IF_MORE_THAN
                      || (countInResultSlot > 0 && countInInputSlot == 0))
                {
                    worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent(COM_MINECOLONIES_COREMOD_STATUS_RETRIEVING));
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
        final ItemListModule module = getOwnBuilding().getModuleMatching(ItemListModule.class, m -> m.getId().equals(FUEL_LIST));
        for (final BlockPos pos : getOwnBuilding().getFirstModuleOccurance(FurnaceUserModule.class).getFurnaces())
        {
            final TileEntity entity = world.getBlockEntity(pos);
            if (entity instanceof FurnaceTileEntity)
            {
                final FurnaceTileEntity furnace = (FurnaceTileEntity) entity;

                if (!furnace.getItem(FUEL_SLOT).isEmpty() && !module.isItemInList(new ItemStorage(furnace.getItem(FUEL_SLOT))))
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
    public IAIState startWorking()
    {
        if (walkToBuilding())
        {
            return getState();
        }

        final FurnaceUserModule furnaceModule = getOwnBuilding().getFirstModuleOccurance(FurnaceUserModule.class);
        final ItemListModule itemListModule = getOwnBuilding().getModuleMatching(ItemListModule.class, m -> m.getId().equals(FUEL_LIST));
        worker.getCitizenData().setVisibleStatus(VisibleCitizenStatus.WORKING);

        if (itemListModule.getList().isEmpty())
        {
            if (worker.getCitizenData() != null)
            {
                worker.getCitizenData().triggerInteraction(new StandardInteraction(new TranslationTextComponent(FURNACE_USER_NO_FUEL), ChatPriority.BLOCKING));
            }
            return getState();
        }

        if (furnaceModule.getFurnaces().isEmpty())
        {
            if (worker.getCitizenData() != null)
            {
                worker.getCitizenData()
                  .triggerInteraction(new StandardInteraction(new TranslationTextComponent(BAKER_HAS_NO_FURNACES_MESSAGE), ChatPriority.BLOCKING));
            }
            return getState();
        }

        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent(COM_MINECOLONIES_COREMOD_STATUS_DECIDING));

        final IAIState nextState = checkForImportantJobs();
        if (nextState != START_WORKING)
        {
            return nextState;
        }

        final BlockPos posOfUsedFuelOven = getPositionOfOvenToRetrieveFuelFrom();
        if (posOfUsedFuelOven != null)
        {
            walkTo = posOfUsedFuelOven;
            worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.retrieving"));
            return RETRIEVING_USED_FUEL_FROM_FURNACE;
        }

        final BlockPos posOfOven = getPositionOfOvenToRetrieveFrom();
        if (posOfOven != null)
        {
            walkTo = posOfOven;
            worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.retrieving"));
            return RETRIEVING_END_PRODUCT_FROM_FURNACE;
        }

        final int amountOfSmeltableInBuilding = InventoryUtils.getCountFromBuilding(getOwnBuilding(), this::isSmeltable);
        final int amountOfSmeltableInInv = InventoryUtils.getItemCountInItemHandler((worker.getInventoryCitizen()), this::isSmeltable);

        final int amountOfFuelInBuilding = InventoryUtils.getCountFromBuilding(getOwnBuilding(), itemListModule.getList());
        final int amountOfFuelInInv =
          InventoryUtils.getItemCountInItemHandler((worker.getInventoryCitizen()), stack -> itemListModule.isItemInList(new ItemStorage(stack)));

        if (amountOfSmeltableInBuilding + amountOfSmeltableInInv <= 0 && !reachedMaxToKeep())
        {
            requestSmeltable();
        }

        if (amountOfFuelInBuilding + amountOfFuelInInv <= 0 && !getOwnBuilding().hasWorkerOpenRequestsFiltered(worker.getCitizenData().getId(),
          req -> req.getShortDisplayString().getSiblings().contains(new TranslationTextComponent(RequestSystemTranslationConstants.REQUESTS_TYPE_BURNABLE))))
        {
            worker.getCitizenData().createRequestAsync(new StackList(getAllowedFuel(), RequestSystemTranslationConstants.REQUESTS_TYPE_BURNABLE, STACKSIZE * furnaceModule.getFurnaces().size(), 1));
        }

        if (amountOfSmeltableInBuilding > 0 && amountOfSmeltableInInv == 0)
        {
            needsCurrently = new Tuple<>(this::isSmeltable, STACKSIZE);
            return GATHERING_REQUIRED_MATERIALS;
        }
        else if (amountOfFuelInBuilding > 0 && amountOfFuelInInv == 0)
        {
            needsCurrently = new Tuple<>(stack -> itemListModule.isItemInList(new ItemStorage(stack)), STACKSIZE);
            return GATHERING_REQUIRED_MATERIALS;
        }

        return checkIfAbleToSmelt(amountOfFuelInBuilding + amountOfFuelInInv, amountOfSmeltableInBuilding + amountOfSmeltableInInv);
    }

    /**
     * Get a copy of the list of allowed fuel.
     * @return the list.
     */
    private List<ItemStack> getAllowedFuel()
    {
        final List<ItemStack> list = new ArrayList<>();
        for (final ItemStorage storage : getOwnBuilding().getModuleMatching(ItemListModule.class, m -> m.getId().equals(FUEL_LIST)).getList())
        {
            final ItemStack stack = storage.getItemStack().copy();
            stack.setCount(stack.getMaxStackSize());
            list.add(stack);
        }
        return list;
    }

    /**
     * Actually accelerate the furnaces
     */
    private IAIState accelerateFurnaces()
    {
        final int accelerationTicks = (worker.getCitizenData().getCitizenSkillHandler().getLevel(getModuleForJob().getPrimarySkill()) / 10) * 2;
        final World world = getOwnBuilding().getColony().getWorld();
        for (final BlockPos pos : getOwnBuilding().getFirstModuleOccurance(FurnaceUserModule.class).getFurnaces())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final TileEntity entity = world.getBlockEntity(pos);
                if (entity instanceof FurnaceTileEntity)
                {
                    final FurnaceTileEntity furnace = (FurnaceTileEntity) entity;
                    for(int i = 0; i < accelerationTicks; i++)
                    {
                        if (furnace.isLit()) 
                        {
                            furnace.tick();
                        }
                    }
                }
            }
        }
        return null;
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
        final FurnaceUserModule module = getOwnBuilding().getFirstModuleOccurance(FurnaceUserModule.class);
        for (final BlockPos pos : module.getFurnaces())
        {
            final TileEntity entity = world.getBlockEntity(pos);

            if (entity instanceof FurnaceTileEntity)
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
            else
            {
                if (!(world.getBlockState(pos).getBlock() instanceof FurnaceBlock))
                {
                    module.removeFromFurnaces(pos);
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

        final TileEntity entity = world.getBlockEntity(walkTo);
        if (!(entity instanceof FurnaceTileEntity)
              || (ItemStackUtils.isEmpty(((FurnaceTileEntity) entity).getItem(RESULT_SLOT))))
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
     * Retrieve used fuel from the furnaces. If no position has been set return. Else navigate to the position of the furnace. On arrival execute the extract method of the
     * specialized worker.
     *
     * @return the next state to go to.
     */
    private IAIState retrieveUsedFuel()
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

        final TileEntity entity = world.getBlockEntity(walkTo);
        if (!(entity instanceof FurnaceTileEntity)
                || (ItemStackUtils.isEmpty(((FurnaceTileEntity) entity).getItem(FUEL_SLOT))))
        {
            walkTo = null;
            return START_WORKING;
        }

        walkTo = null;

        extractFuelFromFurnace((FurnaceTileEntity) entity);
        return START_WORKING;
    }

    /**
     * Smelt the smeltable after the required items are in the inv.
     *
     * @return the next state to go to.
     */
    private IAIState fillUpFurnace()
    {
        if (getOwnBuilding().getFirstModuleOccurance(FurnaceUserModule.class).getFurnaces().isEmpty())
        {
            if (worker.getCitizenData() != null)
            {
                worker.getCitizenData()
                  .triggerInteraction(new StandardInteraction(new TranslationTextComponent(BAKER_HAS_NO_FURNACES_MESSAGE), ChatPriority.BLOCKING));
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

        final TileEntity entity = world.getBlockEntity(walkTo);
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

            final ItemListModule module = getOwnBuilding().getModuleMatching(ItemListModule.class, m -> m.getId().equals(FUEL_LIST));
            if (InventoryUtils.hasItemInItemHandler((worker.getInventoryCitizen()), stack -> module.isItemInList(new ItemStorage(stack)))
                  && (hasSmeltableInFurnaceAndNoFuel(furnace) || hasNeitherFuelNorSmeltAble(furnace)))
            {
                InventoryUtils.transferXOfFirstSlotInItemHandlerWithIntoInItemHandler(
                  (worker.getInventoryCitizen()), stack -> module.isItemInList(new ItemStorage(stack)), STACKSIZE,
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
