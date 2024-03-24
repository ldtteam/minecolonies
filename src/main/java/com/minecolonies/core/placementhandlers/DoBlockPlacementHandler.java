package com.minecolonies.core.placementhandlers;

import com.ldtteam.domumornamentum.block.AbstractPostBlock;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.block.decorative.FancyDoorBlock;
import com.ldtteam.domumornamentum.block.decorative.FancyTrapdoorBlock;
import com.ldtteam.domumornamentum.block.decorative.PanelBlock;
import com.ldtteam.domumornamentum.block.vanilla.DoorBlock;
import com.ldtteam.domumornamentum.block.vanilla.TrapdoorBlock;
import com.ldtteam.domumornamentum.util.BlockUtils;
import com.ldtteam.domumornamentum.util.MaterialTextureDataUtil;
import com.ldtteam.structurize.api.util.ItemStackUtils;
import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ldtteam.structurize.placement.handlers.placement.PlacementHandlers.handleTileEntityPlacement;

public class DoBlockPlacementHandler implements IPlacementHandler
{
    @Override
    public boolean canHandle(@NotNull final Level world, @NotNull final BlockPos pos, @NotNull final BlockState blockState)
    {
        return blockState.getBlock() instanceof IMateriallyTexturedBlock && blockState.getBlock() != ModBlocks.blockRack;
    }

    @Override
    public ActionProcessingResult handle(
      @NotNull final Level world,
      @NotNull final BlockPos pos,
      @NotNull final BlockState blockState,
      @Nullable final CompoundTag tileEntityData,
      final boolean complete,
      final BlockPos centerPos,
      final RotationMirror settings)
    {
        if (world.getBlockState(pos).equals(blockState))
        {
            world.removeBlock(pos, false);
            world.setBlock(pos, blockState, Constants.UPDATE_FLAG);
            if (tileEntityData != null)
            {
                try
                {
                    handleTileEntityPlacement(tileEntityData, world, pos, settings);
                    final BlockHitResult hitresult = new BlockHitResult(new Vec3(0,0,0), Direction.NORTH, pos, false);
                    blockState.getBlock().setPlacedBy(world, pos, blockState, null, blockState.getBlock().getCloneItemStack(blockState,
                      new BlockHitResult(new Vec3(0,0,0), Direction.NORTH, pos, false), world, pos, null));
                }
                catch (final Exception ex)
                {
                    Log.getLogger().warn("Unable to place TileEntity");
                }
            }
            return ActionProcessingResult.PASS;
        }

        if (!WorldUtil.setBlockState(world, pos, blockState, Constants.UPDATE_FLAG))
        {
            return ActionProcessingResult.PASS;
        }

        if (tileEntityData != null)
        {
            try
            {
                handleTileEntityPlacement(tileEntityData, world, pos, settings);
                blockState.getBlock().setPlacedBy(world, pos, blockState, null, blockState.getBlock().getCloneItemStack(blockState,
                  new BlockHitResult(new Vec3(0,0,0), Direction.NORTH, pos, false), world, pos, null));
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
      final boolean complete)
    {
        final List<ItemStack> itemList = new ArrayList<>();
        if (tileEntityData != null)
        {
            BlockPos blockpos = new BlockPos(tileEntityData.getInt("x"), tileEntityData.getInt("y"), tileEntityData.getInt("z"));
            final BlockEntity tileEntity = BlockEntity.loadStatic(blockpos, blockState, tileEntityData);
            if (tileEntity == null)
            {
                return Collections.emptyList();
            }

            final Property<?> property;
            if (blockState.getBlock() instanceof DoorBlock)
            {
                property = DoorBlock.TYPE;
            }
            else if (blockState.getBlock() instanceof FancyDoorBlock)
            {
                property = FancyDoorBlock.TYPE;
            }
            else if (blockState.getBlock() instanceof TrapdoorBlock)
            {
                property = TrapdoorBlock.TYPE;
            }
            else if (blockState.getBlock() instanceof FancyTrapdoorBlock)
            {
                property = FancyTrapdoorBlock.TYPE;
            }
            else if (blockState.getBlock() instanceof PanelBlock)
            {
                property = PanelBlock.TYPE;
            }
            else if (blockState.getBlock() instanceof AbstractPostBlock<?>)
            {
                property = AbstractPostBlock.TYPE;
            }
            else
            {
                property = null;
            }
            itemList.add(property == null ? BlockUtils.getMaterializedItemStack(tileEntity) : BlockUtils.getMaterializedItemStack(tileEntity, property));
        }
        itemList.removeIf(ItemStackUtils::isEmpty);
        return itemList;
    }
}
