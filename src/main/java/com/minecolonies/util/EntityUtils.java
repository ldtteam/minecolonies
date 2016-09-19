package com.minecolonies.util;

import com.minecolonies.entity.EntityCitizen;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

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
     * Default range for moving to something until we stop.
     */
    private static final int DEFAULT_MOVE_RANGE       = 3;
    /**
     * How many blocks the citizen needs to stand safe
     */
    private static final int AIR_SPACE_ABOVE_TO_CHECK = 2;

    /**
     * Private constructor to hide the implicit public one.
     */
    private EntityUtils()
    {
    }

    /**
     * {@link #isWorkerAtSiteWithMove(EntityCitizen, int, int, int, int)}.
     *
     * @param worker Worker to check
     * @param x      X-coordinate
     * @param y      Y-coordinate
     * @param z      Z-coordinate
     * @return True if worker is at site, otherwise false
     */
    public static boolean isWorkerAtSiteWithMove(@NotNull EntityCitizen worker, int x, int y, int z)
    {
        //Default range of 3 works better
        //Range of 2 get some workers stuck
        return isWorkerAtSiteWithMove(worker, x, y, z, DEFAULT_MOVE_RANGE);
    }

    /**
     * Checks if a worker is at his working site.
     * If he isn't, sets it's path to the location.
     *
     * @param worker Worker to check
     * @param x      X-coordinate
     * @param y      Y-coordinate
     * @param z      Z-coordinate
     * @param range  Range to check in
     * @return True if worker is at site, otherwise false.
     */
    public static boolean isWorkerAtSiteWithMove(@NotNull EntityCitizen worker, int x, int y, int z, int range)
    {
        //If too far away
        if (!isWorkerAtSite(worker, x, y, z, range))
        {
            //If not moving the try setting the point where the entity should move to
            if (worker.getNavigator().noPath() && !tryMoveLivingToXYZ(worker, x, y, z))
            {
                worker.setStatus(EntityCitizen.Status.PATHFINDING_ERROR);
            }
            return false;
        }
        return true;
    }

    /**
     * Returns whether or not the worker is within a specific range of his working site.
     *
     * @param worker Worker to check
     * @param x      X-coordinate
     * @param y      Y-coordinate
     * @param z      Z-coordinate
     * @param range  Range to check in
     * @return True if worker is at site, otherwise false
     */
    public static boolean isWorkerAtSite(@NotNull EntityCitizen worker, int x, int y, int z, int range)
    {
        return worker.getPosition().distanceSq(new Vec3i(x, y, z)) < MathUtils.square(range);
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
    public static boolean tryMoveLivingToXYZ(@NotNull EntityLiving living, int x, int y, int z)
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
    public static boolean tryMoveLivingToXYZ(@NotNull EntityLiving living, int x, int y, int z, double speed)
    {
        return living.getNavigator().tryMoveToXYZ(x, y, z, speed);
    }

    /**
     * Returns the loaded Entity with the given UUID.
     *
     * @param world world the entity is in
     * @param id    the entity's UUID
     * @return the Entity
     */
    public static Entity getEntityFromUUID(@NotNull World world, @NotNull UUID id)
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
    public static List<Entity> getEntitiesFromUUID(@NotNull World world, @NotNull Collection<UUID> ids)
    {
        @NotNull List<Entity> entities = new ArrayList<>();

        for (Object o : world.loadedEntityList)
        {
            if (o instanceof Entity)
            {
                @NotNull Entity entity = (Entity) o;
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
    public static List<Entity> getEntitiesFromID(@NotNull World world, @NotNull List<Integer> ids)
    {
        return ids.stream()
                 .map(world::getEntityByID)
                 .collect(Collectors.toList());
    }

    /**
     * Returns whether or not a citizen is heading to a specific location.
     *
     * @param citizen Citizen you want to check
     * @param x       X-coordinate
     * @param z       Z-coordinate
     * @return True if citizen heads to (x, z), otherwise false
     */
    public static boolean isPathingTo(@NotNull EntityCitizen citizen, int x, int z)
    {
        PathPoint pathpoint = citizen.getNavigator().getPath().getFinalPathPoint();
        return pathpoint != null && pathpoint.xCoord == x && pathpoint.zCoord == z;
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
    public static boolean checkForFreeSpace(@NotNull World world, @NotNull BlockPos groundPosition)
    {
        for (int i = 1; i < AIR_SPACE_ABOVE_TO_CHECK; i++)
        {
            if (solidOrLiquid(world, groundPosition.up(i)))
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
    public static boolean solidOrLiquid(@NotNull World world, @NotNull BlockPos blockPos)
    {
        final Material material = world.getBlockState(blockPos).getMaterial();
        return material.isSolid()
                 || material.isLiquid();
    }
}
