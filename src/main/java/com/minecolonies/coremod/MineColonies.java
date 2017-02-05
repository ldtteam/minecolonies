package com.minecolonies.coremod;

import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.colony.Structures;
import com.minecolonies.coremod.commands.CommandEntryPoint;
import com.minecolonies.coremod.configuration.ConfigurationHandler;
import com.minecolonies.coremod.configuration.Configurations;
import com.minecolonies.coremod.lib.Constants;
import com.minecolonies.coremod.network.messages.*;
import com.minecolonies.coremod.proxy.IProxy;
import com.minecolonies.coremod.util.RecipeHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@Mod(modid = Constants.MOD_ID, name = Constants.MOD_NAME, version = Constants.VERSION,
  /*dependencies = Constants.FORGE_VERSION,*/ acceptedMinecraftVersions = Constants.MC_VERSION)
public class MineColonies
{
    /**
     * Forge created instance of the Mod.
     */
    @Mod.Instance(Constants.MOD_ID)
    public static MineColonies instance;
    /**
     * Access to the proxy associated with your current side. Variable updated by forge.
     */
    @SidedProxy(clientSide = Constants.CLIENT_PROXY_LOCATION, serverSide = Constants.SERVER_PROXY_LOCATION)
    public static IProxy       proxy;
    private static final Logger logger = LogManager.getLogger(Constants.MOD_ID);
    private static SimpleNetworkWrapper network;

    /**
     * Returns whether the side is client or not
     *
     * @return True when client, otherwise false
     */
    public static boolean isClient()
    {
        return proxy.isClient() && FMLCommonHandler.instance().getEffectiveSide().isClient();
    }

    /**
     * Returns whether the side is client or not
     *
     * @return True when server, otherwise false
     */
    public static boolean isServer()
    {
        return !proxy.isClient() && FMLCommonHandler.instance().getEffectiveSide().isServer();
    }

    /**
     * Getter for the minecolonies Logger.
     *
     * @return the logger.
     */
    public static Logger getLogger()
    {
        return logger;
    }

    /**
     * Event handler for forge pre init event.
     *
     * @param event the forge pre init event.
     */
    @Mod.EventHandler
    public void preInit(@NotNull final FMLPreInitializationEvent event)
    {
        ConfigurationHandler.init(event.getSuggestedConfigurationFile());
        proxy.registerSounds();
        proxy.registerEntities();

        proxy.registerEntityRendering();
    }

    /**
     * Event handler for forge init event.
     *
     * @param event the forge init event.
     */
    @Mod.EventHandler
    public void init(final FMLInitializationEvent event)
    {
        initializeNetwork();

        proxy.registerTileEntities();

        RecipeHandler.init(Configurations.enableInDevelopmentFeatures, Configurations.supplyChests);

        proxy.registerEvents();

        proxy.registerTileEntityRendering();

        proxy.registerRenderer();

        Structures.init();

        ModAchievements.init();
    }

    private static synchronized void initializeNetwork()
    {
        network = NetworkRegistry.INSTANCE.newSimpleChannel(Constants.MOD_NAME);

        //  ColonyView messages
        getNetwork().registerMessage(ColonyViewMessage.class, ColonyViewMessage.class, 1, Side.CLIENT);
        getNetwork().registerMessage(ColonyViewCitizenViewMessage.class, ColonyViewCitizenViewMessage.class, 2, Side.CLIENT);
        getNetwork().registerMessage(ColonyViewRemoveCitizenMessage.class, ColonyViewRemoveCitizenMessage.class, 3, Side.CLIENT);
        getNetwork().registerMessage(ColonyViewBuildingViewMessage.class, ColonyViewBuildingViewMessage.class, 4, Side.CLIENT);
        getNetwork().registerMessage(ColonyViewRemoveBuildingMessage.class, ColonyViewRemoveBuildingMessage.class, 5, Side.CLIENT);
        getNetwork().registerMessage(PermissionsMessage.View.class, PermissionsMessage.View.class, 6, Side.CLIENT);
        getNetwork().registerMessage(ColonyStylesMessage.class, ColonyStylesMessage.class, 7, Side.CLIENT);
        getNetwork().registerMessage(ColonyViewWorkOrderMessage.class, ColonyViewWorkOrderMessage.class, 8, Side.CLIENT);
        getNetwork().registerMessage(ColonyViewRemoveWorkOrderMessage.class, ColonyViewRemoveWorkOrderMessage.class, 9, Side.CLIENT);

        //  Permission Request messages
        getNetwork().registerMessage(PermissionsMessage.Permission.class, PermissionsMessage.Permission.class, 10, Side.SERVER);
        getNetwork().registerMessage(PermissionsMessage.AddPlayer.class, PermissionsMessage.AddPlayer.class, 11, Side.SERVER);
        getNetwork().registerMessage(PermissionsMessage.RemovePlayer.class, PermissionsMessage.RemovePlayer.class, 12, Side.SERVER);
        getNetwork().registerMessage(PermissionsMessage.ChangePlayerRank.class, PermissionsMessage.ChangePlayerRank.class, 13, Side.SERVER);

        //  Colony Request messages
        getNetwork().registerMessage(BuildRequestMessage.class, BuildRequestMessage.class, 20, Side.SERVER);
        getNetwork().registerMessage(OpenInventoryMessage.class, OpenInventoryMessage.class, 21, Side.SERVER);
        getNetwork().registerMessage(TownHallRenameMessage.class, TownHallRenameMessage.class, 22, Side.SERVER);
        getNetwork().registerMessage(MinerSetLevelMessage.class, MinerSetLevelMessage.class, 23, Side.SERVER);
        getNetwork().registerMessage(RecallCitizenMessage.class, RecallCitizenMessage.class, 25, Side.SERVER);
        getNetwork().registerMessage(BuildToolPlaceMessage.class, BuildToolPlaceMessage.class, 26, Side.SERVER);
        getNetwork().registerMessage(ToggleJobMessage.class, ToggleJobMessage.class, 27, Side.SERVER);
        getNetwork().registerMessage(HireFireMessage.class, HireFireMessage.class, 28, Side.SERVER);
        getNetwork().registerMessage(WorkOrderChangeMessage.class, WorkOrderChangeMessage.class, 29, Side.SERVER);
        getNetwork().registerMessage(AssignFieldMessage.class, AssignFieldMessage.class, 30, Side.SERVER);
        getNetwork().registerMessage(AssignmentModeMessage.class, AssignmentModeMessage.class, 31, Side.SERVER);
        getNetwork().registerMessage(GuardTaskMessage.class, GuardTaskMessage.class, 32, Side.SERVER);
        getNetwork().registerMessage(GuardScepterMessage.class, GuardScepterMessage.class, 33, Side.SERVER);
        getNetwork().registerMessage(RecallTownhallMessage.class, RecallTownhallMessage.class, 34, Side.SERVER);


        //Client side only
        getNetwork().registerMessage(BlockParticleEffectMessage.class, BlockParticleEffectMessage.class, 50, Side.CLIENT);
        getNetwork().registerMessage(SaveScanMessage.class, SaveScanMessage.class, 51, Side.CLIENT);
    }

    public static SimpleNetworkWrapper getNetwork()
    {
        return network;
    }

    @Mod.EventHandler
    public void postInit(final FMLPostInitializationEvent event)
    {
        // Load unimportant resources
    }

    @Mod.EventHandler
    public void serverLoad(final FMLServerStartingEvent event)
    {
        // register server commands
        event.registerServerCommand(new CommandEntryPoint());
    }
}
