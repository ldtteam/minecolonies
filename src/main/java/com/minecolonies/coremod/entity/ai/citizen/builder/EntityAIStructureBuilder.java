package com.minecolonies.coremod.entity.ai.citizen.builder;

import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.*;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.coremod.colony.buildings.utils.BuildingBuilderResource;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.coremod.colony.jobs.JobBuilder;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuild;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildBuilding;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildDecoration;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildRemoval;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIStructureWithWorkOrder;
import com.minecolonies.coremod.entity.ai.util.BuildingStructureHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.CitizenConstants.MIN_OPEN_SLOTS;

/**
 * AI class for the builder. Manages building and repairing buildings.
 */
public class EntityAIStructureBuilder extends AbstractEntityAIStructureWithWorkOrder<JobBuilder, BuildingBuilder>
{
    /**
     * over this y level the builder will be faster.
     */
    private static final int DEPTH_LEVEL_0 = 60;

    /**
     * At this y level the builder will be slower.
     */
    private static final int DEPTH_LEVEL_1 = 30;

    /**
     * Max depth difference.
     */
    private static final int MAX_DEPTH_DIFFERENCE = 5;

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
     * After how many actions should the builder dump his inventory.
     */
    private static final int ACTIONS_UNTIL_DUMP = 4096;

    /**
     * Min distance from placing block.
     */
    private static final int MIN_DISTANCE = 3;

    /**
     * Max distance to placing block.
     */
    private static final int MAX_DISTANCE = 10;

    /**
     * After which distance the builder has to recalculate his position.
     */
    private static final double ACCEPTANCE_DISTANCE = 20;

    /**
     * Building level to purge mobs at the build site.
     */
    private static final int LEVEL_TO_PURGE_MOBS = 4;

    /**
     * The id in the list of the last picked up item.
     */
    private int pickUpCount = 0;

    /**
     * Initialize the builder and add all his tasks.
     *
     * @param job the job he has.
     */
    public EntityAIStructureBuilder(@NotNull final JobBuilder job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING, 100),
          new AITarget(START_WORKING, this::checkForWorkOrder, this::startWorkingAtOwnBuilding, 100),
          new AITarget(PICK_UP, this::pickUpMaterial, 5)
        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    public IAIState getStateAfterPickUp()
    {
        return PICK_UP;
    }

    /**
     * State to pick up material before going back to work.
     *
     * @return the next state to go to.
     */
    public IAIState pickUpMaterial()
    {
        final BuildingBuilder building = getOwnBuilding();
        final List<Tuple<Predicate<ItemStack>, Integer>> neededItemsList = new ArrayList<>();
        boolean only_64 = false;

        Map<String, BuildingBuilderResource> neededRessourcesMap = building.getNeededResources();
        if (neededRessourcesMap.size() > InventoryUtils.openSlotCount(worker.getInventoryCitizen()) - MIN_OPEN_SLOTS)
        {
            only_64 = true;
        }
        else
        {
            int stackCount = 0;

            for (final BuildingBuilderResource stack : neededRessourcesMap.values())
            {
                int amount = stack.getAmount();
                while (amount > 0)
                {
                    stackCount++;
                    amount = amount - stack.getItemStack().getMaxStackSize();
                }
            }
            if (stackCount > InventoryUtils.openSlotCount(worker.getInventoryCitizen()) - MIN_OPEN_SLOTS)
            {
                only_64 = true;
            }
        }

        for (final BuildingBuilderResource stack : neededRessourcesMap.values())
        {
            int amount = stack.getAmount();
            if (only_64)
            {
                if (amount > stack.getItemStack().getMaxStackSize())
                {
                    amount = stack.getItemStack().getMaxStackSize();
                }
            }
            neededItemsList.add(new Tuple<>(itemstack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack.getItemStack(), itemstack, true, true), amount));
        }

        if (neededItemsList.size() <= pickUpCount || InventoryUtils.openSlotCount(worker.getInventoryCitizen()) <= MIN_OPEN_SLOTS)
        {
            pickUpCount = 0;
            return START_WORKING;
        }

        needsCurrently = neededItemsList.get(pickUpCount);
        pickUpCount++;

        if (structurePlacer == null || !structurePlacer.getB().hasBluePrint())
        {
            return IDLE;
        }

        if (structurePlacer.getB().getStage() != BuildingStructureHandler.Stage.DECORATE)
        {
            needsCurrently = new Tuple<>(needsCurrently.getA().and(stack -> !ItemStackUtils.isDecoration(stack)), needsCurrently.getB());
        }

        if (InventoryUtils.hasItemInProvider(building.getTileEntity(), needsCurrently.getA()))
        {
            return GATHERING_REQUIRED_MATERIALS;
        }

        return pickUpMaterial();
    }

    @Override
    public Class<BuildingBuilder> getExpectedBuildingClass()
    {
        return BuildingBuilder.class;
    }

    /**
     * Checks if we got a valid workorder.
     *
     * @return true if we got a workorder to work with
     */
    private boolean checkForWorkOrder()
    {
        if (!job.hasWorkOrder())
        {
            getOwnBuilding().searchWorkOrder();
            return false;
        }

        final WorkOrderBuildDecoration wo = job.getWorkOrder();

        if (wo == null)
        {
            job.setWorkOrder(null);
            getOwnBuilding().setProgressPos(null, null);
            return false;
        }

        final IBuilding building = job.getColony().getBuildingManager().getBuilding(wo.getBuildingLocation());
        if (building == null && wo instanceof WorkOrderBuild && !(wo instanceof WorkOrderBuildRemoval))
        {
            job.complete();
            return false;
        }

        if (!job.hasBlueprint())
        {
            super.initiate();
        }

        return true;
    }

    @Override
    public boolean isAfterDumpPickupAllowed()
    {
        return !checkForWorkOrder();
    }

    private IAIState startWorkingAtOwnBuilding()
    {
        if (walkToBuilding())
        {
            return getState();
        }
        return START_BUILDING;
    }

    /**
     * Kill all mobs at the building site.
     */
    private void killMobs()
    {
        if (getOwnBuilding().getBuildingLevel() >= LEVEL_TO_PURGE_MOBS && job.getWorkOrder() instanceof WorkOrderBuildBuilding)
        {
            final BlockPos buildingPos = job.getWorkOrder().getBuildingLocation();
            final IBuilding building = worker.getCitizenColonyHandler().getColony().getBuildingManager().getBuilding(buildingPos);
            if (building != null)
            {
                world.getEntitiesWithinAABB(MonsterEntity.class, building.getTargetableArea(world)).forEach(Entity::remove);
            }
        }
    }

    @Override
    public void checkForExtraBuildingActions()
    {
        if (!getOwnBuilding().hasPurgedMobsToday())
        {
            killMobs();
            getOwnBuilding().setPurgedMobsToday(true);
        }
    }

    @Override
    public IAIState afterRequestPickUp()
    {
        return INVENTORY_FULL;
    }

    @Override
    public IAIState afterDump()
    {
        return PICK_UP;
    }

    @Override
    public boolean walkToConstructionSite(final BlockPos targetPos)
    {
        if (workFrom == null || MathUtils.twoDimDistance(targetPos, workFrom) < MIN_DISTANCE || MathUtils.twoDimDistance(targetPos, workFrom) > ACCEPTANCE_DISTANCE)
        {
            workFrom = getWorkingPosition(targetPos);
        }

        return worker.isWorkerAtSiteWithMove(workFrom, MAX_DISTANCE) || MathUtils.twoDimDistance(worker.getPosition(), workFrom) <= ACCEPTANCE_DISTANCE;
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
            final int yEnd = targetPosition.getY() < schemPos.getY() ? Math.max(targetPosition.getY(), schemPos.getY() - MAX_DEPTH_DIFFERENCE) : schemPos.getY();
            final Direction direction = BlockPosUtil.getXZFacing(worker.getPosition(), targetPosition).getOpposite();
            for (int i = MIN_DISTANCE + 1; i < MAX_DISTANCE; i++)
            {
                for (int y = yStart; y >= yEnd; y--)
                {
                    final BlockPos pos = targetPosition.offset(direction, i);
                    final BlockPos basePos = new BlockPos(pos.getX(), y, pos.getZ());
                    if (EntityUtils.checkForFreeSpace(world, basePos))
                    {
                        return basePos;
                    }
                }
            }
            return schemPos.up();
        }
        return targetPosition;
    }

    @Override
    public boolean shallReplaceSolidSubstitutionBlock(final Block worldBlock, final BlockState worldMetadata)
    {
        return false;
    }

    @Override
    public BlockState getSolidSubstitution(@NotNull final BlockPos location)
    {
        return BlockUtils.getSubstitutionBlockAtWorld(world, location).getBlockState();
    }

    @Override
    public int getBlockMiningDelay(@NotNull final Block block, @NotNull final BlockPos pos)
    {
        final int initialDelay = super.getBlockMiningDelay(block, pos);

        if (pos.getY() > DEPTH_LEVEL_0 || !MineColonies.getConfig().getCommon().restrictBuilderUnderground.get())
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
