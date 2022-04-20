package com.minecolonies.coremod.entity.ai.citizen.alchemist;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
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
import com.minecolonies.coremod.colony.buildings.modules.ItemListModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingAlchemist;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteraction;
import com.minecolonies.coremod.colony.jobs.JobAlchemist;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import net.minecraft.block.Blocks;
import net.minecraft.block.BrewingStandBlock;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.BrewingStandTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.ItemStackUtils.*;
import static com.minecolonies.api.util.constant.BuildingConstants.FUEL_LIST;
import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Crafts brewing recipes.
 */
public class EntityAIWorkAlchemist extends AbstractEntityAICrafting<JobAlchemist, BuildingAlchemist>
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
     * @param alchemistJob the job he has.
     */
    public EntityAIWorkAlchemist(@NotNull final JobAlchemist alchemistJob)
    {
        super(alchemistJob);
        super.registerTargets(
          /*
           * Check if tasks should be executed.
           */
          new AIEventTarget(AIBlockingEventType.EVENT, this::isFuelNeeded, this::checkBrewingStandFuel, TICKS_SECOND * 10),
          new AIEventTarget(AIBlockingEventType.EVENT, this::accelerateBrewingStand, this::getState, TICKS_SECOND),
          new AITarget(START_USING_FURNACE, this::fillUpbrewingStand, TICKS_SECOND),
          new AITarget(RETRIEVING_END_PRODUCT_FROM_FURNACE, this::retrieveSmeltableFromBrewingStand, TICKS_SECOND),
          new AITarget(RETRIEVING_USED_FUEL_FROM_FURNACE, this::retrieveUsedFuel, TICKS_SECOND),
          new AITarget(ADD_FUEL_TO_FURNACE, this::addFuelToBrewingStand, TICKS_SECOND)
        );
    }

    @Override
    protected IAIState decide()
    {
        if (job.getCurrentTask() == null)
        {
            return IDLE;
        }

        if (walkToBuilding())
        {
            return START_WORKING;
        }

        if (job.getActionsDone() >= getActionsDoneUntilDumping())
        {
            // Wait to dump before continuing.
            return getState();
        }

        return getNextCraftingState();
    }

    //todo list: We want the workermodel, the AI

    //todo craft on demand (RS), and harvest/plant netherwart randomly && harvest mistletoe randomly (small chance for mistletoe).

    //todo we need special fuel handling here.

    @Override
    public Class<BuildingAlchemist> getExpectedBuildingClass()
    {
        return BuildingAlchemist.class;
    }

    @Override
    protected int getExtendedCount(final ItemStack stack)
    {
        if (currentRecipeStorage != null && currentRecipeStorage.getIntermediate() == Blocks.BREWING_STAND)
        {
            int count = 0;
            for (final BlockPos pos : getOwnBuilding().getAllBrewingStandPositions())
            {
                if (WorldUtil.isBlockLoaded(world, pos))
                {
                    final TileEntity entity = world.getBlockEntity(pos);
                    if (entity instanceof BrewingStandTileEntity)
                    {
                        final BrewingStandTileEntity brewingStand = (BrewingStandTileEntity) entity;

                        for (int slot = 0; slot < 4; slot++)
                        {
                            final ItemStack stackInSlot = brewingStand.getItem(slot);
                            if (ItemStackUtils.compareItemStacksIgnoreStackSize(stack, stackInSlot))
                            {
                                count += stackInSlot.getCount();
                            }
                        }
                    }
                    else
                    {
                        getOwnBuilding().removeBrewingStand(pos);
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
            worker.setItemInHand(Hand.MAIN_HAND, ItemStackUtils.EMPTY);
            return START_WORKING;
        }

        job.setMaxCraftingCount(currentTask.getRequest().getCount());

        final BlockPos brewingStandPosWithUsedFuel = getPositionOfOvenToRetrieveFuelFrom();
        if (brewingStandPosWithUsedFuel != null)
        {
            currentRequest = currentTask;
            walkTo = brewingStandPosWithUsedFuel;
            return RETRIEVING_USED_FUEL_FROM_FURNACE;
        }

        final BlockPos brewingStandPos = getPositionOfOvenToRetrieveFrom();
        if (brewingStandPos != null)
        {
            currentRequest = currentTask;
            walkTo = brewingStandPos;
            return RETRIEVING_END_PRODUCT_FROM_FURNACE;
        }

        if(currentRecipeStorage != null && currentRecipeStorage.getIntermediate() == Blocks.BREWING_STAND)
        {
            for (final BlockPos pos : getOwnBuilding().getAllBrewingStandPositions())
            {
                final TileEntity entity = world.getBlockEntity(pos);
                if (entity instanceof BrewingStandTileEntity)
                {
                    //todo check the isLit check as well but with the brewTime
                    final BrewingStandTileEntity brewingStand = (BrewingStandTileEntity) entity;
                    if (brewingStand.brewTime > 0 || !isEmpty(brewingStand.getItem(RESULT_SLOT)) || !isEmpty(brewingStand.getItem(SMELTABLE_SLOT)))
                    {
                        return CRAFT;
                    }
                }
                else
                {
                    getOwnBuilding().removeBrewingStand(pos);
                }
            }
        }
        return super.getRecipe();
    }

    /**
     * Check to see how many furnaces are still processing
     * @return the count.
     */
    private int countOfBurningFurnaces()
    {
        int count = 0;
        final World world = getOwnBuilding().getColony().getWorld();
        for (final BlockPos pos : getOwnBuilding().getAllBrewingStandPositions())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final TileEntity entity = world.getBlockEntity(pos);
                if (entity instanceof BrewingStandTileEntity)
                {
                    final BrewingStandTileEntity brewingStand = (BrewingStandTileEntity) entity;
                    if (brewingStand.brewTime > 0)
                    {
                        count += 1;
                    }
                }
                else
                {
                    getOwnBuilding().removeBrewingStand(pos);
                }
            }
        }
        return count;
    }

    /**
     * Actually accelerate the brewingStand
     */
    private boolean accelerateBrewingStand()
    {
        final int accelerationTicks = (worker.getCitizenData().getCitizenSkillHandler().getLevel(getModuleForJob().getSecondarySkill()) / 10) * 2;
        final World world = getOwnBuilding().getColony().getWorld();
        for (final BlockPos pos : getOwnBuilding().getAllBrewingStandPositions())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final TileEntity entity = world.getBlockEntity(pos);
                if (entity instanceof BrewingStandTileEntity)
                {
                    final BrewingStandTileEntity brewingStand = (BrewingStandTileEntity) entity;
                    for(int i = 0; i < accelerationTicks; i++)
                    {
                        if (brewingStand.brewTime > 0)
                        {
                            brewingStand.tick();
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Quick check to see if there is a brewingStand that needs fuel
     */
    private boolean isFuelNeeded()
    {
        for (final BlockPos pos : getOwnBuilding().getAllBrewingStandPositions())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final TileEntity entity = world.getBlockEntity(pos);
                if(!(entity instanceof BrewingStandTileEntity))
                {
                    getOwnBuilding().removeBrewingStand(pos);
                    continue;
                }
                final BrewingStandTileEntity brewingStand = (BrewingStandTileEntity) entity;
                if (brewingStand.brewTime <= 0
                      && (hasSmeltableInFurnaceAndNoFuel(brewingStand) || hasNeitherFuelNorSmeltAble(brewingStand))
                      && currentRecipeStorage != null && currentRecipeStorage.getIntermediate() == Blocks.FURNACE)
                {
                    //We only want to return true if we're not already gathering materials.
                    return getState() != GATHERING_REQUIRED_MATERIALS;
                }
            }
        }
        return false;
    }

    /**
     * Check Fuel levels in the brewingStand
     */
    private IAIState checkBrewingStandFuel()
    {
        final World world = getOwnBuilding().getColony().getWorld();

        if(!InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(), Items.BLAZE_POWDER) && !InventoryUtils.hasItemInProvider(getOwnBuilding(), Items.BLAZE_POWDER) && !getOwnBuilding().hasWorkerOpenRequestsOfType(worker.getCitizenData().getId(), TypeToken.of(StackList.class)) && currentRecipeStorage != null && currentRecipeStorage.getIntermediate() == Blocks.FURNACE )
        {
            worker.getCitizenData().createRequestAsync(new Stack(new ItemStack(Items.BLAZE_POWDER), STACKSIZE * getOwnBuilding().getAllBrewingStandPositions().size(), 1));
            return getState();
        }

        for (final BlockPos pos : getOwnBuilding().getAllBrewingStandPositions())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final TileEntity entity = world.getBlockEntity(pos);
                if (entity instanceof BrewingStandTileEntity)
                {
                    final BrewingStandTileEntity brewingStand = (BrewingStandTileEntity) entity;
                    if (brewingStand.brewTime <= 0 && (hasSmeltableInFurnaceAndNoFuel(brewingStand) || hasNeitherFuelNorSmeltAble(brewingStand)) && currentRecipeStorage != null && currentRecipeStorage.getIntermediate() == Blocks.FURNACE)
                    {
                        if (!InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(), Items.BLAZE_POWDER))
                        {
                            if(InventoryUtils.hasItemInProvider(getOwnBuilding(), Items.BLAZE_POWDER))
                            {
                                needsCurrently = new Tuple<>(item -> item.getItem() == Items.BLAZE_POWDER, STACKSIZE);
                                walkTo = null;
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
                else
                {
                    getOwnBuilding().removeBrewingStand(pos);
                }
            }
        }
        return getState();
    }

    /**
     * Add brewing stand fuel when necessary
     * @return
     */
    private IAIState addFuelToBrewingStand()
    {
        if(!InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(), Items.BLAZE_POWDER))
        {
            if (InventoryUtils.hasItemInProvider(getOwnBuilding(), Items.BLAZE_POWDER))
            {
                needsCurrently = new Tuple<>(item -> item.getItem() == Items.BLAZE_POWDER, STACKSIZE);
                return GATHERING_REQUIRED_MATERIALS;
            }
            //We shouldn't get here, unless something changed between the checkBrewingStandFuel and the addFueltoBrewingStand calls
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
            final TileEntity entity = world.getBlockEntity(fuelPos);
            if (entity instanceof BrewingStandTileEntity)
            {
                final BrewingStandTileEntity brewingStand = (BrewingStandTileEntity) entity;
                //Stoke the brewing stands
                if (InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(), Items.BLAZE_POWDER)
                        && (hasSmeltableInFurnaceAndNoFuel(brewingStand) || hasNeitherFuelNorSmeltAble(brewingStand)))
                {
                    InventoryUtils.transferXOfFirstSlotInItemHandlerWithIntoInItemHandler(
                        worker.getInventoryCitizen(), item -> item.getItem() == Items.BLAZE_POWDER, STACKSIZE,
                        new InvWrapper(brewingStand), BREWING_FUEL_SLOT);
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

    private int getMaxUsableBrewingStands()
    {
        final int maxSkillBrewingStand = (worker.getCitizenData().getCitizenSkillHandler().getLevel(getModuleForJob().getPrimarySkill()) / 10) + 1;
        return Math.min(maxSkillBrewingStand, getOwnBuilding().getAllBrewingStandPositions().size());
    }

    /**
     * Get the brewingStand which has finished smeltables. For this check each brewingStand which has been registered to the building. Check if the brewingStand is turned off and has something in
     * the result slot or check if the brewingStand has more than x results.
     *
     * @return the position of the brewingStand.
     */
    private BlockPos getPositionOfOvenToRetrieveFrom()
    {
        for (final BlockPos pos : getOwnBuilding().getAllBrewingStandPositions())
        {
            final TileEntity entity = world.getBlockEntity(pos);
            if (entity instanceof BrewingStandTileEntity)
            {
                final BrewingStandTileEntity brewingStand = (BrewingStandTileEntity) entity;
                int countInResultSlot = 0;
                boolean fullResult = false;
                if (!isEmpty(brewingStand.getItem(RESULT_SLOT)))
                {
                    countInResultSlot = brewingStand.getItem(RESULT_SLOT).getCount();
                    fullResult = countInResultSlot >= brewingStand.getItem(RESULT_SLOT).getMaxStackSize();
                }

                if (fullResult || (brewingStand.brewTime <= 0 && countInResultSlot > 0 && isEmpty(brewingStand.getItem(SMELTABLE_SLOT))))
                {
                    worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent(COM_MINECOLONIES_COREMOD_STATUS_RETRIEVING));
                    return pos;
                }
            }
        }
        return null;
    }

    /**
     * Get the brewingStand which has used fuel. For this check each brewingStand which has been registered to the building. Check if the brewingStand has used fuel in the fuel slot.
     *
     * @return the position of the brewingStand.
     */
    protected BlockPos getPositionOfOvenToRetrieveFuelFrom()
    {
        for (final BlockPos pos : getOwnBuilding().getAllBrewingStandPositions())
        {
            final TileEntity entity = world.getBlockEntity(pos);
            if (entity instanceof BrewingStandTileEntity)
            {
                final BrewingStandTileEntity brewingStand = (BrewingStandTileEntity) entity;

                if (!brewingStand.getItem(BREWING_FUEL_SLOT).isEmpty() && !compareItemStackListIgnoreStackSize(getAllowedFuel(), brewingStand.getItem(BREWING_FUEL_SLOT), false, false))
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
        if (storage.getIntermediate() != Blocks.BREWING_STAND)
        {
            return super.checkForItems(storage);
        }

        final List<ItemStorage> input = storage.getCleanedInput();
        final int countInBewingStand = getExtendedCount(storage.getPrimaryOutput());
        int outputInInv = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, storage.getPrimaryOutput()));

        for (final ItemStorage inputStorage : input)
        {
            final Predicate<ItemStack> predicate = stack -> !ItemStackUtils.isEmpty(stack) && ItemStackUtils.compareItemStacksIgnoreStackSize(stack, inputStorage.getItemStack());
            int inputInBrewingStand = getExtendedCount(inputStorage.getItemStack());
            int inputInInv = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), predicate);

            if (countInBewingStand + inputInBrewingStand + inputInInv + outputInInv < inputStorage.getAmount() * job.getMaxCraftingCount())
            {
                if (InventoryUtils.hasItemInProvider(getOwnBuilding(), predicate))
                {
                    needsCurrently = new Tuple<>(predicate, inputStorage.getAmount() * (job.getMaxCraftingCount() - countInBewingStand - inputInBrewingStand));
                    return GATHERING_REQUIRED_MATERIALS;
                }
            }

            //if we don't have enough at all, cancel
            int countOfInput = inputInInv + InventoryUtils.getCountFromBuilding(getOwnBuilding(), predicate) + countInBewingStand + inputInBrewingStand + outputInInv;
            if (countOfInput < inputStorage.getAmount() * job.getMaxCraftingCount())
            {
                job.finishRequest(false);
                resetValues();
            }
        }

        return CRAFT;
    }

    /**
     * Retrieve ready bars from the brewingStand. If no position has been set return. Else navigate to the position of the brewingStand. On arrival execute the extract method of the
     * specialized worker.
     *
     * @return the next state to go to.
     */
    private IAIState retrieveSmeltableFromBrewingStand()
    {
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent(COM_MINECOLONIES_COREMOD_STATUS_RETRIEVING));

        if (walkTo == null || currentRequest == null)
        {
            return START_WORKING;
        }

        final TileEntity entity = world.getBlockEntity(walkTo);
        if (!(entity instanceof BrewingStandTileEntity) || (isEmpty(((BrewingStandTileEntity) entity).getItem(RESULT_SLOT))))
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

        extractFromBrewingStandSlot((BrewingStandTileEntity) entity, RESULT_SLOT);
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
     * Retrieve used fuel from the brewingStand. If no position has been set return. Else navigate to the position of the brewingStand. On arrival execute the extract method of the
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
        if (!(entity instanceof BrewingStandTileEntity)
                || (ItemStackUtils.isEmpty(((BrewingStandTileEntity) entity).getItem(BREWING_FUEL_SLOT))))
        {
            walkTo = null;
            return START_WORKING;
        }

        walkTo = null;

        extractFromBrewingStandSlot((BrewingStandTileEntity) entity, BREWING_FUEL_SLOT);
        return START_WORKING;
    }

    /**
     * Very simple action, straightly extract from a brewingStand slot.
     *
     * @param brewingStand the brewingStand to retrieve from.
     */
    private void extractFromBrewingStandSlot(final BrewingStandTileEntity brewingStand, final int slot)
    {
        InventoryUtils.transferItemStackIntoNextFreeSlotInItemHandler(
          new InvWrapper(brewingStand), slot,
          worker.getInventoryCitizen());
        if (slot == RESULT_SLOT)
        {
            worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
        }
    }

    /**
     * Checks if the brewingStand are ready to start smelting.
     *
     * @return START_USING_FURNACE if enough, else check for additional worker specific jobs.
     */
    private IAIState checkIfAbleToSmelt()
    {
        // We're fully committed currently, try again later.
        final int burning = countOfBurningFurnaces();
        if(burning > 0 && (burning >= getMaxUsableBrewingStands() || (job.getCraftCounter() + job.getProgress() ) >= job.getMaxCraftingCount()))
        {
            setDelay(TICKS_SECOND);
            return getState();            
        }

        for (final BlockPos pos : getOwnBuilding().getAllBrewingStandPositions())
        {
            final TileEntity entity = world.getBlockEntity(pos);

            if (entity instanceof BrewingStandTileEntity)
            {
                if (isEmpty(((BrewingStandTileEntity) entity).getItem(SMELTABLE_SLOT)))
                {
                    walkTo = pos;
                    return START_USING_FURNACE;
                }
            }
            else
            {
                getOwnBuilding().removeBrewingStand(pos);
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
    private IAIState fillUpbrewingStand()
    {
        if (getOwnBuilding().getAllBrewingStandPositions().isEmpty())
        {
            if (worker.getCitizenData() != null)
            {
                worker.getCitizenData().triggerInteraction(new StandardInteraction(new TranslationTextComponent(BAKER_HAS_NO_FURNACES_MESSAGE), ChatPriority.BLOCKING));
            }
            setDelay(STANDARD_DELAY);
            return START_WORKING;
        }

        if (walkTo == null || world.getBlockState(walkTo).getBlock() != Blocks.BREWING_STAND)
        {
            walkTo = null;
            setDelay(STANDARD_DELAY);
            return START_WORKING;
        }

        final int burningCount = countOfBurningFurnaces();
        final TileEntity entity = world.getBlockEntity(walkTo);
        if (entity instanceof BrewingStandTileEntity && currentRecipeStorage != null)
        {
            final BrewingStandTileEntity brewingStand = (BrewingStandTileEntity) entity;
            final int maxFurnaces = getMaxUsableBrewingStands();
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

            if (worker.getItemInHand(Hand.MAIN_HAND).isEmpty())
            {
                worker.setItemInHand(Hand.MAIN_HAND, currentRecipeStorage.getCleanedInput().get(0).getItemStack().copy());
            }
            if (amountOfSmeltableInInv > 0)
            {
                if (hasFuelInFurnaceAndNoSmeltable(brewingStand) || hasNeitherFuelNorSmeltAble(brewingStand))
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
                          new InvWrapper(brewingStand),
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
        else if (!(world.getBlockState(walkTo).getBlock() instanceof BrewingStandBlock))
        {
            getOwnBuilding().removeBrewingStand(walkTo);
        }
        walkTo = null;
        setDelay(STANDARD_DELAY);
        return START_WORKING;
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

    @Override
    protected IAIState craft()
    {
        if (walkToBuilding())
        {
            setDelay(STANDARD_DELAY);
            return getState();
        }
        
        if(currentRecipeStorage != null && currentRequest == null)
        {
            currentRequest = job.getCurrentTask();
        }

        if (currentRecipeStorage != null && currentRecipeStorage.getIntermediate() != Blocks.BREWING_STAND)
        {
            return super.craft();
        }

        if (getOwnBuilding().getAllBrewingStandPositions().isEmpty())
        {
            if (worker.getCitizenData() != null)
            {
                worker.getCitizenData().triggerInteraction(new StandardInteraction(new TranslationTextComponent(BAKER_HAS_NO_FURNACES_MESSAGE), ChatPriority.BLOCKING));
            }
            setDelay(STANDARD_DELAY);
            return START_WORKING;
        }

        final BlockPos furnacePosWithUsedFuel = getPositionOfOvenToRetrieveFuelFrom();
        if (furnacePosWithUsedFuel != null)
        {
            walkTo = furnacePosWithUsedFuel;
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

        // Safety net, should get caught removing things from the brewingStand.
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
