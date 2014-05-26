package com.minecolonies.tileentities;

public class TileEntityHutLumberjack extends TileEntityHutWorker
{
    public TileEntityHutLumberjack()
    {
        setMaxInhabitants(1);
    }

    @Override
    public String getName()
    {
        return "hutLumberJack";
    }
}
