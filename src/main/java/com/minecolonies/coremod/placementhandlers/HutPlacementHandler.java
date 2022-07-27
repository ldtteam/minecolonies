package com.minecolonies.coremod.placementhandlers;

import com.ldtteam.structurize.api.util.ItemStackUtils;
import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.Log;
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
      final PlacementSettings settings)
    {
        if (world.getBlockState(pos).equals(blockState))
        {
            return ActionProcessingResult.PASS;
        }

        if (!WorldUtil.setBlockState(world, pos, blockState, com.ldtteam.structurize.api.util.constant.Constants.UPDATE_FLAG))
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
                    //todo this might cause issues then with included buildings? If they're not an "invisible" one? (what we can do, is check for the tag here, and then treat it as normal.
                    final String folder = blueprint.getFilePath().toString().replace(StructurePacks.packMetas.get(blueprint.getPackName()).getPath().toString(), "").substring(1);
                    if (pos.equals(centerPos))
                    {
                        ((IBlueprintDataProviderBE) be).setBlueprintPath(
                          blueprint.getFilePath().toString().replace(StructurePacks.selectedPack.getPath().toString() + "/", "") + folder + "/" + blueprint.getFileName() + ".blueprint");
                    }
                    else
                    {
                        final String partialPath = folder + "/" + ((IBlueprintDataProviderBE) be).getSchematicName();
                        if (!(world.getBlockEntity(centerPos) instanceof TileEntityColonyBuilding) && be instanceof TileEntityColonyBuilding)
                        {
                            ((IBlueprintDataProviderBE) be).setBlueprintPath(partialPath.substring(0, partialPath.length() - 1) + "0.blueprint");
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
        itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
        if (tileEntityData != null)
        {
            itemList.addAll(ItemStackUtils.getItemStacksOfTileEntity(tileEntityData, blockState));
        }
        itemList.removeIf(ItemStackUtils::isEmpty);
        return itemList;
    }
}
