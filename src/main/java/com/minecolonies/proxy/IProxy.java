package com.minecolonies.proxy;

import com.minecolonies.colony.CitizenData;

public interface IProxy
{
    boolean isClient();

    void registerTileEntities();

    void registerKeybindings();

    void registerEvents();

    void registerEntities();

    void registerEntityRendering();

    void registerTileEntityRendering();

    void showCitizenWindow(CitizenData.View citizen);
}
