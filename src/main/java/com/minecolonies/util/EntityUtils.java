package com.minecolonies.util;

import com.minecolonies.entity.EntityCitizen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.Vec3i;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Entity related utilities
 */
public final class EntityUtils
{
    /**
     * Default range for moving to something until we stop
     */
    private static final int DEFAULT_MOVE_RANGE = 3;

    /**
     * Private constructor to hide the implicit public one
     */
    private EntityUtils()
    {
    }

    /**
     * @param worker Worker to check
     * @param x      X-coordinate
     * @param y      Y-coordinate
     * @param z      Z-coordinate
     * @return True if worker is at site, otherwise false
     * @see {@link #isWorkerAtSiteWithMove(EntityCitizen, int, int, int, int)}
     * Default:
     * range: 3
     */
    public static boolean isWorkerAtSiteWithMove(EntityCitizen worker, int x, int y, int z)
    {
        //Default range of 3 works better
        //Range of 2 get some workers stuck
        return isWorkerAtSiteWithMove(worker, x, y, z, DEFAULT_MOVE_RANGE);
    }

    /**
     * Checks if a worker is at his working site.
     * If he isn't, sets it's path to the location
     *
     * @param worker Worker to check
     * @param x      X-coordinate
     * @param y      Y-coordinate
     * @param z      Z-coordinate
     * @param range  Range to check in
     * @return True if worker is at site, otherwise false.
     */
    public static boolean isWorkerAtSiteWithMove(EntityCitizen worker, int x, int y, int z, int range)
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
     * Returns whether or not the worker is within a specific range of his working site
     *
     * @param worker Worker to check
     * @param x      X-coordinate
     * @param y      Y-coordinate
     * @param z      Z-coordinate
     * @param range  Range to check in
     * @return True if worker is at site, otherwise false
     */
    public static boolean isWorkerAtSite(EntityCitizen worker, int x, int y, int z, int range)
    {
    	return worker.getPosition().distanceSq(new Vec3i(x, y, z)) < MathUtils.square(range);
    }

    /**
     * Sets the movement of the entity to specific point.
     * Returns true if direction is set, otherwise false
     *
     * @param living Entity to move
     * @param x      x-coordinate
     * @param y      y-coordinate
     * @param z      z-coordinate
     * @return True if the path is set to destination, otherwise false
     * @see {@link #tryMoveLivingToXYZ(EntityLiving, int, int, int, double)}
     * Default:
     * speed: 1.0D
     */
    public static boolean tryMoveLivingToXYZ(EntityLiving living, int x, int y, int z)
    {
        return tryMoveLivingToXYZ(living, x, y, z, 1.0D);
    }

    /**
     * Sets the movement of the entity to specific point.
     * Returns true if direction is set, otherwise false
     *
     * @param living Entity to move
     * @param x      x-coordinate
     * @param y      y-coordinate
     * @param z      z-coordinate
     * @param speed  Speed to move with
     * @return True if the path is set to destination, otherwise false
     */
    public static boolean tryMoveLivingToXYZ(EntityLiving living, int x, int y, int z, double speed)
    {
        return living.getNavigator().tryMoveToXYZ(x, y, z, speed);
    }

    /**
     * Returns the online EntityPlayer with the given UUID
     *
     * @param world world the player is in
     * @param id    the player's UUID
     * @return the Player
     */
    public static EntityPlayer getPlayerFromUUID(World world, UUID id)
    {
        for (int i = 0; i < world.playerEntities.size(); ++i)
        {
            if (id.equals(((EntityPlayer) world.playerEntities.get(i)).getGameProfile().getId()))
            {
                return (EntityPlayer) world.playerEntities.get(i);
            }
        }
        return null;
    }

    /**
     * Returns the loaded Entity with the given UUID
     *
     * @param world world the entity is in
     * @param id    the entity's UUID
     * @return the Entity
     */
    public static Entity getEntityFromUUID(World world, UUID id)
    {
        for (int i = 0; i < world.loadedEntityList.size(); ++i)
        {
            if (id.equals(((Entity) world.loadedEntityList.get(i)).getUniqueID()))
            {
                return (Entity) world.loadedEntityList.get(i);
            }
        }
        return null;
    }

    /**
     * Returns a list of online players whose UUID's match the ones provided.
     *
     * @param world the world the players are in.
     * @param ids   List of UUIDs
     * @return list of EntityPlayers
     */
    public static List<EntityPlayer> getPlayersFromUUID(World world, Collection<UUID> ids)
    {
        List<EntityPlayer> players = new ArrayList<>();

        for (Object o : world.playerEntities)
        {
            if (o instanceof EntityPlayer)
            {
                EntityPlayer player = (EntityPlayer) o;
                if (ids.contains(player.getGameProfile().getId()))
                {
                    players.add(player);
                    if (players.size() == ids.size())
                    {
                        return players;
                    }
                }
            }
        }
        return players;
    }

    /**
     * Returns a list of loaded entities whose UUID's match the ones provided.
     *
     * @param world the world the entities are in.
     * @param ids   List of UUIDs
     * @return list of Entity's
     */
    public static List<Entity> getEntitiesFromUUID(World world, Collection<UUID> ids)
    {
        List<Entity> entities = new ArrayList<>();

        for (Object o : world.loadedEntityList)
        {
            if (o instanceof Entity)
            {
                Entity entity = (Entity) o;
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
    public static List<Entity> getEntitiesFromID(World world, List<Integer> ids)
    {
        return ids.stream()
                  .map(world::getEntityByID)
                  .collect(Collectors.toList());
    }

    /**
     * Returns whether or not a citizen is heading to a specific location
     *
     * @param citizen Citizen you want to check
     * @param x       X-coordinate
     * @param z       Z-coordinate
     * @return True if citizen heads to (x, z), otherwise false
     */
    public static boolean isPathingTo(EntityCitizen citizen, int x, int z)
    {
        PathPoint pathpoint = citizen.getNavigator().getPath().getFinalPathPoint();
        return pathpoint != null && pathpoint.xCoord == x && pathpoint.zCoord == z;
    }
}
