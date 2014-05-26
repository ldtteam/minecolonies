package com.minecolonies.tileentities;

public class TileEntityHutFarmer extends TileEntityHutWorker
{
    public TileEntityHutFarmer()
    {
        setMaxInhabitants(1);
    }

    @Override
    public String getName()
    {
        return "hutFarmer";
    }
}
