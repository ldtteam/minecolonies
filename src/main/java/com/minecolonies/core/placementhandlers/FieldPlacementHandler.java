package com.minecolonies.core.placementhandlers;

import com.ldtteam.structurize.placement.IPlacementContext;
import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler;
import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.blocks.BlockScarecrow;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.ldtteam.structurize.placement.handlers.placement.PlacementHandlers.handleTileEntityPlacement;

public class FieldPlacementHandler implements IPlacementHandler
{
    @Override
    public boolean canHandle(@NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState blockState)
    {
        return blockState.getBlock() instanceof BlockScarecrow;
    }

    @Override
    public ActionProcessingResult handle(
      @NotNull Level world,
      @NotNull BlockPos pos,
      @NotNull BlockState blockState,
      @Nullable CompoundTag tileEntityData,
      @NotNull final IPlacementContext placementContext)
    {
        if (blockState.getValue(DoorBlock.HALF).equals(DoubleBlockHalf.LOWER))
        {
            world.setBlock(pos, blockState.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), 3);
            world.setBlock(pos.above(), blockState.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), 3);
        }

        if (tileEntityData != null)
        {
            try
            {
                handleTileEntityPlacement(tileEntityData, world, pos, placementContext.getRotationMirror());
                blockState.getBlock().setPlacedBy(world, pos, blockState, null, BlockUtils.getItemStackFromBlockState(blockState));
            }
            catch (final Exception ex)
            {
                Log.getLogger().warn("Unable to place TileEntity");
            }
        }

        return ActionProcessingResult.SUCCESS;
    }

    @Override
    public List<ItemStack> getRequiredItems(
        @NotNull Level world,
        @NotNull BlockPos pos,
        @NotNull BlockState blockState,
        @Nullable CompoundTag tileEntityData,
        @NotNull final IPlacementContext placementContext)
    {
        List<ItemStack> itemList = new ArrayList<>();
        if (blockState.getValue(DoorBlock.HALF).equals(DoubleBlockHalf.LOWER))
        {
            itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
        }

        return itemList;
    }

    @Override
    public boolean doesWorldStateMatchBlueprintState(
        final BlockState blueprintState,
        final BlockState worldState,
        final Tuple<BlockEntity, CompoundTag> tuple,
        @NotNull final IPlacementContext iPlacementContext)
    {
        return blueprintState.getBlock() == worldState.getBlock();
    }
}
