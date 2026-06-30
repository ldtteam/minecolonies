package com.minecolonies.core.placementhandlers;

import com.ldtteam.domumornamentum.block.decorative.PillarBlock;
import com.ldtteam.structurize.api.ItemStackUtils;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.placement.IPlacementContext;
import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler;
import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.compatibility.candb.ChiselAndBitsCheck;
import com.minecolonies.api.util.BlockStateUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.ldtteam.structurize.placement.handlers.placement.PlacementHandlers.handleTileEntityPlacement;

public class GeneralBlockPlacementHandler implements IPlacementHandler
{
    @Override
    public boolean canHandle(@NotNull final Level world, @NotNull final BlockPos pos, @NotNull final BlockState blockState)
    {
        return true;
    }

    @Override
    public ActionProcessingResult handle(
      @NotNull final Level world,
      @NotNull final BlockPos pos,
      @NotNull final BlockState blockState,
      @Nullable final CompoundTag tileEntityData,
      @NotNull final IPlacementContext context)
    {
        BlockState placementState = blockState;
        if (blockState.getBlock() instanceof WallBlock || blockState.getBlock() instanceof FenceBlock || blockState.getBlock() instanceof PillarBlock || blockState.getBlock() instanceof IronBarsBlock)
        {
            try
            {
                final BlockState tempState = blockState.getBlock().getStateForPlacement(
                  new BlockPlaceContext(world, null, InteractionHand.MAIN_HAND, ItemStack.EMPTY,
                    new BlockHitResult(new Vec3(0, 0, 0), Direction.DOWN, pos, true)));
                if (tempState != null)
                {
                    placementState = BlockStateUtils.copyProperties(placementState, tempState,
                        BlockStateProperties.UP,
                        BlockStateProperties.NORTH,
                        BlockStateProperties.NORTH_WALL,
                        BlockStateProperties.EAST,
                        BlockStateProperties.EAST_WALL,
                        BlockStateProperties.SOUTH,
                        BlockStateProperties.SOUTH_WALL,
                        BlockStateProperties.WEST,
                        BlockStateProperties.WEST_WALL,
                        PillarBlock.COLUMN);
                }
            }
            catch (final Exception ex)
            {
                // Noop
            }
        }

        if (world.getBlockState(pos).equals(placementState))
        {
            return ActionProcessingResult.PASS;
        }

        if (!WorldUtil.setBlockState(world, pos, placementState, Constants.UPDATE_FLAG))
        {
            return ActionProcessingResult.PASS;
        }

        if (tileEntityData != null)
        {
            if (!blockState.hasBlockEntity())
            {
                Log.getLogger()
                    .warn("Schematic with invalid blockentity data for block:" + blockState + " in pack:" + context.getBluePrint().getPackName() + " schematic:"
                        + context.getBluePrint().getName() + " blockentitydata:" + tileEntityData);
                return ActionProcessingResult.PASS;
            }

            try
            {
                handleTileEntityPlacement(tileEntityData, world, pos, context.getRotationMirror());
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
      @NotNull final Level world,
      @NotNull final BlockPos pos,
      @NotNull final BlockState blockState,
      @Nullable final CompoundTag tileEntityData,
      @NotNull final IPlacementContext placementContext)
    {
        final List<ItemStack> itemList = new ArrayList<>();
        if (!ChiselAndBitsCheck.isChiselAndBitsBlock(blockState))
        {
            itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
        }
        if (tileEntityData != null)
        {
            itemList.addAll(ItemStackUtils.getItemStacksOfTileEntity(tileEntityData, blockState, world));
        }
        itemList.removeIf(ItemStackUtils::isEmpty);

        return itemList;
    }

    @Override
    public boolean doesWorldStateMatchBlueprintState(
        final BlockState blueprintState,
        final BlockState worldState,
        final Tuple<BlockEntity, CompoundTag> tuple,
        @NotNull final IPlacementContext iPlacementContext)
    {
        if (worldState.equals(blueprintState))
        {
            return true;
        }
        return blueprintState.getBlock() == worldState.getBlock()
            && (blueprintState.getBlock() instanceof WallBlock
            || blueprintState.getBlock() instanceof FenceBlock
            || blueprintState.getBlock() instanceof IronBarsBlock
            || blueprintState.getBlock() instanceof FenceGateBlock);
    }
}
