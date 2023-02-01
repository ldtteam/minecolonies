package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.colony.buildings.workerbuildings.fields.PlantationField;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.PlantationModule;
import com.minecolonies.coremod.entity.ai.citizen.planter.EntityAIWorkPlanter;
import com.minecolonies.coremod.util.CollectorUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Plantation module for plants that grow attached to any horizontal side of a tree log.
 * Similar to plants like cocoa beans.
 * - Must grow on the side of a vertically standing log.
 * - Must be harvested by chopping and re-planting the plant of the tree.
 */
public abstract class TreeSidePlantModule extends PlantationModule
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
      @NotNull final PlantationField field,
      @NotNull final EntityAIWorkPlanter planterAI,
      @NotNull final AbstractEntityCitizen worker,
      @NotNull final BlockPos workPosition,
      @NotNull final FakePlayer fakePlayer)
    {
        // Because tree side modules can grow on trees to larger heights, we must tell the AI
        // to move to the bottom of the tree, else he ends up attempting to walk to a block he can't reach.
        BlockPos walkPosition = field.getWorkingPositions().stream()
                                  .filter(f -> f.getX() == workPosition.getX() && f.getZ() == workPosition.getZ())
                                  .min(Comparator.comparingInt(Vec3i::getY))
                                  .orElseThrow();
        if (planterAI.planterWalkToBlock(walkPosition))
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

    @Override
    public @Nullable BlockPos getNextWorkingPosition(final PlantationField field)
    {
        for (BlockPos position : field.getWorkingPositions())
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
}
