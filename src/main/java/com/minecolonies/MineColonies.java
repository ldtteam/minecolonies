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
        //  ColonyView messages
        network.registerMessage(ColonyViewMessage.class,                ColonyViewMessage.class,                1,  Side.CLIENT);
        network.registerMessage(ColonyViewCitizenViewMessage.class,     ColonyViewCitizenViewMessage.class,     2,  Side.CLIENT);
        network.registerMessage(ColonyViewRemoveCitizenMessage.class,   ColonyViewRemoveCitizenMessage.class,   3,  Side.CLIENT);
        network.registerMessage(ColonyViewBuildingViewMessage.class,    ColonyViewBuildingViewMessage.class,    4,  Side.CLIENT);
        network.registerMessage(ColonyViewRemoveBuildingMessage.class,  ColonyViewRemoveBuildingMessage.class,  5,  Side.CLIENT);
        network.registerMessage(PermissionsMessage.View.class,          PermissionsMessage.View.class,          6,  Side.CLIENT);
        //  Permission Request messages
        network.registerMessage(PermissionsMessage.Permission.class,    PermissionsMessage.Permission.class,    10, Side.SERVER);
        network.registerMessage(PermissionsMessage.AddPlayer.class,     PermissionsMessage.AddPlayer.class,     11, Side.SERVER);
        network.registerMessage(PermissionsMessage.RemovePlayer.class,  PermissionsMessage.RemovePlayer.class,  12, Side.SERVER);
        network.registerMessage(PermissionsMessage.SetPlayerRank.class, PermissionsMessage.SetPlayerRank.class, 13, Side.SERVER);
        //  Colony Request messages
        network.registerMessage(BuildRequestMessage.class,              BuildRequestMessage.class,              20, Side.SERVER);
        network.registerMessage(OpenInventoryMessage.class,             OpenInventoryMessage.class,             21, Side.SERVER);
        network.registerMessage(TownhallRenameMessage.class,            TownhallRenameMessage.class,            22, Side.SERVER);
        network.registerMessage(MinerSetLevelMessage.class,              MinerSetLevelMessage.class,              23, Side.SERVER);
        network.registerMessage(FarmerCropTypeMessage.class,             FarmerCropTypeMessage.class,             24, Side.SERVER);

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