package com.minecolonies.proxy;

public abstract interface IProxy
{
    public abstract boolean isClient();

    public abstract void registerTileEntities();

    public abstract void registerKeybindings();

    public abstract void registerEvents();

    public abstract void registerEntities();

    public abstract void registerEntityRendering();

    public abstract void registerTileEntityRendering();
}
