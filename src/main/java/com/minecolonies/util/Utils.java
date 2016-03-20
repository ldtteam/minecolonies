package com.minecolonies.util;

import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.ai.AbstractEntityAIWork;
import com.minecolonies.lib.Constants;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.geom.Point2D;
import java.util.*;

public class Utils
{
    /**
     * Find the closest block near the points
     *
     * @param world the world
     * @param blocks Blocks to test for
     * @param x Origin
     * @param y Origin
     * @param z Origin
     * @param radiusX x search distance
     * @param radiusY y search distance
     * @param radiusZ z search distance
     * @param height check if blocks above the found block are air or block
     * @return the coordinates of the found block
     */
    public static ChunkCoordinates scanForBlockNearPoint(World world, int x, int y, int z, int radiusX, int radiusY, int radiusZ, int height, Block... blocks)
    {


        ChunkCoordinates closestCoords = null;
        double minDistance = Double.MAX_VALUE;

        for(int i = x - radiusX; i <= x + radiusX; i++)
        {
            for(int j = y - radiusY; j <= y + radiusY; j++)
            {
                for(int k = z - radiusZ; k <= z + radiusZ; k++)
                {
                    if(checkHeight(world, blocks, i, j, k, height))
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

    private static boolean checkHeight(World world, Block[] blocks, int x, int y, int z, int height)
    {
        for(int dy = 0; dy < height; dy++)
        {
            if(!arrayContains(blocks, world.getBlock(x, y + dy, z)))
            {
                return false;
            }
        }
        return true;
    }

    private static boolean arrayContains(Object[] array, Object key)
    {
        for(Object o : array)
        {
            if(key.equals(o))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean isPathingTo(EntityCitizen citizen, int x, int z)
    {
        PathPoint pathpoint = citizen.getNavigator().getPath().getFinalPathPoint();
        return pathpoint != null && pathpoint.xCoord == x && pathpoint.zCoord == z;
    }

    public static boolean isWorkerAtSite(EntityCitizen worker, int x, int y, int z)
    {
        return isWorkerAtSite(worker, x, y, z, 2);
    }

    public static boolean isWorkerAtSite(EntityCitizen worker, int x, int y, int z, int range)
    {
        return worker.getPosition().squareDistanceTo(x, y, z) < range*range;
    }

    public static boolean isWorkerAtSiteWithMove(EntityCitizen worker, int x, int y, int z)
    {
        //Default range of 3 works better
        //Range of 2 get some workers stuck
        return isWorkerAtSiteWithMove(worker, x, y, z, 3);
    }

    public static boolean isWorkerAtSiteWithMove(EntityCitizen worker, int x, int y, int z, int range)
    {
        if(!isWorkerAtSite(worker, x, y, z, range))//Too far away
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

    public static Logger generateLoggerForClass(Class clazz){
        return LogManager.getLogger(Constants.MOD_ID+"::"+clazz.getSimpleName());
    }

    /**
     * Calculate the mining level an item has as a tool of certain type.
     * @param stack the stack to test
     * @param tool the tool category
     * @return integer value for mining level >= 0 is okay
     */
    public static int getMiningLevel(ItemStack stack, String tool)
    {
        if (tool == null)
        {
            return stack == null ? 0 : 1; //empty hand is best on blocks who don't care (0 better 1)
        }
        if (stack == null)
        {
            return -1;
        }
        return stack.getItem().getHarvestLevel(stack, tool);
    }
    /**
     * Checks if a pickaxe can be used for that mining level.
     * @param minlevel the level needs to have
     * @param level the level it has
     * @return if the pickaxe qualifies
     */
    public static boolean checkIfPickaxeQualifies(int minlevel, int level){
        return checkIfPickaxeQualifies(minlevel,level,false);
    }

    /**
     * Checks if a pickaxe can be used for that mining level.
     * Be aware, it will return false for mining stone
     * with an expensive pickaxe. So set {@code beEfficient} to false
     * for that if you need it the other way around.
     * @param minlevel the level needs to have
     * @param level the level it has
     * @param beEfficient if he should stop using diamond picks on stone
     * @return if the pickaxe qualifies
     */
    public static boolean checkIfPickaxeQualifies(int minlevel, int level, boolean beEfficient)
    {
        //Minecraft handles this as "everything is allowed"
        if (minlevel < 0)
        {
            return true;
        }
        if (beEfficient && minlevel == 0)
        {
            //Code to not overuse on high level pickaxes
            return level >= 0 && level <= 1;

        }
        return level >= minlevel;
    }

    /**
     * Checks if this tool is useful for the miner.
     */
    public static boolean isMiningTool(ItemStack itemStack)
    {
        return isPickaxe(itemStack) || isShovel(itemStack);
    }

    /**
     * Checks if this ItemStack can be used as a Shovel.
     */
    public static boolean isShovel(ItemStack itemStack)
    {
        return isTool(itemStack, AbstractEntityAIWork.SHOVEL);
    }

    /**
     * Checks if this ItemStack can be used as a Tool of type.
     */
    public static boolean isTool(ItemStack itemStack, String toolType)
    {
        return getMiningLevel(itemStack, toolType) >= 0;
    }

    /**
     * Checks if this ItemStack can be used as a Pickaxe.
     */
    public static boolean isPickaxe(ItemStack itemStack)
    {
        return isTool(itemStack, AbstractEntityAIWork.PICKAXE);
    }
}