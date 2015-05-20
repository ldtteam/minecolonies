package com.minecolonies.util;

import com.minecolonies.entity.EntityCitizen;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

import java.util.*;

public class Utils
{
    public static ChunkCoordinates scanForBlockNearPoint(World world, Block block, int x, int y, int z, int radiusX, int radiusY, int radiusZ)
    {
        ChunkCoordinates closestCoords = null;
        double minDistance = Double.MAX_VALUE;

        for(int i = x - radiusX; i <= x + radiusX; i++)
        {
            for(int j = y - radiusY; j <= y + radiusY; j++)
            {
                for(int k = z - radiusZ; k <= z + radiusZ; k++)
                {
                    if(world.getBlock(i, j, k) == block)
                    {
                        ChunkCoordinates tempCoords = new ChunkCoordinates(i, j, k);

                        double distance = ChunkCoordUtils.distanceSqrd(tempCoords, x, y, z);
                        if(closestCoords == null || distance < minDistance)
                        {
                            closestCoords = tempCoords;
                            minDistance = distance;
                        }
                    }
                }
            }
        }
        return closestCoords;
    }

    public static boolean isPathingTo(EntityCitizen citizen, int x, int z)
    {
        PathPoint pathpoint = citizen.getNavigator().getPath().getFinalPathPoint();
        return pathpoint != null && pathpoint.xCoord == x && pathpoint.zCoord == z;
    }

    public static boolean isWorkerAtSite(EntityCitizen worker, int x, int y, int z)
    {
        return worker.getPosition().squareDistanceTo(x, y, z) < 4;
    }

    public static boolean isWorkerAtSiteWithMove(EntityCitizen worker, int x, int y, int z)
    {
        if(!isWorkerAtSite(worker, x, y, z))//Too far away
        {
            if(worker.getNavigator().noPath())//Not moving
            {
                if(!tryMoveLivingToXYZ(worker, x, y, z))
                {
                    worker.setStatus(EntityCitizen.Status.PATHFINDING_ERROR);
                }
            }
            return false;
        }
        else
        {
//            if(!worker.getNavigator().noPath())//within 2 blocks - can stop pathing //TODO may not need this check
//            {
//                worker.getNavigator().clearPathEntity();
//            }
            return true;
        }
    }

    public static boolean tryMoveLivingToXYZ(EntityLiving living, int x, int y, int z)
    {
        return tryMoveLivingToXYZ(living, x, y, z, 1.0D);
    }

    public static boolean tryMoveLivingToXYZ(EntityLiving living, int x, int y, int z, double speed)
    {
        return living.getNavigator().tryMoveToXYZ(x, y, z, speed);
    }

    //TODO world.getTopSolidOrLiquidBlock(x, z)?

    /**
     * Finds the highest block in one y coordinate, but ignores leaves etc.
     *
     * @param world world obj
     * @param x     x coordinate
     * @param z     z coordinate
     * @return yCoordinate
     */
    public static int findTopGround(World world, int x, int z)
    {
        int yHolder = 1;
        while(!world.canBlockSeeTheSky(x, yHolder, z))
        {
            yHolder++;
        }
        while(world.getBlock(x, yHolder, z) == Blocks.air ||
                !world.getBlock(x, yHolder, z).isOpaqueCube() ||
                world.getBlock(x, yHolder, z) == Blocks.leaves ||
                world.getBlock(x, yHolder, z) == Blocks.leaves2)
        {
            yHolder--;
        }
        return yHolder;
    }

    /**
     * Checks if the block is water
     *
     * @param block block to be checked
     * @return true if is water.
     */
    public static boolean isWater(Block block)
    {
        return (block == Blocks.water || block == Blocks.flowing_water);
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
        for(int i = 0; i < world.playerEntities.size(); ++i)
        {
            if(id.equals(((EntityPlayer) world.playerEntities.get(i)).getGameProfile().getId()))
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
        for(int i = 0; i < world.loadedEntityList.size(); ++i)
        {
            if(id.equals(((Entity) world.loadedEntityList.get(i)).getUniqueID()))
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
        List<EntityPlayer> players = new ArrayList<EntityPlayer>();

        for(Object o : world.playerEntities)
        {
            if(o instanceof EntityPlayer)
            {
                EntityPlayer player = (EntityPlayer) o;
                if(ids.contains(player.getGameProfile().getId()))
                {
                    players.add(player);
                    if(players.size() == ids.size())
                    {
                        return players;
                    }
                }
            }
        }
        if(!players.isEmpty())
        {
            return players;
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
    public static List<Entity> getEntitiesFromUUID(World world, Collection<UUID> ids)
    {
        List<Entity> entities = new ArrayList<Entity>();

        for(Object o : world.loadedEntityList)
        {
            if(o instanceof Entity)
            {
                Entity entity = (Entity) o;
                if(ids.contains(entity.getUniqueID()))
                {
                    entities.add(entity);
                    if(entities.size() == ids.size())
                    {
                        return entities;
                    }
                }
            }
        }
        if(!entities.isEmpty())
        {
            return entities;
        }
        return null;
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
        List<Entity> entities = new ArrayList<Entity>();

        for(int id : ids)
        {
            entities.add(world.getEntityByID(id));
        }
        if(!entities.isEmpty())
        {
            return entities;
        }
        return null;
    }

    public static boolean containsStackInArray(ItemStack itemstack, ItemStack... array)
    {
        return containsStackInList(itemstack, Arrays.asList(array));
    }

    public static boolean containsStackInList(ItemStack itemstack, List<ItemStack> list)
    {
        for(ItemStack listStack : list)
        {
            if(listStack.isItemEqual(itemstack))
            {
                return true;
            }
        }
        return false;
    }

    public static double square(double number)
    {
        return number * number;
    }

    public static boolean testFlag(int data, int flag)
    {
        return (data & flag) == flag;
    }

    public static int mask(int data, int mask)
    {
        return data & mask;
    }

    public static int setFlag(int data, int flag)
    {
        return data | flag;
    }

    public static int unsetFlag(int data, int flag)
    {
        return data & ~flag;
    }

    public static int toggleFlag(int data, int flag)
    {
        return data ^ flag;
    }

    public static void blockBreakSoundAndEffect(World world, int x, int y, int z, Block block, int metadata)
    {
        world.playAuxSFX(2001, x, y, z, Block.getIdFromBlock(block) + (metadata << 12));
    }
}