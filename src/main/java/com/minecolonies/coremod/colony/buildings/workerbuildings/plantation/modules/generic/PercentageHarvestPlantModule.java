package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic;

import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.AbstractPlantationModule;
import com.minecolonies.coremod.util.CollectorUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Plantation module for plants that should always keep an X amount of plants on the ground in order to keep spreading, similar to mushrooms.
 * <br/>
 * Requirements:
 * <ol>
 *     <li>These plants generally have a random growth logic, mushrooms can spread in any direction, not even attached to next to the other mushroom. Vines can spread into any direction, etc.</li>
 *     <li>All the positions you expect the plants to appear have to be tagged.</li>
 * </ol>
 */
public abstract class PercentageHarvestPlantModule extends AbstractPlantationModule
{
    /**
     * Default constructor.
     *
     * @param field    the field instance this module is working on.
     * @param fieldTag the tag of the field anchor block.
     * @param workTag  the tag of the working positions.
     * @param item     the item which is harvested.
     */
    protected PercentageHarvestPlantModule(
      final IField field,
      final String fieldTag,
      final String workTag,
      final Item item)
    {
        super(field, fieldTag, workTag, item);
    }

    @Override
    public PlantationModuleResult.Builder decideFieldWork(@NotNull final BlockPos workingPosition)
    {
        ActionToPerform action = decideWorkAction(workingPosition);
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
     * @param plantingPosition the specific position to check for.
     * @return the {@link PlantationModuleResult} that the AI is going to perform.
     */
    private ActionToPerform decideWorkAction(BlockPos plantingPosition)
    {
        BlockState blockState = field.getColony().getWorld().getBlockState(plantingPosition);
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
    public @Nullable BlockPos getNextWorkingPosition()
    {
        final List<BlockPos> workingPositions = getWorkingPositions().stream().collect(CollectorUtils.toShuffledList());
        final List<BlockPos> harvestablePositions = new ArrayList<>();

        final double minimumPlantFraction = Mth.clamp(getMinimumPlantPercentage(), 0, 100) / 100d;
        final int minimumPlantCount = (int) Math.ceil(minimumPlantFraction * workingPositions.size());

        for (BlockPos position : workingPositions)
        {
            final ActionToPerform action = decideWorkAction(position);
            if (action == ActionToPerform.CLEAR)
            {
                return position;
            }
            if (action == ActionToPerform.HARVEST)
            {
                harvestablePositions.add(position);
            }
        }

        if (minimumPlantCount > harvestablePositions.size())
        {
            // We want to prevent putting "harvestable" blocks next to one another as much as possible.
            Set<BlockPos> excludedPositions = harvestablePositions.stream()
                                                .flatMap(f -> Stream.of(f, f.above(), f.below(), f.north(), f.south(), f.west(), f.east()))
                                                .collect(Collectors.toSet());
            return workingPositions.stream()
                     .filter(f -> !excludedPositions.contains(f))
                     .findFirst()
                     .orElse(null);
        }
        else if (minimumPlantCount < harvestablePositions.size())
        {
            Set<BlockPos> duplicateLocator = new HashSet<>();
            return harvestablePositions.stream()
                     .flatMap(f -> Stream.of(f, f.above(), f.below(), f.north(), f.south(), f.west(), f.east()))
                     .filter(f -> !duplicateLocator.add(f))
                     .findFirst()
                     .orElse(harvestablePositions.get(0));
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

    /**
     * The percentage of positions the planter should <b>always</b> leave present at the bare minimum.
     * If this is not reached the planter will plant additional plants to reach this minimum.
     *
     * @return a percentage of plants to always leave be.
     */
    protected abstract int getMinimumPlantPercentage();

    @Override
    public BlockPos getPositionToWalkTo(final BlockPos workingPosition)
    {
        Level world = field.getColony().getWorld();
        return Stream.of(workingPosition.north(), workingPosition.south(), workingPosition.west(), workingPosition.east())
                 .filter(pos -> world.getBlockState(pos).isAir())
                 .findFirst()
                 .orElse(workingPosition);
    }
}
