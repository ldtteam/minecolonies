package com.minecolonies.core.proxy;

import com.minecolonies.apiimp.ClientMinecoloniesAPIImpl;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

/**
 * Client side proxy.
 */
@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientProxy extends CommonProxy
{
    public ClientProxy()
    {
        apiImpl = new ClientMinecoloniesAPIImpl();
    }
}
