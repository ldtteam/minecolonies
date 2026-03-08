package com.minecolonies.core.placementhandlers;

import com.ldtteam.structurize.placement.IPlacementContext;
import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NyliumBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class NetherrackPlacementHandler implements IPlacementHandler
{
    @Override
    public boolean canHandle(@NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState blockState)
    {
        return blockState.getBlock() instanceof NyliumBlock;
    }

    @Override
    public ActionProcessingResult handle(
      @NotNull Level world,
      @NotNull BlockPos pos,
      @NotNull BlockState blockState,
      @Nullable CompoundTag tileEntityData,
      @NotNull final IPlacementContext placementContext)
    {
        return !world.setBlock(pos, blockState, 3) ? ActionProcessingResult.DENY : ActionProcessingResult.SUCCESS;
    }

    @Override
    public List<ItemStack> getRequiredItems(@NotNull Level world,
        @NotNull BlockPos pos,
        @NotNull BlockState blockState,
        @Nullable CompoundTag tileEntityData,
        @NotNull final IPlacementContext placementContext)
    {
        List<ItemStack> itemList = new ArrayList<>();
        if (placementContext.fancyPlacement())
        {
            itemList.add(new ItemStack(Blocks.NETHERRACK));
        }
        else
        {
            itemList.add(new ItemStack(blockState.getBlock()));
        }

        return itemList;
    }

    @Override
    public boolean doesWorldStateMatchBlueprintState(
        final BlockState worldState,
        final BlockState blueprintState,
        final Tuple<BlockEntity, CompoundTag> blockEntityData,
        @NotNull final IPlacementContext placementContext)
    {
        if (placementContext.fancyPlacement())
        {
            return worldState.getBlock() instanceof NyliumBlock || worldState.getBlock() == Blocks.NETHERRACK;
        }
        return worldState.equals(blueprintState);
    }
}
