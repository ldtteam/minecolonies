package com.minecolonies.core.proxy;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.apiimp.CommonMinecoloniesAPIImpl;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.NewRegistryEvent;

/**
 * CommonProxy of the minecolonies mod (Server and Client).
 */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public abstract class CommonProxy implements IProxy
{
    /**
     * API instance.
     */
    protected static CommonMinecoloniesAPIImpl apiImpl;

    /**
     * Creates instance of proxy.
     */
    public CommonProxy()
    {
        apiImpl = new CommonMinecoloniesAPIImpl();
    }


    @SubscribeEvent
    public static void registerNewRegistries(final NewRegistryEvent event)
    {
        apiImpl.onRegistryNewRegistry(event);
    }

    @Override
    public void setupApi()
    {
        MinecoloniesAPIProxy.getInstance().setApiInstance(apiImpl);
    }
}
