package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic;

import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.colony.fields.plantation.BasicPlanterAI;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.constant.CitizenConstants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.AbstractPlantationModule;
import com.minecolonies.coremod.util.CollectorUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Plantation module for plants that grow attached to any horizontal side of a tree log.
 * Similar to plants like cocoa beans.
 * <br/>
 * Requirements:
 * <ol>
 *     <li>Must grow on the side of a vertically standing log.</li>
 *     <li>Must be harvested by chopping and re-planting the plant of the tree.</li>
 *     <li>Every harvestable must be within {@link CitizenConstants#DEFAULT_RANGE_FOR_DELAY} blocks range of any available walking position, if not the entity will not be able path to the position.</li>
 * </ol>
 */
public abstract class TreeSidePlantModule extends AbstractPlantationModule
{
    /**
     * Default constructor.
     *
     * @param fieldTag the tag of the field anchor block.
     * @param workTag  the tag of the working positions.
     * @param item     the item which is harvested.
     */
    protected TreeSidePlantModule(
      final String fieldTag,
      final String workTag,
      final Item item)
    {
        super(fieldTag, workTag, item);
    }

    @Override
    public PlanterAIModuleResult workField(
      @NotNull final IField field,
      @NotNull final BasicPlanterAI planterAI,
      @NotNull final AbstractEntityCitizen worker,
      @NotNull final BlockPos workPosition,
      @NotNull final FakePlayer fakePlayer)
    {
        if (walkToWorkPosition(planterAI, field, workPosition))
        {
            return PlanterAIModuleResult.MOVING;
        }

        PlanterAIModuleState action = decideWorkAction(field, workPosition);
        return switch (action)
        {
            case NONE -> PlanterAIModuleResult.NONE;
            case HARVESTING -> getHarvestingResultFromMiningResult(planterAI.planterMineBlock(workPosition, true));
            case PLANTING ->
            {
                if (planterAI.planterPlaceBlock(workPosition, getItem(), getPlantsToRequest(), state -> generatePlantingBlockState(field, workPosition, state)))
                {
                    yield PlanterAIModuleResult.PLANTED;
                }
                yield PlanterAIModuleResult.REQUIRES_ITEMS;
            }
            case CLEARED -> getClearingResultFromMiningResult(planterAI.planterMineBlock(workPosition, false));
            default -> PlanterAIModuleResult.INVALID;
        };
    }

    /**
     * Logic to walk to a work position.
     * Default implementation, walk to any adjacent block which is free.
     *
     * @param planterAI    the AI class of the planter so instructions can be ordered to it.
     * @param field        the field class.
     * @param workPosition the position that has been chosen for work.
     * @return true if
     */
    protected boolean walkToWorkPosition(final BasicPlanterAI planterAI, final IField field, final BlockPos workPosition)
    {
        // If an empty adjacent position was found, we move to that position directly,
        // else we move to the work position itself and let entity pathing figure out how to get there (within the default range).
        Level world = field.getColony().getWorld();
        final BlockPos walkPosition = Stream.of(workPosition.north(), workPosition.south(), workPosition.west(), workPosition.east())
                                        .filter(pos -> world.getBlockState(pos).isAir())
                                        .findFirst()
                                        .orElse(workPosition);
        return planterAI.planterWalkToBlock(walkPosition);
    }

    /**
     * Responsible for deciding what action the AI is going to perform on a specific field position
     * depending on the state of the working position.
     *
     * @param field            the field to check for.
     * @param plantingPosition the specific position to check for.
     * @return the {@link PlanterAIModuleResult} that the AI is going to perform.
     */
    private PlanterAIModuleState decideWorkAction(IField field, BlockPos plantingPosition)
    {
        BlockState blockState = field.getColony().getWorld().getBlockState(plantingPosition);
        if (isValidPlantingBlock(blockState))
        {
            return PlanterAIModuleState.PLANTING;
        }

        if (isValidClearingBlock(blockState))
        {
            return PlanterAIModuleState.CLEARING;
        }

        if (isValidHarvestBlock(blockState))
        {
            return PlanterAIModuleState.HARVESTING;
        }

        return PlanterAIModuleState.NONE;
    }

    /**
     * Generate a block state for a new plant.
     *
     * @param field        the field reference to fetch data from.
     * @param workPosition the position that has been chosen for work.
     * @param blockState   the default block state for the block.
     */
    protected BlockState generatePlantingBlockState(IField field, BlockPos workPosition, BlockState blockState)
    {
        return blockState;
    }

    /**
     * Check if the block is a correct block for planting.
     * Defaults to being any air block.
     *
     * @param blockState the block state.
     * @return whether the block can be planted.
     */
    protected boolean isValidPlantingBlock(BlockState blockState)
    {
        return blockState.isAir();
    }

    /**
     * Check if the block is a correct block for clearing.
     *
     * @param blockState the block state.
     * @return whether the block can be cleared.
     */
    protected boolean isValidClearingBlock(BlockState blockState)
    {
        return !isValidHarvestBlock(blockState);
    }

    /**
     * Check if the block is a correct block for harvesting.
     *
     * @param blockState the block state.
     * @return whether the block can be harvested.
     */
    protected abstract boolean isValidHarvestBlock(BlockState blockState);

    @Override
    public @Nullable BlockPos getNextWorkingPosition(final IField field)
    {
        for (BlockPos position : getWorkingPositions(field))
        {
            final PlanterAIModuleState action = decideWorkAction(field, position);
            if (action != PlanterAIModuleState.NONE)
            {
                return position;
            }
        }

        return null;
    }

    @Override
    public List<ItemStack> getRequiredItemsForOperation()
    {
        return List.of(new ItemStack(getItem()));
    }

    @Override
    public int getActionLimit()
    {
        return 5;
    }

    @Override
    public List<BlockPos> getValidWorkingPositions(final @NotNull Level world, final List<BlockPos> workingPositions)
    {
        Set<BlockPos> treePositions = new HashSet<>();
        for (BlockPos position : workingPositions)
        {
            for (BlockPos adjacentPosition : List.of(position.north(), position.south(), position.west(), position.east()))
            {
                if (world.getBlockState(adjacentPosition).isAir())
                {
                    treePositions.add(adjacentPosition);
                }
            }
        }
        return super.getValidWorkingPositions(world, treePositions.stream().collect(CollectorUtils.toShuffledList()));
    }
}