package com.minecolonies;

import com.minecolonies.blocks.ModBlocks;
import com.minecolonies.configuration.ConfigurationHandler;
import com.minecolonies.event.EventHandler;
import com.minecolonies.items.ModItems;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.GuiHandler;
import com.minecolonies.network.PacketPipeline;
import com.minecolonies.proxy.IProxy;
import com.minecolonies.util.RecipeHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.Logger;

@Mod(modid = Constants.MODID, name = Constants.MODNAME, version = Constants.VERSION)
public class MineColonies
{
    public static Logger logger;

    public static final PacketPipeline packetPipeline = new PacketPipeline();

    @Mod.Instance
    public static MineColonies instance;

    @SidedProxy(clientSide = Constants.CLIENTPROXYLOCATION, serverSide = Constants.COMMONPROXYLOCATION)
    public static IProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();

        ConfigurationHandler.init(event.getSuggestedConfigurationFile());

        ModBlocks.init();

        ModItems.init();

        proxy.registerKeybindings();//Schematica
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        packetPipeline.initialize();

        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());

        proxy.registerTileEntities();

        RecipeHandler.init();

        MinecraftForge.EVENT_BUS.register(new EventHandler());

        //proxy.registerEvents();//Schematica //proxy doesn't have a method "registerEvents" - Nico

        proxy.registerEntities();

        proxy.registerEntityRendering();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        packetPipeline.postInitialize();
    }
}