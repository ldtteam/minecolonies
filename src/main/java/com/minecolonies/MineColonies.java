package com.minecolonies;

import com.minecolonies.blocks.ModBlocks;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.configuration.ConfigurationHandler;
import com.minecolonies.items.ModItems;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.GuiHandler;
import com.minecolonies.network.messages.*;
import com.minecolonies.proxy.IProxy;
import com.minecolonies.util.RecipeHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLFingerprintViolationEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Constants.MOD_ID, name = Constants.MOD_NAME, version = Constants.VERSION, certificateFingerprint = Constants.FINGERPRINT)
public class MineColonies
{
    public static Logger logger = LogManager.getLogger(Constants.MOD_ID);

    public static SimpleNetworkWrapper network;

    @Mod.Instance(Constants.MOD_ID)
    public static MineColonies instance;

    @SidedProxy(clientSide = Constants.CLIENT_PROXY_LOCATION, serverSide = Constants.SERVER_PROXY_LOCATION)
    public static IProxy proxy;

    @Mod.EventHandler
    @SuppressWarnings("unused")
    public void invalidFingerprint(FMLFingerprintViolationEvent event)
    {
        if(Constants.FINGERPRINT.equals("@FINGERPRINT@"))
        {
            //logger.log(Level.ERROR, LanguageHandler.format("com.minecolonies.error.noFingerprint"));
            logger.error("No Fingerprint. Might not be a valid version!");
        }
    }

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
//        packetPipeline.initialize();

        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());

        network = NetworkRegistry.INSTANCE.newSimpleChannel(Constants.MOD_NAME);
        network.registerMessage(ColonyViewMessage.class, ColonyViewMessage.class, 0, Side.CLIENT);
        network.registerMessage(ColonyViewCitizensMessage.class,    ColonyViewCitizensMessage.class,    1,  Side.CLIENT);
        network.registerMessage(ColonyBuildingViewMessage.class,    ColonyBuildingViewMessage.class,    2,  Side.CLIENT);
        network.registerMessage(BuildRequestMessage.class,          BuildRequestMessage.class,          10,  Side.SERVER);
        network.registerMessage(OpenInventoryMessage.class,         OpenInventoryMessage.class,         11,  Side.SERVER);
        network.registerMessage(TownhallRenameMessage.class,        TownhallRenameMessage.class,        12,  Side.SERVER);

        proxy.registerTileEntities();

        RecipeHandler.init();

        proxy.registerEvents();

        proxy.registerEntities();

        proxy.registerEntityRendering();

        proxy.registerTileEntityRendering();

        ColonyManager.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    }

    public static boolean isClient()
    {
        return proxy.isClient();
    }

    public static boolean isServer()
    {
        return !proxy.isClient();
    }
}