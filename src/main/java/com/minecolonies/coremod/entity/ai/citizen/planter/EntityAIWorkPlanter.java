package com.minecolonies.coremod.entity.ai.citizen.planter;

import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingPlantation;
import com.minecolonies.coremod.colony.jobs.JobPlanter;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.CitizenConstants.TICKS_20;

/**
 * Planter AI class.
 */
public class EntityAIWorkPlanter extends AbstractEntityAICrafting<JobPlanter, BuildingPlantation>
{
    /**
     * Return to chest after this amount of stacks.
     */
    private static final int MAX_BLOCKS_MINED = 64;

    /**
     * The quantity to request.
     */
    private static final Integer PLANT_TO_REQUEST = 16;

    /**
     * Xp per harvesting block
     */
    private static final double XP_PER_HARVEST = 1;

    /**
     * The current farm pos to take care of.
     */
    private BuildingPlantation.PlantationItemPosition workPos;

    /**
     * Constructor for the planter.
     *
     * @param job a planter job to use.
     */
    public EntityAIWorkPlanter(@NotNull final JobPlanter job)
    {
        super(job);
        super.registerTargets(new AITarget(PLANTATION_CLEAR_OBSTACLE, this::clearObstacle, TICKS_20),
          new AITarget(PLANTATION_FARM, this::farm, TICKS_20),
          new AITarget(PLANTATION_PLANT, this::plant, TICKS_20));
        worker.setCanPickUpLoot(true);
    }

    /**
     * Plant something for the current state.
     *
     * @return the next state to go to.
     */
    private IAIState plant()
    {
        if (workPos == null)
        {
            return START_WORKING;
        }

        if (walkToBlock(workPos.getPosition().above()))
        {
            return getState();
        }

        final ItemStack currentStack = new ItemStack(getOwnBuilding().getCurrentPhase());
        final int plantInInv = InventoryUtils.getItemCountInItemHandler((worker.getInventoryCitizen()), itemStack -> itemStack.sameItem(currentStack));
        if (plantInInv <= 0)
        {
            return START_WORKING;
        }

        if (world.setBlockAndUpdate(workPos.getPosition().above(), BlockUtils.getBlockStateFromStack(currentStack)))
        {
            InventoryUtils.reduceStackInItemHandler(worker.getInventoryCitizen(), currentStack);
        }

        return START_WORKING;
    }

    /**
     * Plantation has encountered a non-allowed block on a farming position, remove it.
     *
     * @return next state to go to.
     */
    private IAIState clearObstacle()
    {
        if (workPos == null)
        {
            return START_WORKING;
        }

        if (isItemPositionAir(workPos))
        {
            return START_WORKING;
        }

        if (walkToBlock(workPos.getPosition().above()))
        {
            return getState();
        }

        if (!holdEfficientTool(world.getBlockState(workPos.getPosition().above()), workPos.getPosition().above()))
        {
            return START_WORKING;
        }

        if (positionHasInvalidBlock(workPos))
        {
            mineBlock(workPos.getPosition().above());
            return getState();
        }

        return START_WORKING;
    }

    /**
     * Farm some plants.
     *
     * @return next state to go to.
     */
    private IAIState farm()
    {
        if (workPos == null)
        {
            return START_WORKING;
        }

        if (isItemPositionAir(workPos))
        {
            return START_WORKING;
        }

        if (walkToBlock(workPos.getPosition().above()))
        {
            return getState();
        }

        if (!holdEfficientTool(world.getBlockState(workPos.getPosition().above()), workPos.getPosition().above()))
        {
            return START_WORKING;
        }

        if (!positionHasInvalidBlock(workPos))
        {
            mineBlock(workPos.getPosition().above());
            return getState();
        }

        for (final ItemEntity item : world.getLoadedEntitiesOfClass(ItemEntity.class,
          new AxisAlignedBB(worker.blockPosition()).expandTowards(4.0F, 1.0F, 4.0F).expandTowards(-4.0F, -1.0F, -4.0F)))
        {
            if (item != null)
            {
                worker.getCitizenItemHandler().tryPickupItemEntity(item);
            }
        }

        worker.getCitizenExperienceHandler().addExperience(XP_PER_HARVEST);

        return START_WORKING;
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return MAX_BLOCKS_MINED;
    }

    @Override
    protected int getActionRewardForCraftingSuccess()
    {
        return MAX_BLOCKS_MINED;
    }

    @Override
    public Class<BuildingPlantation> getExpectedBuildingClass()
    {
        return BuildingPlantation.class;
    }

    @Override
    protected IAIState decide()
    {
        final IAIState nextState = super.decide();
        if (nextState != START_WORKING && nextState != IDLE)
        {
            return nextState;
        }

        final BuildingPlantation plantation = getOwnBuilding();

        final Item currentItem = plantation.getNextPhase();
        final List<Item> availablePlants = plantation.getAvailablePlants();
        final List<BuildingPlantation.PlantationItemPosition> list = plantation.getAllWorkPositions();

        // Iterate all positions to check if there's a valid block or if there's anything to harvest
        for (final BuildingPlantation.PlantationItemPosition plantationItemPosition : list)
        {
            if (isItemPositionAir(plantationItemPosition))
            {
                continue;
            }

            if (positionHasInvalidBlock(plantationItemPosition))
            {
                this.workPos = plantationItemPosition;
                return PLANTATION_CLEAR_OBSTACLE;
            }

            if (isAtLeastThreeHigh(plantationItemPosition) || !availablePlants.contains(plantationItemPosition.getCombination().getItem()))
            {
                this.workPos = plantationItemPosition;
                return PLANTATION_FARM;
            }
        }

        final int plantInBuilding = InventoryUtils.getCountFromBuilding(getOwnBuilding(), itemStack -> itemStack.sameItem(new ItemStack(currentItem)));
        final int plantInInv = InventoryUtils.getItemCountInItemHandler((worker.getInventoryCitizen()), itemStack -> itemStack.sameItem(new ItemStack(currentItem)));

        if (plantInBuilding + plantInInv <= 0)
        {
            requestPlantable(currentItem);
            return START_WORKING;
        }

        if (plantInInv == 0 && plantInBuilding > 0)
        {
            needsCurrently = new Tuple<>(itemStack -> itemStack.sameItem(new ItemStack(currentItem)), Math.min(plantInBuilding, PLANT_TO_REQUEST));
            return GATHERING_REQUIRED_MATERIALS;
        }

        for (final BuildingPlantation.PlantationItemPosition plantationItemPosition : list)
        {
            if (plantationItemPosition.getCombination().getItem() == currentItem && isItemPositionAir(plantationItemPosition))
            {
                this.workPos = plantationItemPosition;
                return PLANTATION_PLANT;
            }
        }

        return START_WORKING;
    }

    /**
     * Async request for paper to the colony.
     *
     * @param current the current plantable.
     */
    private void requestPlantable(final Item current)
    {
        if (!getOwnBuilding().hasWorkerOpenRequestsFiltered(worker.getCitizenData().getId(),
          q -> q.getRequest() instanceof Stack && ((Stack) q.getRequest()).getStack().getItem() == current))
        {
            worker.getCitizenData().createRequestAsync(new Stack(new ItemStack(current, PLANT_TO_REQUEST)));
        }
    }

    /**
     * Check if the position is air.
     *
     * @param plantationItemPosition the item position data.
     * @return true if so.
     */
    private boolean isItemPositionAir(final BuildingPlantation.PlantationItemPosition plantationItemPosition)
    {
        Block foundBlock = world.getBlockState(plantationItemPosition.getPosition().above(1)).getBlock();
        return foundBlock instanceof AirBlock;
    }

    /**
     * Check if the position has a valid plant.
     *
     * @param plantationItemPosition the item position data.
     * @return true if so.
     */
    private boolean positionHasInvalidBlock(final BuildingPlantation.PlantationItemPosition plantationItemPosition)
    {
        Block foundBlock = world.getBlockState(plantationItemPosition.getPosition().above(1)).getBlock();
        return plantationItemPosition.getCombination().getBlock() != foundBlock;
    }

    /**
     * Check if the plant at pos it at least three high.
     *
     * @param plantationItemPosition the item position data.
     * @return true if so.
     */
    private boolean isAtLeastThreeHigh(final BuildingPlantation.PlantationItemPosition plantationItemPosition)
    {
        BlockPos position = plantationItemPosition.getPosition();
        Block firstBlock = world.getBlockState(position.above(1)).getBlock();
        if (plantationItemPosition.getCombination().getBlock() != firstBlock)
        {
            return false;
        }
        return firstBlock == world.getBlockState(position.above(2)).getBlock() && firstBlock == world.getBlockState(position.above(3)).getBlock();
    }
}
