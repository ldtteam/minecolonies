package com.minecolonies.tileentities;

import com.minecolonies.lib.Constants;

public class TileEntityHutBuilder extends TileEntityHutWorker
{
    public TileEntityHutBuilder()
    {
        setMaxInhabitants(1);
    }

    @Override
    public String getName()
    {
        return "hutBuilder";
    }
}
