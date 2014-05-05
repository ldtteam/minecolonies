package com.minecolonies.tilentities;

import com.minecolonies.util.Utils;

public class TileEntityHut extends TileEntityBuildable
{
    private int maxInhabitants;
    private int maleInhabitants;
    private int femaleInhabitants;

    public TileEntityHut()
    {

    }

    public void findAndAddClosestTownhall()
    {
        TileEntityTownHall tileEntityTownHall = Utils.getClosestTownHall(worldObj, xCoord, yCoord, zCoord);
        if(tileEntityTownHall != null)
        {
            this.setTownHall(tileEntityTownHall);
        }
    }

    public int getMaxInhabitants()
    {
        return maxInhabitants;
    }

    public void setMaxInhabitants(int maxInhabitants)
    {
        this.maxInhabitants = maxInhabitants;
    }

    public int getMaleInhabitants()
    {
        return maleInhabitants;
    }

    public void setMaleInhabitants(int maleInhabitants)
    {
        this.maleInhabitants = maleInhabitants;
    }

    public int getFemaleInhabitants()
    {
        return femaleInhabitants;
    }

    public void setFemaleInhabitants(int femaleInhabitants)
    {
        this.femaleInhabitants = femaleInhabitants;
    }
}
