package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic;

import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.colony.fields.plantation.BasicPlanterAI;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.AbstractPlantationModule;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

/**
 * Plantation module for plants that grow vertically downwards, similar to sugar glowberries.
 * <br/>
 * Requirements:
 * <ol>
 *     <li>Grow vertically downwards without any outcroppings.</li>
 *     <li>Grow uninterrupted (no gaps in between the plant).</li>
 *     <li>Must break all blocks below when an upper block is destroyed.</li>
 * </ol>
 */
public abstract class DownwardsGrowingPlantModule extends AbstractPlantationModule
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
     * @param fieldTag the tag of the field anchor block.
     * @param workTag  the tag of the working positions.
     * @param item     the item which is harvested.
     */
    protected DownwardsGrowingPlantModule(
      final String fieldTag,
      final String workTag,
      final Item item)
    {
        super(fieldTag, workTag, item);
        this.random = new Random();
    }

    @Override
    public PlanterAIModuleResult workField(
      final @NotNull IField field,
      final @NotNull BasicPlanterAI planterAI,
      final @NotNull AbstractEntityCitizen worker,
      final @NotNull BlockPos workPosition,
      final @NotNull FakePlayer fakePlayer)
    {
        if (walkToWorkPosition(planterAI, field, workPosition.below()))
        {
            return PlanterAIModuleResult.MOVING;
        }

        PlanterAIModuleState action = decideWorkAction(field, workPosition, false);
        return switch (action)
        {
            case NONE -> PlanterAIModuleResult.NONE;
            case HARVESTING ->
                // Tell the AI to mine a block, if we're harvesting we need to mine 1 block from the ceiling (2 below).
              getHarvestingResultFromMiningResult(planterAI.planterMineBlock(workPosition.below(2), true));
            case PLANTING ->
            {
                if (planterAI.planterPlaceBlock(workPosition.below(), getItem(), getPlantsToRequest()))
                {
                    yield PlanterAIModuleResult.PLANTED;
                }
                yield PlanterAIModuleResult.REQUIRES_ITEMS;
            }
            case CLEARING ->
                // Tell the AI to mine a block, if we're clearing an obstacle we need to clear the item at the position directly below (1 high).
              getClearingResultFromMiningResult(planterAI.planterMineBlock(workPosition.below(), false));
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
     * @param field               the field to check for.
     * @param plantingPosition    the specific position to check for.
     * @param enablePercentChance if the field has a maximum length, ensure a percentage roll is thrown to check if harvesting is allowed.
     * @return the {@link PlanterAIModuleResult} that the AI is going to perform.
     */
    private PlanterAIModuleState decideWorkAction(IField field, BlockPos plantingPosition, boolean enablePercentChance)
    {
        BlockState blockState = field.getColony().getWorld().getBlockState(plantingPosition.below());
        if (isValidPlantingBlock(blockState))
        {
            return PlanterAIModuleState.PLANTING;
        }

        if (isValidClearingBlock(blockState))
        {
            return PlanterAIModuleState.CLEARING;
        }

        if (canHarvest(field, plantingPosition, enablePercentChance))
        {
            return PlanterAIModuleState.HARVESTING;
        }

        return PlanterAIModuleState.NONE;
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
     * @param field               the field to check for.
     * @param plantingPosition    the specific position to check for.
     * @param enablePercentChance if the field has a maximum length, ensure a percentage roll is thrown to check if harvesting is allowed.
     * @return true if plant is harvestable.
     */
    private boolean canHarvest(IField field, BlockPos plantingPosition, boolean enablePercentChance)
    {
        int minimumPlantLength = getMinimumPlantLength();
        Integer maximumPlantLength = getMaximumPlantLength();

        if (maximumPlantLength != null && enablePercentChance)
        {
            float currentHeight = 0;
            for (int height = minimumPlantLength; height <= maximumPlantLength; height++)
            {
                BlockState blockState = field.getColony().getWorld().getBlockState(plantingPosition.below(height));
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
            BlockState blockAtMinHeight = field.getColony().getWorld().getBlockState(plantingPosition.below(minimumPlantLength));
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
     * Defaults to {@link DownwardsGrowingPlantModule#DEFAULT_MINIMUM_PLANT_LENGTH}.
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
    public BlockPos getNextWorkingPosition(IField field)
    {
        for (BlockPos position : getWorkingPositions(field))
        {
            if (decideWorkAction(field, position, true) != PlanterAIModuleState.NONE)
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
        return 10;
    }
}