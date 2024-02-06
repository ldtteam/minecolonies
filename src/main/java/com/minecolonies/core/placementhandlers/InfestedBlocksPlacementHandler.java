package com.minecolonies.core.placementhandlers;

import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler;
import com.ldtteam.structurize.util.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.ldtteam.structurize.api.constants.Constants.UPDATE_FLAG;

/**
 * Placement handler for replacing infested blocks with their non-infested variants.
 */
public class InfestedBlocksPlacementHandler implements IPlacementHandler
{
    @Override
    public boolean canHandle(final Level world, final BlockPos pos, final BlockState blockState)
    {
        return blockState.getBlock() instanceof InfestedBlock;
    }

    @Override
    public ActionProcessingResult handle(
      final Level world,
      final BlockPos pos,
      final BlockState blockState,
      @Nullable final CompoundTag tileEntityData,
      final boolean complete,
      final BlockPos centerPos,
      final PlacementSettings settings)
    {
        final BlockState expectedBlockState = getExpectedBlockState(blockState, complete);
        if (expectedBlockState == null)
        {
            return ActionProcessingResult.PASS;
        }

        if (world.getBlockState(pos).equals(expectedBlockState))
        {
            return ActionProcessingResult.PASS;
        }

        if (!world.setBlock(pos, expectedBlockState, UPDATE_FLAG))
        {
            return ActionProcessingResult.DENY;
        }

        return ActionProcessingResult.SUCCESS;
    }

    /**
     * Generates the correct block state for the placement.
     *
     * @param blockState the input block state.
     * @param complete   place it complete (with or without substitution blocks etc.).
     * @return the new block state.
     */
    @Nullable
    private static BlockState getExpectedBlockState(final BlockState blockState, final boolean complete)
    {
        if (blockState.getBlock() instanceof InfestedBlock infestedBlock)
        {
            if (complete)
            {
                return blockState;
            }
            else
            {
                return infestedBlock.hostStateByInfested(blockState);
            }
        }

        return null;
    }

    @Override
    public List<ItemStack> getRequiredItems(final Level world, final BlockPos pos, final BlockState blockState, @Nullable final CompoundTag tileEntityData, final boolean complete)
    {
        final BlockState expectedBlockState = getExpectedBlockState(blockState, complete);
        return expectedBlockState != null ? List.of(BlockUtils.getItemStackFromBlockState(expectedBlockState)) : List.of();
    }
}
