package com.minecolonies.tilentities;

import com.minecolonies.util.Utils;

public class TileEntityHut extends TileEntityBuildable
{
    public TileEntityHut(){}

    public void findTownHall()
    {
        TileEntityTownHall tileEntityTownHall = Utils.getClosestTownHall(worldObj, xCoord, yCoord, zCoord);
        if(tileEntityTownHall != null)
        {
            townHall = tileEntityTownHall;
        }
    }
}
