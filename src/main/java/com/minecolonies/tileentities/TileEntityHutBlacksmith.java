package com.minecolonies.tileentities;

public class TileEntityHutBlacksmith extends TileEntityHutWorker
{
    public TileEntityHutBlacksmith()
    {
        setMaxInhabitants(1);
    }

    @Override
    public String getName()
    {
        return "hutBlacksmith";
    }
}
