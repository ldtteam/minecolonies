package com.minecolonies.coremod.placementhandlers;

import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler;
import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.util.WorldUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BubbleColumnBlock;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
    public boolean canHandle(@NotNull World world, @NotNull BlockPos pos, @NotNull BlockState blockState)
    {
         return blockState.getBlock() instanceof FlowingFluidBlock || blockState.getBlock() instanceof BubbleColumnBlock;
    }

    @Override
    public List<ItemStack> getRequiredItems(
      @NotNull World world,
      @NotNull BlockPos pos,
      @NotNull BlockState blockState,
      @Nullable CompoundNBT tileEntityData,
      boolean complete)
    {
        if (WorldUtil.isNetherType(world) && blockState.getBlock() == Blocks.LAVA)
        {
            return Collections.emptyList();
        }
        else if (blockState.getBlock() == Blocks.WATER)
        {
            return Collections.emptyList();
        }

        //Todo 1.17 will need adjustments for "spreading fluids".
        if (!blockState.getFluidState().isSource())
        {
            return Collections.emptyList();
        }
        final List<ItemStack> itemList = new ArrayList<>();
        itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
        return itemList;
    }

    @Override
    public IPlacementHandler.ActionProcessingResult handle(
      @NotNull World world,
      @NotNull BlockPos pos,
      @NotNull BlockState blockState,
      @Nullable CompoundNBT tileEntityData,
      boolean complete,
      BlockPos centerPos)
    {
        if (!blockState.getFluidState().isSource() && !complete)
        {
            return ActionProcessingResult.PASS;
        }
        world.setBlockState(pos, blockState, UPDATE_FLAG);
        world.getPendingFluidTicks().scheduleTick(pos, blockState.getFluidState().getFluid(), blockState.getFluidState().getFluid().getTickRate(world));
        return ActionProcessingResult.SUCCESS;
    }
}
