package com.minecolonies;

import com.minecolonies.blocks.ModBlocks;
import com.minecolonies.client.gui.GuiHandler;
import com.minecolonies.configuration.ConfigurationHandler;
import com.minecolonies.event.EventHandler;
import com.minecolonies.items.ModItems;
import com.minecolonies.items.crafting.RecipeHandler;
import com.minecolonies.lib.Constants;
import com.minecolonies.proxy.IProxy;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = Constants.MODID, name = Constants.MODNAME, version = Constants.VERSION)
public class MineColonies
{

    @Mod.Instance
    public static MineColonies instance;

    @SidedProxy(clientSide = Constants.CLIENTPROXYLOCATION, serverSide = Constants.COMMONPROXYLOCATION)
    public static IProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        ConfigurationHandler.init(event.getSuggestedConfigurationFile());

        ModBlocks.init();

        ModItems.init();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());

        proxy.registerTileEntities();
        RecipeHandler.init();

        MinecraftForge.EVENT_BUS.register(new EventHandler());
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event){}
}