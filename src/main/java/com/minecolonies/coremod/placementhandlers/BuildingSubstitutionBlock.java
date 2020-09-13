package com.minecolonies.coremod.placementhandlers;

import com.ldtteam.structurize.blocks.PlaceholderBlock;
import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler;
import com.ldtteam.structurize.tileentities.TileEntityPlaceholder;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BuildingSubstitutionBlock implements IPlacementHandler
{
    @Override
    public boolean canHandle(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final BlockState blockState)
    {
        return blockState.getBlock() == com.ldtteam.structurize.blocks.ModBlocks.placeholderBlock;
    }

    @Override
    public ActionProcessingResult handle(
      @NotNull final World world,
      @NotNull final BlockPos pos,
      @NotNull final BlockState blockState,
      @Nullable final CompoundNBT tileEntityData,
      final boolean complete,
      final BlockPos centerPos)
    {
        if (tileEntityData != null)
        {
            TileEntity tileEntity = TileEntity.create(tileEntityData);
            if (tileEntity instanceof TileEntityPlaceholder)
            {
                final ItemStack stack = ((TileEntityPlaceholder) tileEntity).getStack();
                if (stack.getItem() instanceof BlockItem)
                {
                    final Block block = ((BlockItem) stack.getItem()).getBlock();
                    if (block instanceof AbstractBlockHut)
                    {
                        if (world.getBlockState(pos).getBlock() == block)
                        {
                            return ActionProcessingResult.SUCCESS;
                        }

                        final TileEntity building = world.getTileEntity(centerPos);
                        if (building instanceof TileEntityColonyBuilding)
                        {
                            world.setBlockState(pos, block.getDefaultState().with(AbstractBlockHut.FACING, blockState.get(PlaceholderBlock.HORIZONTAL_FACING)));
                            final TileEntity tile = world.getTileEntity(pos);
                            if (tile instanceof TileEntityColonyBuilding)
                            {
                                ((TileEntityColonyBuilding) tile).setStyle(((TileEntityColonyBuilding) building).getStyle());
                            }
                            ((TileEntityColonyBuilding) building).getColony().getBuildingManager().addNewBuilding((TileEntityColonyBuilding) world.getTileEntity(pos), world);

                            final IBuilding theBuilding = ((TileEntityColonyBuilding) building).getColony().getBuildingManager().getBuilding(pos);
                            theBuilding.setStyle(((TileEntityColonyBuilding) building).getStyle());
                        }
                    }
                }
            }
        }
        return ActionProcessingResult.SUCCESS;
    }

    @Override
    public List<ItemStack> getRequiredItems(
      @NotNull final World world,
      @NotNull final BlockPos pos,
      @NotNull final BlockState blockState,
      @Nullable final CompoundNBT tileEntityData,
      final boolean complete)
    {
        if (tileEntityData != null)
        {
            TileEntity tileEntity = TileEntity.create(tileEntityData);
            if (tileEntity instanceof TileEntityPlaceholder)
            {
                final ItemStack stack = ((TileEntityPlaceholder) tileEntity).getStack();
                if (stack.getItem() instanceof BlockItem)
                {
                    final Block block = ((BlockItem) stack.getItem()).getBlock();
                    if (block instanceof AbstractBlockHut)
                    {
                        if (world.getBlockState(pos).getBlock() == block)
                        {
                            return Collections.emptyList();
                        }
                    }
                    final List<ItemStack> list = new ArrayList<>();
                    list.add(stack);
                    return list;
                }
            }
        }
        return Collections.emptyList();
    }
}
