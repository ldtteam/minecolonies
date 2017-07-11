package com.minecolonies.coremod.entity.ai.citizen.builder;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuild;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildDecoration;
import com.minecolonies.coremod.util.StructureWrapper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
     * Proxy to place the tape also with the building only.
     *
     * @param building the building.
     * @param world    the world.
     */
    public static void placeConstructionTape(@NotNull final AbstractBuilding building, @NotNull final World world)
    {
        placeConstructionTape(new WorkOrderBuild(building, 1), world);
    }

    /**
     * Place construction tape.
     *
     * @param workOrder the workorder.
     * @param world     the world.
     */

    public static void placeConstructionTape(@NotNull final WorkOrderBuildDecoration workOrder, @NotNull final World world)
    {
        if (Configurations.gameplay.builderPlaceConstructionTape)
        {
            final StructureWrapper wrapper = new StructureWrapper(world, workOrder.getStructureName());
            final BlockPos pos = workOrder.getBuildingLocation();
            final IBlockState constructionTape = ModBlocks.blockConstructionTape.getDefaultState();
            final IBlockState constructionTapeCorner = ModBlocks.blockConstructionTapeCorner.getDefaultState();

            wrapper.rotate(workOrder.getRotation(world), world, workOrder.getBuildingLocation(), workOrder.isMirrored() ? Mirror.FRONT_BACK : Mirror.NONE);
            wrapper.setPosition(pos);

            final int x1 = wrapper.getPosition().getX() - wrapper.getOffset().getX() - 1;
            final int z1 = wrapper.getPosition().getZ() - wrapper.getOffset().getZ() - 1;
            final int x3 = wrapper.getPosition().getX() + (wrapper.getWidth() - wrapper.getOffset().getX());
            final int z3 = wrapper.getPosition().getZ() + (wrapper.getLength() - wrapper.getOffset().getZ());
            final int y = wrapper.getPosition().getY();
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
            world.setBlockState(corner1, constructionTapeCorner.withProperty(FACING, EnumFacing.SOUTH));
            world.setBlockState(corner2, constructionTapeCorner.withProperty(FACING, EnumFacing.EAST));
            world.setBlockState(corner3, constructionTapeCorner.withProperty(FACING, EnumFacing.WEST));
            world.setBlockState(corner4, constructionTapeCorner.withProperty(FACING, EnumFacing.NORTH));
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
        int newY = y;
        boolean working = true;
        while (working)
        {
            final BlockPos block = new BlockPos(x, newY, z);
            final BlockPos blockMin1 = new BlockPos(x, newY - 1, z);
            if (world.getBlockState(block).getMaterial().isReplaceable())
            {
                if (world.getBlockState(blockMin1).getMaterial().isReplaceable() && newY >= 1)
                {
                    newY = newY - 1;
                }
                else
                {
                    working = false;
                }
            }
            else
            {
                newY = newY + 1;
            }
        }
        return newY > 0 ? newY : y;
    }

    /**
     * Proxy to remove the tape also with the building only.
     *
     * @param building the building.
     * @param world    the world.
     */
    public static void removeConstructionTape(@NotNull final AbstractBuilding building, @NotNull final World world)
    {
        removeConstructionTape(new WorkOrderBuild(building, 1), world);
    }

    /**
     * Remove construction tape.
     *
     * @param workOrder the workorder.
     * @param world     the world.
     */

    public static void removeConstructionTape(@NotNull final WorkOrderBuildDecoration workOrder, @NotNull final World world)
    {
        final StructureWrapper wrapper = new StructureWrapper(world, workOrder.getStructureName());
        final BlockPos pos = workOrder.getBuildingLocation();
        final int tempRotation =  workOrder.getRotation(world);
        wrapper.rotate(tempRotation, world, workOrder.getBuildingLocation(), workOrder.isMirrored() ? Mirror.FRONT_BACK : Mirror.NONE);
        wrapper.setPosition(pos);
        final int x1 = wrapper.getPosition().getX() - wrapper.getOffset().getX() - 1;
        final int z1 = wrapper.getPosition().getZ() - wrapper.getOffset().getZ() - 1;
        final int x3 = wrapper.getPosition().getX() + (wrapper.getWidth() - wrapper.getOffset().getX());
        final int z3 = wrapper.getPosition().getZ() + (wrapper.getLength() - wrapper.getOffset().getZ());
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
        removeTapeIfNecessary(world, corner1, ModBlocks.blockConstructionTapeCorner);
        removeTapeIfNecessary(world, corner2, ModBlocks.blockConstructionTapeCorner);
        removeTapeIfNecessary(world, corner3, ModBlocks.blockConstructionTapeCorner);
        removeTapeIfNecessary(world, corner4, ModBlocks.blockConstructionTapeCorner);
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
