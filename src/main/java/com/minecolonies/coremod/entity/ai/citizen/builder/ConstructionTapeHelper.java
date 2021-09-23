package com.minecolonies.coremod.entity.ai.citizen.builder;

import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.util.LoadOnlyStructureHandler;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.blocks.decorative.BlockConstructionTape;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildDecoration;
import com.minecolonies.coremod.util.ColonyUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Helper class to place and remove constructionTapes from the buildings.
 */
public final class ConstructionTapeHelper
{
    public static final DirectionProperty FACING    = HorizontalBlock.FACING;
    public static final BooleanProperty   CORNER    = BooleanProperty.create("corner");

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
          = ColonyUtils.calculateCorners(workOrder.getSchematicLocation(),
          world,
          new LoadOnlyStructureHandler(world, workOrder.getSchematicLocation(), workOrder.getStructureName(), new PlacementSettings(), true).getBluePrint(),
          workOrder.getRotation(world),
          workOrder.isMirrored());
        placeConstructionTape(corners, world);
    }

    /**
     * Place construction tape.
     *
     * @param orgCorners the corner positions.
     * @param world   the world.
     */
    public static void placeConstructionTape(final Tuple<BlockPos, BlockPos> orgCorners, @NotNull final World world)
    {
        if (!MineColonies.getConfig().getServer().builderPlaceConstructionTape.get())
        {
            return;
        }

        final Tuple<BlockPos, BlockPos> corners = new Tuple<>(orgCorners.getA().offset(-1, 0, -1), orgCorners.getB().offset(1, 0, 1));
        final BlockState constructionTape = ModBlocks.blockConstructionTape.defaultBlockState();

        final int x = Math.min(corners.getA().getX(), corners.getB().getX());
        final int y = Math.max(corners.getA().getY(), corners.getB().getY());
        final int z = Math.min(corners.getA().getZ(), corners.getB().getZ());
        final int sizeX = Math.abs(corners.getA().getX() - corners.getB().getX());
        final int sizeZ = Math.abs(corners.getA().getZ() - corners.getB().getZ());
        final int sizeY = Math.abs(corners.getA().getY() - corners.getB().getY());
        BlockPos working;

        for (BlockPos place = new BlockPos(x, y, z); place.getX() < x + sizeX || place.getZ() < z + sizeZ; )
        {

            if (place.getX() < x + sizeX)
            {
                working = firstValidPosition(new BlockPos(place.getX(), y, z), world, sizeY);
                if (working != null)
                {
                    world.setBlockAndUpdate(working, BlockConstructionTape.getPlacementState(constructionTape.setValue(CORNER, place.getX() == x), world, working, Direction.SOUTH));
                }

                working = firstValidPosition(new BlockPos(place.getX(), y, z + sizeZ), world, sizeY);
                if (working != null)
                {
                    world.setBlockAndUpdate(working, BlockConstructionTape.getPlacementState(constructionTape.setValue(CORNER, place.getX() == x), world, working, Direction.NORTH));
                }
            }

            if (place.getZ() < z + sizeZ)
            {
                working = firstValidPosition(new BlockPos(x, y, place.getZ()), world, sizeY);
                if (working != null)
                {
                    world.setBlockAndUpdate(working, BlockConstructionTape.getPlacementState(constructionTape.setValue(CORNER, place.getZ() == z), world, working, Direction.EAST));
                }

                working = firstValidPosition(new BlockPos(x + sizeX, y, place.getZ()), world, sizeY);
                if (working != null)
                {
                    world.setBlockAndUpdate(working,
                      BlockConstructionTape.getPlacementState(constructionTape.setValue(CORNER, place.getZ() == z), world, working, place.getZ() == z ? Direction.SOUTH : Direction.WEST));
                }
            }

            place = place.south().east();
        }

        working = firstValidPosition(new BlockPos(x + sizeX, y, z + sizeZ), world, sizeY);
        if (working != null)
        {
            world.setBlockAndUpdate(working, BlockConstructionTape.getPlacementState(constructionTape.setValue(CORNER, true), world, working, Direction.WEST));
        }
    }

    /**
     * Find and return the highest position that is directly above a non-replaceable block.
     *
     * @param target the target position for the block
     * @param world  the world.
     * @return The new block position or null if no valid one is found.
     */
    @Nullable
    public static BlockPos firstValidPosition(@NotNull final BlockPos target, @NotNull final World world, final int height)
    {
        for (int i = 0; i <= height + 5; i++)
        {
            final BlockPos tempTarget = new BlockPos(target.getX(), target.getY() - i, target.getZ());
            final BlockState state = world.getBlockState(tempTarget);
            final BlockState upState = world.getBlockState(tempTarget.above());

            if (state.canOcclude() && !upState.canOcclude() && (upState.getMaterial().isReplaceable() || upState.isAir(world, tempTarget.above())))
            {
                return tempTarget.above();
            }
        }

        return null;
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
          new LoadOnlyStructureHandler(world, workOrder.getSchematicLocation(), workOrder.getStructureName(), new PlacementSettings(), true);
        if (structure.hasBluePrint())
        {
            final Tuple<BlockPos, BlockPos> corners = ColonyUtils.calculateCorners(workOrder.getSchematicLocation(), world,
              structure.getBluePrint(), workOrder.getRotation(world), workOrder.isMirrored());
            removeConstructionTape(corners, world);
        }
    }

    /**
     * Remove construction tape.
     *
     * @param orgCorners the corner positions.
     * @param world   the world.
     */
    public static void removeConstructionTape(final Tuple<BlockPos, BlockPos> orgCorners, @NotNull final World world)
    {
        final Tuple<BlockPos, BlockPos> corners = new Tuple<>(orgCorners.getA().offset(-1, 0, -1), orgCorners.getB().offset(1, 0, 1));

        final int x1 = corners.getA().getX();
        final int x3 = corners.getB().getX();
        final int z1 = corners.getA().getZ();
        final int z3 = corners.getB().getZ();

        final int minHeight = Math.min(corners.getB().getY(), corners.getA().getY()) - 5;
        final int maxHeight = Math.max(corners.getB().getY(), corners.getA().getY()) + 1;

        if (x1 < x3)
        {
            for (int i = x1; i <= x3; i++)
            {
                final BlockPos block1 = new BlockPos(i, 0, z1);
                final BlockPos block2 = new BlockPos(i, 0, z3);
                removeTapeIfNecessary(world, block1, ModBlocks.blockConstructionTape, minHeight, maxHeight);
                removeTapeIfNecessary(world, block2, ModBlocks.blockConstructionTape, minHeight, maxHeight);
            }
        }
        else
        {
            for (int i = x3; i <= x1; i++)
            {
                final BlockPos block1 = new BlockPos(i, 0, z1);
                final BlockPos block2 = new BlockPos(i, 0, z3);
                removeTapeIfNecessary(world, block1, ModBlocks.blockConstructionTape, minHeight, maxHeight);
                removeTapeIfNecessary(world, block2, ModBlocks.blockConstructionTape, minHeight, maxHeight);
            }
        }
        if (z1 < z3)
        {
            for (int i = z1; i <= z3; i++)
            {
                final BlockPos block1 = new BlockPos(x1, 0, i);
                final BlockPos block2 = new BlockPos(x3, 0, i);
                removeTapeIfNecessary(world, block1, ModBlocks.blockConstructionTape, minHeight, maxHeight);
                removeTapeIfNecessary(world, block2, ModBlocks.blockConstructionTape, minHeight, maxHeight);
            }
        }
        else
        {
            for (int i = z3; i <= z1; i++)
            {
                final BlockPos block1 = new BlockPos(x1, 0, i);
                final BlockPos block2 = new BlockPos(x3, 0, i);
                removeTapeIfNecessary(world, block1, ModBlocks.blockConstructionTape, minHeight, maxHeight);
                removeTapeIfNecessary(world, block2, ModBlocks.blockConstructionTape, minHeight, maxHeight);
            }
        }

        final BlockPos corner1 = new BlockPos(x1, 0, z1);
        final BlockPos corner2 = new BlockPos(x1, 0, z3);
        final BlockPos corner3 = new BlockPos(x3, 0, z1);
        final BlockPos corner4 = new BlockPos(x3, 0, z3);
        removeTapeIfNecessary(world, corner1, ModBlocks.blockConstructionTape, minHeight, maxHeight);
        removeTapeIfNecessary(world, corner2, ModBlocks.blockConstructionTape, minHeight, maxHeight);
        removeTapeIfNecessary(world, corner3, ModBlocks.blockConstructionTape, minHeight, maxHeight);
        removeTapeIfNecessary(world, corner4, ModBlocks.blockConstructionTape, minHeight, maxHeight);
    }

    /**
     * @param world            the world.
     * @param block            the block.
     * @param tapeOrTapeCorner Is the checked block supposed to be ConstructionTape or ConstructionTapeCorner.
     */
    public static void removeTapeIfNecessary(@NotNull final World world, @NotNull final BlockPos block, @NotNull final Block tapeOrTapeCorner, final int minHeight, final int maxHeight)
    {
        for (int y = minHeight; y <= maxHeight; y++)
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
