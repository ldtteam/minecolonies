package com.minecolonies.util;

import com.minecolonies.tilentities.TileEntityTownHall;
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
}