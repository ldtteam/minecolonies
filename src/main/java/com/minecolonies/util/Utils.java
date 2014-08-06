package com.minecolonies.util;

import com.minecolonies.entity.EntityWorker;
import com.minecolonies.entity.PlayerProperties;
import com.minecolonies.tileentities.TileEntityTownHall;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Utils
{
    /**
     * Method to find the closest townhall
     *
     * @param world world obj
     * @param x     x coordinate to check from
     * @param y     y coordinate to check from
     * @param z     z coordinate to check from
     * @return closest TileEntityTownHall
     */
    public static TileEntityTownHall getClosestTownHall(World world, int x, int y, int z)
    {
        double closestDist = Double.MAX_VALUE;
        TileEntityTownHall closestTownHall = null;

        if(world == null || world.loadedTileEntityList == null) return null;

        for(Object o : world.loadedTileEntityList)
            if(o instanceof TileEntityTownHall)
            {
                TileEntityTownHall townHall = (TileEntityTownHall) o;

                if(Vec3Utils.equals(townHall.getPosition(), x, y, z)) continue;

                double distanceSquared = townHall.getDistanceFrom(x, y, z);
                if(closestDist > distanceSquared)
                {
                    closestTownHall = townHall;
                    closestDist = distanceSquared;
                }
            }
        return closestTownHall;
    }

    /**
     * find the distance to the closest townhall.
     *
     * @param world world townhall is in
     * @param x     x coordinate to check from
     * @param y     y coordinate to check from
     * @param z     z coordinate to check from
     * @return distance to nearest townhall
     */
    public static double getDistanceToClosestTownHall(World world, int x, int y, int z)
    {
        double closestDist = Double.MAX_VALUE;

        if(world == null || world.loadedTileEntityList == null) return -1;

        for(Object o : world.loadedTileEntityList)
            if(o instanceof TileEntityTownHall)
            {
                TileEntityTownHall townHall = (TileEntityTownHall) o;

                if(Vec3Utils.equals(townHall.getPosition(), x, y, z)) continue;

                double distanceSquared = townHall.getDistanceFrom(x, y, z);
                if(closestDist > distanceSquared)
                {
                    closestDist = distanceSquared;
                }
            }
        return Math.sqrt(closestDist);
    }

    /**
     * Gives the distance to a given townhall
     *
     * @param x          x coordinate to check from
     * @param y          y coordinate to check from
     * @param z          z coordinate to check from
     * @param tileEntity TileEntityTownhall to check to.
     * @return distance
     */
    public static double getDistanceToTileEntity(int x, int y, int z, TileEntity tileEntity)
    {
        return Math.sqrt(tileEntity.getDistanceFrom(x, y, z));
    }

    public static Vec3 scanForBlockNearPoint(World world, Block block, int x, int y, int z, int radiusX, int radiusY, int radiusZ)
    {
        Vec3 closestVec = null;
        double minDistance = Double.MAX_VALUE;

        for(int i = x - radiusX; i <= x + radiusX; i++)
        {
            for(int j = y - radiusY; j <= y + radiusY; j++)
            {
                for(int k = z - radiusZ; k <= z + radiusZ; k++)
                {
                    if(world.getBlock(i, j, k) == block)
                    {
                        Vec3 tempVec = Vec3.createVectorHelper(i, j, k);

                        if(closestVec == null || Vec3Utils.distanceTo(tempVec, x, y, z) < minDistance)
                        {
                            closestVec = tempVec;
                            minDistance = Vec3Utils.distanceTo(closestVec, x, y, z);
                        }
                    }
                }
            }
        }
        return closestVec;
    }

    public static boolean isWorkerAtSite(EntityWorker worker, int x, int y, int z)
    {
        if(worker.getPosition().squareDistanceTo(x, y, z) > 4)//Too far away
        {
            if(worker.getNavigator().noPath())//Not moving
            {
                if(!tryMoveLivingToXYZ(worker, x, y, z))
                {
                    worker.setStatus(EntityWorker.Status.PATHFINDING_ERROR);
                }
            }
            return false;
        }
        else
        {
            if(!worker.getNavigator().noPath())//within 2 blocks - can stop pathing //TODO may not need this check
            {
                worker.getNavigator().clearPathEntity();
            }
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

    /**
     * Gets a Townhall that a given player is owner of
     *
     * @param world  world object
     * @param player player to be checked
     * @return TileEntityTownHall the player is user of, or null when he is no owner.
     */
    public static TileEntityTownHall getTownhallByOwner(World world, EntityPlayer player)
    {
        PlayerProperties props = PlayerProperties.get(player);
        if(props.hasPlacedTownHall())
        {
            return (TileEntityTownHall) Vec3Utils.getTileEntityFromVec(world, props.getTownhallPos());
        }
        return null;
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
            if(id.equals(((EntityPlayer) world.playerEntities.get(i)).getUniqueID()))
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
    public static List<EntityPlayer> getPlayersFromUUID(World world, List<UUID> ids)
    {
        List<EntityPlayer> players = new ArrayList<EntityPlayer>();

        for(Object o : world.playerEntities)
        {
            if(o instanceof EntityPlayer)
            {
                EntityPlayer player = (EntityPlayer) o;
                if(ids.contains(player.getUniqueID()))
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
    public static List<Entity> getEntitiesFromUUID(World world, List<UUID> ids)
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

    public static boolean containsItemStack(List<ItemStack> list, ItemStack itemstack)
    {
        for(ItemStack listItem : list)
        {
            if(listItem.isItemEqual(itemstack))
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
}