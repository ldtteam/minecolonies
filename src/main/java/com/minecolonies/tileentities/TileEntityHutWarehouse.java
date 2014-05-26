package com.minecolonies.tileentities;

public class TileEntityHutWarehouse extends TileEntityHutWorker
{
    public TileEntityHutWarehouse()
    {
        setMaxInhabitants(1);
    }

    @Override
    public String getName()
    {
        return "hutWarehouse";
    }
}
