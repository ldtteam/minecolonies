package com.minecolonies;

import com.minecolonies.lib.Constants;
import com.minecolonies.proxy.IProxy;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Constants.MODID, name = Constants.MODNAME, version = Constants.VERSION)
public class MineColonies
{
    @Mod.Instance
    public static MineColonies instance;

    @SidedProxy(clientSide = Constants.CLIENTPROXYLOCATION, serverSide = Constants.COMMONPROXYLOCATION)
    public static IProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event){}

    @Mod.EventHandler
    public void init(FMLInitializationEvent event){}

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event){}
}
