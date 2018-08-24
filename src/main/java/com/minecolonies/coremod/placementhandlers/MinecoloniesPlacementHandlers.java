package com.minecolonies.coremod.placementhandlers;

import com.minecolonies.api.compatibility.candb.ChiselAndBitsCheck;
import com.minecolonies.api.util.BlockUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.blocks.BlockMinecoloniesRack;
import com.minecolonies.coremod.blocks.schematic.BlockSolidSubstitution;
import com.minecolonies.coremod.blocks.schematic.BlockWaypoint;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingWareHouse;
import net.minecraft.block.BlockChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.Constants.UPDATE_FLAG;
import static com.minecolonies.coremod.placementhandlers.PlacementHandlers.getItemsFromTileEntity;
import static com.minecolonies.coremod.placementhandlers.PlacementHandlers.handleTileEntityPlacement;

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
        PlacementHandlers.handlers.add(new BlockSolidSubstitutionPlacementHandler());
        PlacementHandlers.handlers.add(new ChestPlacementHandler());
        PlacementHandlers.handlers.add(new WayPointBlockPlacementHandler());
        PlacementHandlers.handlers.add(new RackPlacementHandler());
        PlacementHandlers.handlers.add(new PlacementHandlers.FallingBlockPlacementHandler());
        PlacementHandlers.handlers.add(new GeneralBlockPlacementHandler());
    }

    public static class WayPointBlockPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState)
        {
            return blockState.getBlock() instanceof BlockWaypoint;
        }

        @Override
        public Object handle(
          @NotNull final World world,
          @NotNull final BlockPos pos,
          @NotNull final IBlockState blockState,
          @Nullable final NBTTagCompound tileEntityData,
          final boolean complete,
          final BlockPos centerPos)
        {
            world.setBlockToAir(pos);
            final Colony colony = ColonyManager.getClosestColony(world, pos);
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
            return blockState;
        }

        @Override
        public List<ItemStack> getRequiredItems(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState, @Nullable final NBTTagCompound tileEntityData, final boolean complete)
        {
            return new ArrayList<>();
        }
    }

    public static class RackPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState)
        {
            return blockState.getBlock() instanceof BlockMinecoloniesRack;
        }

        @Override
        public Object handle(
          @NotNull final World world,
          @NotNull final BlockPos pos,
          @NotNull final IBlockState blockState,
          @Nullable final NBTTagCompound tileEntityData,
          final boolean complete,
          final BlockPos centerPos)
        {
            if (tileEntityData != null)
            {
                handleTileEntityPlacement(tileEntityData, world, pos);
            }

            final TileEntity entity = world.getTileEntity(pos);
            if (entity instanceof TileEntityChest)
            {
                BuildingWareHouse.handleBuildingOverChest(pos, (TileEntityChest) entity, world);
            }
            else if (!world.setBlockState(pos, blockState, UPDATE_FLAG))
            {
                return ActionProcessingResult.DENY;
            }

            return blockState;
        }

        @Override
        public List<ItemStack> getRequiredItems(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState, @Nullable final NBTTagCompound tileEntityData, final boolean complete)
        {
            final List<ItemStack> itemList = new ArrayList<>();
            itemList.add(BlockUtils.getItemStackFromBlockState(blockState));

            for (final ItemStack stack : getItemsFromTileEntity(tileEntityData, world))
            {
                if (!ItemStackUtils.isEmpty(stack))
                {
                    itemList.add(stack);
                }
            }
            return itemList;
        }
    }

    public static class BlockSolidSubstitutionPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState)
        {
            return blockState.getBlock() instanceof BlockSolidSubstitution;
        }

        @Override
        public Object handle(
          @NotNull final World world,
          @NotNull final BlockPos pos,
          @NotNull final IBlockState blockState,
          @Nullable final NBTTagCompound tileEntityData,
          final boolean complete,
          final BlockPos centerPos)
        {
            final IBlockState newBlockState = BlockUtils.getSubstitutionBlockAtWorld(world, pos);
            if (complete)
            {
                if (!world.setBlockState(pos, blockState, UPDATE_FLAG))
                {
                    return ActionProcessingResult.DENY;
                }
            }
            else
            {
                if (!world.setBlockState(pos, newBlockState, UPDATE_FLAG))
                {
                    return ActionProcessingResult.DENY;
                }
            }

            return newBlockState;
        }

        @Override
        public List<ItemStack> getRequiredItems(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState, @Nullable final NBTTagCompound tileEntityData, final boolean complete)
        {
            final IBlockState newBlockState = BlockUtils.getSubstitutionBlockAtWorld(world, pos);
            final List<ItemStack> itemList = new ArrayList<>();
            itemList.add(BlockUtils.getItemStackFromBlockState(newBlockState));
            return itemList;
        }
    }

    public static class ChestPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState)
        {
            return blockState.getBlock() instanceof BlockChest;
        }

        @Override
        public Object handle(
          @NotNull final World world,
          @NotNull final BlockPos pos,
          @NotNull final IBlockState blockState,
          @Nullable final NBTTagCompound tileEntityData,
          final boolean complete,
          final BlockPos centerPos)
        {
            if (tileEntityData != null)
            {
                handleTileEntityPlacement(tileEntityData, world, pos);
            }

            final TileEntity entity = world.getTileEntity(pos);
            final Colony colony = ColonyManager.getClosestColony(world, pos);
            if (colony != null && entity instanceof TileEntityChest && colony.getBuildingManager().getBuilding(centerPos) instanceof BuildingWareHouse)
            {
                BuildingWareHouse.handleBuildingOverChest(pos, (TileEntityChest) entity, world);
            }

            if (!world.setBlockState(pos, blockState, UPDATE_FLAG))
            {
                return ActionProcessingResult.DENY;
            }

            return blockState;
        }

        @Override
        public List<ItemStack> getRequiredItems(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState, @Nullable final NBTTagCompound tileEntityData, final boolean complete)
        {
            final List<ItemStack> itemList = new ArrayList<>();
            itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
            itemList.addAll(getItemsFromTileEntity(tileEntityData, world));

            itemList.removeIf(ItemStackUtils::isEmpty);

            return itemList;
        }
    }

    public static class GeneralBlockPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState)
        {
            return true;
        }

        @Override
        public Object handle(
          @NotNull final World world,
          @NotNull final BlockPos pos,
          @NotNull final IBlockState blockState,
          @Nullable final NBTTagCompound tileEntityData,
          final boolean complete,
          final BlockPos centerPos)
        {
            if (world.getBlockState(pos).equals(blockState))
            {
                return ActionProcessingResult.ACCEPT;
            }

            if (!world.setBlockState(pos, blockState, UPDATE_FLAG))
            {
                return ActionProcessingResult.DENY;
            }

            if (tileEntityData != null)
            {
               handleTileEntityPlacement(tileEntityData, world, pos);
            }

            return blockState;
        }

        @Override
        public List<ItemStack> getRequiredItems(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState, @Nullable final NBTTagCompound tileEntityData, final boolean complete)
        {
            final List<ItemStack> itemList = new ArrayList<>();
            if (!ChiselAndBitsCheck.isChiselAndBitsBlock(blockState))
            {
                itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
            }
            itemList.addAll(getItemsFromTileEntity(tileEntityData, world));
            itemList.removeIf(ItemStackUtils::isEmpty);

            return itemList;
        }
    }
}
