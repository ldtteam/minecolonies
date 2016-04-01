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

import java.util.*;

public class Utils
{
    /**
     * Find the closest block near the points
     *
     * @param world     the world
     * @param x         Origin
     * @param y         Origin
     * @param z         Origin
     * @param radiusX   x search distance
     * @param radiusY   y search distance
     * @param radiusZ   z search distance
     * @param height    check if blocks above the found block are air or block
     * @param blocks    Blocks to test for
     * @return          the coordinates of the found block
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

                        double distance = tempCoords.getDistanceSquared(x, y, z);
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

    /**
     *  //TODO document
     * @param world
     * @param blocks
     * @param x
     * @param y
     * @param z
     * @param height
     * @return
     */
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

    /**
     * Returns whether or not the array contains the object given
     *
     * @param array     Array to scan
     * @param key       Object to look for
     * @return          True if found, otherwise false
     */
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

    /**
     * Returns whether or not a citizen is heading to a specific location
     *
     * @param citizen       Citizen you want to check
     * @param x             X-coordinate
     * @param z             Z-coordinate
     * @return              True if citizen heads to (x, z), otherwise false
     */
    public static boolean isPathingTo(EntityCitizen citizen, int x, int z)
    {
        PathPoint pathpoint = citizen.getNavigator().getPath().getFinalPathPoint();
        return pathpoint != null && pathpoint.xCoord == x && pathpoint.zCoord == z;
    }

    /**
     * @see {@link #isWorkerAtSite(EntityCitizen, int, int, int, int)}
     * Default:
     *      range: 2
     *
     * @param worker    Worker to check
     * @param x         X-coordinate
     * @param y         Y-coordinate
     * @param z         Z-coordinate
     * @return          True if worker is at site, otherwise false
     */
    public static boolean isWorkerAtSite(EntityCitizen worker, int x, int y, int z)
    {
        return isWorkerAtSite(worker, x, y, z, 2);
    }

    /**
     * Returns whether or not the worker is within a specific range of his working site
     *
     * @param worker    Worker to check
     * @param x         X-coordinate
     * @param y         Y-coordinate
     * @param z         Z-coordinate
     * @param range     Range to check in
     * @return          True if worker is at site, otherwise false
     */
    public static boolean isWorkerAtSite(EntityCitizen worker, int x, int y, int z, int range)
    {
        return worker.getPosition().squareDistanceTo(x, y, z) < square(range);
    }

    /**
     * @see {@link #isWorkerAtSiteWithMove(EntityCitizen, int, int, int, int)}
     * Default:
     *      range: 3
     *
     * @param worker    Worker to check
     * @param x         X-coordinate
     * @param y         Y-coordinate
     * @param z         Z-coordinate
     * @return          True if worker is at site, otherwise false
     */
    public static boolean isWorkerAtSiteWithMove(EntityCitizen worker, int x, int y, int z)
    {
        //Default range of 3 works better
        //Range of 2 get some workers stuck
        return isWorkerAtSiteWithMove(worker, x, y, z, 3);
    }

    /**
     * Checks if a worker is at his working site.
     * If he isn't, sets it's path to the location
     *
     * @param worker    Worker to check
     * @param x         X-coordinate
     * @param y         Y-coordinate
     * @param z         Z-coordinate
     * @param range     Range to check in
     * @return          True if worker is at site, otherwise false.
     */
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

    /**
     * @see {@link #tryMoveLivingToXYZ(EntityLiving, int, int, int, double)}
     * Default:
     *      speed: 1.0D
     *
     * @param living        Entity to move
     * @param x             x-coordinate
     * @param y             y-coordinate
     * @param z             z-coordinate
     * @return              True if the path is set to destination, otherwise false
     */
    public static boolean tryMoveLivingToXYZ(EntityLiving living, int x, int y, int z)
    {
        return tryMoveLivingToXYZ(living, x, y, z, 1.0D);
    }

    /**
     * Sets the movement of the entity to specific point.
     * Returns true if direction is set, otherwise false
     *
     * @param living        Entity to move
     * @param x             x-coordinate
     * @param y             y-coordinate
     * @param z             z-coordinate
     * @param speed         Speed to move with
     * @return              True if the path is set to destination, otherwise false
     */
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
     * @return      true if is water.
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
     * @return      the Player
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
     * @return      the Entity
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
     * @return      list of EntityPlayers
     */
    public static List<EntityPlayer> getPlayersFromUUID(World world, Collection<UUID> ids)
    {
        List<EntityPlayer> players = new ArrayList<>();

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
     * @return      list of Entity's
     */
    public static List<Entity> getEntitiesFromUUID(World world, Collection<UUID> ids)
    {
        List<Entity> entities = new ArrayList<>();

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
     * @return      list of Entity's
     */
    public static List<Entity> getEntitiesFromID(World world, List<Integer> ids)
    {
        List<Entity> entities = new ArrayList<>();

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

    /**
     * @see {@link #containsStackInList(ItemStack, List)}
     *
     * @param itemstack     ItemStack to check
     * @param array         Array to check in
     * @return              True if item stack in array, otherwise false
     */
    public static boolean containsStackInArray(ItemStack itemstack, ItemStack... array)
    {
        return containsStackInList(itemstack, Arrays.asList(array));
    }

    /**
     * Checks if an item stack is in a list of item stacks
     *
     * @param itemstack     Item stack to find
     * @param list          List to check in
     * @return              True if itemStack is in list, otherwise false
     */
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

    /**
     * Returns the square product of a number
     *
     * @param number    Number to square
     * @return          Answer of calculation
     */
    public static double square(double number)
    {
        return number * number;
    }

    /**
     * Checks if the flag is set in the data
     * E.G.
     *      - Flag: 000101
     *      - Data: 100101
     *      - All Flags are set in data, so returns true.
     *          Some more flags are set, but not take into account
     *
     * @param data  Data to check flag in
     * @param flag  Flag to check whether it is set or not
     * @return      True if flag is set, otherwise false.
     */
    public static boolean testFlag(int data, int flag)
    {
        return mask(data, flag) == flag;
    }

    /**
     * Returns what flags are set, and given in mask
     * E.G.
     *      - Flag: 000101
     *      - Mask: 100101
     *      - The 4th and 6th bit are set, so only those will be returned
     *
     * @param data      Data to check
     * @param mask      Mask to check
     * @return          Byte in which both data bits and mask bits are set
     */
    public static int mask(int data, int mask)
    {
        return data & mask;
    }

    /**
     * Sets a flag in in the data
     * E.G.
     *      - Flag: 000101
     *      - Mask: 100001
     *      - The 4th bit will now be set, both the 1st and 6th bit are maintained
     *
     * @param data      Data to set flag in
     * @param flag      Flag to set
     * @return          Data with flags set
     */
    public static int setFlag(int data, int flag)
    {
        return data | flag;
    }

    /**
     * Unsets a flag
     * E.G.
     *      - Flag: 000101
     *      - Mask: 100101
     *      - The 4th and 6th bit will be unset, the 1st bit is maintained
     *
     * @param data      Data to remove flag from
     * @param flag      Flag to remove
     * @return          Data with flag unset
     */
    public static int unsetFlag(int data, int flag)
    {
        return data & ~flag;
    }

    /**
     * Toggles flags
     * E.G.
     *      - Flag: 000101
     *      - Mask: 100101
     *      - The 4th and 6th will be toggled, the 1st bit is maintained
     *
     * @param data      Data to toggle flag in
     * @param flag      Flag to toggle
     * @return          Data with flag toggled
     */
    public static int toggleFlag(int data, int flag)
    {
        return data ^ flag;
    }

    /**
     * Plays the block break effect at specific location
     *
     * @param world         World to play effect in
     * @param x             x-coordinate
     * @param y             y-coordinate
     * @param z             z-coordinate
     * @param block         Block that makes the sound
     * @param metadata      Metadata of the block that makes sound
     */
    public static void blockBreakSoundAndEffect(World world, int x, int y, int z, Block block, int metadata)
    {
        world.playAuxSFX(2001, x, y, z, Block.getIdFromBlock(block) + (metadata << 12));
    }

    /**
     * Generates a logger for a specific class
     *
     * @param clazz     Class to generate logger for
     * @return          Created {@link org.apache.logging.log4j.Logger}
     */
    public static Logger generateLoggerForClass(Class clazz){
        return LogManager.getLogger(Constants.MOD_ID+"::"+clazz.getSimpleName());
    }

    /**
     * Calculate the mining level an item has as a tool of certain type.
     *
     * @param stack     the stack to test
     * @param tool      the tool category
     * @return          integer value for mining level >= 0 is okay
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
     *
     * @param minlevel  the level needs to have
     * @param level     the level it has
     * @return          whether the pickaxe qualifies
     */
    public static boolean checkIfPickaxeQualifies(int minlevel, int level){
        return checkIfPickaxeQualifies(minlevel,level,false);
    }

    /**
     * Checks if a pickaxe can be used for that mining level.
     * Be aware, it will return false for mining stone
     * with an expensive pickaxe. So set {@code beEfficient} to false
     * for that if you need it the other way around.
     *
     * @param minlevel      the level needs to have
     * @param level         the level it has
     * @param beEfficient   if he should stop using diamond picks on stone
     * @return              whether the pickaxe qualifies
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
     *
     * @param itemStack     Item to check
     * @return              True if mining tool, otherwise false
     */
    public static boolean isMiningTool(ItemStack itemStack)
    {
        return isPickaxe(itemStack) || isShovel(itemStack);
    }

    /**
     * Checks if this ItemStack can be used as a Shovel.
     *
     * @param itemStack     Item to check
     * @return              True if item is shovel, otherwise false
     */
    public static boolean isShovel(ItemStack itemStack)
    {
        return isTool(itemStack, AbstractEntityAIWork.SHOVEL);
    }

    /**
     * Checks if this ItemStack can be used as a Tool of type.
     *
     * @param itemStack     Item to check
     * @param toolType      Type of the tool
     * @return              true if item can be used, otherwise false
     */
    public static boolean isTool(ItemStack itemStack, String toolType)
    {
        return getMiningLevel(itemStack, toolType) >= 0;
    }

    /**
     * Checks if this ItemStack can be used as a Pick axe.
     *
     * @param itemStack     Item to check
     * @return              True if item is a pick axe, otherwise false
     */
    public static boolean isPickaxe(ItemStack itemStack)
    {
        return isTool(itemStack, AbstractEntityAIWork.PICKAXE);
    }
}