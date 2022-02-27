package com.minecolonies.coremod.placementhandlers;

import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler;
import com.ldtteam.structurize.placement.handlers.placement.PlacementHandlers;
import com.ldtteam.structurize.util.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LecternPlacementHandler implements IPlacementHandler
{
    @Override
    public boolean canHandle(@NotNull final Level world,
                             @NotNull final BlockPos pos,
                             @NotNull final BlockState blockState)
    {
        return blockState.getBlock() instanceof LecternBlock;
    }

    @Override
    public List<ItemStack> getRequiredItems(@NotNull final Level world,
                                            @NotNull final BlockPos pos,
                                            @NotNull final BlockState blockState,
                                            @Nullable final CompoundTag tileEntityData,
                                            final boolean complete)
    {
        final List<ItemStack> itemList = new ArrayList<>();
        itemList.add(BlockUtils.getItemStackFromBlockState(blockState));

        final LecternBlockEntity lectern = getLectern(pos, blockState, tileEntityData);
        if (lectern != null && lectern.hasBook())
        {
            itemList.add(new ItemStack(Items.BOOK));
        }

        return itemList;
    }

    @Override
    public ActionProcessingResult handle(@NotNull final Level world,
                                         @NotNull final BlockPos pos,
                                         @NotNull final BlockState blockState,
                                         @Nullable CompoundTag tileEntityData,
                                         final boolean complete,
                                         final BlockPos centerPos)
    {
        if (!world.setBlock(pos, blockState, Block.UPDATE_ALL))
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
    private static LecternBlockEntity getLectern(@NotNull final BlockPos pos,
                                                 @NotNull final BlockState blockState,
                                                 @Nullable final CompoundTag tileEntityData)
    {
        if (tileEntityData != null)
        {
            final BlockEntity tileEntity = BlockEntity.loadStatic(pos, blockState, tileEntityData);
            if (tileEntity instanceof LecternBlockEntity)
            {
                return (LecternBlockEntity) tileEntity;
            }
        }
        return null;
    }
}
