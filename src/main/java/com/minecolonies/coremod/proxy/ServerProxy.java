package com.minecolonies.coremod.proxy;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Proxy to the server.
 */
public class ServerProxy extends CommonProxy
{
    @Override
    public Level getWorld(final ResourceKey<Level> dimension)
    {
        return net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer().getLevel(dimension);
    }
}
