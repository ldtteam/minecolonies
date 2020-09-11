package com.minecolonies.coremod.placementhandlers;

import com.ldtteam.structurize.api.util.ItemStackUtils;
import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingWareHouse;
import net.minecraft.block.AbstractChestBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.ldtteam.structurize.placement.handlers.placement.PlacementHandlers.handleTileEntityPlacement;
import static com.minecolonies.api.util.constant.Constants.UPDATE_FLAG;

public class ChestPlacementHandler implements IPlacementHandler
{
    @Override
    public boolean canHandle(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final BlockState blockState)
    {
        return blockState.getBlock() instanceof AbstractChestBlock;
    }

    @Override
    public ActionProcessingResult handle(
      @NotNull final World world,
      @NotNull final BlockPos pos,
      @NotNull final BlockState blockState,
      @Nullable final CompoundNBT tileEntityData,
      final boolean complete,
      final BlockPos centerPos,
      final PlacementSettings settings)
    {
        final TileEntity entity = world.getTileEntity(pos);
        final IColony colony = IColonyManager.getInstance().getClosestColony(world, pos);
        if (colony != null && entity instanceof ChestTileEntity)
        {
            BuildingWareHouse.handleBuildingOverChest(pos, (ChestTileEntity) entity, world, tileEntityData);
        }
        else
        {
            if (!world.setBlockState(pos, blockState, UPDATE_FLAG))
            {
                return ActionProcessingResult.DENY;
            }

            if (tileEntityData != null)
            {
                handleTileEntityPlacement(tileEntityData, world, pos, settings);
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
        final List<ItemStack> itemList = new ArrayList<>();
        itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
        itemList.addAll(ItemStackUtils.getItemStacksOfTileEntity(tileEntityData, world, pos));

        itemList.removeIf(ItemStackUtils::isEmpty);

        return itemList;
    }
}
