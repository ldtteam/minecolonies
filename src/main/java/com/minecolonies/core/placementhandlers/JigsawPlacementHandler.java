package com.minecolonies.core.placementhandlers;

import com.google.common.collect.ImmutableList;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler;
import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.WorldUtil;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static com.ldtteam.structurize.placement.handlers.placement.PlacementHandlers.handleTileEntityPlacement;

/**
 * Handler for some specific blocks that we want only to paste/cost in the complete mode.
 */
public class JigsawPlacementHandler implements IPlacementHandler
{
    @Override
    public boolean canHandle(@NotNull final Level world, @NotNull final BlockPos pos, @NotNull final BlockState blockState)
    {
        return blockState.getBlock() instanceof JigsawBlock;
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
        if (complete)
        {
            WorldUtil.setBlockState(world, pos, blockState, Constants.UPDATE_FLAG);
            if (tileEntityData != null)
            {
                try
                {
                    handleTileEntityPlacement(tileEntityData, world, pos, settings);
                    blockState.getBlock().setPlacedBy(world, pos, blockState, null, BlockUtils.getItemStackFromBlockState(blockState));
                }
                catch (final Exception ex)
                {
                    Log.getLogger().warn("Unable to place TileEntity");
                }
            }
            return ActionProcessingResult.SUCCESS;
        }

        if (tileEntityData != null && tileEntityData.contains("final_state"))
        {
            final String stateString = tileEntityData.getString("final_state");

            BlockState finalState = Blocks.AIR.defaultBlockState();

            try
            {
                BlockStateParser.BlockResult stateParser = BlockStateParser.parseForBlock(world.holderLookup(Registries.BLOCK), stateString, false);
                BlockState resultState = stateParser.blockState();
                if (resultState != null)
                {
                    finalState = resultState;
                }
            }
            catch (CommandSyntaxException commandsyntaxexception)
            {
                Log.getLogger().warn("Unable to place Jigsaw");
            }

            if (finalState.getBlock() == Blocks.STRUCTURE_VOID)
            {
                return ActionProcessingResult.SUCCESS;
            }

            WorldUtil.setBlockState(world, pos, finalState, Constants.UPDATE_FLAG);
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
        if (complete)
        {
            return ImmutableList.of(new ItemStack(Blocks.JIGSAW));
        }

        final String stateString = tileEntityData.getString("final_state");

        BlockState finalState = Blocks.AIR.defaultBlockState();

        try
        {
            BlockStateParser.BlockResult stateParser = BlockStateParser.parseForBlock(world.holderLookup(Registries.BLOCK), stateString, true);
            BlockState resultState = stateParser.blockState();
            if (resultState != null)
            {
                finalState = resultState;
            }
        }
        catch (CommandSyntaxException commandsyntaxexception)
        {
            Log.getLogger().warn("Unable to place Jigsaw");
        }

        if (finalState.getBlock() == Blocks.AIR || finalState.getBlock() == Blocks.STRUCTURE_VOID)
        {
            return Collections.emptyList();
        }

        return ImmutableList.of(BlockUtils.getItemStackFromBlockState(finalState));
    }
}
