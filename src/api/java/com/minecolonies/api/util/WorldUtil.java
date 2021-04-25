package com.minecolonies.api.util;

import com.google.common.collect.Lists;
import com.minecolonies.api.colony.buildings.IBuilding;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.*;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.server.ServerWorld;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.CitizenConstants.NIGHT;
import static com.minecolonies.api.util.constant.CitizenConstants.NOON;

/**
 * Class which has world related util functions like chunk load checks
 */
public class WorldUtil
{
    /**
     * Checks if the block is loaded for block access
     *
     * @param world world to use
     * @param pos   position to check
     * @return true if block is accessible/loaded
     */
    public static boolean isBlockLoaded(final IWorld world, final BlockPos pos)
    {
        return isChunkLoaded(world, pos.getX() >> 4, pos.getZ() >> 4);
    }

    /**
     * Returns whether a chunk is fully loaded
     *
     * @param world world to check on
     * @param x     chunk position
     * @param z     chunk position
     * @return true if loaded
     */
    public static boolean isChunkLoaded(final IWorld world, final int x, final int z)
    {
        return world.getChunk(x, z, ChunkStatus.FULL, false) != null;
    }

    /**
     * Mark a chunk at a position dirty if loaded.
     *
     * @param world the world to mark it dirty in.
     * @param pos   the position within the chunk.
     */
    public static void markChunkDirty(final World world, final BlockPos pos)
    {
        if (WorldUtil.isBlockLoaded(world, pos))
        {
            world.getChunk(pos.getX() >> 4, pos.getZ() >> 4).markDirty();
            final BlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    /**
     * Returns whether a chunk is fully loaded
     *
     * @param world world to check on
     * @param pos   chunk position
     * @return true if loaded
     */
    public static boolean isChunkLoaded(final IWorld world, final ChunkPos pos)
    {
        return isChunkLoaded(world, pos.x, pos.z);
    }

    /**
     * Checks if the block is loaded for ticking entities(not all chunks tick entities)
     *
     * @param world world to use
     * @param pos   position to check
     * @return true if block is accessible/loaded
     */
    public static boolean isEntityBlockLoaded(final IWorld world, final BlockPos pos)
    {
        return isEntityChunkLoaded(world, pos.getX() >> 4, pos.getZ() >> 4);
    }

    /**
     * Returns whether an entity ticking chunk is loaded at the position
     *
     * @param world world to check on
     * @param x     chunk position
     * @param z     chunk position
     * @return true if loaded
     */
    public static boolean isEntityChunkLoaded(final IWorld world, final int x, final int z)
    {
        return isEntityChunkLoaded(world, new ChunkPos(x, z));
    }

    /**
     * Returns whether an entity ticking chunk is loaded at the position
     *
     * @param world world to check on
     * @param pos   chunk position
     * @return true if loaded
     */
    public static boolean isEntityChunkLoaded(final IWorld world, final ChunkPos pos)
    {
        return world.getChunkProvider().isChunkLoaded(pos);
    }

    /**
     * Returns whether an axis aligned bb is entirely loaded.
     *
     * @param world world to check on.
     * @param box   the box.
     * @return true if loaded.
     */
    public static boolean isAABBLoaded(final World world, final AxisAlignedBB box)
    {
        return isChunkLoaded(world, ((int) box.minX) >> 4, ((int) box.minZ) >> 4) && isChunkLoaded(world, ((int) box.maxX) >> 4, ((int) box.maxZ) >> 4);
    }

    /**
     * Check if it's currently day inn the world.
     *
     * @param world the world to check.
     * @return true if so.
     */
    public static boolean isDayTime(final World world)
    {
        return world.getDayTime() % 24000 <= NIGHT;
    }

    /**
     * Check if it's currently day inn the world.
     *
     * @param world the world to check.
     * @return true if so.
     */
    public static boolean isPastTime(final World world, final int pastTime)
    {
        return world.getDayTime() % 24000 <= pastTime;
    }

    /**
     * Check if it's currently afternoon the world.
     *
     * @param world the world to check.
     * @return true if so.
     */
    public static boolean isPastNoon(final World world)
    {
        return isPastTime(world, NOON);
    }

    /**
     * Check if a world is of the overworld type.
     *
     * @param world the world to check.
     * @return true if so.
     */
    public static boolean isOverworldType(@NotNull final World world)
    {
        return isOfWorldType(world, DimensionType.OVERWORLD);
    }

    /**
     * Check if a world is of the nether type.
     *
     * @param world the world to check.
     * @return true if so.
     */
    public static boolean isNetherType(@NotNull final World world)
    {
        return isOfWorldType(world, DimensionType.THE_NETHER);
    }

    /**
     * Check if a world has a specific dimension type.
     *
     * @param world the world to check.
     * @param type  the type to compare.
     * @return true if it matches.
     */
    public static boolean isOfWorldType(@NotNull final World world, @NotNull final RegistryKey<DimensionType> type)
    {
        DynamicRegistries dynRegistries = world.func_241828_r();
        ResourceLocation loc = dynRegistries.func_230520_a_().getKey(world.getDimensionType());
        if (loc == null)
        {
            if (world.isRemote)
            {
                //todo Remove this line once forge fixes this.
                return world.getDimensionType().getEffects().equals(type.getLocation());
            }
            return false;
        }
        RegistryKey<DimensionType> regKey = RegistryKey.getOrCreateKey(Registry.DIMENSION_TYPE_KEY, loc);
        return regKey == type;
    }

    /**
     * Check to see if the world is peaceful.
     * <p>
     * There are several checks performed here, currently both gamerule and difficulty.
     *
     * @param world world to check
     * @return true if peaceful
     */
    public static boolean isPeaceful(@NotNull final World world)
    {
        return !world.getWorldInfo().getGameRulesInstance().getBoolean(GameRules.DO_MOB_SPAWNING) || world.getDifficulty().equals(Difficulty.PEACEFUL);
    }

    /**
     * Custom set block state, with 1 instead of default flag 3, to skip vanilla's path notify upon block change, making setBlockState expensive. The state change still affects
     * neighbours and is synced
     *
     * @param world world to use
     * @param pos   position to set
     * @param state state to set
     */
    public static boolean setBlockState(final IWorld world, final BlockPos pos, final BlockState state)
    {
        if (world.isRemote())
        {
            return world.setBlockState(pos, state, 3);
        }

        return setBlockState(world, pos, state, 3);
    }

    /**
     * Custom set block state, skips vanilla's path notify upon block change, making setBlockState expensive.
     *
     * @param world world to use
     * @param pos   position to set
     * @param state state to set
     * @param flags flags to use
     */
    public static boolean setBlockState(final IWorld world, final BlockPos pos, final BlockState state, int flags)
    {
        if (world.isRemote() || !(world instanceof ServerWorld))
        {
            return world.setBlockState(pos, state, flags);
        }

        if ((flags & 2) != 0)
        {
            final Set<PathNavigator> navigators = ((ServerWorld) world).navigations;
            ((ServerWorld) world).navigations = Collections.emptySet();
            final boolean result = world.setBlockState(pos, state, flags);
            ((ServerWorld) world).navigations = navigators;
            return result;
        }
        else
        {
            return world.setBlockState(pos, state, flags);
        }
    }

    /**
     * See World#removeBLock
     *
     * @param world    world to remove a block
     * @param pos      position the block is removed at
     * @param isMoving moving flag
     * @return true if success
     */
    public static boolean removeBlock(final IWorld world, BlockPos pos, boolean isMoving)
    {
        final FluidState fluidstate = world.getFluidState(pos);
        return setBlockState(world, pos, fluidstate.getBlockState(), 3 | (isMoving ? 64 : 0));
    }

    /**
     * Get all entities within a building.
     *
     * @param world     the world to check this for.
     * @param clazz     the entity class.
     * @param building  the building to check the range for.
     * @param predicate the predicate to check
     * @param <T>       the type of the predicate.
     * @return a list of all within those borders.
     */
    public static <T extends Entity> List<T> getEntitiesWithinBuilding(
      final @NotNull World world,
      final @NotNull Class<? extends T> clazz,
      final @NotNull IBuilding building,
      @Nullable final Predicate<? super T> predicate)
    {
        final Tuple<BlockPos, BlockPos> corners = building.getCorners();

        int minX = corners.getA().getX() >> 4;
        int maxX = corners.getB().getX() >> 4;
        int minZ = corners.getA().getZ() >> 4;
        int maxZ = corners.getB().getZ() >> 4;
        int minY = corners.getA().getY() >> 4;
        int maxY = corners.getB().getY() >> 4;

        List<T> list = Lists.newArrayList();
        AbstractChunkProvider abstractchunkprovider = world.getChunkProvider();

        for (int x = minX; x <= maxX; ++x)
        {
            for (int z = minZ; z <= maxZ; ++z)
            {
                if (isEntityChunkLoaded(world, x, z))
                {
                    Chunk chunk = abstractchunkprovider.getChunkNow(x, z);
                    if (chunk != null)
                    {
                        for (int y = minY; y <= maxY; y++)
                        {
                            for (final T entity : chunk.getEntityLists()[y].getByClass(clazz))
                            {
                                if (building.isInBuilding(entity.getPosition()) && (predicate == null || predicate.test(entity)))
                                {
                                    list.add(entity);
                                }
                            }
                        }
                    }
                }
            }
        }

        return list;
    }
}
