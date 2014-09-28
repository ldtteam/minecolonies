package com.minecolonies.proxy;

public interface IProxy
{
    boolean isClient();

    void registerTileEntities();

    void registerKeybindings();

    void registerEvents();

    void registerEntities();

    void registerEntityRendering();

    void registerTileEntityRendering();
}
