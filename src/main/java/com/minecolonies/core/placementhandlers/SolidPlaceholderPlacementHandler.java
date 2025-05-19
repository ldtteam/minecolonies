package com.minecolonies.core.placementhandlers;

import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.schematic.BlockSolidSubstitution;
import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler;
import com.ldtteam.structurize.placement.handlers.placement.PlacementHandlers;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.PlacementSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.ldtteam.structurize.api.util.constant.Constants.UPDATE_FLAG;

public class SolidPlaceholderPlacementHandler implements IPlacementHandler
{
    /**
     * Replacement block for solid placeholders
     */
    private BlockState replacement = Blocks.DIRT.defaultBlockState();

    private IPlacementHandler replacementHandler = null;

    /**
     * Sets a different replacement block
     *
     * @param state
     */
    public void setReplacement(final BlockState state)
    {
        replacement = state;
    }

    /**
     * Get the replacement block
     *
     * @return
     */
    public BlockState getReplacement()
    {
        return replacement;
    }

    @Override
    public boolean canHandle(Level world, BlockPos pos, BlockState blockState)
    {
        return blockState.getBlock() instanceof BlockSolidSubstitution;
    }

    private void searchHandler(final Level world, final BlockPos pos)
    {
        if (replacementHandler == null)
        {
            for (final IPlacementHandler handler : PlacementHandlers.handlers)
            {
                if (handler != this && handler.canHandle(world, pos, replacement))
                {
                    replacementHandler = handler;
                    break;
                }
            }
        }
    }

    @Override
    public List<ItemStack> getRequiredItems(
        Level world,
        BlockPos pos,
        BlockState blockState,
        @Nullable CompoundTag tileEntityData,
        boolean complete)
    {
        searchHandler(world, pos);
        List<ItemStack> items = new ArrayList<>();

        if (complete)
        {
            // for scan tool, show the actual placeholder block
            items.add(new ItemStack(blockState.getBlock()));
        }
        else
        {
            return replacementHandler.getRequiredItems(world, pos, replacement, tileEntityData, complete);
        }

        return items;
    }

    @Override
    public ActionProcessingResult handle(
        Level world,
        BlockPos pos,
        BlockState blockState,
        @Nullable CompoundTag tileEntityData,
        boolean complete,
        BlockPos centerPos, final PlacementSettings settings)
    {
        if (complete)
        {
            world.setBlock(pos, ModBlocks.blockSubstitution.get().defaultBlockState(), UPDATE_FLAG);
            return ActionProcessingResult.PASS;
        }

        if (BlockUtils.isAnySolid(world.getBlockState(pos)))
        {
            return ActionProcessingResult.DENY;
        }

        searchHandler(world, pos);
        return replacementHandler.handle(world, pos, replacement, tileEntityData, complete, centerPos, settings);
    }
}