package com.minecolonies.coremod.proxy;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
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
        return new File(ServerLifecycleHooks.getCurrentServer().getDataDirectory()
                          + "/" + Constants.MOD_ID);
    }

    @Override
    public World getWorld(final ResourceLocation dimension)
    {
        return ServerLifecycleHooks.getCurrentServer().getWorld(RegistryKey.func_240903_a_(Registry.WORLD_KEY, dimension));
    }
}
