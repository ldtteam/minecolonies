package com.minecolonies.util;

import com.minecolonies.tileentities.TileEntityTownHall;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.UUID;

public class Utils
{
    /**
     * Method to find the closest townhall
     *
     * @param world world obj
     * @param x     xCoord to check from
     * @param y     yCoord to check from
     * @param z     zCoord to check from
     * @return closest TileEntityTownHall
     */
    public static TileEntityTownHall getClosestTownHall(World world, int x, int y, int z)
    {
        double closestDist = 9999;
        TileEntityTownHall closestTownHall = null;

        if(world == null || world.loadedTileEntityList == null) return null;

        for(Object o : world.loadedTileEntityList)
            if(o instanceof TileEntityTownHall)
            {
                TileEntityTownHall townHall = (TileEntityTownHall) o;

                if(closestDist > Math.sqrt(Math.sqrt((x - townHall.xCoord) * (x - townHall.xCoord) + (y - townHall.yCoord) * (y - townHall.yCoord) + (z - townHall.zCoord) * (z - townHall.zCoord))))
                {
                    closestTownHall = townHall;
                    closestDist = Math.sqrt((x - townHall.xCoord) * (x - townHall.xCoord) + (y - townHall.yCoord) * (y - townHall.yCoord) + (z - townHall.zCoord) * (z - townHall.zCoord));
                }
            }
        return closestTownHall;
    }

    public static double getDistanceToClosestTownHall(World world, int x, int y, int z)
    {
        double closestDist = 9999;
        TileEntityTownHall closestTownHall = null;

        if(world == null || world.loadedTileEntityList == null) return -1;

        for(Object o : world.loadedTileEntityList)
            if(o instanceof TileEntityTownHall)
            {
                TileEntityTownHall townHall = (TileEntityTownHall) o;

                if(closestDist > Math.sqrt(Math.sqrt((x - townHall.xCoord) * (x - townHall.xCoord) + (y - townHall.yCoord) * (y - townHall.yCoord) + (z - townHall.zCoord) * (z - townHall.zCoord))))
                {
                    closestDist = Math.sqrt((x - townHall.xCoord) * (x - townHall.xCoord) + (y - townHall.yCoord) * (y - townHall.yCoord) + (z - townHall.zCoord) * (z - townHall.zCoord));
                }
            }
        return closestDist;
    }

    /**
     * Gives the distance to a given townhall
     * @param world world obj
     * @param x xCoord to check from
     * @param y yCoord to check from
     * @param z zCoord to check from
     * @param tileEntity TileEntityTownhall to check to.
     * @return distance
     */
    public static double getDistanceToTileEntity(World world, int x, int y, int z, TileEntity tileEntity)
    {
        int xTown = tileEntity.xCoord;
        int yTown = tileEntity.yCoord;
        int zTown = tileEntity.zCoord;
        return Math.sqrt(Math.pow(Math.abs(x - tileEntity.xCoord), 2)
                       + Math.pow(Math.abs(y - tileEntity.yCoord), 2)
                       + Math.pow(Math.abs(z - tileEntity.zCoord), 2));
    }

    /**
     * Gets a Townhall that a given player is owner of
     *
     * @param world world obj
     * @param player player to be checked
     * @return TileEntityTownHall the player is user of, or null when he is no owner.
     */
    public static TileEntityTownHall getTownhallByOwner(World world, EntityPlayer player)
    {
        for(Object o : world.loadedTileEntityList)
            if(o instanceof TileEntityTownHall)
                for(UUID owners : ((TileEntityTownHall) o).getOwners())
                    if(owners.equals(player.getUniqueID()))
                        return (TileEntityTownHall)o;
        return null;
    }

    /**
     * Finds the highest block in one yCoord, but ignores leaves etc.
     *
     * @param world world obj
     * @param x     xCoord
     * @param z     zCoord
     * @return yCoordinate
     */
    protected int findTopGround(World world, int x, int z)
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
     * Still unused
     */
    @SuppressWarnings("UnusedDeclaration") //TODO Check for uses (Inherited from old mod)
    protected Vec3 scanForBlockNearPoint(World world, Block block, int x, int y, int z, int radiusX, int radiusY, int radiusZ)
    {
        Vec3 entityVec = Vec3.createVectorHelper(x, y, z);

        Vec3 closestVec = null;
        double minDistance = 999999999;

        for(int i = x - radiusX; i <= x + radiusX; i++)
        {
            for(int j = y - radiusY; j <= y + radiusY; j++)
            {
                for(int k = z - radiusZ; k <= z + radiusZ; k++)
                {
                    if(world.getBlock(i, j, k) == block)
                    {
                        Vec3 tempVec = Vec3.createVectorHelper(i, j, k);

                        if(closestVec == null || tempVec.distanceTo(entityVec) < minDistance)
                        {
                            closestVec = tempVec;
                            minDistance = closestVec.distanceTo(entityVec);
                        }
                    }
                }
            }
        }
        return closestVec;
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
     * Checks if the block is water
     *
     * @param world world obj
     * @param x     xCoord
     * @param y     yCoord
     * @param z     zCoord
     * @return true if is water.
     */
    public static boolean isWater(World world, int x, int y, int z)
    {
        return world.getBlock(x, y, z) == Blocks.water || world.getBlock(x, y, z) == Blocks.flowing_water;
    }

    public static void sendPlayerMessage(EntityPlayer player, String message)
    {
        player.addChatComponentMessage(new ChatComponentText(message));
    }
}