package com.minecolonies.coremod.proxy;

import com.minecolonies.api.util.constant.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;
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
}
