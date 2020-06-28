package com.minecolonies.coremod.placementhandlers;

import com.ldtteam.structurize.api.util.ItemStackUtils;
import com.ldtteam.structurize.blocks.PlaceholderBlock;
import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler;
import com.ldtteam.structurize.placement.handlers.placement.PlacementHandlers;
import com.ldtteam.structurize.tileentities.TileEntityPlaceholder;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.compatibility.candb.ChiselAndBitsCheck;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.tileentities.TileEntityRack;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.blocks.BlockMinecoloniesRack;
import com.minecolonies.coremod.blocks.schematic.BlockWaypoint;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingWareHouse;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ldtteam.structurize.placement.handlers.placement.PlacementHandlers.handleTileEntityPlacement;
import static com.minecolonies.api.util.constant.Constants.UPDATE_FLAG;

/**
 * Contains all Minecolonies specific placement handlers.
 */
public final class MinecoloniesPlacementHandlers
{
    /**
     * Private constructor to hide implicit one.
     */
    private MinecoloniesPlacementHandlers()
    {
        /*
         * Intentionally left empty.
         */
    }

    public static void initHandlers()
    {
        PlacementHandlers.handlers.clear();
        PlacementHandlers.handlers.add(new PlacementHandlers.AirPlacementHandler());
        PlacementHandlers.handlers.add(new PlacementHandlers.FirePlacementHandler());
        PlacementHandlers.handlers.add(new PlacementHandlers.GrassPlacementHandler());
        PlacementHandlers.handlers.add(new PlacementHandlers.DoorPlacementHandler());
        PlacementHandlers.handlers.add(new PlacementHandlers.BedPlacementHandler());
        PlacementHandlers.handlers.add(new PlacementHandlers.DoublePlantPlacementHandler());
        PlacementHandlers.handlers.add(new PlacementHandlers.SpecialBlockPlacementAttemptHandler());
        PlacementHandlers.handlers.add(new PlacementHandlers.FlowerPotPlacementHandler());
        PlacementHandlers.handlers.add(new PlacementHandlers.BlockGrassPathPlacementHandler());
        PlacementHandlers.handlers.add(new PlacementHandlers.StairBlockPlacementHandler());
        PlacementHandlers.handlers.add(new FencePlacementHandler());
        PlacementHandlers.handlers.add(new ChestPlacementHandler());
        PlacementHandlers.handlers.add(new WayPointBlockPlacementHandler());
        PlacementHandlers.handlers.add(new RackPlacementHandler());
        PlacementHandlers.handlers.add(new PlacementHandlers.FallingBlockPlacementHandler());
        PlacementHandlers.handlers.add(new PlacementHandlers.BannerPlacementHandler());
        PlacementHandlers.handlers.add(new BuildingSubstitutionBlock());
        PlacementHandlers.handlers.add(new BuildingBarracksTowerSub());
        PlacementHandlers.handlers.add(new GeneralBlockPlacementHandler());
    }

    public static class WayPointBlockPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final BlockState blockState)
        {
            return blockState.getBlock() instanceof BlockWaypoint;
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
            world.removeBlock(pos, false);
            final IColony colony = IColonyManager.getInstance().getClosestColony(world, pos);
            if (colony != null)
            {
                if (!complete)
                {
                    colony.addWayPoint(pos, Blocks.AIR.getDefaultState());
                }
                else
                {
                    world.setBlockState(pos, blockState);
                }
            }
            return ActionProcessingResult.SUCCESS;
        }

        @Override
        public List<ItemStack> getRequiredItems(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final BlockState blockState, @Nullable final CompoundNBT tileEntityData, final boolean complete)
        {
            return new ArrayList<>();
        }
    }

    public static class RackPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final BlockState blockState)
        {
            return blockState.getBlock() instanceof BlockMinecoloniesRack;
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
            if (world.getBlockState(pos).getBlock() == ModBlocks.blockRack)
            {
                return ActionProcessingResult.SUCCESS;
            }

            TileEntity entity = world.getTileEntity(pos);
            if (entity instanceof ChestTileEntity)
            {
                BuildingWareHouse.handleBuildingOverChest(pos, (ChestTileEntity) entity, world, tileEntityData);
            }
            else
            {
                world.setBlockState(pos, blockState, UPDATE_FLAG);
                if (tileEntityData != null)
                {
                    handleTileEntityPlacement(tileEntityData, world, pos);
                }

                entity = world.getTileEntity(pos);
                if (entity instanceof TileEntityRack)
                {
                    final BlockState newState = BlockMinecoloniesRack.getPlacementState(blockState, entity, pos);
                    world.setBlockState(pos, newState);
                }
            }
            return ActionProcessingResult.SUCCESS;
        }

        @Override
        public List<ItemStack> getRequiredItems(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final BlockState blockState, @Nullable final CompoundNBT tileEntityData, final boolean complete)
        {
            final List<ItemStack> itemList = new ArrayList<>();
            itemList.add(BlockUtils.getItemStackFromBlockState(blockState));

            for (final ItemStack stack : PlacementHandlers.getItemsFromTileEntity(tileEntityData, world))
            {
                if (!ItemStackUtils.isEmpty(stack))
                {
                    itemList.add(stack);
                }
            }
            return itemList;
        }
    }

    public static class ChestPlacementHandler implements IPlacementHandler
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
        public List<ItemStack> getRequiredItems(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final BlockState blockState, @Nullable final CompoundNBT tileEntityData, final boolean complete)
        {
            final List<ItemStack> itemList = new ArrayList<>();
            itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
            itemList.addAll(PlacementHandlers.getItemsFromTileEntity(tileEntityData, world));

            itemList.removeIf(ItemStackUtils::isEmpty);

            return itemList;
        }
    }

    public static class BuildingSubstitutionBlock implements IPlacementHandler
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
        public List<ItemStack> getRequiredItems(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final BlockState blockState, @Nullable final CompoundNBT tileEntityData, final boolean complete)
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

    public static class BuildingBarracksTowerSub implements IPlacementHandler
    {
        @Override
        public boolean canHandle(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final BlockState blockState)
        {
            return blockState.getBlock() == ModBlocks.blockBarracksTowerSubstitution;
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
            if (world.getBlockState(pos).equals(blockState))
            {
                return ActionProcessingResult.PASS;
            }

            if (!world.setBlockState(pos, blockState, com.ldtteam.structurize.api.util.constant.Constants.UPDATE_FLAG))
            {
                return ActionProcessingResult.PASS;
            }

            if (tileEntityData != null)
            {
                try
                {
                    handleTileEntityPlacement(tileEntityData, world, pos, settings);
                }
                catch (final Exception ex)
                {
                    Log.getLogger().warn("Unable to place TileEntity");
                }
            }

            return ActionProcessingResult.SUCCESS;
        }

        @Override
        public List<ItemStack> getRequiredItems(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final BlockState blockState, @Nullable final CompoundNBT tileEntityData, final boolean complete)
        {
            return Collections.emptyList();
        }
    }


    public static class GeneralBlockPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final BlockState blockState)
        {
            return true;
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
            if (world.getBlockState(pos).equals(blockState))
            {
                return ActionProcessingResult.PASS;
            }

            if (!world.setBlockState(pos, blockState, com.ldtteam.structurize.api.util.constant.Constants.UPDATE_FLAG))
            {
                return ActionProcessingResult.PASS;
            }

            if (tileEntityData != null)
            {
                try
                {
                    handleTileEntityPlacement(tileEntityData, world, pos, settings);
                    blockState.getBlock().onBlockPlacedBy(world, pos, blockState, null, BlockUtils.getItemStackFromBlockState(blockState));
                }
                catch (final Exception ex)
                {
                    Log.getLogger().warn("Unable to place TileEntity");
                }
            }

            return ActionProcessingResult.SUCCESS;
        }

        @Override
        public List<ItemStack> getRequiredItems(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final BlockState blockState, @Nullable final CompoundNBT tileEntityData, final boolean complete)
        {
            final List<ItemStack> itemList = new ArrayList<>();
            if (!ChiselAndBitsCheck.isChiselAndBitsBlock(blockState))
            {
                itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
            }
            if (tileEntityData != null)
            {
                itemList.addAll(ItemStackUtils.getItemStacksOfTileEntity(tileEntityData, world));
            }
            itemList.removeIf(ItemStackUtils::isEmpty);

            return itemList;
        }
    }

    public static class FencePlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final BlockState blockState)
        {
            return BlockTags.FENCES.contains(blockState.getBlock());
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
            if (world.getBlockState(pos).equals(blockState))
            {
                return ActionProcessingResult.PASS;
            }

            BlockState northState = world.getBlockState(pos.north());
            BlockState eastState = world.getBlockState(pos.east());
            BlockState southState = world.getBlockState(pos.south());
            BlockState westState = world.getBlockState(pos.west());
            final BlockState fence = blockState
              .with(FenceBlock.NORTH, ((FenceBlock) blockState.getBlock()).canConnect(northState, northState.isSolidSide(world, pos.north(), Direction.SOUTH), Direction.SOUTH))
              .with(FenceBlock.EAST, ((FenceBlock) blockState.getBlock()).canConnect(eastState, eastState.isSolidSide(world, pos.east(), Direction.WEST), Direction.WEST))
              .with(FenceBlock.SOUTH, ((FenceBlock) blockState.getBlock()).canConnect(southState, southState.isSolidSide(world, pos.south(), Direction.NORTH), Direction.NORTH))
              .with(FenceBlock.WEST, ((FenceBlock) blockState.getBlock()).canConnect(westState, westState.isSolidSide(world, pos.west(), Direction.EAST), Direction.EAST));

            if (!world.setBlockState(pos, fence, UPDATE_FLAG))
            {
                return ActionProcessingResult.PASS;
            }

            return ActionProcessingResult.SUCCESS;
        }

        @Override
        public List<ItemStack> getRequiredItems(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final BlockState blockState, @Nullable final CompoundNBT tileEntityData, final boolean complete)
        {
            final List<ItemStack> itemList = new ArrayList<>();
            itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
            itemList.removeIf(ItemStackUtils::isEmpty);
            return itemList;
        }
    }
}
