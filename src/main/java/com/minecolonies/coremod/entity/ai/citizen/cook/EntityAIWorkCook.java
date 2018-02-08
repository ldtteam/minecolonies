package com.minecolonies.coremod.entity.ai.citizen.cook;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.requestable.Burnable;
import com.minecolonies.api.colony.requestsystem.requestable.Food;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.buildings.BuildingCook;
import com.minecolonies.coremod.colony.jobs.JobCook;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAISkill;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_ENTITY_BAKER_NO_FURNACES;
import static com.minecolonies.api.util.constant.TranslationConstants.HUNGRY_INV_FULL;
import static com.minecolonies.coremod.entity.ai.util.AIState.COOK_COOK_FOOD;
import static com.minecolonies.coremod.entity.ai.util.AIState.COOK_GATHER_FOOD_FROM_BUILDING;
import static com.minecolonies.coremod.entity.ai.util.AIState.COOK_GATHER_FUEL;
import static com.minecolonies.coremod.entity.ai.util.AIState.COOK_GATHER_COOKED_FOOD_FROM_FURNACE;
import static com.minecolonies.coremod.entity.ai.util.AIState.COOK_SERVE_FOOD_TO_CITIZEN;
import static com.minecolonies.coremod.entity.ai.util.AIState.IDLE;
import static com.minecolonies.coremod.entity.ai.util.AIState.START_WORKING;

/**
 * Cook AI class.
 */
public class EntityAIWorkCook extends AbstractEntityAISkill<JobCook>
{
    /**
     * How often should charisma factor into the cook's skill modifier.
     */
    private static final int CHARISMA_MULTIPLIER = 2;

    /**
     * How often should intelligence factor into the cook's skill modifier.
     */
    private static final int INTELLIGENCE_MULTIPLIER = 1;

    /**
     * This times the building level the worker has to keep.
     */
    private static final int LEAST_KEEP_FOOD_MULTIPLIER = 64;

    /**
     * The amount of food which should be served to the woker.
     */
    private static final int AMOUNT_OF_FOOD_TO_SERVE = 3;

    /**
     * Delay between each serving.
     */
    private static final int SERVE_DELAY = 30;

    /**
     * The standard delay after each terminated action.
     */
    private static final int STANDARD_DELAY = 5;

    /**
     * Wait this amount of ticks after requesting a burnable material.
     */
    private static final int WAIT_AFTER_REQUEST = 400;

    /**
     * Slot with the result of the furnace.
     */
    private static final int RESULT_SLOT = 2;

    /**
     * Slot where cookables should be put in the furnace.
     */
    public static final int COOK_SLOT = 0;

    /**
     * Slot where the fuel should be put in the furnace.
     */
    private static final int FUEL_SLOT                                 = 1;

    /**
     * The citizen the worker is currently trying to serve.
     */
    private final List<EntityCitizen> citizenToServe = new ArrayList<>();

    /**
     * The current position the worker should walk to.
     */
    private BlockPos walkTo = null;

    /**
     * The building range the cook should search for clients.
     */
    private AxisAlignedBB range = null;

    /**
     * What he currently might be needing.
     */
    private Predicate<ItemStack> needsCurrently = null;

    /**
     * Constructor for the Cook.
     * Defines the tasks the cook executes.
     *
     * @param job a cook job to use.
     */
    public EntityAIWorkCook(@NotNull final JobCook job)
    {
        super(job);
        super.registerTargets(
                new AITarget(IDLE, START_WORKING),
                new AITarget(START_WORKING, this::startWorking),
                new AITarget(COOK_GATHER_FOOD_FROM_BUILDING, this::gatherFoodFromBuilding),
                new AITarget(COOK_COOK_FOOD, this::cookFood),
                new AITarget(COOK_SERVE_FOOD_TO_CITIZEN, this::serveFoodToCitizen),
                new AITarget(COOK_GATHER_COOKED_FOOD_FROM_FURNACE, this::gatherCookedFoodFromFurnace),
                new AITarget(COOK_GATHER_FUEL, this::gatherFuel)
        );
        worker.setSkillModifier(CHARISMA_MULTIPLIER * worker.getCitizenData().getCharisma()
                + INTELLIGENCE_MULTIPLIER * worker.getCitizenData().getIntelligence());
        worker.setCanPickUpLoot(true);
    }

    /**
     * Find Fuel.
     *
     * If not walking and we need to walk to building, repeat this state.
     * If building has fuel or is requesting fuel, wait a while, then COOK_COOK_FOOD
     * If no fuel in building, transition to START_WORKING.
     * If fuel in building but not nearby, walk toward it and repeat this state.
     * If near fuel in building, pick up fuel.
     * If fuel was no longer available, transition to START_WORKING.
     * If we have fuel, then COOK_COOK_FOOD
     *
     * @return next AIState
     */
    private AIState gatherFuel()
    {
        // TODO: set status to getting fuel

        // TODO: check if we already have the fuel in our inventory. If so, transition to COOK_FOOD immediately.

        if (walkTo == null && walkToBuilding())
        {
            return getState();
        }

        if (!InventoryUtils.hasItemInProvider(getOwnBuilding(), TileEntityFurnace::isItemFuel))
        {
            if (!getOwnBuilding().hasWorkerOpenRequestsOfType(worker.getCitizenData(), TypeToken.of(Burnable.class)))
            {
                worker.getCitizenData().createRequestAsync(new Burnable(Constants.STACKSIZE));
            }
            setDelay(WAIT_AFTER_REQUEST);
        }
        else
        {
            if (walkTo == null)
            {
                final BlockPos pos = getOwnBuilding().getTileEntity().getPositionOfChestWithItemStack(TileEntityFurnace::isItemFuel);
                if (pos == null)
                {
                    return START_WORKING;
                }
                walkTo = pos;
            }

            if (walkToBlock(walkTo))
            {
                return getState();
            }

            final boolean transfered = tryTransferFromPosToWorker(walkTo, TileEntityFurnace::isItemFuel);
            if (!transfered)
            {
                walkTo = null;
                return START_WORKING;
            }
            walkTo = null;
        }

        return COOK_COOK_FOOD;
    }

    /**
     * Retrieve cooked food from furnace and put more fuel in if needed.
     *
     * If not going anywhere, transition to START_WORKING.
     * If we need to walk to building, repeat this state.
     * If not a furnace target or furnace cooking or furnace is missing both uncooked and cooked food, transition to START_WORKING.
     * If cooked food in furnace, transfer to inventory.
     * If no fuel in furnace, and no fuel in inventory, then COOK_RETRIEVE_FUEL.
     * If no fuel in furnace, and fuel in inventory, transfer fuel to furnace, and delay and transition to START_WORKING
     * If fuel in furnace, delay and transition to START_WORKING
     *
     * @return next AIState
     */
    private AIState gatherCookedFoodFromFurnace()
    {
        // TODO: set status to retrieve food from furnace

        if (walkTo == null)
        {
            return START_WORKING;
        }

        if (walkToBlock(walkTo))
        {
            return getState();
        }

        final TileEntity entity = world.getTileEntity(walkTo);
        if (!(entity instanceof TileEntityFurnace)
                || ((TileEntityFurnace) entity).isBurning()
                || (ItemStackUtils.isEmpty(((TileEntityFurnace) entity).getStackInSlot(RESULT_SLOT))
                && ItemStackUtils.isEmpty(((TileEntityFurnace) entity).getStackInSlot(COOK_SLOT))))
        {
            walkTo = null;
            return START_WORKING;
        }
        walkTo = null;

        InventoryUtils.transferItemStackIntoNextFreeSlotInItemHandlers(
                new InvWrapper((TileEntityFurnace) entity), RESULT_SLOT,
                new InvWrapper(worker.getInventoryCitizen()));

        if (!ItemStackUtils.isEmpty(((TileEntityFurnace) entity).getStackInSlot(COOK_SLOT)))
        {
            if (!InventoryUtils.hasItemInItemHandler(new InvWrapper(worker.getInventoryCitizen()), TileEntityFurnace::isItemFuel))
            {
                walkTo = null;
                return COOK_GATHER_FUEL;
            }

            InventoryUtils.transferItemStackIntoNextFreeSlotInItemHandlers(
                    new InvWrapper(worker.getInventoryCitizen()),
                    InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(worker.getInventoryCitizen()), TileEntityFurnace::isItemFuel),
                    new InvWrapper((TileEntityFurnace) entity));
        }

        incrementActionsDone();
        setDelay(STANDARD_DELAY);
        return START_WORKING;
    }

    /**
     * Retrieve food from building in order to serve it
     *
     * If we have food, then COOK_SERVE.
     * If no food in the building, transition to START_WORKING.
     * If we need to walk to the food's location, repeat this state.
     * If we were able to get the stored food, then COOK_SERVE.
     * If food is no longer available, delay and transition to START_WORKING.
     *
     * @return next AIState
     */
    private AIState gatherFoodFromBuilding()
    {
        // TODO: set status to retrieve food from storage to serve it

        if (needsCurrently == null)
        {
            needsCurrently = ItemStackUtils.ISFOOD;
        }

        if (InventoryUtils.hasItemInItemHandler(new InvWrapper(worker.getInventoryCitizen()), needsCurrently))
        {
            return COOK_SERVE_FOOD_TO_CITIZEN;
        }

        final BlockPos pos = getOwnBuilding().getTileEntity().getPositionOfChestWithItemStack(needsCurrently);
        if (pos == null)
        {
            return START_WORKING;
        }

        if (walkToBlock(pos))
        {
            return getState();
        }

        final boolean transfered = tryTransferFromPosToWorker(pos, needsCurrently);
        if (transfered)
        {
            return COOK_SERVE_FOOD_TO_CITIZEN;
        }
        setDelay(STANDARD_DELAY);
        return START_WORKING;
    }

    /**
     * Serve food to customer
     *
     * If no customer, transition to START_WORKING.
     * If we need to walk to the customer, repeat this state with tiny delay.
     * If the customer has a full inventory, report and remove customer, delay and repeat this state.
     * If we have food, then COOK_SERVE.
     * If no food in the building, transition to START_WORKING.
     * If we were able to get the stored food, then COOK_SERVE.
     * If food is no longer available, delay and transition to START_WORKING.
     * Otherwise, give the customer some food, then delay and repeat this state.
     *
     * @return next AIState
     */
    private AIState serveFoodToCitizen()
    {
        // TODO: set status to server food

        if (citizenToServe.isEmpty())
        {
            return START_WORKING;
        }

        if (walkToBlock(citizenToServe.get(0).getPosition()))
        {
            setDelay(2);
            return getState();
        }

        if (InventoryUtils.isItemHandlerFull(new InvWrapper(citizenToServe.get(0).getInventoryCitizen())))
        {
            chatSpamFilter.talkWithoutSpam(HUNGRY_INV_FULL);
            citizenToServe.remove(0);
            setDelay(SERVE_DELAY);
            return getState();
        }
        InventoryUtils.transferXOfFirstSlotInItemHandlerWithIntoNextFreeSlotInItemHandler(
                new InvWrapper(worker.getInventoryCitizen()),
                ItemStackUtils.ISFOOD,
                AMOUNT_OF_FOOD_TO_SERVE, new InvWrapper(citizenToServe.get(0).getInventoryCitizen())
                );

        citizenToServe.remove(0);
        setDelay(SERVE_DELAY);
        return getState();
    }

    /**
     * Find a furnace and cook raw food in it.
     *
     * If no furnaces, report and transition to START_WORKING.
     * If we have no cookable food and either we are going nowhere || where we are going is not a furnace, COOK_GATHERING some kind of cookable food
     * if we are going nowhere, find an empty furnace and walk to it, then repeat this state.
     * If there are no empty furnaces, transition to START_WORKING.
     * If we are at a furnace, attempt to put our uncooked food in it.
     * If the furnace has no fuel and we have no fuel, then COOK_RETRIEVE_FUEL
     * If the furnace has no fuel and we have fuel, then put fuel in and transition to START_WORKING.
     * If the furnace has fuel, transition to START_WORKING.
     * If we walked to the furnace and it's not there, then COOK_COOK_FOOD.
     *
     * @return next AIState
     */
    private AIState cookFood()
    {
        // TODO: set status to cook food

        if (((BuildingCook) getOwnBuilding()).getFurnaces().isEmpty())
        {
            chatSpamFilter.talkWithoutSpam(COM_MINECOLONIES_COREMOD_ENTITY_BAKER_NO_FURNACES);
            return START_WORKING;
        }

        if (!InventoryUtils.hasItemInItemHandler(
                new InvWrapper(worker.getInventoryCitizen()), ItemStackUtils.ISCOOKABLE)
                && (walkTo == null || world.getBlockState(walkTo).getBlock() != Blocks.FURNACE))
        {
            walkTo = null;
            needsCurrently = ItemStackUtils.ISCOOKABLE;
            return COOK_GATHER_FOOD_FROM_BUILDING;
        }

        // TODO: this could leave us walking to a full furnace

        if (walkTo == null)
        {
            for (final BlockPos pos : ((BuildingCook) getOwnBuilding()).getFurnaces())
            {
                final TileEntity entity = world.getTileEntity(pos);
                if (entity instanceof TileEntityFurnace && ItemStackUtils.isEmpty(((TileEntityFurnace) entity).getStackInSlot(COOK_SLOT)))
                {
                    walkTo = pos;
                }
            }
        }

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
        if (entity instanceof TileEntityFurnace)
        {
            // TODO: if the furnace isn't cooking food, we should complain about that

            // TODO: if the furnace is no longer able to accept our uncooked food, should we keep feeding it fuel?
            // TODO: we should probably empty out a furnace that isn't cooking food.

            InventoryUtils.transferXOfFirstSlotInItemHandlerWithIntoInItemHandler(
                    new InvWrapper(worker.getInventoryCitizen()), ItemStackUtils.ISCOOKABLE, Constants.STACKSIZE,
                    new InvWrapper((TileEntityFurnace) entity), COOK_SLOT);

            if (ItemStackUtils.isEmpty(((TileEntityFurnace) entity).getStackInSlot(FUEL_SLOT)))
            {
                if (!InventoryUtils.hasItemInItemHandler(new InvWrapper(worker.getInventoryCitizen()), TileEntityFurnace::isItemFuel))
                {
                    walkTo = null;
                    return COOK_GATHER_FUEL;
                }

                InventoryUtils.transferXOfFirstSlotInItemHandlerWithIntoInItemHandler(
                        new InvWrapper(worker.getInventoryCitizen()), TileEntityFurnace::isItemFuel, Constants.STACKSIZE,
                        new InvWrapper((TileEntityFurnace) entity), FUEL_SLOT);
            }

            walkTo = null;
            return START_WORKING;
        }
        walkTo = null;
        setDelay(STANDARD_DELAY);
        // TODO: why COOK_COOK_FOOD instead of getState()?
        return COOK_COOK_FOOD;
    }

    /**
     * getPositionOfOvenToRetrieveFrom
     *
     * Find the first furnace in the building which is not cooking and either has a cooked result or has an uncooked input
     * and set the worker's status to "Retrieving" and return that furnace's position.
     *
     * @return location of a matching furnace or null if no furnace found
     */
    private BlockPos getPositionOfOvenToRetrieveFrom()
    {
        for (final BlockPos pos : ((BuildingCook) getOwnBuilding()).getFurnaces())
        {
            // TODO: why do we care if the furnace is not burning?  burning && food to cook, maybe, but not burning in general.
            // TODO: Why do we care about food in the cook slot but not fuel? both are required.
            // TODO: It makes more sense to find ovens which have cooked food (no matter what else is going on), or will have cooked food (either because it is in the process of
            // being cooked or because there is uncooked food plus fuel)
            final TileEntity entity = world.getTileEntity(pos);
            if (entity instanceof TileEntityFurnace && !((TileEntityFurnace) entity).isBurning()
                    && (!ItemStackUtils.isEmpty(((TileEntityFurnace) entity)
                    .getStackInSlot(RESULT_SLOT))
                    || !ItemStackUtils.isEmpty(((TileEntityFurnace) entity).getStackInSlot(COOK_SLOT))))
            {
                // TODO: side effect: worker status should be set in caller, not here.
                worker.setLatestStatus(new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_RETRIEVING));
                return pos;
            }
        }
        return null;
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
     * Execute tasks in this order: Retrieve food or cook food in furnaces, request food, serve food, cook food, other tasks.
     *
     * If any furnace has food waiting to be cooked, Find the first furnace in the building which is not cooking and either has a cooked result or has an uncooked input
     * and set the worker's status to "Retrieving" and walk to that furnace's position by transitioning to COOK_RETRIEVE_FOOD.
     * If any furnace has food waiting to be cooked, and if no such furnace, then COOK_COOK_FOOD.
     * If no food in the building nor inventory, set status to "Gathering" and request food if we haven't already done so. In any case, repeat this state.
     * If we have food in building or inventory, find all citizens nearby that are not cooks and have a zero or negative saturation.
     * If there is a hungry citizen, then set status to "Serving"
     * If there is a hungry citizen and we have food in inventory, then COOK_SERVE
     * If there is a hungry citizen and we have no food in inventory, then COOK_GATHER_FOOD_FROM_BUILDING
     * if no hungry citizens and we have less than 64 * building-level food stores, request food if we haven't already done so.
     * If there is uncooked food in building or inventory, then set status to "Cooking", then COOK_COOK_FOOD.
     * If any furnace is idle and has cooked food, set status to "Retrieving" then COOK_RETRIEVE_FOOD.
     * If any furnace is idle and has uncooked food, set status to "Cooking" then COOK_COOK_FOOD.
     * Otherwise, set status to "Idling", then delay and START_WORKING
     *
     * @return next AIState
     */
    private AIState startWorking()
    {
        // TODO: Doesn't seem like checking the ovens should be our first order of business.
        // TODO: seems like we should try to serve food if we can, then cook if we can, then request food if no other options.
        // TODO: If we have hungry customers, we might want to request food even if we could possibly cook it as well.

        // TODO: set statuses at top of other Ai states since START_WORKING will not be the only caller.

        if (((BuildingCook) getOwnBuilding()).isSomethingInOven(world))
        {
            final BlockPos posOfOven = getPositionOfOvenToRetrieveFrom();
            final AIState nextState;
            if (posOfOven == null)
            {
                nextState = COOK_COOK_FOOD;
            }
            else
            {
                walkTo = posOfOven;
                nextState = COOK_GATHER_COOKED_FOOD_FROM_FURNACE;
            }
            return nextState;
        }

        final int amountOfFood = InventoryUtils.getItemCountInProvider(getOwnBuilding(), ItemStackUtils.ISFOOD)
                + InventoryUtils.getItemCountInItemHandler(new InvWrapper(worker.getInventoryCitizen()), ItemStackUtils.ISFOOD);

        if (amountOfFood <= 0)
        {
            worker.setLatestStatus(new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_GATHERING));
            if (!getOwnBuilding().hasWorkerOpenRequestsOfType(worker.getCitizenData(), TypeToken.of(Food.class)))
            {
                // TODO: in my play-throughs, why doesn't the cook ask for food from the warehouse, but only from the player?
                worker.getCitizenData().createRequestAsync(new Food(Constants.STACKSIZE));
            }
            return getState();
        }

        if (range == null)
        {
            range = getOwnBuilding().getTargetableArea(world);
        }

        citizenToServe.clear();
        final List<EntityCitizen> citizenList = world.getEntitiesWithinAABB(EntityCitizen.class,
                range, cit -> !(cit.getColonyJob() instanceof JobCook) && cit.getCitizenData() != null && cit.getCitizenData().getSaturation() <= 0);
        if (!citizenList.isEmpty())
        {
            citizenToServe.addAll(citizenList);
            worker.setLatestStatus(new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_SERVING));
            if (InventoryUtils.hasItemInItemHandler(
                    new InvWrapper(worker.getInventoryCitizen()), ItemStackUtils.ISFOOD))
            {
                return COOK_SERVE_FOOD_TO_CITIZEN;
            }
            return COOK_GATHER_FOOD_FROM_BUILDING;
        }

        if (amountOfFood < getOwnBuilding().getBuildingLevel() * LEAST_KEEP_FOOD_MULTIPLIER
                && !getOwnBuilding().hasWorkerOpenRequestsOfType(worker.getCitizenData(), TypeToken.of(Food.class)))
        {
            worker.getCitizenData().createRequestAsync(new Food(Constants.STACKSIZE));
        }

        if (InventoryUtils.hasItemInProvider(getOwnBuilding(), ItemStackUtils.ISCOOKABLE)
                || InventoryUtils.getItemCountInItemHandler(new InvWrapper(worker.getInventoryCitizen()), ItemStackUtils.ISCOOKABLE) >= 1)
        {
            worker.setLatestStatus(new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_COOKING));
            return COOK_COOK_FOOD;
        }

        // If no clear tasks are given, check for something else to do.

        for (final BlockPos pos : ((BuildingCook) getOwnBuilding()).getFurnaces())
        {
            final TileEntity entity = world.getTileEntity(pos);
            if (entity instanceof TileEntityFurnace && !((TileEntityFurnace) entity).isBurning())
            {
                walkTo = pos;
                if (!ItemStackUtils.isEmpty(((TileEntityFurnace) entity).getStackInSlot(RESULT_SLOT)))
                {
                    worker.setLatestStatus(new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_RETRIEVING));
                    return COOK_GATHER_COOKED_FOOD_FROM_FURNACE;
                }
                else if (!ItemStackUtils.isEmpty(((TileEntityFurnace) entity).getStackInSlot(COOK_SLOT)))
                {
                    worker.setLatestStatus(new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_COOKING));
                    return COOK_COOK_FOOD;
                }
            }
        }

        worker.setLatestStatus(new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_IDLING));
        setDelay(STANDARD_DELAY);
        return START_WORKING;
    }
}
