package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic;

import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.util.constant.CitizenConstants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.AbstractPlantationModule;
import com.minecolonies.coremod.util.CollectorUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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
     * @param field    the field instance this module is working on.
     * @param fieldTag the tag of the field anchor block.
     * @param workTag  the tag of the working positions.
     * @param item     the item which is harvested.
     */
    protected TreeSidePlantModule(
      final IField field,
      final String fieldTag,
      final String workTag,
      final Item item)
    {
        super(field, fieldTag, workTag, item);
    }

    @Override
    public PlantationModuleResult.Builder decideFieldWork(final Level world, final @NotNull BlockPos workingPosition)
    {
        ActionToPerform action = decideWorkAction(world, workingPosition);
        return switch (action)
        {
            case HARVEST -> new PlantationModuleResult.Builder()
                              .harvest(workingPosition)
                              .pickNewPosition();
            case PLANT -> new PlantationModuleResult.Builder()
                            .plant(workingPosition)
                            .pickNewPosition();
            case CLEAR -> new PlantationModuleResult.Builder()
                            .clear(workingPosition)
                            .pickNewPosition();
            default -> PlantationModuleResult.NONE;
        };
    }

    /**
     * Responsible for deciding what action the AI is going to perform on a specific field position
     * depending on the state of the working position.
     *
     * @param world            the world reference that can be used for block state lookups.
     * @param plantingPosition the specific position to check for.
     * @return the {@link PlantationModuleResult} that the AI is going to perform.
     */
    private ActionToPerform decideWorkAction(final Level world, final BlockPos plantingPosition)
    {
        BlockState blockState = world.getBlockState(plantingPosition);
        if (isValidPlantingBlock(blockState))
        {
            return ActionToPerform.PLANT;
        }

        if (isValidClearingBlock(blockState))
        {
            return ActionToPerform.CLEAR;
        }

        if (isValidHarvestBlock(blockState))
        {
            return ActionToPerform.HARVEST;
        }

        return ActionToPerform.NONE;
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
    public @Nullable BlockPos getNextWorkingPosition(final Level world)
    {
        for (BlockPos position : getWorkingPositions())
        {
            if (decideWorkAction(world, position) != ActionToPerform.NONE)
            {
                return position;
            }
        }

        return null;
    }

    @Override
    public int getActionLimit()
    {
        return 5;
    }

    @Override
    public List<ItemStack> getRequiredItemsForOperation()
    {
        return List.of(new ItemStack(getItem()));
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

    @Override
    public BlockPos getPositionToWalkTo(final Level world, final BlockPos workingPosition)
    {
        return Stream.of(workingPosition.north(), workingPosition.south(), workingPosition.west(), workingPosition.east())
                 .filter(pos -> world.getBlockState(pos).isAir())
                 .findFirst()
                 .orElse(workingPosition);
    }
}