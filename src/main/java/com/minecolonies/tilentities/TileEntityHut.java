package com.minecolonies.tilentities;

import com.minecolonies.util.Utils;

public class TileEntityHut extends TileEntityBuildable
{
    public TileEntityHut(){}

    //TODO Check that this hut isn't already bound to a different existing townhall. Also, this may not be implemented here, but when a player places a hut, we should make sure they own the townhall that the hut is being bound to.
    public void findTownHall()
    {
        TileEntityTownHall tileEntityTownHall = Utils.getClosestTownHall(worldObj, xCoord, yCoord, zCoord);
        if(tileEntityTownHall != null)
        {
            townHall = tileEntityTownHall;
        }
    }
}
