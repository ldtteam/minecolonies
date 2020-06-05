package com.minecolonies.coremod.entity.ai.citizen.planter;

import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingPlantation;
import com.minecolonies.coremod.colony.jobs.JobPlanter;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import net.minecraft.block.AirBlock;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
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
    private static final int    MAX_BLOCKS_MINED    = 64;

    /**
     * The quantity to request.
     */
    private static final Integer PLANT_TO_REQUEST = 16;

    /**
     * The current farm pos to take care of.
     */
    private BlockPos workPos;

    /**
     * Constructor for the planter.
     *
     * @param job a planter job to use.
     */
    public EntityAIWorkPlanter(@NotNull final JobPlanter job)
    {
        super(job);
        super.registerTargets(
          new AITarget(PLANTATION_FARM, this::farm, TICKS_20),
          new AITarget(PLANTATION_PLANT, this::plant, TICKS_20)
        );
        worker.setCanPickUpLoot(true);
    }

    /**
     * Plant something for the current state.
     * @return the next state to go to.
     */
    private IAIState plant()
    {
        if (workPos == null)
        {
            return START_WORKING;
        }

        if (walkToBlock(workPos.up()))
        {
            return getState();
        }

        final ItemStack currentStack = new ItemStack(getOwnBuilding().getCurrentPhase());
        final int plantInInv = InventoryUtils.getItemCountInItemHandler((worker.getInventoryCitizen()), itemStack -> itemStack.isItemEqual(currentStack));
        if (plantInInv <= 0)
        {
            return START_WORKING;
        }

        if (world.setBlockState(workPos.up(), BlockUtils.getBlockStateFromStack(currentStack)))
        {
            InventoryUtils.reduceStackInItemHandler(worker.getInventoryCitizen(), currentStack);
        }

        return START_WORKING;
    }

    /**
     * Farm some of the plants.
     * @return next state to go to.
     */
    private IAIState farm()
    {
        if (workPos == null)
        {
            return START_WORKING;
        }

        if (walkToBlock(workPos.up()))
        {
            return getState();
        }

        if (!holdEfficientTool(world.getBlockState(workPos.up()).getBlock(), workPos.up()))
        {
            return START_WORKING;
        }

        if (!(world.getBlockState(workPos.up()).getBlock() instanceof AirBlock))
        {
            mineBlock(workPos.up());
            return getState();
        }

        for (final ItemEntity item : world.getEntitiesWithinAABB(ItemEntity.class, new AxisAlignedBB(worker.getPosition()).expand(4.0F, 1.0F, 4.0F).expand(-4.0F, -1.0F, -4.0F)))
        {
            if (item != null)
            {
                worker.getCitizenItemHandler().tryPickupItemEntity(item);
            }
        }

        return START_WORKING;
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return MAX_BLOCKS_MINED;
    }

    @Override
    public Class<BuildingPlantation> getExpectedBuildingClass()
    {
        return BuildingPlantation.class;
    }

    //todo check on healer
    @Override
    protected IAIState decide()
    {
        final IAIState nextState = super.decide();
        if (nextState != START_WORKING)
        {
            return nextState;
        }

        final BuildingPlantation plantation = getOwnBuilding();

        final List<BlockPos> list = plantation.getPosForPhase(world);
        for (final BlockPos pos : list)
        {
            if (isAtLeastThreeHigh(pos))
            {
                this.workPos = pos;
                return PLANTATION_FARM;
            }
        }

        final Item current = plantation.getCurrentPhase();

        final int plantInBuilding = InventoryUtils.getItemCountInProvider(getOwnBuilding(), itemStack -> itemStack.isItemEqual(new ItemStack(current)));
        final int plantInInv = InventoryUtils.getItemCountInItemHandler((worker.getInventoryCitizen()), itemStack -> itemStack.isItemEqual(new ItemStack(current)));

        if (plantInBuilding + plantInInv <= 0)
        {
            requestPlantable(current);
            return START_WORKING;
        }

        if (plantInInv == 0 && plantInBuilding > 0)
        {
            needsCurrently = new Tuple<>(itemStack -> itemStack.isItemEqual(new ItemStack(current)), Math.min(plantInBuilding, PLANT_TO_REQUEST));
            return GATHERING_REQUIRED_MATERIALS;
        }

        for (final BlockPos pos : list)
        {
            if (world.getBlockState(pos.up()).getBlock() instanceof AirBlock)
            {
                this.workPos = pos;
                return PLANTATION_PLANT;
            }
        }

        return START_WORKING;
    }

    /**
     * Async request for paper to the colony.
     * @param current the current plantable.
     */
    private void requestPlantable(final Item current)
    {
        if (!getOwnBuilding().hasWorkerOpenRequestsFiltered(worker.getCitizenData(),
          q -> q.getRequest() instanceof Stack && ((Stack) q.getRequest()).getStack().getItem() == current))
        {
            worker.getCitizenData().createRequestAsync(new Stack(new ItemStack(current, PLANT_TO_REQUEST)));
        }
    }

    /**
     * Check if the plant at pos it at least three high.
     * @param pos the pos to check
     * @return true if so.
     */
    private boolean isAtLeastThreeHigh(final BlockPos pos)
    {
        return !(world.getBlockState(pos.up(1)).getBlock() instanceof AirBlock)
        && !(world.getBlockState(pos.up(2)).getBlock() instanceof AirBlock)
        && !(world.getBlockState(pos.up(3)).getBlock() instanceof AirBlock);
    }
}
