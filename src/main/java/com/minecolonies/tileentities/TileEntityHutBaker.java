package com.minecolonies.tileentities;

public class TileEntityHutBaker extends TileEntityHutWorker
{
    public TileEntityHutBaker()
    {
        setMaxInhabitants(1);
    }

    @Override
    public String getName()
    {
        return "hutBaker";
    }
}
