package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.PlantationModule;
import com.minecolonies.coremod.colony.fields.PlantationField;
import com.minecolonies.coremod.entity.ai.citizen.planter.EntityAIWorkPlanter;
import com.minecolonies.coremod.util.CollectorUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayer;
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
public abstract class PercentageHarvestPlantModule extends PlantationModule
{
    /**
     * Default constructor.
     *
     * @param fieldTag the tag of the field anchor block.
     * @param workTag  the tag of the working positions.
     * @param item     the item which is harvested.
     */
    protected PercentageHarvestPlantModule(
      final String fieldTag,
      final String workTag,
      final Item item)
    {
        super(fieldTag, workTag, item);
    }

    @Override
    public PlanterAIModuleResult workField(
      @NotNull final PlantationField field,
      @NotNull final EntityAIWorkPlanter planterAI,
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
    protected boolean walkToWorkPosition(final EntityAIWorkPlanter planterAI, final PlantationField field, final BlockPos workPosition)
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
    private PlanterAIModuleState decideWorkAction(PlantationField field, BlockPos plantingPosition)
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
    protected BlockState generatePlantingBlockState(PlantationField field, BlockPos workPosition, BlockState blockState)
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
    public @Nullable BlockPos getNextWorkingPosition(final PlantationField field)
    {
        final List<BlockPos> workingPositions = field.getWorkingPositions().stream().collect(CollectorUtils.toShuffledList());
        final List<BlockPos> harvestablePositions = new ArrayList<>();

        final double minimumPlantFraction = Mth.clamp(getMinimumPlantPercentage(), 0, 100) / 100d;
        final int minimumPlantCount = (int) Math.ceil(minimumPlantFraction * workingPositions.size());

        for (BlockPos position : workingPositions)
        {
            final PlanterAIModuleState action = decideWorkAction(field, position);
            if (action == PlanterAIModuleState.CLEARING)
            {
                return position;
            }
            if (action == PlanterAIModuleState.HARVESTING)
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
    public List<ItemStack> getRequiredItemsForOperation()
    {
        return List.of(new ItemStack(getItem()));
    }

    @Override
    public int getActionLimit()
    {
        return 5;
    }

    /**
     * The percentage of positions the planter should <b>always</b> leave present at the bare minimum.
     * If this is not reached the planter will plant additional plants to reach this minimum.
     *
     * @return a percentage of plants to always leave be.
     */
    protected abstract int getMinimumPlantPercentage();
}
