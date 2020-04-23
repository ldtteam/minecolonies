package com.minecolonies.coremod.entity.ai.citizen.builder;

import com.ldtteam.structures.helpers.Structure;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.blocks.decorative.BlockConstructionTape;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildDecoration;
import com.minecolonies.coremod.util.ColonyUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.state.DirectionProperty;
import net.minecraft.util.Direction;
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
    public static final DirectionProperty FACING    = HorizontalBlock.HORIZONTAL_FACING;
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
          new Structure(world, workOrder.getStructureName(), new PlacementSettings()), workOrder.getRotation(world), workOrder.isMirrored());
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
        if (MineColonies.getConfig().getCommon().builderPlaceConstructionTape.get())
        {
            final BlockState constructionTape = ModBlocks.blockConstructionTape.getDefaultState();

            final int x1 = corners.getA().getA();
            final int x3 = corners.getA().getB();
            final int z1 = corners.getB().getA();
            final int z3 = corners.getB().getB();
            final int y = pos.getY();
            int newY;

            if (x1 < x3)
            {
                for (int i = x1 + 1; i < x3; i++)
                {
                    newY = checkIfPlaceable(i, y, z1, world);
                    final BlockPos row1 = new BlockPos(i, newY, z1);
                    world.setBlockState(row1, BlockConstructionTape.getOptimalStateForPlacement(constructionTape.with(FACING, Direction.SOUTH), world, row1));
                    newY = checkIfPlaceable(i, y, z3, world);
                    final BlockPos row2 = new BlockPos(i, newY, z3);
                    world.setBlockState(row2, BlockConstructionTape.getOptimalStateForPlacement(constructionTape.with(FACING, Direction.NORTH), world, row2));
                }
            }
            else
            {
                for (int i = x3 + 1; i < x1; i++)
                {
                    newY = checkIfPlaceable(i, y, z1, world);
                    final BlockPos row1 = new BlockPos(i, newY, z1);
                    world.setBlockState(row1, BlockConstructionTape.getOptimalStateForPlacement(constructionTape.with(FACING, Direction.SOUTH), world, row1));
                    newY = checkIfPlaceable(i, y, z3, world);
                    final BlockPos row2 = new BlockPos(i, newY, z3);
                    world.setBlockState(row2, BlockConstructionTape.getOptimalStateForPlacement(constructionTape.with(FACING, Direction.NORTH), world, row2));
                }
            }
            if (z1 < z3)
            {
                for (int i = z1 + 1; i < z3; i++)
                {
                    newY = checkIfPlaceable(x1, y, i, world);
                    final BlockPos row3 = new BlockPos(x1, newY, i);
                    world.setBlockState(row3, BlockConstructionTape.getOptimalStateForPlacement(constructionTape.with(FACING, Direction.EAST), world, row3));
                    newY = checkIfPlaceable(x3, y, i, world);
                    final BlockPos row4 = new BlockPos(x3, newY, i);
                    world.setBlockState(row4, BlockConstructionTape.getOptimalStateForPlacement(constructionTape.with(FACING, Direction.WEST), world, row4));
                }
            }
            else
            {
                for (int i = z3 + 1; i < z1; i++)
                {
                    newY = checkIfPlaceable(x1, y, i, world);
                    final BlockPos row3 = new BlockPos(x1, newY, i);
                    world.setBlockState(row3, BlockConstructionTape.getOptimalStateForPlacement(constructionTape.with(FACING, Direction.EAST), world, row3));
                    newY = checkIfPlaceable(x3, y, i, world);
                    final BlockPos row4 = new BlockPos(x3, newY, i);
                    world.setBlockState(row4, BlockConstructionTape.getOptimalStateForPlacement(constructionTape.with(FACING, Direction.WEST), world, row4));
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
            world.setBlockState(corner1, BlockConstructionTape.getOptimalStateForPlacement(constructionTape.with(FACING, Direction.SOUTH), world, corner1));
            world.setBlockState(corner2, BlockConstructionTape.getOptimalStateForPlacement(constructionTape.with(FACING, Direction.EAST), world, corner2));
            world.setBlockState(corner3, BlockConstructionTape.getOptimalStateForPlacement(constructionTape.with(FACING, Direction.WEST), world, corner3));
            world.setBlockState(corner4, BlockConstructionTape.getOptimalStateForPlacement(constructionTape.with(FACING, Direction.NORTH), world, corner4));
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
        final Chunk chunk = world.getChunkAt(target);

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
        final Structure structure = new Structure(world, workOrder.getStructureName(), new PlacementSettings());
        if (!structure.isBluePrintMissing())
        {
            final Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> corners = ColonyUtils.calculateCorners(workOrder.getBuildingLocation(), world,
              structure, workOrder.getRotation(world), workOrder.isMirrored());
            removeConstructionTape(corners, world);
        }
    }

    /**
     * Remove construction tape.
     *
     * @param corners the corner positions.
     * @param world   the world.
     */
    public static void removeConstructionTape(final Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> corners, @NotNull final World world)
    {
        final int x1 = corners.getA().getA();
        final int x3 = corners.getA().getB();
        final int z1 = corners.getB().getA();
        final int z3 = corners.getB().getB();
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
