package com.minecolonies;

import com.minecolonies.blocks.ModBlocks;
import com.minecolonies.colony.Schematics;
import com.minecolonies.configuration.ConfigurationHandler;
import com.minecolonies.items.ModItems;
import com.minecolonies.lib.Constants;
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
    public  static final    Logger               logger     = LogManager.getLogger(Constants.MOD_ID);

    private static          SimpleNetworkWrapper network;

    @Mod.Instance(Constants.MOD_ID)
    public static           MineColonies         instance;

    @SidedProxy(clientSide = Constants.CLIENT_PROXY_LOCATION, serverSide = Constants.SERVER_PROXY_LOCATION)
    public static           IProxy               proxy;

    public static SimpleNetworkWrapper getNetwork()
    {
        return network;
    }

    @Mod.EventHandler
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
        //not needed, code already in init
        //logger = event.getModLog();

        ConfigurationHandler.init(event.getSuggestedConfigurationFile());

        ModBlocks.init();

        ModItems.init();

        proxy.registerKeyBindings();//Schematica
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        network = NetworkRegistry.INSTANCE.newSimpleChannel(Constants.MOD_NAME);
        //  ColonyView messages
        getNetwork().registerMessage(ColonyViewMessage.class,                ColonyViewMessage.class,                1,  Side.CLIENT);
        getNetwork().registerMessage(ColonyViewCitizenViewMessage.class,     ColonyViewCitizenViewMessage.class,     2,  Side.CLIENT);
        getNetwork().registerMessage(ColonyViewRemoveCitizenMessage.class,   ColonyViewRemoveCitizenMessage.class,   3,  Side.CLIENT);
        getNetwork().registerMessage(ColonyViewBuildingViewMessage.class,    ColonyViewBuildingViewMessage.class,    4,  Side.CLIENT);
        getNetwork().registerMessage(ColonyViewRemoveBuildingMessage.class,  ColonyViewRemoveBuildingMessage.class,  5,  Side.CLIENT);
        getNetwork().registerMessage(PermissionsMessage.View.class,          PermissionsMessage.View.class,          6,  Side.CLIENT);
        getNetwork().registerMessage(ColonyStylesMessage.class,              ColonyStylesMessage.class,              7,  Side.CLIENT);
        //  Permission Request messages
        getNetwork().registerMessage(PermissionsMessage.Permission.class,    PermissionsMessage.Permission.class,    10, Side.SERVER);
        getNetwork().registerMessage(PermissionsMessage.AddPlayer.class,     PermissionsMessage.AddPlayer.class,     11, Side.SERVER);
        getNetwork().registerMessage(PermissionsMessage.RemovePlayer.class,  PermissionsMessage.RemovePlayer.class,  12, Side.SERVER);
        getNetwork().registerMessage(PermissionsMessage.SetPlayerRank.class, PermissionsMessage.SetPlayerRank.class, 13, Side.SERVER);
        //  Colony Request messages
        getNetwork().registerMessage(BuildRequestMessage.class,              BuildRequestMessage.class,              20, Side.SERVER);
        getNetwork().registerMessage(OpenInventoryMessage.class,             OpenInventoryMessage.class,             21, Side.SERVER);
        getNetwork().registerMessage(TownHallRenameMessage.class,            TownHallRenameMessage.class,            22, Side.SERVER);
        getNetwork().registerMessage(MinerSetLevelMessage.class,             MinerSetLevelMessage.class,             23, Side.SERVER);
        getNetwork().registerMessage(FarmerCropTypeMessage.class,            FarmerCropTypeMessage.class,            24, Side.SERVER);
        getNetwork().registerMessage(RecallCitizenMessage.class,             RecallCitizenMessage.class,             25, Side.SERVER);
        getNetwork().registerMessage(BuildToolPlaceMessage.class,            BuildToolPlaceMessage.class,            26, Side.SERVER);
        
        proxy.registerTileEntities();

        RecipeHandler.init();

        proxy.registerEvents();

        proxy.registerEntities();

        proxy.registerEntityRendering();

        proxy.registerTileEntityRendering();

        Schematics.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    }
}