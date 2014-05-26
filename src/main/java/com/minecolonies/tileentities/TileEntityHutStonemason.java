package com.minecolonies.tileentities;

public class TileEntityHutStonemason extends TileEntityHutWorker
{
    public TileEntityHutStonemason()
    {
        setMaxInhabitants(1);
    }

    @Override
    public String getName()
    {
        return "hutStonemason";
    }
}
