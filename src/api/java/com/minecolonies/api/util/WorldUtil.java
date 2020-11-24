package com.minecolonies.api.util;

import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.CitizenConstants.NIGHT;

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
     * @param world the world to mark it dirty in.
     * @param pos the position within the chunk.
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
     * Check if a world is of the overworld type.
     * @param world the world to check.
     * @return true if so.
     */
    public static boolean isOverworldType(@NotNull final World world)
    {
        return isOfWorldType(world, DimensionType.OVERWORLD);
    }

    /**
     * Check if a world is of the nether type.
     * @param world the world to check.
     * @return true if so.
     */
    public static boolean isNetherType(@NotNull final World world)
    {
        return isOfWorldType(world, DimensionType.THE_NETHER);
    }

    /**
     * Check if a world has a specific dimension type.
     * @param world the world to check.
     * @param type the type to compare.
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
}
