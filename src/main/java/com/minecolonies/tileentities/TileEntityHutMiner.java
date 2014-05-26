package com.minecolonies.tileentities;

public class TileEntityHutMiner extends TileEntityHutWorker
{
    public TileEntityHutMiner()
    {
        setMaxInhabitants(1);
    }

    @Override
    public String getName()
    {
        return "hutMiner";
    }
}
