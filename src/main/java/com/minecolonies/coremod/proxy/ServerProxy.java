package com.minecolonies.coremod.proxy;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.File;

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
