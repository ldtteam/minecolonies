package com.minecolonies.core.entity.pathfinding;

import com.ldtteam.domumornamentum.block.decorative.PanelBlock;
import com.ldtteam.domumornamentum.block.vanilla.TrapdoorBlock;
import com.minecolonies.api.blocks.huts.AbstractBlockMinecoloniesDefault;
import com.minecolonies.api.entity.mobs.drownedpirate.AbstractDrownedEntityPirate;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.ShapeUtil;
import com.minecolonies.core.Network;
import com.minecolonies.core.entity.pathfinding.world.CachingBlockLookup;
import com.minecolonies.core.network.messages.client.SyncPathReachedMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PathfindingUtils
{
    /**
     * Empty fluid comparison
     */
    private static Object empty = Fluids.EMPTY.defaultFluidState();

    /**
     * Which citizens are being tracked by which players. Player to entity uuid
     */
    public static final Map<UUID, UUID> trackingMap = new ConcurrentHashMap<>();

    /**
     * Map for tracking specific path types, type to player uuid
     */
    public static final Map<String, UUID> trackByType = new HashMap<>();

    /**
     * Set the set of reached blocks to the client.
     *
     * @param reached the reached blocks.
     * @param mob     the tracked mob.
     */
    public static void syncDebugReachedPositions(final HashSet<BlockPos> reached, final List<ServerPlayer> players)
    {
        if (reached.isEmpty() || players.isEmpty())
        {
            return;
        }

        final SyncPathReachedMessage message = new SyncPathReachedMessage(reached);

        for (final ServerPlayer player : players)
        {
            Network.getNetwork().sendToPlayer(message, player);
        }
    }

    /**
     * Generates a good path starting location for the entity to path from, correcting for the following conditions. - Being in water: pathfinding in water occurs along the
     * surface; adjusts position to surface. - Being in a fence space: finds correct adjacent position which is not a fence space, to prevent starting path. from within the fence
     * block.
     *
     * @param entity Entity for the pathfinding operation.
     * @return ChunkCoordinates for starting location.
     */
    public static BlockPos prepareStart(@NotNull final LivingEntity entity)
    {
        @NotNull BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(Mth.floor(entity.getX()),
            Mth.floor(entity.getY()),
            Mth.floor(entity.getZ()));
        final Level level = entity.level;
        BlockState bs = level.getBlockState(pos);
        // 1 Up when we're standing within this collision shape
        final VoxelShape collisionShape = bs.getCollisionShape(level, pos);
        final boolean isFineToStandIn = canStandInSolidBlock(bs);
        if (bs.blocksMotion() && !isFineToStandIn && collisionShape.max(Direction.Axis.Y) > 0)
        {
            final double relPosX = Math.abs(entity.getX() % 1);
            final double relPosZ = Math.abs(entity.getZ() % 1);

            for (final AABB box : collisionShape.toAabbs())
            {
                if (relPosX >= box.minX && relPosX <= box.maxX
                    && relPosZ >= box.minZ && relPosZ <= box.maxZ
                    && box.maxY > 0)
                {
                    pos.set(pos.getX(), pos.getY() + 1, pos.getZ());
                    bs = level.getBlockState(pos);
                    break;
                }
            }
        }

        BlockState down = level.getBlockState(pos.below());
        while (canStandInSolidBlock(bs) && canStandInSolidBlock(down) && !down.getBlock().isLadder(down, level, pos.below(), entity) && down.getFluidState().isEmpty())
        {
            pos.move(Direction.DOWN, 1);
            bs = down;
            down = level.getBlockState(pos.below());

            if (pos.getY() < entity.getCommandSenderWorld().getMinBuildHeight())
            {
                return entity.blockPosition();
            }
        }

        final Block b = bs.getBlock();

        if (entity.isInWater() && !(entity instanceof AbstractDrownedEntityPirate))
        {
            while (!bs.getFluidState().isEmpty())
            {
                pos.set(pos.getX(), pos.getY() + 1, pos.getZ());
                bs = level.getBlockState(pos);
            }
        }
        else if (b instanceof FenceBlock || b instanceof WallBlock || b instanceof AbstractBlockMinecoloniesDefault || (bs.blocksMotion() && !canStandInSolidBlock(bs)))
        {
            final VoxelShape shape = bs.getCollisionShape(level, pos);
            if (shape.isEmpty())
            {
                return pos.immutable();
            }

            final Vec3 relativePos = entity.position().subtract(shape.move(entity.getBlockX(), entity.getBlockY(), entity.getBlockZ()).bounds().getCenter());

            //Push away from fence
            final double dX = relativePos.x;
            final double dZ = relativePos.z;

            if (Math.abs(dX) < Math.abs(dZ))
            {
                pos.set(pos.getX(), pos.getY(), dZ < 0 ? pos.getZ() - 1 : pos.getZ() + 1);
            }
            else
            {
                pos.set(dX < 0 ? pos.getX() - 1 : pos.getX() + 1, pos.getY(), pos.getZ());
            }
        }

        return pos.immutable();
    }

    /**
     * Check if this a valid state to stand in.
     *
     * @param state the state to check.
     * @return true if so.
     */
    private static boolean canStandInSolidBlock(final BlockState state)
    {
        return state.getBlock() instanceof DoorBlock || state.getBlock() instanceof TrapDoorBlock || (state.getBlock() instanceof PanelBlock && state.getValue(PanelBlock.OPEN))
            || !state.getBlock().properties.hasCollision;
    }

    /**
     * Sets the direction where the ladder is facing.
     *
     * @param world the world in.
     * @param pos   the position.
     * @param p     the path.
     */
    public static void setLadderFacing(@NotNull final LevelReader world, final BlockPos pos, @NotNull final PathPointExtended p)
    {
        final BlockState state = world.getBlockState(pos);
        final Block block = state.getBlock();
        if (block instanceof VineBlock)
        {
            if (state.getValue(VineBlock.SOUTH))
            {
                p.setLadderFacing(Direction.NORTH);
            }
            else if (state.getValue(VineBlock.WEST))
            {
                p.setLadderFacing(Direction.EAST);
            }
            else if (state.getValue(VineBlock.NORTH))
            {
                p.setLadderFacing(Direction.SOUTH);
            }
            else if (state.getValue(VineBlock.EAST))
            {
                p.setLadderFacing(Direction.WEST);
            }
        }
        else if (block instanceof LadderBlock)
        {
            p.setLadderFacing(state.getValue(LadderBlock.FACING));
        }
        else
        {
            p.setLadderFacing(Direction.UP);
        }
    }

    /**
     * Check if this is a liquid state for swimming.
     *
     * @param state the state to check.
     * @return true if so.
     */
    public static boolean isLiquid(final BlockState state)
    {
        return state.liquid() || (!state.blocksMotion() && !state.getFluidState().isEmpty());
    }

    /**
     * Check if the block at this position is actually some kind of waterly fluid.
     *
     * @param pos the pos in the world.
     * @return true if so.
     */
    public static boolean isWater(@NotNull final BlockGetter world, final BlockPos pos)
    {
        return isWater(world, pos, null, null);
    }

    /**
     * Check if the block at this position is actually some kind of waterly fluid.
     *
     * @param pos         the pos in the world.
     * @param pState      existing blockstate or null
     * @param pFluidState existing fluidstate or null
     * @return true if so.
     */
    public static boolean isWater(@NotNull final BlockGetter world, final BlockPos pos, @Nullable BlockState pState, @Nullable FluidState pFluidState)
    {
        BlockState state = pState;
        if (state == null)
        {
            state = world.getBlockState(pos);
        }

        if (state.isSolid())
        {
            return false;
        }
        if (state.getBlock() == Blocks.WATER)
        {
            return true;
        }

        FluidState fluidState = pFluidState;
        if (fluidState == null)
        {
            fluidState = state.getFluidState();
        }

        if (fluidState == empty || fluidState.isEmpty())
        {
            return false;
        }

        if (state.getBlock() instanceof TrapdoorBlock
            || state.getBlock() instanceof PanelBlock && (!state.getValue(TrapdoorBlock.OPEN) && state.getValue(TrapdoorBlock.HALF) == Half.TOP))
        {
            return false;
        }

        final Fluid fluid = fluidState.getType();
        return fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER;
    }

    /**
     * Check if the block at this position is lava.
     *
     * @param pos         the pos in the world.
     * @param pState      existing blockstate or null
     * @param pFluidState existing fluidstate or null
     * @return true if so.
     */
    public static boolean isLava(@NotNull final BlockGetter world, final BlockPos pos, @Nullable BlockState pState, @Nullable FluidState pFluidState)
    {
        BlockState state = pState;
        if (state == null)
        {
            state = world.getBlockState(pos);
        }

        if (state.getBlock() == Blocks.LAVA)
        {
            return true;
        }

        FluidState fluidState = pFluidState;
        if (fluidState == null)
        {
            fluidState = world.getFluidState(pos);
        }

        if (fluidState == empty || fluidState.isEmpty())
        {
            return false;
        }

        final Fluid fluid = fluidState.getType();
        return fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA;
    }

    /**
     * Checks if the given state is a ladder, or if pathing options allow a different type of climbable
     *
     * @param blockState
     * @param options
     * @return
     */
    public static boolean isLadder(final BlockState blockState, @Nullable final PathingOptions options)
    {
        if (options != null && options.canWalkUnderWater() && isLiquid(blockState))
        {
            return true;
        }
        return blockState.is(BlockTags.CLIMBABLE) && ((options != null && options.canClimbAdvanced()) ||
            blockState.getBlock() instanceof LadderBlock ||
            blockState.is(ModTags.freeClimbBlocks));
    }

    /**
     * Is the surface inherently dangerous to stand on/in (i.e. causes damage).
     *
     * @param blockState block to check.
     * @return true if dangerous.
     */
    public static boolean isDangerous(final BlockState blockState)
    {
        final Block block = blockState.getBlock();

        return blockState.is(ModTags.dangerousBlocks) ||
            block instanceof FireBlock ||
            block instanceof CampfireBlock ||
            block instanceof MagmaBlock ||
            block instanceof SweetBerryBushBlock ||
            block instanceof PowderSnowBlock;
    }

    /**
     * Checks for collisions along a line between two given positions
     *
     * @param startX
     * @param startY
     * @param startZ
     * @param endX
     * @param endY
     * @param endZ
     * @param blockLookup
     * @return
     */
    public static boolean hasAnyCollisionAlong(int startX, int startY, int startZ, int endX, int endY, int endZ, CachingBlockLookup blockLookup)
    {
        int x = startX, y = startY, z = startZ;

        int dx = Math.abs(endX - startX);
        int dy = Math.abs(endY - startY);
        int dz = Math.abs(endZ - startZ);

        int stepX = (endX > startX) ? 1 : -1;
        int stepY = (endY > startY) ? 1 : -1;
        int stepZ = (endZ > startZ) ? 1 : -1;

        double stepCostX = 1.0 / dx;
        double stepCostY = 1.0 / dy;
        double stepCostZ = 1.0 / dz;

        // Init with step cost, to determine first step
        double stepCostSumX = (dx == 0) ? Double.POSITIVE_INFINITY : 0.5 / dx;
        double stepCostSumY = (dy == 0) ? Double.POSITIVE_INFINITY : 0.5 / dy;
        double stepCostSumZ = (dz == 0) ? Double.POSITIVE_INFINITY : 0.5 / dz;

        for (int i = 0; i < (dx + dy + dz) && (x != endX || y != endY || z != endZ); i++)
        {
            if (ShapeUtil.hasCollision(blockLookup, x, y, z, blockLookup.getBlockState(x, y, z)))
            {
                return true;
            }

            // Explore multiple possibilities if two options have the same cost, e.g. on quadratic distance to make sure all blocks around get checked:
            if (doubleEquals(stepCostSumX, stepCostSumY))
            {
                if (ShapeUtil.hasCollision(blockLookup, x + stepX, y, z, blockLookup.getBlockState(x + stepX, y, z)))
                {
                    return true;
                }

                if (ShapeUtil.hasCollision(blockLookup, x + stepX, y + stepY, z, blockLookup.getBlockState(x + stepX, y + stepY, z)))
                {
                    return true;
                }
            }

            if (doubleEquals(stepCostSumX, stepCostSumZ))
            {
                if (ShapeUtil.hasCollision(blockLookup, x + stepX, y, z, blockLookup.getBlockState(x + stepX, y, z)))
                {
                    return true;
                }

                if (ShapeUtil.hasCollision(blockLookup, x + stepX, y, z + stepZ, blockLookup.getBlockState(x + stepX, y, z + stepZ)))
                {
                    return true;
                }
            }

            if (doubleEquals(stepCostSumY, stepCostSumZ))
            {
                if (ShapeUtil.hasCollision(blockLookup, x, y + stepY, z, blockLookup.getBlockState(x, y + stepY, z)))
                {
                    return true;
                }

                if (ShapeUtil.hasCollision(blockLookup, x, y + stepY, z + stepZ, blockLookup.getBlockState(x, y + stepY, z + stepZ)))
                {
                    return true;
                }
            }

            if (stepCostSumX < stepCostSumY)
            {
                if (stepCostSumX < stepCostSumZ)
                {
                    x += stepX;
                    stepCostSumX += stepCostX;
                }
                else
                {
                    z += stepZ;
                    stepCostSumZ += stepCostZ;
                }
            }
            else
            {
                if (stepCostSumY < stepCostSumZ)
                {
                    y += stepY;
                    stepCostSumY += stepCostY;
                }
                else
                {
                    z += stepZ;
                    stepCostSumZ += stepCostZ;
                }
            }
        }

        return ShapeUtil.hasCollision(blockLookup, endX, endY, endZ, blockLookup.getBlockState(endX, endY, endZ));
    }

    private static boolean doubleEquals(final double a, final double b)
    {
        if (Double.isFinite(a) || Double.isFinite(b) || Double.isNaN(a) || Double.isNaN(b))
        {
            return false;
        }

        return Math.abs(a - b) < 0.000005;
    }
}
