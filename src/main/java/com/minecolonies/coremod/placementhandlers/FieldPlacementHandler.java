package com.minecolonies.coremod.placementhandlers;

import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler;
import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.coremod.blocks.BlockScarecrow;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler.ActionProcessingResult;

public class FieldPlacementHandler implements IPlacementHandler
{
    @Override
    public boolean canHandle(@NotNull World world, @NotNull BlockPos pos, @NotNull BlockState blockState)
    {
        return blockState.getBlock() instanceof BlockScarecrow;
    }

    @Override
    public ActionProcessingResult handle(
      @NotNull World world,
      @NotNull BlockPos pos,
      @NotNull BlockState blockState,
      @Nullable CompoundNBT tileEntityData,
      boolean complete,
      BlockPos centerPos)
    {
        if (world.getBlockState(pos).getBlock() == ModBlocks.blockScarecrow)
        {
            return ActionProcessingResult.SUCCESS;
        }

        if (blockState.getValue(DoorBlock.HALF).equals(DoubleBlockHalf.LOWER))
        {
            world.setBlock(pos, blockState.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), 3);
            world.setBlock(pos.above(), blockState.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), 3);
        }

        return ActionProcessingResult.SUCCESS;
    }

    @Override
    public List<ItemStack> getRequiredItems(@NotNull World world, @NotNull BlockPos pos, @NotNull BlockState blockState, @Nullable CompoundNBT tileEntityData, boolean complete)
    {
        List<ItemStack> itemList = new ArrayList<>();
        if (blockState.getValue(DoorBlock.HALF).equals(DoubleBlockHalf.LOWER))
        {
            itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
        }

        return itemList;
    }
}
