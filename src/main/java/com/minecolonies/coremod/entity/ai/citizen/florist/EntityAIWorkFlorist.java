package com.minecolonies.coremod.entity.ai.citizen.florist;

import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingFlorist;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteraction;
import com.minecolonies.coremod.colony.jobs.JobFlorist;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.tileentities.TileEntityCompostedDirt;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.ItemStackUtils.IS_COMPOST;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.coremod.util.WorkerUtil.isThereCompostedLand;

/**
 * Florist AI class.
 */
public class EntityAIWorkFlorist extends AbstractEntityAIInteract<JobFlorist, BuildingFlorist>
{
    /**
     * Max 2d distance the florist should be from the hut.
     */
    private static final long MAX_DISTANCE = 50;

    /**
     * Harvest actions to actually dump per building level.
     */
    private static final int HARVEST_ACTIONS_TO_DUMP = 10;

    /**
     * The chance for something to grow per second on one of the fields.
     */
    private static final double PERCENT_CHANGE_FOR_GROWTH = 0.2;

    /**
     * Base XP gain for the florist for composting or harvesting.
     */
    private static final double BASE_XP_GAIN = 0.5;

    /**
     * Quantity of compost to request at a time.
     */
    private static final int COMPOST_REQUEST_QTY = 16;

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
     * Gardening icon
     */
    private final static VisibleCitizenStatus GARDENING =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/florist.png"), "com.minecolonies.gui.visiblestatus.florist");

    /**
     * Xp gained on harvest
     */
    private static final double XP_PER_FLOWER = 2;

    /**
     * Position the florist should harvest a flower at now.
     */
    private BlockPos harvestPosition;

    /**
     * Position the florist should compost the tileEntity at.
     */
    private BlockPos compostPosition;

    /*
       Florist uses compost on them if not composted yet
       Block which is composted produces flowers in an interval for a certain time (For 1 minute it produces 1 flower randomly between 1-60s),
        so it might make 10 flowers but average it's 2 flowers per minute.                                                                                                                                                                       - Flourist checks if on top of block is flower and harvests it.
       Depending on the florists level he has smaller delays so he harvests faster
     */

    /**
     * Creates the abstract part of the AI. Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAIWorkFlorist(@NotNull final JobFlorist job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING, 1),
          new AITarget(START_WORKING, DECIDE, TICKS_SECOND),
          new AITarget(DECIDE, this::decide, 200),
          new AITarget(FLORIST_HARVEST, this::harvest, TICKS_SECOND),
          new AITarget(FLORIST_COMPOST, this::compost, TICKS_SECOND)
        );
        worker.setCanPickUpLoot(true);
    }

    /**
     * Main decision method of florist to decide what to do.
     *
     * @return the next AI state to go to.
     */
    private IAIState decide()
    {
        worker.getCitizenData().setVisibleStatus(VisibleCitizenStatus.WORKING);
        if (getOwnBuilding().getPlantGround().isEmpty())
        {
            worker.getCitizenData().triggerInteraction(new StandardInteraction(new TranslationTextComponent(NO_PLANT_GROUND_FLORIST), ChatPriority.BLOCKING));
            return IDLE;
        }

        worker.setItemInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        final long distance = BlockPosUtil.getDistance2D(worker.blockPosition(), getOwnBuilding().getPosition());
        if (distance > MAX_DISTANCE && walkToBuilding())
        {
            return DECIDE;
        }

        final int amountOfCompostInInv = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), IS_COMPOST);
        if (amountOfCompostInInv <= 0)
        {
            final int amountOfCompostInBuilding = InventoryUtils.getCountFromBuilding(getOwnBuilding(), IS_COMPOST);
            if (amountOfCompostInBuilding > 0)
            {
                needsCurrently = new Tuple<>(IS_COMPOST, STACKSIZE);
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
            if (!isThereCompostedLand(getOwnBuilding(), world))
            {
                worker.getCitizenData().triggerInteraction(new StandardInteraction(new TranslationTextComponent(NO_COMPOST), ChatPriority.BLOCKING));
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
     *
     * @return the next state to go to.
     */
    private IAIState compost()
    {
        if (compostPosition == null)
        {
            return START_WORKING;
        }

        worker.getCitizenData().setVisibleStatus(GARDENING);

        if (walkToBlock(compostPosition))
        {
            return getState();
        }

        final TileEntity entity = world.getBlockEntity(compostPosition);
        if (entity instanceof TileEntityCompostedDirt)
        {
            @Nullable final ItemStack stack = getOwnBuilding().getFlowerToGrow();
            if (stack != null)
            {
                if (worker.getRandom().nextInt(200 - getPrimarySkillLevel()) < 0 || InventoryUtils.shrinkItemCountInItemHandler(worker.getInventoryCitizen(), IS_COMPOST))
                {
                    ((TileEntityCompostedDirt) entity).compost(PERCENT_CHANGE_FOR_GROWTH - (getOwnBuilding().getBuildingLevel() * 0.01), getOwnBuilding().getFlowerToGrow());
                }
            }
            else
            {
                worker.getCitizenData().triggerInteraction(new StandardInteraction(new TranslationTextComponent(NO_FLOWERS_IN_CONFIG), ChatPriority.BLOCKING));
            }
        }

        incrementActionsDone();
        worker.decreaseSaturationForContinuousAction();
        compostPosition = null;
        return START_WORKING;
    }

    /**
     * Walk to a piece of land and harvest the flower.
     *
     * @return the next state to go to.
     */
    private IAIState harvest()
    {
        if (harvestPosition == null)
        {
            return START_WORKING;
        }

        worker.getCitizenData().setVisibleStatus(GARDENING);

        if (walkToBlock(harvestPosition))
        {
            return getState();
        }

        if (!mineBlock(harvestPosition))
        {
            return getState();
        }

        worker.getCitizenExperienceHandler().addExperience(XP_PER_FLOWER);
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
     * @param state the blockstate
     * @param pos   coordinate
     * @return the delay in ticks
     */
    @Override
    public int getBlockMiningDelay(@NotNull final BlockState state, @NotNull final BlockPos pos)
    {
        return BASE_BLOCK_MINING_DELAY * (int) (1 + Math.max(0, MAX_BONUS - PER_LEVEL_BONUS * (getSecondarySkillLevel() / 2.0)));
    }

    /**
     * Check if there is any flower to gather.
     *
     * @return if so, return the position it is at.
     */
    @Nullable
    private BlockPos areThereFlowersToGather()
    {
        for (final BlockPos pos : getOwnBuilding().getPlantGround())
        {
            if (!world.isEmptyBlock(pos.above()))
            {
                return pos.above();
            }
        }
        return null;
    }

    /**
     * Check to get some not composted land and return its position.
     *
     * @return the land to compost.
     */
    private BlockPos getFirstNotCompostedLand()
    {
        for (final BlockPos pos : getOwnBuilding().getPlantGround())
        {
            if (WorldUtil.isEntityBlockLoaded(world, pos))
            {
                final TileEntity entity = world.getBlockEntity(pos);
                if (entity instanceof TileEntityCompostedDirt)
                {
                    if (!((TileEntityCompostedDirt) entity).isComposted())
                    {
                        return pos;
                    }
                }
                else
                {
                    getOwnBuilding().removePlantableGround(pos);
                }
            }
        }
        return null;
    }

    @Override
    public Class<BuildingFlorist> getExpectedBuildingClass()
    {
        return BuildingFlorist.class;
    }
}
