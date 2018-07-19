package com.minecolonies.coremod.placementhandlers;

import com.minecolonies.api.util.BlockUtils;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.minecolonies.api.util.constant.Constants.UPDATE_FLAG;

/**
 * Class containing all placement handler implementations.
 * <p>
 * We suppress warning squid:S2972 which handles the max size of internal classes.
 * This doesn't apply here since it wouldn't make sense extracting all of those in separate classes.
 */
@SuppressWarnings("squid:S2972")
public final class PlacementHandlers
{
    public static final List<IPlacementHandler> handlers = new ArrayList<>();

    static
    {
        handlers.add(new AirPlacementHandler());
        handlers.add(new FirePlacementHandler());
        handlers.add(new GrassPlacementHandler());
        handlers.add(new DoorPlacementHandler());
        handlers.add(new BedPlacementHandler());
        handlers.add(new DoublePlantPlacementHandler());
        handlers.add(new SpecialBlockPlacementAttemptHandler());
        handlers.add(new FlowerPotPlacementHandler());
        handlers.add(new BlockGrassPathPlacementHandler());
        handlers.add(new StairBlockPlacementHandler());
        handlers.add(new ChestPlacementHandler());
        handlers.add(new GeneralBlockPlacementHandler());
    }

    /**
     * Private constructor to hide implicit one.
     */
    private PlacementHandlers()
    {
        /*
         * Intentionally left empty.
         */
    }
    
    public static class FirePlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState)
        {
            return blockState.getBlock() instanceof BlockFire;
        }
        
        @Override
        public List<ItemStack> getRequiredItems(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState, @Nullable final NBTTagCompound tileEntityData, final boolean complete)
        {
            final List<ItemStack> itemList = new ArrayList<>();
            itemList.add(new ItemStack(Items.FLINT_AND_STEEL, 1));
            return itemList;
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
            world.setBlockState(pos, blockState, UPDATE_FLAG);
            return ActionProcessingResult.ACCEPT;
        }
    }

    public static class GrassPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState)
        {
            return blockState.getBlock() == Blocks.GRASS;
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
            if (!world.setBlockState(pos, Blocks.DIRT.getDefaultState(), UPDATE_FLAG))
            {
                return ActionProcessingResult.DENY;
            }
            return Blocks.DIRT.getDefaultState();
        }
        
        @Override
        public List<ItemStack> getRequiredItems(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState, @Nullable final NBTTagCompound tileEntityData, final boolean complete)
        {
            final List<ItemStack> itemList = new ArrayList<>();
            itemList.add(new ItemStack(Blocks.DIRT));
            return itemList;
        }
    }

    public static class DoorPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState)
        {
            return blockState.getBlock() instanceof BlockDoor;
        }
        
        @Override
        public Object handle(
          @NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState,
          @Nullable final NBTTagCompound tileEntityData, final boolean complete, final BlockPos centerPos)
        {
            if (blockState.getValue(BlockDoor.HALF).equals(BlockDoor.EnumDoorHalf.LOWER))
            {
                ItemDoor.placeDoor(world, pos, blockState.getValue(BlockDoor.FACING), blockState.getBlock(), false);
            }

            return blockState;
        }
        
        @Override
        public List<ItemStack> getRequiredItems(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState, @Nullable final NBTTagCompound tileEntityData, final boolean complete)
        {
            final List<ItemStack> itemList = new ArrayList<>();
            itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
            return itemList;
        }
    }

    public static class BedPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState)
        {
            return blockState.getBlock() instanceof BlockBed;
        }
        
        @Override
        public Object handle(
          @NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState,
          @Nullable final NBTTagCompound tileEntityData, final boolean complete, final BlockPos centerPos)
        {
            if (blockState.getValue(BlockBed.PART) == BlockBed.EnumPartType.HEAD)
            {
                final EnumFacing facing = blockState.getValue(BlockBed.FACING);

                //pos.offset(facing) will get the other part of the bed
                world.setBlockState(pos.offset(facing.getOpposite()), blockState.withProperty(BlockBed.PART, BlockBed.EnumPartType.FOOT), UPDATE_FLAG);
                world.setBlockState(pos, blockState.withProperty(BlockBed.PART, BlockBed.EnumPartType.HEAD), UPDATE_FLAG);

                if (tileEntityData != null)
                {
                    handleTileEntityPlacement(tileEntityData, world, pos);
                    handleTileEntityPlacement(tileEntityData, world, pos.offset(facing.getOpposite()));
                }
                return blockState;
            }

            return ActionProcessingResult.ACCEPT;
        }
        
        @Override
        public List<ItemStack> getRequiredItems(
          @NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState, @Nullable final NBTTagCompound tileEntityData, final boolean complete)
        {
            return getItemsFromTileEntity(tileEntityData, world);
        }
    }

    public static class DoublePlantPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState)
        {
            return blockState.getBlock() instanceof BlockDoublePlant;
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
            if (blockState.getValue(BlockDoublePlant.HALF).equals(BlockDoublePlant.EnumBlockHalf.LOWER))
            {
                world.setBlockState(pos, blockState.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.LOWER), UPDATE_FLAG);
                world.setBlockState(pos.up(), blockState.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER), UPDATE_FLAG);
                return blockState;
            }
            return ActionProcessingResult.ACCEPT;
        }

        @Override
        public List<ItemStack> getRequiredItems(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState, @Nullable final NBTTagCompound tileEntityData, final boolean complete)
        {
            final List<ItemStack> itemList = new ArrayList<>();
            itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
            return itemList;
        }
    }

    public static class SpecialBlockPlacementAttemptHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState)
        {
            return blockState instanceof BlockEndPortal
                     || blockState instanceof BlockMobSpawner
                     || blockState instanceof BlockDragonEgg
                     || blockState instanceof BlockPortal;
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
            return ActionProcessingResult.ACCEPT;
        }

        @Override
        public List<ItemStack> getRequiredItems(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState, @Nullable final NBTTagCompound tileEntityData, final boolean complete)
        {
            return new ArrayList<>();
        }
    }

    public static class FlowerPotPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState)
        {
            return blockState.getBlock() instanceof BlockFlowerPot;
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
            if (!world.setBlockState(pos, blockState, UPDATE_FLAG))
            {
                return false;
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
            itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
            itemList.addAll(getItemsFromTileEntity(tileEntityData, world));
            itemList.removeIf(ItemStackUtils::isEmpty);

            return itemList;
        }
    }

    public static class AirPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState)
        {
            return blockState.getBlock() instanceof BlockAir;
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

            final List<Entity> entityList = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos), entity -> !(entity instanceof EntityLiving || entity instanceof EntityItem));
            if (!entityList.isEmpty())
            {
                for (final Entity entity : entityList)
                {
                    entity.setDead();
                }
            }

            world.setBlockToAir(pos);
            return ActionProcessingResult.ACCEPT;
        }

        @Override
        public List<ItemStack> getRequiredItems(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState, @Nullable final NBTTagCompound tileEntityData, final boolean complete)
        {
            return new ArrayList<>();
        }
    }

    public static class BlockGrassPathPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState)
        {
            return blockState.getBlock() instanceof BlockGrassPath;
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
            if (!world.setBlockState(pos, Blocks.GRASS_PATH.getDefaultState(), UPDATE_FLAG))
            {
                return ActionProcessingResult.DENY;
            }

            return Blocks.DIRT.getDefaultState();
        }

        @Override
        public List<ItemStack> getRequiredItems(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState, @Nullable final NBTTagCompound tileEntityData, final boolean complete)
        {
            final List<ItemStack> itemList = new ArrayList<>();
            itemList.add(new ItemStack(Blocks.DIRT, 1));
            return itemList;
        }
    }

    public static class StairBlockPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState)
        {
            return blockState.getBlock() instanceof BlockStairs
                     && world.getBlockState(pos).getBlock() instanceof BlockStairs
                     && world.getBlockState(pos).getValue(BlockStairs.FACING) == blockState.getValue(BlockStairs.FACING)
                     && blockState.getBlock() == world.getBlockState(pos).getBlock();
        }

        @Override
        public Object handle(
          @NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState,
          @Nullable final NBTTagCompound tileEntityData, final boolean complete, final BlockPos centerPos)
        {
            return ActionProcessingResult.ACCEPT;
        }

        @Override
        public List<ItemStack> getRequiredItems(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState, @Nullable final NBTTagCompound tileEntityData, final boolean complete)
        {
            return new ArrayList<>();
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
            final List<ItemStack> itemList = new ArrayList<>(getItemsFromTileEntity(tileEntityData, world));
            itemList.removeIf(ItemStackUtils::isEmpty);
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

    /**
     * Handles tileEntity placement.
     * @param tileEntityData the data of the tile entity.
     * @param world the world.
     * @param pos the position.
     */
    public static void handleTileEntityPlacement(final NBTTagCompound tileEntityData, final World world, @NotNull final BlockPos pos)
    {
        if (tileEntityData != null)
        {
            final TileEntity tileEntityFlowerpot = world.getTileEntity(pos);
            if (tileEntityFlowerpot == null)
            {
                TileEntity.create(world, tileEntityData);
            }
            else
            {
                tileEntityFlowerpot.readFromNBT(tileEntityData);
                world.setTileEntity(pos, tileEntityFlowerpot);
            }
        }
    }

    /**
     * Gets the list of items from a possible tileEntity.
     * @param tileEntityData the data.
     * @param world the world.
     * @return the required list.
     */
    public static List<ItemStack> getItemsFromTileEntity(final NBTTagCompound tileEntityData, final World world)
    {
        if (tileEntityData != null)
        {
            return ItemStackUtils.getItemStacksOfTileEntity(tileEntityData, world);
        }
        return Collections.emptyList();
    }
}
