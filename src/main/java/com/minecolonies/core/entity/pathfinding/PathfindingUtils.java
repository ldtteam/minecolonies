package com.minecolonies.core.entity.pathfinding;

import com.ldtteam.domumornamentum.block.decorative.PanelBlock;
import com.ldtteam.domumornamentum.block.vanilla.TrapdoorBlock;
import com.minecolonies.api.blocks.huts.AbstractBlockMinecoloniesDefault;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.core.entity.pathfinding.PathingOptions;
import com.minecolonies.core.Network;
import com.minecolonies.core.entity.pathfinding.PathPointExtended;
import com.minecolonies.core.network.messages.client.SyncPathReachedMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.minecolonies.api.util.constant.PathingConstants.HALF_A_BLOCK;

public class PathfindingUtils
{
    /**
     * Empty fluid comparison
     */
    private static Object empty = Fluids.EMPTY.defaultFluidState();

    /**
     * Which citizens are being tracked by which players.
     */
    public static final Map<UUID, UUID> trackingMap = new ConcurrentHashMap<>();

    /**
     * Set the set of reached blocks to the client.
     *
     * @param reached the reached blocks.
     * @param mob     the tracked mob.
     */
    public static void syncDebugReachedPositions(final HashSet<BlockPos> reached, final Mob mob)
    {
        if (reached.isEmpty())
        {
            return;
        }

        for (final Map.Entry<UUID, UUID> entry : trackingMap.entrySet())
        {
            if (entry.getValue().equals(mob.getUUID()))
            {
                final ServerPlayer player = mob.level.getServer().getPlayerList().getPlayer(entry.getKey());
                if (player != null)
                {
                    Network.getNetwork().sendToPlayer(new SyncPathReachedMessage(reached), player);
                }
            }
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

        if (entity.isInWater())
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

        if (state.canOcclude())
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
            fluidState = world.getFluidState(pos);
        }

        if (fluidState == empty || fluidState.isEmpty())
        {
            return false;
        }

        if (state.getBlock() instanceof TrapdoorBlock
              || state.getBlock() instanceof PanelBlock && !state.getValue(TrapdoorBlock.OPEN) && state.getValue(TrapdoorBlock.HALF) == Half.TOP)
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
        return blockState.is(BlockTags.CLIMBABLE) && ((options != null && options.canClimbNonLadders()) || blockState.getBlock() instanceof LadderBlock);
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
}
