package com.minecolonies.core.placementhandlers;

import com.ldtteam.structurize.api.ItemStackUtils;
import com.ldtteam.structurize.api.RotationMirror;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.Utils;
import com.minecolonies.api.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ldtteam.structurize.placement.handlers.placement.PlacementHandlers.handleTileEntityPlacement;

public class HutPlacementHandler implements IPlacementHandler
{
    @Override
    public boolean canHandle(@NotNull final Level world, @NotNull final BlockPos pos, @NotNull final BlockState blockState)
    {
        return blockState.getBlock() instanceof AbstractBlockHut<?>;
    }

    @Override
    public ActionProcessingResult handle(
      @NotNull final Blueprint blueprint,
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
                final BlockEntity be = world.getBlockEntity(pos);
                if (be != null)
                {
                    if (pos.equals(centerPos))
                    {
                        final String location = StructurePacks.getStructurePack(blueprint.getPackName()).getSubPath(blueprint.getFilePath().resolve(blueprint.getFileName()));
                        ((IBlueprintDataProviderBE) be).setBlueprintPath(location);
                    }
                    else if(!((IBlueprintDataProviderBE) be).getPositionedTags().getOrDefault(BlockPos.ZERO, Collections.emptyList()).contains("invisible"))
                    {
                        final String partialPath;
                        if (((IBlueprintDataProviderBE) be).getSchematicName().isEmpty())
                        {
                            final String[] elements = Utils.splitPath(((IBlueprintDataProviderBE) be).getBlueprintPath());
                            partialPath = StructurePacks.getStructurePack(blueprint.getPackName()).getSubPath(blueprint.getFilePath().resolve(elements[elements.length - 1].replace(".blueprint", "")));
                        }
                        else
                        {
                            partialPath = StructurePacks.getStructurePack(blueprint.getPackName()).getSubPath(Utils.resolvePath(blueprint.getFilePath(), ((IBlueprintDataProviderBE) be).getSchematicName()));
                        }

                        if (!(world.getBlockEntity(centerPos) instanceof TileEntityColonyBuilding) && be instanceof TileEntityColonyBuilding)
                        {
                            ((IBlueprintDataProviderBE) be).setBlueprintPath(partialPath.substring(0, partialPath.length() - 1) + "1.blueprint");
                            ((TileEntityColonyBuilding) be).setSchematicName("");
                        }
                        else
                        {
                            ((IBlueprintDataProviderBE) be).setBlueprintPath(partialPath + ".blueprint");
                        }
                    }
                    ((IBlueprintDataProviderBE) be).setPackName(blueprint.getPackName());

                    if (!complete)
                    {
                        blockState.getBlock().setPlacedBy(world, pos, blockState, null, BlockUtils.getItemStackFromBlockState(blockState));
                    }
                }
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
        if (blockState.getBlock() != ModBlocks.blockHutBarracksTower)
        {
            itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
        }

        if (tileEntityData != null)
        {
            itemList.addAll(ItemStackUtils.getItemStacksOfTileEntity(tileEntityData, blockState));
        }
        itemList.removeIf(ItemStackUtils::isEmpty);
        return itemList;
    }
}
