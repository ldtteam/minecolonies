package com.minecolonies.coremod.entity.ai.citizen.florist;

import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingFlorist;
import com.minecolonies.coremod.colony.jobs.JobFlorist;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.tileentities.TileEntityCompostedDirt;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.TranslationConstants.NO_FLOWERS_IN_CONFIG;
import static com.minecolonies.api.util.constant.TranslationConstants.NO_PLANT_GROUND_FLORIST;

/**
 * Florist AI class.
 */
public class EntityAIWorkFlorist extends AbstractEntityAIInteract<JobFlorist>
{
    /**
     * How often should intelligence factor into the composter's skill modifier.
     */
    private static final int INTELLIGENCE_MULTIPLIER = 1;

    /**
     * How often should intelligence factor into the composter's skill modifier.
     */
    private static final int CHARISMA_MULTIPLIER = 2;

    /**
     * Predicate to check for compost items.
     */
    private static final Predicate<ItemStack> IS_COMPOST = stack -> !stack.isEmpty() && stack.getItem() == ModItems.compost;

    /**
     * Max 2d distance the florist should be from the hut.
     */
    private static final long MAX_DISTANCE           = 50;

    /**
     * Harvest actions to actually dump per building level.
     */
    private static final int HARVEST_ACTIONS_TO_DUMP   = 10;

    /**
     * The chance for something to grow per second on one of the fields.
     */
    private static final double PERCENT_CHANGE_FOR_GROWTH = 0.5;

    /**
     * Base XP gain for the florist for composting or harvesting.
     */
    private static final double BASE_XP_GAIN     = 0.5;

    /**
     * Quantity of compost to request at a time.
     */
    private static final int COMPOST_REQUEST_QTY     = 16;

    /**
     * Base block mining delay multiplier.
     */
    private static final int BASE_BLOCK_MINING_DELAY = 10;

    /**
     * The per level mining delay bonus.
     */
    private static final double PER_LEVEL_BONUS = 0.1;

    /**
     * Max level bonus is this x 10.
     */
    private static final double MAX_BONUS = 5;

    /**
     * Position the florist should harvest a flower at now.
     */
    private BlockPos harvestPosition;

    /**
     * Position the florist should compost the tileEntity at.
     */
    private BlockPos compostPosition;

    /*
       Florst uses compost on them if not composted yet
       Block which is composted produces flowers in an interval for a certain time (For 1 minute it produces 1 flower randomly between 1-60s),
        so it might make 10 flowers but average it's 2 flowers per minute.                                                                                                                                                                       - Flourist checks if on top of block is flower and harvests it.
       Depending on the florists level he has smaller delays so he harvests faster
     */

    /**
     * Creates the abstract part of the AI.
     * Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAIWorkFlorist(@NotNull final JobFlorist job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING),
          new AITarget(START_WORKING, DECIDE, TICKS_SECOND),
          new AITarget(DECIDE, this::decide, TICKS_SECOND),
          new AITarget(FLORIST_HARVEST, this::harvest, TICKS_SECOND),
          new AITarget(FLORIST_COMPOST, this::compost, TICKS_SECOND)
        );
        worker.getCitizenExperienceHandler().setSkillModifier(CHARISMA_MULTIPLIER * worker.getCitizenData().getCharisma()
                                                                + INTELLIGENCE_MULTIPLIER * worker.getCitizenData().getIntelligence());
        worker.setCanPickUpLoot(true);
    }

    /**
     * Main decision method of florist to decide what to do.
     *
     * @return the next AI state to go to.
     */
    private IAIState decide()
    {
        if (getOwnBuilding(BuildingFlorist.class).getPlantGround().isEmpty())
        {
            chatProxy.setCurrentChat(NO_PLANT_GROUND_FLORIST);
            return IDLE;
        }

        worker.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
        final long distance = BlockPosUtil.getDistance2D(worker.getPosition(), getOwnBuilding().getPosition());
        if (distance > MAX_DISTANCE && walkToBuilding())
        {
            return DECIDE;
        }

        final int amountOfCompostInInv = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), IS_COMPOST);
        if (amountOfCompostInInv <= 0)
        {
            final int amountOfCompostInBuilding = InventoryUtils.getItemCountInProvider(getOwnBuilding(), IS_COMPOST);
            if (amountOfCompostInBuilding > 0)
            {
                needsCurrently = IS_COMPOST;
                return GATHERING_REQUIRED_MATERIALS;
            }
            else
            {
                checkIfRequestForItemExistOrCreateAsynch(new ItemStack(ModItems.compost, COMPOST_REQUEST_QTY));
            }
        }

        harvestPosition = areThereFlowersToGather();
        if (harvestPosition != null)
        {
            return FLORIST_HARVEST;
        }

        if (amountOfCompostInInv <= 0)
        {
            if (!isThereCompostedLand())
            {
                chatProxy.setCurrentChat("com.minecolonies.coremod.florist.nocompost");
                return START_WORKING;
            }
            return DECIDE;
        }
        else
        {
            compostPosition = getFirstNotCompostedLand();
            return FLORIST_COMPOST;
        }
    }

    /**
     * Walk to the block to compost and apply compost to it.
     * @return the next state to go to.
     */
    private IAIState compost()
    {
        if (compostPosition == null)
        {
            return START_WORKING;
        }

        if (walkToBlock(compostPosition))
        {
            return getState();
        }

        final TileEntity entity = world.getTileEntity(compostPosition);
        if (entity instanceof TileEntityCompostedDirt)
        {
            @Nullable final ItemStack stack = getOwnBuilding(BuildingFlorist.class).getFlowerToGrow();
            if (stack != null && InventoryUtils.shrinkItemCountInItemHandler(worker.getInventoryCitizen(), IS_COMPOST))
            {
                ((TileEntityCompostedDirt) entity).compost(PERCENT_CHANGE_FOR_GROWTH, getOwnBuilding(BuildingFlorist.class).getFlowerToGrow());
            }
            else
            {
                chatProxy.setCurrentChat(NO_FLOWERS_IN_CONFIG);
            }
        }

        worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
        incrementActionsDone();
        worker.decreaseSaturationForContinuousAction();
        compostPosition = null;
        return START_WORKING;
    }

    /**
     * Walk to a piece of land and harvest the flower.
     * @return the next state to go to.
     */
    private IAIState harvest()
    {
        if (harvestPosition == null)
        {
            return START_WORKING;
        }

        if (walkToBlock(harvestPosition))
        {
            return getState();
        }

        if (!mineBlock(harvestPosition))
        {
            return getState();
        }

        worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
        incrementActionsDone();
        worker.decreaseSaturationForContinuousAction();
        harvestPosition = null;
        return START_WORKING;
    }

    // ------------------------------------------------ HELPER METHODS ------------------------------------------------ //

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return HARVEST_ACTIONS_TO_DUMP * getOwnBuilding().getBuildingLevel();
    }

    /**
     * Calculate how long it takes to mine this block.
     *
     * @param block the block type
     * @param pos   coordinate
     * @return the delay in ticks
     */
    @Override
    public int getBlockMiningDelay(@NotNull final Block block, @NotNull final BlockPos pos)
    {
        return BASE_BLOCK_MINING_DELAY * (int) (1 + Math.max(0,  MAX_BONUS - PER_LEVEL_BONUS * worker.getCitizenExperienceHandler().getLevel()));
    }

    /**
     * Check if there is any flower to gather.
     *
     * @return if so, return the position it is at.
     */
    @Nullable
    private BlockPos areThereFlowersToGather()
    {
        for (final BlockPos pos : getOwnBuilding(BuildingFlorist.class).getPlantGround())
        {
            if (!world.isAirBlock(pos.up()))
            {
                return pos.up();
            }
        }
        return null;
    }

    /**
     * Check if there is any already composted land.
     * @return true if there is any.
     */
    private boolean isThereCompostedLand()
    {
        for (final BlockPos pos : getOwnBuilding(BuildingFlorist.class).getPlantGround())
        {
            if (world.isBlockLoaded(pos))
            {
                final TileEntity entity = world.getTileEntity(pos);
                if (entity instanceof TileEntityCompostedDirt)
                {
                    if (((TileEntityCompostedDirt) entity).isComposted())
                    {
                        return true;
                    }
                }
                else
                {
                    getOwnBuilding(BuildingFlorist.class).removePlantableGround(pos);
                }
            }
        }
        return false;
    }

    /**
     * Check to get some not composted land and return its position.
     * @return the land to compost.
     */
    private BlockPos getFirstNotCompostedLand()
    {
        for (final BlockPos pos : getOwnBuilding(BuildingFlorist.class).getPlantGround())
        {
            if (world.isBlockLoaded(pos))
            {
                final TileEntity entity = world.getTileEntity(pos);
                if (entity instanceof TileEntityCompostedDirt)
                {
                    if (!((TileEntityCompostedDirt) entity).isComposted())
                    {
                        return pos;
                    }
                }
                else
                {
                    getOwnBuilding(BuildingFlorist.class).removePlantableGround(pos);
                }
            }
        }
        return null;
    }
}
