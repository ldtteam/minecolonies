package com.minecolonies.api.util;

import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.core.entity.pathfinding.PathfindingUtils;
import com.minecolonies.core.entity.pathfinding.SurfaceType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.BlockPosUtil.HORIZONTAL_DIRS;
import static net.minecraft.world.entity.EntitySelector.NO_SPECTATORS;

/**
 * Entity related utilities.
 */
public final class EntityUtils
{
    /**
     * How many blocks the citizen needs to stand safe.
     */
    private static final int AIR_SPACE_ABOVE_TO_CHECK = 2;

    /**
     * Default range for moving to something until we stop.
     */
    private static final int    DEFAULT_MOVE_RANGE  = 3;
    private static final int    TELEPORT_RANGE      = 512;
    private static final double MIDDLE_BLOCK_OFFSET = 0.5D;
    private static final int    SCAN_RADIUS         = 5;

    /**
     * Private constructor to hide the implicit public one.
     */
    private EntityUtils()
    {

    }

    /**
     * Checks if a player is a fakePlayer and tries to get the owning player if possible.
     *
     * @param player the incoming player.
     * @param world  the world.
     * @return the PlayerEntity owner in the best case.
     */
    @NotNull
    public static Player getPlayerOfFakePlayer(@NotNull final Player player, @NotNull final Level world)
    {
        if (player instanceof FakePlayer)
        {
            final Player tempPlayer = world.getPlayerByUUID(player.getUUID());
            if (tempPlayer != null)
            {
                return tempPlayer;
            }
        }
        return player;
    }

    /**
     * Returns the loaded Entity with the given UUID.
     *
     * @param world world the entity is in
     * @param id    the entity's UUID
     * @return the Entity
     */
    public static Entity getPlayerByUUID(@NotNull final Level world, @NotNull final UUID id)
    {
        return world.getPlayerByUUID(id);
    }

    /**
     * Returns a list of loaded entities whose id's match the ones provided.
     *
     * @param world the world the entities are in.
     * @param ids   List of Entity id's
     * @return list of Entity's
     */
    public static List<Entity> getEntitiesFromID(@NotNull final Level world, @NotNull final List<Integer> ids)
    {
        return ids.stream()
                 .map(world::getEntity)
                 .collect(Collectors.toList());
    }

    /**
     * Returns the new rotation degree calculated from the current and intended rotation up to a max.
     *
     * @param currentRotation  the current rotation the citizen has.
     * @param intendedRotation the wanted rotation he should have after applying this.
     * @param maxIncrement     the 'movement speed.
     * @return a rotation value he should move.
     */
    public static double updateRotation(final double currentRotation, final double intendedRotation, final double maxIncrement)
    {
        double wrappedAngle = Mth.wrapDegrees(intendedRotation - currentRotation);

        if (wrappedAngle > maxIncrement)
        {
            wrappedAngle = maxIncrement;
        }

        if (wrappedAngle < -maxIncrement)
        {
            wrappedAngle = -maxIncrement;
        }

        return currentRotation + wrappedAngle;
    }

    /**
     * Check for free space AIR_SPACE_ABOVE_TO_CHECK blocks high.
     * <p>
     * And ensure a solid ground
     *
     * @param world          the world to look in
     * @param groundPosition the position to maybe stand on
     * @return true if a suitable Place to walk to
     */
    public static boolean checkForFreeSpace(@NotNull final Level world, @NotNull final BlockPos groundPosition)
    {
        for (int i = 1; i < AIR_SPACE_ABOVE_TO_CHECK; i++)
        {
            if (solidOrLiquid(world, groundPosition.above(i)) || world.getBlockState(groundPosition.above(i)).is(BlockTags.LEAVES))
            {
                return false;
            }
        }

        return BlockUtils.isAnySolid(world.getBlockState(groundPosition));
    }

    /**
     * Checks if a blockPos in a world is solid or liquid.
     * <p>
     * Useful to find a suitable Place to stand. (avoid these blocks to find one)
     *
     * @param world    the world to look in
     * @param blockPos the blocks position
     * @return true if solid or liquid
     */
    public static boolean solidOrLiquid(@NotNull final Level world, @NotNull final BlockPos blockPos)
    {
        final BlockState state = world.getBlockState(blockPos);
        return state.liquid() || BlockUtils.isAnySolid(state);
    }

    /**
     * Get a safe spawnpoint near a location.
     *
     * @param world     the world he should spawn in.
     * @param nearPoint the point to search near.
     * @return The spawn position.
     */
    @Nullable
    public static BlockPos getSpawnPoint(final Level world, final BlockPos nearPoint)
    {
        return BlockPosUtil.findAround(world, nearPoint, SCAN_RADIUS, SCAN_RADIUS, (w, p) -> {
            if (checkValidSpawn(w, p, 2))
            {
                // Also find a valid neighbouring space, to decrease stuck chance
                for (final Direction dir : HORIZONTAL_DIRS)
                {
                    if (checkValidSpawn(w, p.relative(dir, 1), 2))
                    {
                        return true;
                    }
                }
            }

            return false;
        });
    }


    /**
     * Checks if the blocks above that point are all of the specified block types and that there is a solid block to stand on.
     *
     * @param world the world we check on.
     * @param pos the position.
     * @param height the number of blocks above to check.
     * @return true if all blocks are of that type.
     */
    private static boolean checkValidSpawn(@NotNull final BlockGetter world, final BlockPos pos, final int height)
    {
        for (int dy = 0; dy < height; dy++)
        {
            final BlockState state = world.getBlockState(pos.above(dy));
            if (!state.is(ModTags.validSpawn) && (PathfindingUtils.isLiquid(state) || ShapeUtil.hasCollision(world, pos.above(dy), state)))
            {
                return false;
            }
        }

        return SurfaceType.getSurfaceType(world, world.getBlockState(pos.below()), pos.below()) == SurfaceType.WALKABLE
         || SurfaceType.getSurfaceType(world, world.getBlockState(pos.below(2)), pos.below(2)) == SurfaceType.WALKABLE;
    }


    /**
     * Sets the movement of the entity to specific point. Returns true if direction is set, otherwise false.
     *
     * @param living Entity to move
     * @param x      x-coordinate
     * @param y      y-coordinate
     * @param z      z-coordinate
     * @return True if the path is set to destination, otherwise false
     */
    public static boolean tryMoveLivingToXYZ(@NotNull final Mob living, final int x, final int y, final int z)
    {
        return tryMoveLivingToXYZ(living, x, y, z, 1.0D);
    }

    /**
     * Sets the movement of the entity to specific point. Returns true if direction is set, otherwise false.
     *
     * @param living Entity to move
     * @param x      x-coordinate
     * @param y      y-coordinate
     * @param z      z-coordinate
     * @param speedFactor  Speedfactor to modify base speed with
     * @return True if the path is set to destination, otherwise false
     */
    public static boolean tryMoveLivingToXYZ(@NotNull final Mob living, final int x, final int y, final int z, final double speedFactor)
    {
        return living.getNavigation().moveTo(x, y, z, speedFactor);
    }

    /**
     * {@link #isLivingAtSiteWithMove(LivingEntity, int, int, int)}
     *
     * @param entity entity to check
     * @param x      X-coordinate
     * @param y      Y-coordinate
     * @param z      Z-coordinate
     * @return True if entity is at site, otherwise false
     */
    public static boolean isLivingAtSiteWithMove(@NotNull final LivingEntity entity, final int x, final int y, final int z)
    {
        //Default range of 3 works better
        //Range of 2 get some entitys stuck
        return isLivingAtSiteWithMove(entity, x, y, z, DEFAULT_MOVE_RANGE);
    }

    /**
     * Checks if a entity is at his working site. If he isn't, sets it's path to the location.
     *
     * @param entity entity to check
     * @param x      X-coordinate
     * @param y      Y-coordinate
     * @param z      Z-coordinate
     * @param range  Range to check in
     * @return True if entity is at site, otherwise false.
     */
    public static boolean isLivingAtSiteWithMove(@NotNull final LivingEntity entity, final int x, final int y, final int z, final int range)
    {
        if (x == 0 && y == 0 && z == 0)
        {
            return false;
        }

        if (!isLivingAtSite(entity, x, y, z, TELEPORT_RANGE))
        {
            BlockPos spawnPoint = getSpawnPoint(entity.getCommandSenderWorld(), new BlockPos(x,y,z));

            if (spawnPoint == null)
            {
                spawnPoint = new BlockPos(x, y, z);
            }

            entity.moveTo(
              spawnPoint.getX() + MIDDLE_BLOCK_OFFSET,
              spawnPoint.getY(),
              spawnPoint.getZ() + MIDDLE_BLOCK_OFFSET,
              entity.getYRot(),
              entity.getXRot());
            return true;
        }

        return EntityUtils.isLivingAtSite(entity, x, y, z, range);
    }

    /**
     * Checks if a certain entity is in the world at a certain position already.
     *
     * @param entity the entity.
     * @param world  the world.
     * @param placer the entity to get the itemstacks from to check.
     * @return true if there.
     */
    public static boolean isEntityAtPosition(final Entity entity, final Level world, final Entity placer)
    {
        final List<ItemStorage> existingReq = ItemStackUtils.getListOfStackForEntity(entity, placer);
        final BlockPos pos = BlockPos.containing(entity.getX(), entity.getY(), entity.getZ());
        return world.getEntitiesOfClass(Entity.class, AABB.encapsulatingFullBlocks(pos.offset(1, 1, 1), pos.offset(-1, -1, -1)))
                 .stream()
                 .anyMatch(ent -> ent.getX() == entity.getX() && ent.getY() == entity.getY() && ent.getZ() == entity.getZ() && ItemStackUtils.getListOfStackForEntity(entity, placer)
                                                                                                                     .equals(existingReq));
    }

    public static boolean isEntityAtPosition(final Entity entity, final Level world, final AbstractEntityCitizen entityCitizen)
    {
        if (entity != null)
        {
            return EntityUtils.isEntityAtPosition(entity, world, (Entity) entityCitizen);
        }

        return false;
    }

    /**
     * Returns whether or not the entity is within a specific range of his working site.
     *
     * @param entityLiving entity to check
     * @param x            X-coordinate
     * @param y            Y-coordinate
     * @param z            Z-coordinate
     * @param range        Range to check in
     * @return True if entity is at site, otherwise false
     */
    public static boolean isLivingAtSite(@NotNull final LivingEntity entityLiving, final int x, final int y, final int z, final int range)
    {
        final BlockPos pos = BlockPos.containing(entityLiving.getX(), entityLiving.getY(), entityLiving.getZ());
        return BlockPosUtil.distSqr(pos, x, y, z) < MathUtils.square(range);
    }

    /**
     * Checks if the target is flying
     *
     * @param target entity to check
     * @return true if flying or falling deeper
     */
    public static boolean isFlying(final LivingEntity target)
    {
        return target != null && (target.hasImpulse || !target.onGround()) && target.fallDistance <= 0.1f && target.level().isEmptyBlock(target.blockPosition().below(2));
    }

    /**
     * Entity pushable by predicate. Cheaper version compared to mojank.
     * @return The predicate.
     */
    public static Predicate<Entity> pushableBy()
    {
        return NO_SPECTATORS.and((localEntity) -> {
            if (!localEntity.isPushable())
            {
                return false;
            }
            else
            {
                return !localEntity.level().isClientSide || localEntity instanceof Player && ((Player) localEntity).isLocalPlayer();
            }
        });
    }
}

