package com.minecolonies.coremod.entity.ai.citizen.builder;

import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.util.LoadOnlyStructureHandler;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.blocks.decorative.BlockConstructionTape;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildDecoration;
import com.minecolonies.coremod.util.ColonyUtils;
import net.minecraft.block.*;
import net.minecraft.state.BooleanProperty;
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
    public static final BooleanProperty   CORNER    = BooleanProperty.create("corner");
    public static final int               MINHEIGHT = 1;
    public static final int               MAXHEIGHT = 256;

    /**
     * Private Constructor to hide implicit one. Intentionally empty.
     */
    private ConstructionTapeHelper() {}

    /**
     * Calculates the borders for the workOrderBuildDecoration and sends it to the placement.
     *
     * @param workOrder the workOrder.
     * @param world     the world.
     */
    public static void placeConstructionTape(@NotNull final WorkOrderBuildDecoration workOrder, @NotNull final World world)
    {
        final Tuple<BlockPos, BlockPos> corners
          = ColonyUtils.calculateCorners(workOrder.getBuildingLocation(),
          world,
          new LoadOnlyStructureHandler(world, workOrder.getBuildingLocation(), workOrder.getStructureName(), new PlacementSettings(), true).getBluePrint(),
          workOrder.getRotation(world),
          workOrder.isMirrored());
        placeConstructionTape(workOrder.getBuildingLocation(), corners, world);
    }

    /**
     * Place construction tape.
     *
     * @param pos     the building pos
     * @param corners the corner positions.
     * @param world   the world.
     */
    public static void placeConstructionTape(final BlockPos pos, final Tuple<BlockPos, BlockPos> corners, @NotNull final World world)
    {
        if (!MineColonies.getConfig().getCommon().builderPlaceConstructionTape.get())
        {
            return;
        }

        final BlockState constructionTape = ModBlocks.blockConstructionTape.getDefaultState();

        final int x = Math.min(corners.getA().getX(), corners.getB().getX());
        final int y = pos.getY();
        final int z = Math.min(corners.getA().getZ(), corners.getB().getZ());
        final int sizeX = Math.abs(corners.getA().getX() - corners.getB().getX());
        final int sizeZ = Math.abs(corners.getA().getZ() - corners.getB().getZ());
        BlockPos working;

        for (BlockPos place = new BlockPos(x, y, z); place.getX() < x + sizeX || place.getZ() < z + sizeZ; )
        {
            if (place.getX() < x + sizeX)
            {
                working = firstValidPosition(new BlockPos(place.getX(), y, z), world);
                world.setBlockState(working,
                  BlockConstructionTape.getPlacementState(constructionTape.with(CORNER, place.getX() == x), world, working, Direction.SOUTH)
                );

                working = firstValidPosition(new BlockPos(place.getX(), y, z + sizeZ), world);
                world.setBlockState(working,
                  BlockConstructionTape.getPlacementState(constructionTape.with(CORNER, place.getX() == x), world, working, Direction.NORTH)
                );
            }

            if (place.getZ() < z + sizeZ)
            {
                working = firstValidPosition(new BlockPos(x, y, place.getZ()), world);
                world.setBlockState(working,
                  BlockConstructionTape.getPlacementState(constructionTape.with(CORNER, place.getZ() == z), world, working, Direction.EAST)
                );

                working = firstValidPosition(new BlockPos(x + sizeX, y, place.getZ()), world);
                world.setBlockState(working,
                  BlockConstructionTape.getPlacementState(constructionTape.with(CORNER, place.getZ() == z), world, working, place.getZ() == z ? Direction.SOUTH : Direction.WEST)
                );
            }

            place = place.south().east();
        }

        working = firstValidPosition(new BlockPos(x + sizeX, y, z + sizeZ), world);
        world.setBlockState(working,
          BlockConstructionTape.getPlacementState(constructionTape.with(CORNER, true), world, working, Direction.WEST)
        );
    }

    /**
     * Find and return the highest position that is directly above a non-replaceable block.
     *
     * @param target the target position for the block
     * @param world  the world.
     * @return The new block position.
     */
    public static BlockPos firstValidPosition(@NotNull BlockPos target, @NotNull World world)
    {
        final Chunk chunk = world.getChunkAt(target);

        target = new BlockPos(target.getX(), chunk.getTopFilledSegment() + 16, target.getZ());

        while (target.getY() > 0)
        {
            target = target.down();

            if (world.getBlockState(target).getBlock() instanceof LeavesBlock)
            {
                for (BlockPos seeker = new BlockPos(target.down()); seeker.getY() > 0; seeker = seeker.down())
                {
                    if (world.getBlockState(seeker).getMaterial().isReplaceable() || world.isAirBlock(seeker))
                    {
                        target = seeker;
                        break;
                    }
                }
            }


            if (!world.getBlockState(target).getMaterial().isReplaceable()
                  && !(world.getBlockState(target).getBlock() instanceof FlowerBlock))
            {
                break;
            }
        }

        return target.up();
    }

    /**
     * Calculates the borders for the workOrderBuildDecoration and sends it to the removal.
     *
     * @param workOrder the workOrder.
     * @param world     the world.
     */
    public static void removeConstructionTape(@NotNull final WorkOrderBuildDecoration workOrder, @NotNull final World world)
    {
        final LoadOnlyStructureHandler structure =
          new LoadOnlyStructureHandler(world, workOrder.getBuildingLocation(), workOrder.getStructureName(), new PlacementSettings(), true);
        if (structure.hasBluePrint())
        {
            final Tuple<BlockPos, BlockPos> corners = ColonyUtils.calculateCorners(workOrder.getBuildingLocation(), world,
              structure.getBluePrint(), workOrder.getRotation(world), workOrder.isMirrored());
            removeConstructionTape(corners, world);
        }
    }

    /**
     * Remove construction tape.
     *
     * @param corners the corner positions.
     * @param world   the world.
     */
    public static void removeConstructionTape(final Tuple<BlockPos, BlockPos> corners, @NotNull final World world)
    {
        final int x1 = corners.getA().getX();
        final int x3 = corners.getB().getX();
        final int z1 = corners.getA().getZ();
        final int z3 = corners.getB().getZ();
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
                world.removeBlock(newBlock, false);
                break;
            }
        }
    }
}
