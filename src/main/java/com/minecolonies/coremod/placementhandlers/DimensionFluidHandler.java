package com.minecolonies.coremod.placementhandlers;

import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler;
import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ldtteam.structurize.api.util.constant.Constants.UPDATE_FLAG;

/**
 * Makes lava in the nether free and water everywhere else.
 */
public class DimensionFluidHandler implements IPlacementHandler
{
    @Override
    public boolean canHandle(@NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState blockState)
    {
         return blockState.getBlock() instanceof LiquidBlock || blockState.getBlock() instanceof BubbleColumnBlock;
    }

    @Override
    public List<ItemStack> getRequiredItems(
      @NotNull Level world,
      @NotNull BlockPos pos,
      @NotNull BlockState blockState,
      @Nullable CompoundTag tileEntityData,
      boolean complete)
    {
        final List<ItemStack> itemList = new ArrayList<>();
        if (complete)
        {
            itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
            return itemList;
        }
        if (WorldUtil.isNetherType(world) && blockState.getBlock() == Blocks.LAVA)
        {
            return Collections.emptyList();
        }
        else if (blockState.getBlock() == Blocks.WATER)
        {
            return Collections.emptyList();
        }

        if (!blockState.getFluidState().isSource())
        {
            return Collections.emptyList();
        }

        itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
        return itemList;
    }

    @Override
    public IPlacementHandler.ActionProcessingResult handle(
      @NotNull Level world,
      @NotNull BlockPos pos,
      @NotNull BlockState blockState,
      @Nullable CompoundTag tileEntityData,
      boolean complete,
      BlockPos centerPos)
    {
        if (!blockState.getFluidState().isSource() && !complete)
        {
            return ActionProcessingResult.PASS;
        }
        world.setBlock(pos, blockState, UPDATE_FLAG);
        world.scheduleTick(pos, blockState.getFluidState().getType(), blockState.getFluidState().getType().getTickDelay(world));
        return ActionProcessingResult.SUCCESS;
    }
}
