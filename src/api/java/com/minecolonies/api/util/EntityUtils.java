package com.minecolonies.api.util;

import net.minecraft.block.BlockLeaves;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
     * Checks if a player is a fakePlayer and tries to get the owning player if
     * possible.
     *
     * @param player the incoming player.
     * @param world  the world.
     * @return the EntityPlayer owner in the best case.
     */
    @NotNull
    public static EntityPlayer getPlayerOfFakePlayer(@NotNull final EntityPlayer player, @NotNull final World world)
    {
        if (player instanceof FakePlayer)
        {
            final EntityPlayer tempPlayer = world.getPlayerEntityByUUID(player.getUniqueID());
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
    public static Entity getEntityFromUUID(@NotNull final World world, @NotNull final UUID id)
    {
        for (int i = 0; i < world.loadedEntityList.size(); ++i)
        {
            if (id.equals(world.loadedEntityList.get(i).getUniqueID()))
            {
                return world.loadedEntityList.get(i);
            }
        }
        return null;
    }

    /**
     * Returns a list of loaded entities whose UUID's match the ones provided.
     *
     * @param world the world the entities are in.
     * @param ids   List of UUIDs
     * @return list of Entity's
     */
    @NotNull
    public static List<Entity> getEntitiesFromUUID(@NotNull final World world, @NotNull final Collection<UUID> ids)
    {
        @NotNull final List<Entity> entities = new ArrayList<>();

        for (final Object o : world.loadedEntityList)
        {
            if (o instanceof Entity)
            {
                @NotNull final Entity entity = (Entity) o;
                if (ids.contains(entity.getUniqueID()))
                {
                    entities.add(entity);
                    if (entities.size() == ids.size())
                    {
                        return entities;
                    }
                }
            }
        }
        return entities;
    }

    /**
     * Returns a list of loaded entities whose id's match the ones provided.
     *
     * @param world the world the entities are in.
     * @param ids   List of Entity id's
     * @return list of Entity's
     */
    public static List<Entity> getEntitiesFromID(@NotNull final World world, @NotNull final List<Integer> ids)
    {
        return ids.stream()
                 .map(world::getEntityByID)
                 .collect(Collectors.toList());
    }

    /**
     * Returns the new rotation degree calculated from the current and intended
     * rotation up to a max.
     *
     * @param currentRotation  the current rotation the citizen has.
     * @param intendedRotation the wanted rotation he should have after applying
     *                         this.
     * @param maxIncrement     the 'movement speed.
     * @return a rotation value he should move.
     */
    public static double updateRotation(final double currentRotation, final double intendedRotation, final double maxIncrement)
    {
        double wrappedAngle = MathHelper.wrapDegrees(intendedRotation - currentRotation);

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
    public static boolean checkForFreeSpace(@NotNull final World world, @NotNull final BlockPos groundPosition)
    {
        for (int i = 1; i < AIR_SPACE_ABOVE_TO_CHECK; i++)
        {
            if (solidOrLiquid(world, groundPosition.up(i)) || world.getBlockState(groundPosition.up(i)).getBlock() instanceof BlockLeaves)
            {
                return false;
            }
        }
        return world.getBlockState(groundPosition).getMaterial().isSolid();
    }

    /**
     * Checks if a blockPos in a world is solid or liquid.
     * <p>
     * Useful to find a suitable Place to stand.
     * (avoid these blocks to find one)
     *
     * @param world    the world to look in
     * @param blockPos the blocks position
     * @return true if solid or liquid
     */
    public static boolean solidOrLiquid(@NotNull final World world, @NotNull final BlockPos blockPos)
    {
        final Material material = world.getBlockState(blockPos).getMaterial();
        return material.isSolid()
                 || material.isLiquid();
    }

    /**
     * Get a safe spawnpoint near a location.
     *
     * @param world     the world he should spawn in.
     * @param nearPoint the point to search near.
     * @return The spawn position.
     */
    @Nullable
    public static BlockPos getSpawnPoint(final World world, final BlockPos nearPoint)
    {
        return Utils.scanForBlockNearPoint(
          world,
          nearPoint.down(),
          1,
          1,
          1,
          2,
          Blocks.AIR,
          Blocks.SNOW_LAYER,
          Blocks.TALLGRASS,
          Blocks.RED_FLOWER,
          Blocks.YELLOW_FLOWER,
          Blocks.CARPET);
    }

    /**
     * Sets the movement of the entity to specific point.
     * Returns true if direction is set, otherwise false.
     * {@link #tryMoveLivingToXYZ(EntityLiving, int, int, int, double)}
     *
     * @param living Entity to move
     * @param x      x-coordinate
     * @param y      y-coordinate
     * @param z      z-coordinate
     * @return True if the path is set to destination, otherwise false
     */
    public static boolean tryMoveLivingToXYZ(@NotNull final EntityLiving living, final int x, final int y, final int z)
    {
        return tryMoveLivingToXYZ(living, x, y, z, 1.0D);
    }

    /**
     * Sets the movement of the entity to specific point.
     * Returns true if direction is set, otherwise false.
     *
     * @param living Entity to move
     * @param x      x-coordinate
     * @param y      y-coordinate
     * @param z      z-coordinate
     * @param speed  Speed to move with
     * @return True if the path is set to destination, otherwise false
     */
    public static boolean tryMoveLivingToXYZ(@NotNull final EntityLiving living, final int x, final int y, final int z, final double speed)
    {
        return living.getNavigator().tryMoveToXYZ(x, y, z, speed);
    }

    /**
     * {@link #isLivingAtSiteWithMove(EntityLiving, int, int, int)}
     *
     * @param entity entity to check
     * @param x      X-coordinate
     * @param y      Y-coordinate
     * @param z      Z-coordinate
     * @return True if entity is at site, otherwise false
     */
    public static boolean isLivingAtSiteWithMove(@NotNull final EntityLiving entity, final int x, final int y, final int z)
    {
        //Default range of 3 works better
        //Range of 2 get some entitys stuck
        return isLivingAtSiteWithMove(entity, x, y, z, DEFAULT_MOVE_RANGE);
    }

    /**
     * Checks if a entity is at his working site.
     * If he isn't, sets it's path to the location.
     *
     * @param entity entity to check
     * @param x      X-coordinate
     * @param y      Y-coordinate
     * @param z      Z-coordinate
     * @param range  Range to check in
     * @return True if entity is at site, otherwise false.
     */
    public static boolean isLivingAtSiteWithMove(@NotNull final EntityLiving entity, final int x, final int y, final int z, final int range)
    {
        if (!isLivingAtSite(entity, x, y, z, TELEPORT_RANGE))
        {
            BlockPos spawnPoint =
              Utils.scanForBlockNearPoint(entity.getEntityWorld(),
                new BlockPos(x, y, z),
                SCAN_RADIUS, SCAN_RADIUS, SCAN_RADIUS, 2,
                Blocks.AIR,
                Blocks.SNOW_LAYER,
                Blocks.TALLGRASS,
                Blocks.RED_FLOWER,
                Blocks.YELLOW_FLOWER,
                Blocks.CARPET);

            if (spawnPoint == null)
            {
                spawnPoint = new BlockPos(x,y,z);
            }

            entity.setLocationAndAngles(
              spawnPoint.getX() + MIDDLE_BLOCK_OFFSET,
              spawnPoint.getY(),
              spawnPoint.getZ() + MIDDLE_BLOCK_OFFSET,
              entity.rotationYaw,
              entity.rotationPitch);
            return true;
        }

        return EntityUtils.isLivingAtSite(entity, x, y, z, range);
    }

    /**
     * Returns whether or not the entity is within a specific range of his
     * working site.
     *
     * @param entityLiving entity to check
     * @param x            X-coordinate
     * @param y            Y-coordinate
     * @param z            Z-coordinate
     * @param range        Range to check in
     * @return True if entity is at site, otherwise false
     */
    public static boolean isLivingAtSite(@NotNull final EntityLiving entityLiving, final int x, final int y, final int z, final int range)
    {
        return entityLiving.getPosition().distanceSq(new Vec3i(x, y, z)) < MathUtils.square(range);
    }
}
