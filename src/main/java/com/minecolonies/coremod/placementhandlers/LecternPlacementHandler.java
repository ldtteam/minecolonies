package com.minecolonies.coremod.placementhandlers;

import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler;
import com.ldtteam.structurize.placement.handlers.placement.PlacementHandlers;
import com.ldtteam.structurize.util.BlockUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.LecternBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.LecternTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LecternPlacementHandler implements IPlacementHandler
{
    @Override
    public boolean canHandle(@NotNull final World world,
                             @NotNull final BlockPos pos,
                             @NotNull final BlockState blockState)
    {
        return blockState.getBlock() instanceof LecternBlock;
    }

    @Override
    public List<ItemStack> getRequiredItems(@NotNull final World world,
                                            @NotNull final BlockPos pos,
                                            @NotNull final BlockState blockState,
                                            @Nullable final CompoundNBT tileEntityData,
                                            final boolean complete)
    {
        final List<ItemStack> itemList = new ArrayList<>();
        itemList.add(BlockUtils.getItemStackFromBlockState(blockState));

        final LecternTileEntity lectern = getLectern(blockState, tileEntityData);
        if (lectern != null && lectern.hasBook())
        {
            itemList.add(new ItemStack(Items.BOOK));
        }

        return itemList;
    }

    @Override
    public ActionProcessingResult handle(@NotNull final World world,
                                         @NotNull final BlockPos pos,
                                         @NotNull final BlockState blockState,
                                         @Nullable CompoundNBT tileEntityData,
                                         final boolean complete,
                                         final BlockPos centerPos)
    {
        if (!world.setBlock(pos, blockState, Constants.BlockFlags.DEFAULT))
        {
            return ActionProcessingResult.DENY;
        }

        if (tileEntityData != null)
        {
            PlacementHandlers.handleTileEntityPlacement(tileEntityData, world, pos);
        }

        return ActionProcessingResult.SUCCESS;
    }

    @Nullable
    private static LecternTileEntity getLectern(@NotNull final BlockState blockState,
                                                @Nullable final CompoundNBT tileEntityData)
    {
        if (tileEntityData != null)
        {
            final TileEntity tileEntity = TileEntity.loadStatic(blockState, tileEntityData);
            if (tileEntity instanceof LecternTileEntity)
            {
                return (LecternTileEntity) tileEntity;
            }
        }
        return null;
    }
}
