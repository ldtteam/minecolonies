package com.minecolonies.coremod.entity.ai.citizen.builder;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildDecoration;
import com.minecolonies.coremod.util.ColonyUtils;
import com.minecolonies.coremod.util.StructureWrapper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

/**
 * Helper class to place and remove constructionTapes from the buildings.
 */
public final class ConstructionTapeHelper
{
    public static final PropertyDirection FACING    = BlockHorizontal.FACING;
    public static final int               MINHEIGHT = 1;
    public static final int               MAXHEIGHT = 256;

    /**
     * Private Constructor to hide implicit one.
     */
    private ConstructionTapeHelper()
    {
        /**
         * Intentionally left empty.
         */
    }

    /**
     * Calculates the borders for the workOrderBuildDecoration and sends it to the placement.
     *
     * @param workOrder the workOrder.
     * @param world     the world.
     */
    public static void placeConstructionTape(@NotNull final WorkOrderBuildDecoration workOrder, @NotNull final World world)
    {
        final Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> corners
          = ColonyUtils.calculateCorners(workOrder.getBuildingLocation(), world,
          new StructureWrapper(world, workOrder.getStructureName()), workOrder.getRotation(world), workOrder.isMirrored());
        placeConstructionTape(workOrder.getBuildingLocation(), corners, world);
    }

    /**
     * Place construction tape.
     *
     * @param pos     the building pos
     * @param corners the corner positions.
     * @param world   the world.
     */
    public static void placeConstructionTape(final BlockPos pos, final Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> corners, @NotNull final World world)
    {
        if (Configurations.gameplay.builderPlaceConstructionTape)
        {
            final IBlockState constructionTape = ModBlocks.blockConstructionTape.getDefaultState();

            final int x1 = corners.getFirst().getFirst();
            final int x3 = corners.getFirst().getSecond();
            final int z1 = corners.getSecond().getFirst();
            final int z3 = corners.getSecond().getSecond();
            final int y = pos.getY();
            int newY;

            if (x1 < x3)
            {
                for (int i = x1 + 1; i < x3; i++)
                {
                    newY = checkIfPlaceable(i, y, z1, world);
                    final BlockPos row1 = new BlockPos(i, newY, z1);
                    world.setBlockState(row1, constructionTape.withProperty(FACING, EnumFacing.SOUTH));
                    newY = checkIfPlaceable(i, y, z3, world);
                    final BlockPos row2 = new BlockPos(i, newY, z3);
                    world.setBlockState(row2, constructionTape.withProperty(FACING, EnumFacing.NORTH));
                }
            }
            else
            {
                for (int i = x3 + 1; i < x1; i++)
                {
                    newY = checkIfPlaceable(i, y, z1, world);
                    final BlockPos row1 = new BlockPos(i, newY, z1);
                    world.setBlockState(row1, constructionTape.withProperty(FACING, EnumFacing.SOUTH));
                    newY = checkIfPlaceable(i, y, z3, world);
                    final BlockPos row2 = new BlockPos(i, newY, z3);
                    world.setBlockState(row2, constructionTape.withProperty(FACING, EnumFacing.NORTH));
                }
            }
            if (z1 < z3)
            {
                for (int i = z1 + 1; i < z3; i++)
                {
                    newY = checkIfPlaceable(x1, y, i, world);
                    final BlockPos row3 = new BlockPos(x1, newY, i);
                    world.setBlockState(row3, constructionTape.withProperty(FACING, EnumFacing.EAST));
                    newY = checkIfPlaceable(x3, y, i, world);
                    final BlockPos row4 = new BlockPos(x3, newY, i);
                    world.setBlockState(row4, constructionTape.withProperty(FACING, EnumFacing.WEST));
                }
            }
            else
            {
                for (int i = z3 + 1; i < z1; i++)
                {
                    newY = checkIfPlaceable(x1, y, i, world);
                    final BlockPos row3 = new BlockPos(x1, newY, i);
                    world.setBlockState(row3, constructionTape.withProperty(FACING, EnumFacing.EAST));
                    newY = checkIfPlaceable(x3, y, i, world);
                    final BlockPos row4 = new BlockPos(x3, newY, i);
                    world.setBlockState(row4, constructionTape.withProperty(FACING, EnumFacing.WEST));
                }
            }
            newY = checkIfPlaceable(x1, y, z1, world);
            final BlockPos corner1 = new BlockPos(x1, newY, z1);
            newY = checkIfPlaceable(x1, y, z3, world);
            final BlockPos corner2 = new BlockPos(x1, newY, z3);
            newY = checkIfPlaceable(x3, y, z1, world);
            final BlockPos corner3 = new BlockPos(x3, newY, z1);
            newY = checkIfPlaceable(x3, y, z3, world);
            final BlockPos corner4 = new BlockPos(x3, newY, z3);
            world.setBlockState(corner1, constructionTape.withProperty(FACING, EnumFacing.SOUTH));
            world.setBlockState(corner2, constructionTape.withProperty(FACING, EnumFacing.EAST));
            world.setBlockState(corner3, constructionTape.withProperty(FACING, EnumFacing.WEST));
            world.setBlockState(corner4, constructionTape.withProperty(FACING, EnumFacing.NORTH));
        }
    }

    /**
     * Check if a block is placeable and return new Y position.
     *
     * @param x     Block X position.
     * @param y     Block Y position.
     * @param z     Block Z position.
     * @param world the world.
     * @return The new Y position.
     */

    public static int checkIfPlaceable(@NotNull final int x, @NotNull final int y, @NotNull final int z, @NotNull final World world)
    {
        BlockPos target = new BlockPos(x,y,z);
        final Chunk chunk = world.getChunk(target);

        target = new BlockPos(x, chunk.getTopFilledSegment() + 16, z);
        while(world.getBlockState(target).getMaterial().isReplaceable())
        {
            target = target.down();
            if (target.getY() == 0)
            {
                break;
            }
        }

        return target.getY() + 1;
    }

    /**
     * Calculates the borders for the workOrderBuildDecoration and sends it to the removal.
     *
     * @param workOrder the workOrder.
     * @param world     the world.
     */
    public static void removeConstructionTape(@NotNull final WorkOrderBuildDecoration workOrder, @NotNull final World world)
    {
        final Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> corners
          = ColonyUtils.calculateCorners(workOrder.getBuildingLocation(), world,
          new StructureWrapper(world, workOrder.getStructureName()), workOrder.getRotation(world), workOrder.isMirrored());
        removeConstructionTape(corners, world);
    }

    /**
     * Remove construction tape.
     *
     * @param corners the corner positions.
     * @param world   the world.
     */
    public static void removeConstructionTape(final Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> corners, @NotNull final World world)
    {
        final int x1 = corners.getFirst().getFirst();
        final int x3 = corners.getFirst().getSecond();
        final int z1 = corners.getSecond().getFirst();
        final int z3 = corners.getSecond().getSecond();
        if (x1 < x3)
        {
            for (int i = x1; i <= x3; i++)
            {
                final BlockPos block1 = new BlockPos(i, 0, z1);
                final BlockPos block2 = new BlockPos(i, 0, z3);
                removeTapeIfNecessary(world, block1, ModBlocks.blockConstructionTape);
                removeTapeIfNecessary(world, block2, ModBlocks.blockConstructionTape);
            }
        }
        else
        {
            for (int i = x3; i <= x1; i++)
            {
                final BlockPos block1 = new BlockPos(i, 0, z1);
                final BlockPos block2 = new BlockPos(i, 0, z3);
                removeTapeIfNecessary(world, block1, ModBlocks.blockConstructionTape);
                removeTapeIfNecessary(world, block2, ModBlocks.blockConstructionTape);
            }
        }
        if (z1 < z3)
        {
            for (int i = z1; i <= z3; i++)
            {
                final BlockPos block1 = new BlockPos(x1, 0, i);
                final BlockPos block2 = new BlockPos(x3, 0, i);
                removeTapeIfNecessary(world, block1, ModBlocks.blockConstructionTape);
                removeTapeIfNecessary(world, block2, ModBlocks.blockConstructionTape);
            }
        }
        else
        {
            for (int i = z3; i <= z1; i++)
            {
                final BlockPos block1 = new BlockPos(x1, 0, i);
                final BlockPos block2 = new BlockPos(x3, 0, i);
                removeTapeIfNecessary(world, block1, ModBlocks.blockConstructionTape);
                removeTapeIfNecessary(world, block2, ModBlocks.blockConstructionTape);
            }
        }

        final BlockPos corner1 = new BlockPos(x1, 0, z1);
        final BlockPos corner2 = new BlockPos(x1, 0, z3);
        final BlockPos corner3 = new BlockPos(x3, 0, z1);
        final BlockPos corner4 = new BlockPos(x3, 0, z3);
        removeTapeIfNecessary(world, corner1, ModBlocks.blockConstructionTape);
        removeTapeIfNecessary(world, corner2, ModBlocks.blockConstructionTape);
        removeTapeIfNecessary(world, corner3, ModBlocks.blockConstructionTape);
        removeTapeIfNecessary(world, corner4, ModBlocks.blockConstructionTape);
    }

    /**
     * @param world            the world.
     * @param block            the block.
     * @param tapeOrTapeCorner Is the checked block supposed to be ConstructionTape or ConstructionTapeCorner.
     */
    public static void removeTapeIfNecessary(@NotNull final World world, @NotNull final BlockPos block, @NotNull final Block tapeOrTapeCorner)
    {
        for (int y = MINHEIGHT; y <= MAXHEIGHT; y++)
        {
            final BlockPos newBlock = new BlockPos(block.getX(), y, block.getZ());
            if (world.getBlockState(newBlock).getBlock() == tapeOrTapeCorner)
            {
                world.setBlockState(newBlock, Blocks.AIR.getDefaultState());
                break;
            }
        }
    }
}
