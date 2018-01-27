package com.minecolonies.coremod.entity.ai.citizen.cook;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.requestable.Burnable;
import com.minecolonies.api.colony.requestsystem.requestable.Food;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
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
import static com.minecolonies.coremod.entity.ai.util.AIState.*;

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
    private static final int COOK_SLOT   = 0;

    /**
     * Slot where the fuel should be put in the furnace.
     */
    private static final int FUEL_SLOT   = 1;

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
                new AITarget(COOK_GATHERING, this::gatherFoodFromBuilding),
                new AITarget(COOK_COOK_FOOD, this::cookFood),
                new AITarget(COOK_SERVE, this::serveFood),
                new AITarget(COOK_RETRIEVE_FOOD, this::retrieve),
                new AITarget(COOK_GET_FIREWOOD, this::getBurnableMaterial)
        );
        worker.setSkillModifier(CHARISMA_MULTIPLIER * worker.getCitizenData().getCharisma()
                + INTELLIGENCE_MULTIPLIER * worker.getCitizenData().getIntelligence());
        worker.setCanPickUpLoot(true);
    }

    private AIState getBurnableMaterial()
    {
        if(walkTo == null && walkToBuilding())
        {
            return getState();
        }

        if (getOwnBuilding().getCountOfPredicateInHut(TileEntityFurnace::isItemFuel, 1, world) < 1)
        {
            if(!getOwnBuilding().hasWorkerOpenRequestsOfType(worker.getCitizenData(), TypeToken.of(Burnable.class)))
            {
                worker.getCitizenData().createRequestAsync(new Burnable(Constants.STACKSIZE));
            }
            setDelay(WAIT_AFTER_REQUEST);
        }
        else
        {
            if(walkTo == null)
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

    private AIState retrieve()
    {
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
                return AIState.COOK_GET_FIREWOOD;
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

    private AIState gatherFoodFromBuilding()
    {
        if(needsCurrently == null)
        {
            needsCurrently = ItemStackUtils.ISFOOD;
        }

        if(InventoryUtils.hasItemInItemHandler(new InvWrapper(worker.getInventoryCitizen()), needsCurrently))
        {
            return COOK_SERVE;
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
            return COOK_SERVE;
        }
        setDelay(STANDARD_DELAY);
        return START_WORKING;
    }

    private AIState serveFood()
    {
        if (citizenToServe.isEmpty())
        {
            return START_WORKING;
        }

        if (walkToBlock(citizenToServe.get(0).getPosition()))
        {
            setDelay(2);
            return getState();
        }

        InventoryUtils.transferXOfFirstSlotInItemHandlerWithIntoNextFreeSlotInItemHandler(
                new InvWrapper(worker.getInventoryCitizen()),
                ItemStackUtils.ISFOOD,
                AMOUNT_OF_FOOD_TO_SERVE,
                new InvWrapper(citizenToServe.get(0).getInventoryCitizen()));

        citizenToServe.remove(0);
        setDelay(SERVE_DELAY);
        return getState();
    }

    private AIState cookFood()
    {
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
            return COOK_GATHERING;
        }

        if (walkTo == null)
        {
            for (final BlockPos pos : ((BuildingCook) getOwnBuilding()).getFurnaces())
            {
                final TileEntity entity = world.getTileEntity(pos);
                if (ItemStackUtils.isEmpty(((TileEntityFurnace) entity).getStackInSlot(COOK_SLOT)))
                {
                    walkTo = pos;
                }
            }
        }

        if(walkTo == null)
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
            InventoryUtils.transferXOfFirstSlotInItemHandlerWithIntoInItemHandler(
                    new InvWrapper(worker.getInventoryCitizen()), ItemStackUtils.ISCOOKABLE, Constants.STACKSIZE,
                    new InvWrapper((TileEntityFurnace) entity), COOK_SLOT);

            if (ItemStackUtils.isEmpty(((TileEntityFurnace) entity).getStackInSlot(FUEL_SLOT)))
            {
                if (!InventoryUtils.hasItemInItemHandler(new InvWrapper(worker.getInventoryCitizen()), TileEntityFurnace::isItemFuel))
                {
                    walkTo = null;
                    return AIState.COOK_GET_FIREWOOD;
                }

                InventoryUtils.transferXOfFirstSlotInItemHandlerWithIntoInItemHandler(
                        new InvWrapper(worker.getInventoryCitizen()), TileEntityFurnace::isItemFuel, Constants.STACKSIZE,
                        new InvWrapper((TileEntityFurnace) entity), FUEL_SLOT);
            }

            ((BuildingCook) getOwnBuilding()).setIsSomethingInOven(true);
            walkTo = null;
            return START_WORKING;
        }
        walkTo = null;
        setDelay(STANDARD_DELAY);
        return COOK_COOK_FOOD;
    }

    private BlockPos getPositionOfOvenToRetrieveFrom()
    {
        for (final BlockPos pos : ((BuildingCook) getOwnBuilding()).getFurnaces())
        {
            final TileEntity entity = world.getTileEntity(pos);
            if (entity instanceof TileEntityFurnace && !((TileEntityFurnace) entity).isBurning()
                    && (!ItemStackUtils.isEmpty(((TileEntityFurnace) entity)
                    .getStackInSlot(RESULT_SLOT))
                    || !ItemStackUtils.isEmpty(((TileEntityFurnace) entity).getStackInSlot(COOK_SLOT))))
            {
                worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.retrieving"));
                return pos;
            }
        }
        ((BuildingCook) getOwnBuilding()).setIsSomethingInOven(false);
        return null;
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return 1;
    }

    private AIState startWorking()
    {
        if (((BuildingCook) getOwnBuilding()).isSomethingInOven())
        {
            final BlockPos posOfOven = getPositionOfOvenToRetrieveFrom();
            if(posOfOven != null)
            {
                walkTo = posOfOven;
                return COOK_RETRIEVE_FOOD;
            }
        }

        final int amountOfFood = getOwnBuilding()
                .getCountOfPredicateInHut(ItemStackUtils.ISFOOD,
                        getOwnBuilding().getBuildingLevel() * LEAST_KEEP_FOOD_MULTIPLIER, world)
                + InventoryUtils.getItemCountInItemHandler(new InvWrapper(worker.getInventoryCitizen()), ItemStackUtils.ISFOOD);

        if (amountOfFood <= 0)
        {
            worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.gathering"));
            if(!getOwnBuilding().hasWorkerOpenRequestsOfType(worker.getCitizenData(), TypeToken.of(Food.class)))
            {
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
            worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.serving"));
            if(InventoryUtils.hasItemInItemHandler(
                    new InvWrapper(worker.getInventoryCitizen()), ItemStackUtils.ISFOOD))
            {
                return COOK_SERVE;
            }
            return COOK_GATHERING;
        }

        if (amountOfFood < getOwnBuilding().getBuildingLevel() * LEAST_KEEP_FOOD_MULTIPLIER)
        {
            if(!getOwnBuilding().hasWorkerOpenRequestsOfType(worker.getCitizenData(), TypeToken.of(Food.class)))
            {
                worker.getCitizenData().createRequestAsync(new Food(Constants.STACKSIZE));
            }
            return getState();
        }

        if (getOwnBuilding().getCountOfPredicateInHut(ItemStackUtils.ISCOOKABLE, 1, world) >= 1
                || InventoryUtils.getItemCountInItemHandler(new InvWrapper(worker.getInventoryCitizen()), ItemStackUtils.ISCOOKABLE) >= 1)
        {
            worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.cooking"));
            return COOK_COOK_FOOD;
        }
        return checkForAdditionalJobs();
    }

    /**
     * If no clear tasks are given, check if something else is to do.
     * @return the next AIState to traverse to.
     */
    private AIState checkForAdditionalJobs()
    {
        for (final BlockPos pos : ((BuildingCook) getOwnBuilding()).getFurnaces())
        {
            final TileEntity entity = world.getTileEntity(pos);
            if (entity instanceof TileEntityFurnace && !((TileEntityFurnace) entity).isBurning())
            {
                walkTo = pos;
                if(!ItemStackUtils.isEmpty(((TileEntityFurnace) entity).getStackInSlot(RESULT_SLOT)))
                {
                    worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.retrieving"));
                    return COOK_RETRIEVE_FOOD;
                }
                else if(!ItemStackUtils.isEmpty(((TileEntityFurnace) entity).getStackInSlot(COOK_SLOT)))
                {
                    worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.cooking"));
                    return COOK_COOK_FOOD;
                }
            }
        }

        worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.idling"));
        setDelay(STANDARD_DELAY);
        return START_WORKING;
    }
}
