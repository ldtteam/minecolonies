package com.minecolonies.coremod.placementhandlers;

import com.ldtteam.domumornamentum.block.AbstractBlockDoor;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.block.decorative.FancyDoorBlock;
import com.ldtteam.domumornamentum.block.decorative.FancyTrapdoorBlock;
import com.ldtteam.domumornamentum.block.vanilla.DoorBlock;
import com.ldtteam.domumornamentum.block.vanilla.TrapdoorBlock;
import com.ldtteam.domumornamentum.util.BlockUtils;
import com.ldtteam.structurize.api.util.ItemStackUtils;
import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.util.Log;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ldtteam.structurize.api.util.constant.Constants.UPDATE_FLAG;
import static com.ldtteam.structurize.placement.handlers.placement.PlacementHandlers.handleTileEntityPlacement;

public class DoDoorBlockPlacementHandler implements IPlacementHandler
{
    @Override
    public boolean canHandle(@NotNull final Level world, @NotNull final BlockPos pos, @NotNull final BlockState blockState)
    {
        return blockState.getBlock() instanceof IMateriallyTexturedBlock && blockState.getBlock() instanceof AbstractBlockDoor<?>;
    }

    @Override
    public ActionProcessingResult handle(
      @NotNull final Level world,
      @NotNull final BlockPos pos,
      @NotNull final BlockState blockState,
      @Nullable final CompoundTag tileEntityData,
      final boolean complete,
      final BlockPos centerPos,
      final PlacementSettings settings)
    {
        if (blockState.getValue(net.minecraft.world.level.block.DoorBlock.HALF).equals(DoubleBlockHalf.LOWER))
        {
            if (world.getBlockState(pos).equals(blockState))
            {
                world.removeBlock(pos, false);
                world.removeBlock(pos.above(), false);
                world.setBlock(pos, blockState, com.ldtteam.structurize.api.util.constant.Constants.UPDATE_FLAG);
                world.setBlock(pos.above(), blockState.setValue(net.minecraft.world.level.block.DoorBlock.HALF, DoubleBlockHalf.UPPER), UPDATE_FLAG);
                if (tileEntityData != null)
                {
                    try
                    {
                        handleTileEntityPlacement(tileEntityData, world, pos, settings);
                        handleTileEntityPlacement(tileEntityData, world, pos.above(), settings);
                    }
                    catch (final Exception ex)
                    {
                        Log.getLogger().warn("Unable to place TileEntity");
                    }
                }
                return ActionProcessingResult.PASS;
            }
            else
            {
                world.setBlock(pos, blockState.setValue(net.minecraft.world.level.block.DoorBlock.HALF, DoubleBlockHalf.LOWER), UPDATE_FLAG);
                world.setBlock(pos.above(), blockState.setValue(net.minecraft.world.level.block.DoorBlock.HALF, DoubleBlockHalf.UPPER), UPDATE_FLAG);
            }

            if (tileEntityData != null)
            {
                try
                {
                    handleTileEntityPlacement(tileEntityData, world, pos, settings);
                    handleTileEntityPlacement(tileEntityData, world, pos.above(), settings);
                }
                catch (final Exception ex)
                {
                    Log.getLogger().warn("Unable to place TileEntity");
                }
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
      final boolean complete)
    {
        final List<ItemStack> itemList = new ArrayList<>();
        if (tileEntityData != null && blockState.getValue(net.minecraft.world.level.block.DoorBlock.HALF).equals(DoubleBlockHalf.LOWER))
        {
            BlockPos blockpos = new BlockPos(tileEntityData.getInt("x"), tileEntityData.getInt("y"), tileEntityData.getInt("z"));
            final BlockEntity tileEntity = BlockEntity.loadStatic(blockpos, blockState, tileEntityData);
            if (tileEntity == null)
            {
                return Collections.emptyList();
            }

            final ItemStack item = BlockUtils.getMaterializedItemStack(null, tileEntity);
            if (blockState.getBlock() instanceof DoorBlock)
            {
                item.getOrCreateTag().putString("type", blockState.getValue(DoorBlock.TYPE).toString().toUpperCase());
            }
            else if (blockState.getBlock() instanceof FancyDoorBlock)
            {
                item.getOrCreateTag().putString("type", blockState.getValue(FancyDoorBlock.TYPE).toString().toUpperCase());
            }
            else if (blockState.getBlock() instanceof TrapdoorBlock)
            {
                item.getOrCreateTag().putString("type", blockState.getValue(TrapdoorBlock.TYPE).toString().toUpperCase());
            }
            else if (blockState.getBlock() instanceof FancyTrapdoorBlock)
            {
                item.getOrCreateTag().putString("type", blockState.getValue(FancyTrapdoorBlock.TYPE).toString().toUpperCase());
            }
            itemList.add(item);
            itemList.removeIf(ItemStackUtils::isEmpty);
        }
        return itemList;
    }
}
