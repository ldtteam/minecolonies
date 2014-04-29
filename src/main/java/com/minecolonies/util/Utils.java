package com.minecolonies.util;

import com.minecolonies.tilentities.TileEntityTownHall;
import net.minecraft.block.Block;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class Utils
{
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

    @SuppressWarnings("UnusedDeclaration") //TODO Check for uses (Inherited from old mod)
    protected int findTopGround(World world, int x, int z)
    {
        //TODO
        return 1;
    }

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
}