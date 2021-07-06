package com.minecolonies.coremod.proxy;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.io.File;

/**
 * Proxy to the server.
 */
public class ServerProxy extends CommonProxy
{

    @Override
    public File getSchematicsFolder()
    {
        return new File(ServerLifecycleHooks.getCurrentServer().getServerDirectory()
                          + "/" + Constants.MOD_ID);
    }

    @Override
    public World getWorld(final RegistryKey<World> dimension)
    {
        return ServerLifecycleHooks.getCurrentServer().getLevel(dimension);
    }
}
