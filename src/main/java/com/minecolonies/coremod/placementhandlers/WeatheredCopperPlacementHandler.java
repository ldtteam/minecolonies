package com.minecolonies.coremod.placementhandlers;

import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler;
import com.ldtteam.structurize.util.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static com.ldtteam.structurize.api.util.constant.Constants.UPDATE_FLAG;

/**
 * Handler for non-waxed copper blocks.
 */
public class WeatheredCopperPlacementHandler implements IPlacementHandler
{
    /**
     * Generates the correct block state for the placement.
     *
     * @param world      the level.
     * @param pos        the target position.
     * @param blockState the new block state.
     * @param complete   place it complete (with or without substitution blocks etc.).
     * @return the new block state.
     */
    @Nullable
    private static BlockState getExpectedBlockState(
      final Level world,
      final BlockPos pos,
      final BlockState blockState,
      final boolean complete)
    {
        final BlockState inWorldState = world.getBlockState(pos);

        // If we're doing a schematic paste, just always keep what is in the schematic.
        if (complete)
        {
            return blockState;
        }

        // In case the block in the world is not currently any copper block at all, replace it with the minimum level of copper outlined by the schematic.
        if (!(inWorldState.getBlock() instanceof WeatheringCopper inWorldCopper))
        {
            return blockState;
        }

        // In case the block is the wrong "kind" of copper block, replace it with the correct copper block.
        if (!(blockState.getBlock() instanceof WeatheringCopper)
              || !Objects.equals(WeatheringCopper.getFirst(inWorldState.getBlock()), WeatheringCopper.getFirst(blockState.getBlock())))
        {
            return blockState;
        }

        // In case the copper block in the schematic its weathering state is lower than the one in the world, we have to change the block.
        Block currentBlock = blockState.getBlock();
        while (currentBlock != null && !((WeatheringCopper) currentBlock).getAge().equals(inWorldCopper.getAge()))
        {
            currentBlock = WeatheringCopper.getNext(currentBlock).orElse(null);
        }

        // When currentBlock is not null, it means that the in world copper is less aged than the one in the schematic, which means we have to replace it.
        if (currentBlock == null)
        {
            return blockState;
        }

        return null;
    }

    @Override
    public boolean canHandle(final Level world, final BlockPos pos, final BlockState blockState)
    {
        return blockState.getBlock() instanceof WeatheringCopper;
    }

    @Override
    public ActionProcessingResult handle(
      final Level world,
      final BlockPos pos,
      final BlockState blockState,
      @Nullable final CompoundTag tileEntityData,
      final boolean complete,
      final BlockPos centerPos)
    {
        final BlockState expectedBlockState = getExpectedBlockState(world, pos, blockState, complete);

        if (expectedBlockState == null)
        {
            return ActionProcessingResult.PASS;
        }

        if (!world.setBlock(pos, expectedBlockState, UPDATE_FLAG))
        {
            return ActionProcessingResult.DENY;
        }

        return ActionProcessingResult.SUCCESS;
    }

    @Override
    public List<ItemStack> getRequiredItems(final Level world, final BlockPos pos, final BlockState blockState, @Nullable final CompoundTag tileEntityData, final boolean complete)
    {
        final BlockState expectedBlockState = getExpectedBlockState(world, pos, blockState, complete);
        return expectedBlockState != null ? List.of(BlockUtils.getItemStackFromBlockState(expectedBlockState)) : List.of();
    }
}
