package com.minecolonies.coremod.entity.ai.citizen.planter;

import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.CitizenConstants;
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
import org.apache.logging.log4j.Level;
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
    private BuildingPlantation.PlantationSoilPosition plantableSoilPos;

    /**
     * Constructor for the planter.
     *
     * @param job a planter job to use.
     */
    public EntityAIWorkPlanter(@NotNull final JobPlanter job)
    {
        super(job);
        super.registerTargets(
          new AITarget(PLANTATION_MOVE_TO_SOIL, this::moveToSoil, TICKS_20),
          new AITarget(PLANTATION_CHECK_SOIL, this::checkSoil, TICKS_20),
          new AITarget(PLANTATION_CLEAR_OBSTACLE, this::clearObstacle, TICKS_20),
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
        if (plantableSoilPos == null)
        {
            return START_WORKING;
        }

        if (walkToBlock(plantableSoilPos.getPosition().above()))
        {
            return getState();
        }

        final ItemStack currentStack = new ItemStack(plantableSoilPos.getCombination().getItem());
        if (!hasSufficientItems(currentStack))
        {
            return START_WORKING;
        }

        if (world.setBlockAndUpdate(plantableSoilPos.getPosition().above(), BlockUtils.getBlockStateFromStack(currentStack)))
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
        if (plantableSoilPos == null)
        {
            return START_WORKING;
        }

        if (isItemPositionAir(plantableSoilPos))
        {
            return START_WORKING;
        }

        if (walkToBlock(plantableSoilPos.getPosition().above()))
        {
            return getState();
        }

        if (!holdEfficientTool(world.getBlockState(plantableSoilPos.getPosition().above()), plantableSoilPos.getPosition().above()))
        {
            return START_WORKING;
        }

        if (positionHasInvalidBlock(plantableSoilPos))
        {
            mineBlock(plantableSoilPos.getPosition().above());
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
        if (plantableSoilPos == null)
        {
            return START_WORKING;
        }

        if (isItemPositionAir(plantableSoilPos))
        {
            return START_WORKING;
        }

        if (walkToBlock(plantableSoilPos.getPosition().above()))
        {
            return getState();
        }

        if (!holdEfficientTool(world.getBlockState(plantableSoilPos.getPosition().above()), plantableSoilPos.getPosition().above()))
        {
            return START_WORKING;
        }

        if (!positionHasInvalidBlock(plantableSoilPos))
        {
            mineBlock(plantableSoilPos.getPosition().above());
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

    /**
     * Check the selected soil on what to do.
     *
     * @return next state to go to.
     */
    private IAIState checkSoil()
    {
        if (plantableSoilPos == null)
        {
            return START_WORKING;
        }

        final BuildingPlantation plantation = getOwnBuilding();
        final List<Item> availablePlants = plantation.getAvailablePlants();

        if (isItemPositionAir(plantableSoilPos))
        {
            final Item currentItem = plantableSoilPos.getCombination().getItem();

            if (!availablePlants.contains(currentItem))
            {
                return START_WORKING;
            }

            if (!hasSufficientItems(new ItemStack(currentItem)))
            {
                requestPlantable(currentItem);
                return START_WORKING;
            }

            return PLANTATION_PLANT;
        }
        else
        {
            if (positionHasInvalidBlock(plantableSoilPos))
            {
                return PLANTATION_CLEAR_OBSTACLE;
            }

            if (isSufficientHeight(plantableSoilPos) || !availablePlants.contains(plantableSoilPos.getCombination().getItem()))
            {
                return PLANTATION_FARM;
            }
        }

        return START_WORKING;
    }

    /**
     * Move towards the selected soil.
     *
     * @return next state to go to.
     */
    private IAIState moveToSoil()
    {
        if (plantableSoilPos == null)
        {
            return START_WORKING;
        }

        if (walkToBlock(plantableSoilPos.getPosition().above(), CitizenConstants.DEFAULT_RANGE_FOR_DELAY * 2))
        {
            return getState();
        }

        return PLANTATION_CHECK_SOIL;
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
        final List<BuildingPlantation.PlantationSoilPosition> soilPositions = plantation.getAllSoilPositions();

        if (soilPositions.isEmpty())
        {
            Log.getLogger()
              .log(Level.WARN, "Planter building returned 0 available soil positions, schematic is " + plantation.getSchematicName() + ", please report this to the developer!");
            return START_WORKING;
        }

        final int soilPositionIndex = worker.getRandom().nextInt(soilPositions.size());

        plantableSoilPos = soilPositions.get(soilPositionIndex);
        return PLANTATION_MOVE_TO_SOIL;
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
     * @param plantationSoilPosition the item position data.
     * @return true if so.
     */
    private boolean isItemPositionAir(final BuildingPlantation.PlantationSoilPosition plantationSoilPosition)
    {
        Block foundBlock = world.getBlockState(plantationSoilPosition.getPosition().above(1)).getBlock();
        return foundBlock instanceof AirBlock;
    }

    /**
     * Check if the position has a valid plant.
     *
     * @param plantationSoilPosition the item position data.
     * @return true if so.
     */
    private boolean positionHasInvalidBlock(final BuildingPlantation.PlantationSoilPosition plantationSoilPosition)
    {
        Block foundBlock = world.getBlockState(plantationSoilPosition.getPosition().above(1)).getBlock();
        return plantationSoilPosition.getCombination().getBlock() != foundBlock;
    }

    /**
     * Check if the plant at pos it at least x blocks high.
     *
     * @param plantationSoilPosition the item position data.
     * @return true if so.
     */
    private boolean isSufficientHeight(final BuildingPlantation.PlantationSoilPosition plantationSoilPosition)
    {
        BlockPos position = plantationSoilPosition.getPosition();
        int minLength = plantationSoilPosition.getCombination().getMinimumLength();
        for (int i = 1; i <= minLength; i++)
        {
            if (world.getBlockState(position.above(i)).getBlock() != plantationSoilPosition.getCombination().getBlock())
            {
                return false;
            }
        }
        return true;
    }

    private boolean hasSufficientItems(ItemStack stack)
    {
        final int plantInBuilding = InventoryUtils.getCountFromBuilding(getOwnBuilding(), itemStack -> itemStack.sameItem(stack));
        final int plantInInv = InventoryUtils.getItemCountInItemHandler((worker.getInventoryCitizen()), itemStack -> itemStack.sameItem(stack));
        return plantInBuilding + plantInInv > 0;
    }
}
