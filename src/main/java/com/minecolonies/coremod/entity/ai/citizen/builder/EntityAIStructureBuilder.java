package com.minecolonies.coremod.entity.ai.citizen.builder;

import com.minecolonies.api.util.*;
import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.coremod.colony.jobs.JobBuilder;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuild;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildDecoration;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildRemoval;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIStructureWithWorkOrder;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_GATHERING;
import static com.minecolonies.coremod.entity.ai.util.AIState.*;

/**
 * AI class for the builder.
 * Manages building and repairing buildings.
 */
public class EntityAIStructureBuilder extends AbstractEntityAIStructureWithWorkOrder<JobBuilder>
{
    /**
     * How often should intelligence factor into the builders skill modifier.
     */
    private static final int INTELLIGENCE_MULTIPLIER = 2;

    /**
     * How often should strength factor into the builders skill modifier.
     */
    private static final int STRENGTH_MULTIPLIER = 1;

    /**
     * over this y level the builder will be faster.
     */
    private static final int DEPTH_LEVEL_0 = 60;

    /**
     * At this y level the builder will be slower.
     */
    private static final int DEPTH_LEVEL_1 = 30;

    /**
     * At this y level the builder will be way slower..
     */
    private static final int DEPTH_LEVEL_2 = 15;

    /**
     * Speed buff at 0 depth level.
     */
    private static final double SPEED_BUFF_0 = 0.5;

    /**
     * Speed buff at first depth level.
     */
    private static final int SPEED_BUFF_1 = 2;

    /**
     * Speed buff at second depth level.
     */
    private static final int SPEED_BUFF_2 = 4;

    /**
     * The standard delay after each terminated action.
     */
    private static final int STANDARD_DELAY = 5;

    /**
     * After how many actions should the builder dump his inventory.
     */
    private static final int ACTIONS_UNTIL_DUMP = 1024;

    /**
     * The id in the list of the last picked up item.
     */
    private int pickUpCount = 0;

    /**
     * The currently needed item to pickUp.
     */
    private Predicate<ItemStack> needsCurrently;

    /**
     * The position to walk to at the moment to gather something.
     */
    private BlockPos walkTo;

    /**
     * Initialize the builder and add all his tasks.
     *
     * @param job the job he has.
     */
    public EntityAIStructureBuilder(@NotNull final JobBuilder job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING),
          new AITarget(this::checkIfExecute, this::getState),
          new AITarget(START_WORKING, this::startWorkingAtOwnBuilding),
          new AITarget(PICK_UP, this::pickUpMaterial),
          new AITarget(GATHERING_REQUIRED_MATERIALS, this::getNeededItem)
        );
        worker.getCitizenExperienceHandler().setSkillModifier(INTELLIGENCE_MULTIPLIER * worker.getCitizenData().getIntelligence()
                                  + STRENGTH_MULTIPLIER * worker.getCitizenData().getStrength());
        worker.setCanPickUpLoot(true);
    }

    /**
     * State to pick up material before going back to work.
     * @return the next state to go to.
     */
    public AIState pickUpMaterial()
    {
        final BuildingBuilder building = getOwnBuilding();
        final List<Predicate<ItemStack>> neededItemsList = new ArrayList<>(building.getRequiredItemsAndAmount().keySet());
        if (neededItemsList.size() <= pickUpCount)
        {
            pickUpCount = 0;
            return START_WORKING;
        }

        needsCurrently = neededItemsList.get(pickUpCount);
        pickUpCount++;
        return GATHERING_REQUIRED_MATERIALS;
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
        setDelay(STANDARD_DELAY);

        if (walkTo == null && walkToBuilding())
        {
            return getState();
        }

        if (needsCurrently == null || !InventoryUtils.hasItemInProvider(getOwnBuilding(), needsCurrently))
        {
            return PICK_UP;
        }
        else
        {
            if (walkTo == null)
            {
                final BlockPos pos = getOwnBuilding().getTileEntity().getPositionOfChestWithItemStack(needsCurrently);
                if (pos == null)
                {
                    return PICK_UP;
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
                return PICK_UP;
            }
            walkTo = null;
        }

        return PICK_UP;
    }

    @Override
    public Class getExpectedBuildingClass()
    {
        return BuildingBuilder.class;
    }

    private boolean checkIfExecute()
    {
        setDelay(1);

        if (!job.hasWorkOrder())
        {
            getOwnBuilding(AbstractBuildingStructureBuilder.class).searchWorkOrder();
            return true;
        }

        final WorkOrderBuildDecoration wo = job.getWorkOrder();

        if (wo == null)
        {
            job.setWorkOrder(null);
            return true;
        }

        final AbstractBuilding building = job.getColony().getBuildingManager().getBuilding(wo.getBuildingLocation());
        if (building == null && wo instanceof WorkOrderBuild && !(wo instanceof WorkOrderBuildRemoval))
        {
            job.complete();
            return true;
        }

        if (!job.hasStructure())
        {
            super.initiate();
        }

        return false;
    }

    @Override
    public AIState switchStage(final AIState state)
    {
        if (job.getWorkOrder() instanceof WorkOrderBuildRemoval && state.equals(BUILDING_STEP))
        {
            return COMPLETE_BUILD;
        }
        return super.switchStage(state);
    }


    private AIState startWorkingAtOwnBuilding()
    {
        if (walkToBuilding())
        {
            return getState();
        }
        return START_BUILDING;
    }

    @Override
    public AIState afterRequestPickUp()
    {
        return INVENTORY_FULL;
    }

    @Override
    public AIState afterDump()
    {
        return PICK_UP;
    }

    @Override
    public boolean walkToConstructionSite(final BlockPos targetPos)
    {
        if (workFrom == null || MathUtils.twoDimDistance(targetPos, workFrom) < 3 || MathUtils.twoDimDistance(targetPos, workFrom) > 10)
        {
            workFrom = getWorkingPosition(targetPos);
        }

        return worker.isWorkerAtSiteWithMove(workFrom, 5) || MathUtils.twoDimDistance(worker.getPosition(), workFrom) < 10;
    }

    /**
     * Calculates the working position.
     * <p>
     * Takes a min distance from width and length.
     * <p>
     * Then finds the floor level at that distance and then check if it does contain two air levels.
     *
     * @param targetPosition the position to work at.
     * @return BlockPos position to work from.
     */
    @Override
    public BlockPos getWorkingPosition(final BlockPos targetPosition)
    {
        if (job.getWorkOrder() != null)
        {
            final BlockPos schemPos = job.getWorkOrder().getBuildingLocation();
            final int yStart = targetPosition.getY() > schemPos.getY() ? targetPosition.getY() : schemPos.getY();
            final EnumFacing direction = BlockPosUtil.getXZFacing(worker.getPosition(), targetPosition).getOpposite();
            for (int i = 3; i < 5; i++)
            {
                for (int y = yStart; y >= schemPos.getY()-3; y--)
                {
                    final BlockPos pos = targetPosition.offset(direction, i);
                    final BlockPos basePos = new BlockPos(pos.getX(), y, pos.getZ());
                    if (EntityUtils.checkForFreeSpace(world, basePos))
                    {
                        return basePos;
                    }
                }
            }
        }
        return targetPosition;
    }

    @Override
    public void connectBlockToBuildingIfNecessary(@NotNull final IBlockState blockState, @NotNull final BlockPos pos)
    {
        final BlockPos buildingLocation = job.getWorkOrder().getBuildingLocation();
        final AbstractBuilding building = this.getOwnBuilding().getColony().getBuildingManager().getBuilding(buildingLocation);

        if (building != null)
        {
            building.registerBlockPosition(blockState, pos, world);
        }

        if (blockState.getBlock() == ModBlocks.blockWayPoint)
        {
            worker.getCitizenColonyHandler().getColony().addWayPoint(pos, world.getBlockState(pos));
        }
    }

    @Override
    public boolean shallReplaceSolidSubstitutionBlock(final Block worldBlock, final IBlockState worldMetadata)
    {
        return false;
    }

    @Override
    public IBlockState getSolidSubstitution(@NotNull final BlockPos location)
    {
        return BlockUtils.getSubstitutionBlockAtWorld(world, location);
    }

    @Override
    public int getBlockMiningDelay(@NotNull final Block block, @NotNull final BlockPos pos)
    {
        final int initialDelay = super.getBlockMiningDelay(block, pos);

        if (pos.getY() > DEPTH_LEVEL_0)
        {
            return (int) (initialDelay * SPEED_BUFF_0);
        }

        if (pos.getY() > DEPTH_LEVEL_1)
        {
            return initialDelay;
        }

        if (pos.getY() < DEPTH_LEVEL_2)
        {
            return initialDelay * SPEED_BUFF_2;
        }
        return initialDelay * SPEED_BUFF_1;
    }

    /**
     * Calculates after how many actions the ai should dump it's inventory.
     *
     * @return the number of actions done before item dump.
     */
    @Override
    protected int getActionsDoneUntilDumping()
    {
        return ACTIONS_UNTIL_DUMP;
    }
}
