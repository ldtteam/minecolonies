package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic;

import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.AbstractPlantationModule;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

/**
 * Plantation module for plants that grow vertically upwards, similar to sugar cane.
 * <br/>
 * Requirements:
 * <ol>
 *     <li>Grow vertically upwards without any outcroppings.</li>
 *     <li>Grow uninterrupted (no gaps in between the plant).</li>
 *     <li>Must break all blocks above when a lower block is destroyed.</li>
 * </ol>
 */
public abstract class UpwardsGrowingPlantModule extends AbstractPlantationModule
{
    /**
     * The default minimum plant length.
     */
    protected static final int DEFAULT_MINIMUM_PLANT_LENGTH = 3;

    /**
     * The internal random used to decide whether to work this field or not.
     */
    private final Random random;

    /**
     * Default constructor.
     *
     * @param field    the field instance this module is working on.
     * @param fieldTag the tag of the field anchor block.
     * @param workTag  the tag of the working positions.
     * @param item     the item which is harvested.
     */
    protected UpwardsGrowingPlantModule(
      final IField field,
      final String fieldTag,
      final String workTag,
      final Item item)
    {
        super(field, fieldTag, workTag, item);
        this.random = new Random();
    }

    @Override
    public PlantationModuleResult.Builder decideFieldWork(final Level world, final @NotNull BlockPos workingPosition)
    {
        ActionToPerform action = decideWorkAction(world, workingPosition, false);
        return switch (action)
        {
            case HARVEST -> new PlantationModuleResult.Builder()
                              .harvest(workingPosition.above(2))
                              .pickNewPosition();
            case PLANT -> new PlantationModuleResult.Builder()
                            .plant(workingPosition.above())
                            .pickNewPosition();
            case CLEAR -> new PlantationModuleResult.Builder()
                            .clear(workingPosition.above())
                            .pickNewPosition();
            default -> PlantationModuleResult.NONE;
        };
    }

    /**
     * Responsible for deciding what action the AI is going to perform on a specific field position
     * depending on the state of the working position.
     *
     * @param world               the world reference that can be used for block state lookups.
     * @param plantingPosition    the specific position to check for.
     * @param enablePercentChance if the field has a maximum length, ensure a percentage roll is thrown to check if harvesting is allowed.
     * @return the {@link PlantationModuleResult} that the AI is going to perform.
     */
    private ActionToPerform decideWorkAction(final Level world, final BlockPos plantingPosition, final boolean enablePercentChance)
    {
        BlockState blockState = world.getBlockState(plantingPosition.above());
        if (isValidPlantingBlock(blockState))
        {
            return ActionToPerform.PLANT;
        }

        if (isValidClearingBlock(blockState))
        {
            return ActionToPerform.CLEAR;
        }

        if (canHarvest(world, plantingPosition, enablePercentChance))
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
     * Checks if the provided block at the given location is harvestable.
     *
     * @param world               the world reference that can be used for block state lookups.
     * @param plantingPosition    the specific position to check for.
     * @param enablePercentChance if the field has a maximum length, ensure a percentage roll is thrown to check if harvesting is allowed.
     * @return true if plant is harvestable.
     */
    private boolean canHarvest(final Level world, final BlockPos plantingPosition, final boolean enablePercentChance)
    {
        int minimumPlantLength = getMinimumPlantLength();
        Integer maximumPlantLength = getMaximumPlantLength();

        if (maximumPlantLength != null && enablePercentChance)
        {
            float currentHeight = 0;
            for (int height = minimumPlantLength; height <= maximumPlantLength; height++)
            {
                BlockState blockState = world.getBlockState(plantingPosition.above(height));
                if (!isValidHarvestBlock(blockState))
                {
                    break;
                }
                currentHeight = height;
            }

            float chance = currentHeight / (float) maximumPlantLength;
            return random.nextFloat() < chance;
        }
        else
        {
            BlockState blockAtMinHeight = world.getBlockState(plantingPosition.above(minimumPlantLength));
            return isValidHarvestBlock(blockAtMinHeight);
        }
    }

    /**
     * Check if the block is a correct block for harvesting.
     *
     * @param blockState the block state.
     * @return whether the block can be harvested.
     */
    protected abstract boolean isValidHarvestBlock(BlockState blockState);

    /**
     * Get the minimum length this plant should grow to before considered harvestable.
     * Defaults to {@link UpwardsGrowingPlantModule#DEFAULT_MINIMUM_PLANT_LENGTH}.
     *
     * @return the minimum plant length
     */
    protected int getMinimumPlantLength()
    {
        return DEFAULT_MINIMUM_PLANT_LENGTH;
    }

    /**
     * Get the maximum length this plant can grow.
     * Defaults to null.
     *
     * @return the maximum plant length
     */
    @Nullable
    protected Integer getMaximumPlantLength()
    {
        return null;
    }

    @Override
    public BlockPos getNextWorkingPosition(final Level world)
    {
        for (BlockPos position : getWorkingPositions())
        {
            if (decideWorkAction(world, position, true) != ActionToPerform.NONE)
            {
                return position;
            }
        }

        return null;
    }

    @Override
    public int getActionLimit()
    {
        return 10;
    }

    @Override
    public List<ItemStack> getRequiredItemsForOperation()
    {
        return List.of(new ItemStack(getItem()));
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