package com.minecolonies;

import com.minecolonies.colony.Schematics;
import com.minecolonies.configuration.ConfigurationHandler;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.messages.*;
import com.minecolonies.proxy.IProxy;
import com.minecolonies.util.RecipeHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;


@Mod(modid = Constants.MOD_ID, name = Constants.MOD_NAME, version = Constants.VERSION,
        dependencies = Constants.FORGE_VERSION, acceptedMinecraftVersions = Constants.MC_VERSION)
public class MineColonies
{
    private static SimpleNetworkWrapper network;

    /**
     * Forge created instance of the Mod.
     */
    @Mod.Instance(Constants.MOD_ID)
    public static MineColonies instance;

    /**
     * Access to the proxy associated with your current side. Variable updated by forge.
     */
    @SidedProxy(clientSide = Constants.CLIENT_PROXY_LOCATION, serverSide = Constants.SERVER_PROXY_LOCATION)
    public static IProxy proxy;

    public static SimpleNetworkWrapper getNetwork()
    {
        return network;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        ConfigurationHandler.init(event.getSuggestedConfigurationFile());
        
        proxy.registerEntities();

        proxy.registerEntityRendering();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        initializeNetwork();

        proxy.registerTileEntities();

        RecipeHandler.init(Configurations.enableInDevelopmentFeatures, Configurations.supplyChests);

        proxy.registerEvents();

        proxy.registerTileEntityRendering();
        
        proxy.registerRenderer();

        Schematics.init();
    }

    private static synchronized void initializeNetwork()
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
        getNetwork().registerMessage(PermissionsMessage.ChangePlayerRank.class, PermissionsMessage.ChangePlayerRank.class, 13, Side.SERVER);
        //  Colony Request messages
        getNetwork().registerMessage(BuildRequestMessage.class,              BuildRequestMessage.class,              20, Side.SERVER);
        getNetwork().registerMessage(OpenInventoryMessage.class,             OpenInventoryMessage.class,             21, Side.SERVER);
        getNetwork().registerMessage(TownHallRenameMessage.class,            TownHallRenameMessage.class,            22, Side.SERVER);
        getNetwork().registerMessage(MinerSetLevelMessage.class,             MinerSetLevelMessage.class,             23, Side.SERVER);
        getNetwork().registerMessage(FarmerCropTypeMessage.class,            FarmerCropTypeMessage.class,            24, Side.SERVER);
        getNetwork().registerMessage(RecallCitizenMessage.class,             RecallCitizenMessage.class,             25, Side.SERVER);
        getNetwork().registerMessage(BuildToolPlaceMessage.class,            BuildToolPlaceMessage.class,            26, Side.SERVER);
        getNetwork().registerMessage(ToggleJobMessage.class,                 ToggleJobMessage.class,                 27, Side.SERVER);
        getNetwork().registerMessage(HireFireMessage.class,                  HireFireMessage.class,                  28, Side.SERVER);

        //Client side only
        getNetwork().registerMessage(BlockParticleEffectMessage.class,       BlockParticleEffectMessage.class,       50, Side.CLIENT);

    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        // Load unimportant resources
    }

    /**
     * Returns whether the side is client or not
     *
     * @return      True when client, otherwise false
     */
    public static boolean isClient()
    {
        return proxy.isClient() && FMLCommonHandler.instance().getEffectiveSide().isClient();
    }

    /**
     * Returns whether the side is client or not
     *
     * @return      True when server, otherwise false
     */
    public static boolean isServer()
    {
        return !proxy.isClient() && FMLCommonHandler.instance().getEffectiveSide().isServer();
    }
}
