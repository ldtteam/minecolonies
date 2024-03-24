package com.minecolonies.api.util;

import com.minecolonies.api.colony.buildings.IBuilding;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
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
    public static boolean isBlockLoaded(final LevelAccessor world, final BlockPos pos)
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
    public static boolean isChunkLoaded(final LevelAccessor world, final int x, final int z)
    {
        if (world.getChunkSource() instanceof ServerChunkCache)
        {
            final ChunkHolder holder = ((ServerChunkCache) world.getChunkSource()).chunkMap.getVisibleChunkIfPresent(ChunkPos.asLong(x, z));
            if (holder != null)
            {
                return holder.getFullChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).left().isPresent();
            }

            return false;
        }
        return world.getChunk(x, z, ChunkStatus.FULL, false) != null;
    }

    /**
     * Mark a chunk at a position dirty if loaded.
     *
     * @param world the world to mark it dirty in.
     * @param pos   the position within the chunk.
     */
    public static void markChunkDirty(final Level world, final BlockPos pos)
    {
        if (WorldUtil.isBlockLoaded(world, pos))
        {
            world.getChunk(pos.getX() >> 4, pos.getZ() >> 4).setUnsaved(true);
            final BlockState state = world.getBlockState(pos);
            world.sendBlockUpdated(pos, state, state, 3);
        }
    }

    /**
     * Returns whether a chunk is fully loaded
     *
     * @param world world to check on
     * @param pos   chunk position
     * @return true if loaded
     */
    public static boolean isChunkLoaded(final LevelAccessor world, final ChunkPos pos)
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
    public static boolean isEntityBlockLoaded(final LevelAccessor world, final BlockPos pos)
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
    public static boolean isEntityChunkLoaded(final LevelAccessor world, final int x, final int z)
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
    public static boolean isEntityChunkLoaded(final LevelAccessor world, final ChunkPos pos)
    {
        if (world instanceof ServerLevel)
        {
            return ((ServerLevel) world).isPositionEntityTicking(pos.getWorldPosition());
        }
        return isChunkLoaded(world, pos);
    }

    /**
     * Returns whether an axis aligned bb is entirely loaded.
     *
     * @param world world to check on.
     * @param box   the box.
     * @return true if loaded.
     */
    public static boolean isAABBLoaded(final Level world, final AABB box)
    {
        return isChunkLoaded(world, ((int) box.minX) >> 4, ((int) box.minZ) >> 4) && isChunkLoaded(world, ((int) box.maxX) >> 4, ((int) box.maxZ) >> 4);
    }

    /**
     * Check if it's currently day inn the world.
     *
     * @param world the world to check.
     * @return true if so.
     */
    public static boolean isDayTime(final Level world)
    {
        return world.getDayTime() % 24000 <= NIGHT;
    }

    /**
     * Check if it's currently day inn the world.
     *
     * @param world the world to check.
     * @return true if so.
     */
    public static boolean isPastTime(final Level world, final int pastTime)
    {
        return world.getDayTime() % 24000 <= pastTime;
    }

    /**
     * Check if it's currently afternoon the world.
     *
     * @param world the world to check.
     * @return true if so.
     */
    public static boolean isPastNoon(final Level world)
    {
        return isPastTime(world, NOON);
    }

    /**
     * Check if a world is of the overworld type.
     *
     * @param world the world to check.
     * @return true if so.
     */
    public static boolean isOverworldType(@NotNull final Level world)
    {
        return isOfWorldType(world, BuiltinDimensionTypes.OVERWORLD);
    }

    /**
     * Check if a world is of the nether type.
     *
     * @param world the world to check.
     * @return true if so.
     */
    public static boolean isNetherType(@NotNull final Level world)
    {
        return isOfWorldType(world, BuiltinDimensionTypes.NETHER);
    }

    /**
     * Check if a world has a specific dimension type.
     *
     * @param world the world to check.
     * @param type  the type to compare.
     * @return true if it matches.
     */
    public static boolean isOfWorldType(@NotNull final Level world, @NotNull final ResourceKey<DimensionType> type)
    {
        RegistryAccess dynRegistries = world.registryAccess();
        ResourceLocation loc = dynRegistries.registry(Registries.DIMENSION_TYPE).get().getKey(world.dimensionType());
        if (loc == null)
        {
            if (world.isClientSide)
            {
                return world.dimensionType().effectsLocation().equals(type.location());
            }
            return false;
        }
        ResourceKey<DimensionType> regKey = ResourceKey.create(Registries.DIMENSION_TYPE, loc);
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
    public static boolean isPeaceful(@NotNull final Level world)
    {
        return !world.getLevelData().getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING) || world.getDifficulty().equals(Difficulty.PEACEFUL);
    }

    /**
     * Custom set block state, with 1 instead of default flag 3, to skip vanilla's path notify upon block change, making setBlockState expensive. The state change still affects
     * neighbours and is synced
     *
     * @param world world to use
     * @param pos   position to set
     * @param state state to set
     */
    public static boolean setBlockState(final LevelAccessor world, final BlockPos pos, final BlockState state)
    {
        if (world.isClientSide())
        {
            return world.setBlock(pos, state, 3);
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
    public static boolean setBlockState(final LevelAccessor world, final BlockPos pos, final BlockState state, int flags)
    {
        if (world.isClientSide() || !(world instanceof ServerLevel serverLevel))
        {
            return world.setBlock(pos, state, flags);
        }

        if ((flags & 2) != 0)
        {
            final Set<Mob> navigators = serverLevel.navigatingMobs;
            serverLevel.navigatingMobs.clear();
            final boolean result = world.setBlock(pos, state, flags);
            serverLevel.navigatingMobs.addAll(navigators);
            return result;
        }
        else
        {
            return world.setBlock(pos, state, flags);
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
    public static boolean removeBlock(final LevelAccessor world, BlockPos pos, boolean isMoving)
    {
        final FluidState fluidstate = world.getFluidState(pos);
        return setBlockState(world, pos, fluidstate.createLegacyBlock(), 3 | (isMoving ? 64 : 0));
    }

    /**
     * Get all entities within a building.
     *
     * @param <T>       the type of the predicate.
     * @param world     the world to check this for.
     * @param clazz     the entity class.
     * @param building  the building to check the range for.
     * @param predicate the predicate to check
     * @return a list of all within those borders.
     */
    public static <T extends Entity> List<? extends T> getEntitiesWithinBuilding(
      final @NotNull Level world,
      final @NotNull Class<? extends T> clazz,
      final @NotNull IBuilding building,
      @Nullable final Predicate<? super T> predicate)
    {
        final Tuple<BlockPos, BlockPos> corners = building.getCorners();

        if (predicate == null)
        {
            return world.getEntitiesOfClass(clazz,
              new AABB(corners.getA().getX(), corners.getA().getY(), corners.getA().getZ(), corners.getB().getX(), corners.getB().getY(), corners.getB().getZ()));
        }
        return world.getEntitiesOfClass(clazz,
          new AABB(corners.getA().getX(), corners.getA().getY(), corners.getA().getZ(), corners.getB().getX(), corners.getB().getY(), corners.getB().getZ()),
          predicate);
    }

    /**
     * Returns a dimensions max height
     *
     * @param dimensionType
     * @return
     */
    public static int getDimensionMaxHeight(final DimensionType dimensionType)
    {
        return dimensionType.logicalHeight() + dimensionType.minY();
    }

    /**
     * Returns a dimension min height
     *
     * @param dimensionType
     * @return
     */
    public static int getDimensionMinHeight(final DimensionType dimensionType)
    {
        return dimensionType.minY();
    }

    /**
     * Check if a given block y is within world bounds
     *
     * @param yBlock
     * @param world
     * @return
     */
    public static boolean isInWorldHeight(final int yBlock, final Level world)
    {
        final DimensionType dimensionType = world.dimensionType();
        return yBlock > getDimensionMinHeight(dimensionType) && yBlock < getDimensionMaxHeight(dimensionType);
    }

    /**
     * Get nearest player, our own cheaper check.
     * @param livingEntity the entity to check.
     * @param x pos x
     * @param y pos y
     * @param z pos z
     * @param lookDistance min distance.
     * @return closest player or null.
     */
    @Nullable
    public static Player getNearestPlayer(Mob livingEntity, final int x, final int y, final int z, final double lookDistance)
    {
        return getNearestEntity(livingEntity.level().players(), livingEntity, x, y, z, lookDistance);
    }

    /**
     * Get the closest entity, cheaper than mojank.
     * @param entityList entity list to check.
     * @param livingEntity the entity they should be close to.
     * @param x pos x
     * @param y pos y
     * @param z pos z
     * @param lookDistance max distance.
     * @return the entity or null.
     * @param <T> type of entity.
     */
    @Nullable
    public static <T extends LivingEntity> T getNearestEntity(List<? extends T> entityList, @Nullable Mob livingEntity, int x, int y, int z, double lookDistance)
    {
        double currentEntityDistance = 100;
        T closestEntity = null;
        final BlockPos entityPos = new BlockPos(x, y, z);

        for (T entity : entityList)
        {
            if (entity == livingEntity)
            {
                continue;
            }
            else if (!entity.canBeSeenByAnyone())
            {
                continue;
            }

            double entityDistance = entity.blockPosition().above().distSqr(entityPos);
            if (entityDistance > lookDistance)
            {
                continue;
            }

            double invisPct = entity.getVisibilityPercent(livingEntity);
            double invisPctModifier = Math.max(lookDistance * invisPct, 2.0D);
            if (entityDistance > invisPctModifier * invisPctModifier)
            {
                continue;
            }

            if (livingEntity instanceof Mob)
            {
                Mob mob = (Mob) livingEntity;
                if (!mob.getSensing().hasLineOfSight(entity))
                {
                    continue;
                }
            }

            if (entityDistance < currentEntityDistance)
            {
                currentEntityDistance = entityDistance;
                closestEntity = entity;
            }
        }

        return closestEntity;
    }
}
