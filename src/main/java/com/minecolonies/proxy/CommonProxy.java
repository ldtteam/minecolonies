package com.minecolonies.proxy;

import com.minecolonies.tilentities.TileEntityTownHall;
import cpw.mods.fml.common.registry.GameRegistry;

public abstract class CommonProxy implements IProxy
{
    @Override
    public void registerTileEntities()
    {
        GameRegistry.registerTileEntity(TileEntityTownHall.class, "tilEntityTownHall");
    }
}
