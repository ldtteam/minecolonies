package com.minecolonies.coremod.entity.ai.citizen.smelter;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.requestable.Burnable;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.BuildingSmeltery;
import com.minecolonies.coremod.colony.jobs.JobSmelter;
import com.minecolonies.coremod.colony.requestable.SmeltableOre;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAISkill;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_ENTITY_BAKER_NO_FURNACES;
import static com.minecolonies.coremod.entity.ai.util.AIState.*;

/**
 * Smelter AI class.
 */
public class EntityAIWorkSmelter extends AbstractEntityAISkill<JobSmelter>
{
    /**
     * How often should strength factor into the smelter's skill modifier.
     */
    private static final int STRENGTH_MULTIPLIER = 2;

    /**
     * How often should intelligence factor into the smelter's skill modifier.
     */
    private static final int INTELLIGENCE_MULTIPLIER = 1;

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
     * Slot where ores should be put in the furnace.
     */
    private static final int ORE_SLOT = 0;

    /**
     * Slot where the fuel should be put in the furnace.
     */
    private static final int FUEL_SLOT = 1;

    /**
     * The current position the worker should walk to.
     */
    private BlockPos walkTo = null;

    /**
     * What he currently might be needing.
     */
    private Predicate<ItemStack> needsCurrently = null;

    /**
     * Constructor for the Smelter.
     * Defines the tasks the cook executes.
     *
     * @param job a cook job to use.
     */
    public EntityAIWorkSmelter(@NotNull final JobSmelter job)
    {
        super(job);
        super.registerTargets(
                new AITarget(IDLE, START_WORKING),
                new AITarget(START_WORKING, this::startWorking),
                new AITarget(SMELTER_GATHERING, this::gatherOreFromBuilding),
                new AITarget(SMELTER_SMELT_ORE, this::smeltOre),
                new AITarget(SMELTER_RETRIEVE_ORE, this::retrieve),
                new AITarget(SMELTER_GET_FIREWOOD, this::getBurnableMaterial)
        );
        worker.setSkillModifier(STRENGTH_MULTIPLIER * worker.getCitizenData().getStrength()
                + INTELLIGENCE_MULTIPLIER * worker.getCitizenData().getIntelligence());
        worker.setCanPickUpLoot(true);
    }

    private AIState getBurnableMaterial()
    {
        if (walkTo == null && walkToBuilding())
        {
            return getState();
        }

        if (getOwnBuilding().getCountOfPredicateInHut(TileEntityFurnace::isItemFuel, 1, world) < 1)
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

        return SMELTER_SMELT_ORE;
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
                && ItemStackUtils.isEmpty(((TileEntityFurnace) entity).getStackInSlot(ORE_SLOT))))
        {
            walkTo = null;
            return START_WORKING;
        }
        walkTo = null;

        InventoryUtils.transferItemStackIntoNextFreeSlotInItemHandlers(
                new InvWrapper((TileEntityFurnace) entity), RESULT_SLOT,
                new InvWrapper(worker.getInventoryCitizen()));

        if (!ItemStackUtils.isEmpty(((TileEntityFurnace) entity).getStackInSlot(ORE_SLOT)))
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

    private AIState gatherOreFromBuilding()
    {
        if (needsCurrently == null)
        {
            needsCurrently = EntityAIWorkSmelter::isSmeltableOre;
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

        tryTransferFromPosToWorker(pos, needsCurrently);
        setDelay(STANDARD_DELAY);
        return START_WORKING;
    }

    private AIState smeltOre()
    {
        if (((BuildingSmeltery) getOwnBuilding()).getFurnaces().isEmpty())
        {
            chatSpamFilter.talkWithoutSpam(COM_MINECOLONIES_COREMOD_ENTITY_BAKER_NO_FURNACES);
            return START_WORKING;
        }

        if (!InventoryUtils.hasItemInItemHandler(
                new InvWrapper(worker.getInventoryCitizen()), EntityAIWorkSmelter::isSmeltableOre)
                && (walkTo == null || world.getBlockState(walkTo).getBlock() != Blocks.FURNACE))
        {
            walkTo = null;
            needsCurrently = EntityAIWorkSmelter::isSmeltableOre;
            return SMELTER_GATHERING;
        }

        if (walkTo == null)
        {
            for (final BlockPos pos : ((BuildingSmeltery) getOwnBuilding()).getFurnaces())
            {
                final TileEntity entity = world.getTileEntity(pos);
                if (ItemStackUtils.isEmpty(((TileEntityFurnace) entity).getStackInSlot(ORE_SLOT)))
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
            InventoryUtils.transferXOfFirstSlotInItemHandlerWithIntoInItemHandler(
                    new InvWrapper(worker.getInventoryCitizen()), EntityAIWorkSmelter::isSmeltableOre, Constants.STACKSIZE,
                    new InvWrapper((TileEntityFurnace) entity), ORE_SLOT);

            if (ItemStackUtils.isEmpty(((TileEntityFurnace) entity).getStackInSlot(FUEL_SLOT)))
            {
                if (!InventoryUtils.hasItemInItemHandler(new InvWrapper(worker.getInventoryCitizen()), TileEntityFurnace::isItemFuel))
                {
                    walkTo = null;
                    return AIState.SMELTER_GET_FIREWOOD;
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
        return SMELTER_SMELT_ORE;
    }

    private BlockPos getPositionOfOvenToRetrieveFrom()
    {
        for (final BlockPos pos : ((BuildingSmeltery) getOwnBuilding()).getFurnaces())
        {
            final TileEntity entity = world.getTileEntity(pos);
            if (entity instanceof TileEntityFurnace && !((TileEntityFurnace) entity).isBurning()
                    && (!ItemStackUtils.isEmpty(((TileEntityFurnace) entity)
                    .getStackInSlot(RESULT_SLOT))
                    || !ItemStackUtils.isEmpty(((TileEntityFurnace) entity).getStackInSlot(ORE_SLOT))))
            {
                worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.retrieving"));
                return pos;
            }
        }
        return null;
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return 1;
    }

    private AIState startWorking()
    {

        final BlockPos posOfOven = getPositionOfOvenToRetrieveFrom();
        if (posOfOven != null)
        {
            walkTo = posOfOven;
            return SMELTER_RETRIEVE_ORE;
        }


        final int amountOfOre = getOwnBuilding().getCountOfPredicateInHut(EntityAIWorkSmelter::isSmeltableOre, 1, world)
                + InventoryUtils.getItemCountInItemHandler(new InvWrapper(worker.getInventoryCitizen()), EntityAIWorkSmelter::isSmeltableOre);

        if (amountOfOre <= 0)
        {
            worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.gathering"));
            if (!getOwnBuilding().hasWorkerOpenRequestsOfType(worker.getCitizenData(), TypeToken.of(SmeltableOre.class)))
            {
                worker.getCitizenData().createRequestAsync(new SmeltableOre(Constants.STACKSIZE));
            }
            return getState();
        }

        return checkForAdditionalJobs();
    }

    /**
     * If no clear tasks are given, check if something else is to do.
     *
     * @return the next AIState to traverse to.
     */
    private AIState checkForAdditionalJobs()
    {
        for (final BlockPos pos : ((BuildingSmeltery) getOwnBuilding()).getFurnaces())
        {
            final TileEntity entity = world.getTileEntity(pos);
            if (entity instanceof TileEntityFurnace && !((TileEntityFurnace) entity).isBurning())
            {
                walkTo = pos;
                if (!ItemStackUtils.isEmpty(((TileEntityFurnace) entity).getStackInSlot(RESULT_SLOT)))
                {
                    worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.retrieving"));
                    return SMELTER_RETRIEVE_ORE;
                }
                else if (!ItemStackUtils.isEmpty(((TileEntityFurnace) entity).getStackInSlot(ORE_SLOT)))
                {
                    worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.cooking"));
                    return SMELTER_SMELT_ORE;
                }
            }
        }

        worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.idling"));
        setDelay(STANDARD_DELAY);
        return START_WORKING;
    }

    /**
     * Check if a stack is a smeltable ore.
     *
     * @param stack the stack to test.
     * @return true if so.
     */
    private static boolean isSmeltableOre(final ItemStack stack)
    {
        return ItemStackUtils.IS_SMELTABLE.and(
                itemStack -> itemStack.getItem() instanceof ItemBlock
                        && ColonyManager.getCompatabilityManager().isOre(((ItemBlock) itemStack.getItem()).getBlock().getDefaultState())).test(stack);
    }
}
