package com.minecolonies.coremod;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.commands.CommandEntryPoint;
import com.minecolonies.coremod.event.ClientEventHandler;
import com.minecolonies.coremod.event.EventHandler;
import com.minecolonies.coremod.event.FMLEventHandler;
import com.minecolonies.coremod.network.messages.*;
import com.minecolonies.coremod.proxy.IProxy;
import com.minecolonies.coremod.util.RecipeHandler;
import com.minecolonies.structures.event.RenderEventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
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
    private static final Logger logger = LogManager.getLogger(Constants.MOD_ID);
    /**
     * Forge created instance of the Mod.
     */
    @Mod.Instance(Constants.MOD_ID)
    public static  MineColonies         instance;
    /**
     * Access to the proxy associated with your current side. Variable updated
     * by forge.
     */
    @SidedProxy(clientSide = Constants.CLIENT_PROXY_LOCATION, serverSide = Constants.SERVER_PROXY_LOCATION)

    public static  IProxy               proxy;

    private static SimpleNetworkWrapper network;

    static
    {
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        MinecraftForge.EVENT_BUS.register(new FMLEventHandler());
        MinecraftForge.EVENT_BUS.register(new RenderEventHandler());
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
    }

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
        proxy.registerSounds();
        proxy.registerEntities();
        proxy.registerEntityRendering();

        @NotNull Configuration configuration = new Configuration(event.getSuggestedConfigurationFile());
        configuration.load();

        if (configuration.hasChanged())
        {
            configuration.save();
        }
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

        RecipeHandler.init(Configurations.gameplay.enableInDevelopmentFeatures, Configurations.gameplay.supplyChests);

        proxy.registerTileEntityRendering();

        proxy.registerRenderer();

        ModAchievements.init();
    }

    private static synchronized void initializeNetwork()
    {
        int id = 0;
        network = NetworkRegistry.INSTANCE.newSimpleChannel(Constants.MOD_NAME);

        getNetwork().registerMessage(ServerUUIDMessage.class, ServerUUIDMessage.class, ++id, Side.CLIENT);

        //  ColonyView messages
        getNetwork().registerMessage(ColonyViewMessage.class, ColonyViewMessage.class, ++id, Side.CLIENT);
        getNetwork().registerMessage(ColonyViewCitizenViewMessage.class, ColonyViewCitizenViewMessage.class, ++id, Side.CLIENT);
        getNetwork().registerMessage(ColonyViewRemoveCitizenMessage.class, ColonyViewRemoveCitizenMessage.class, ++id, Side.CLIENT);
        getNetwork().registerMessage(ColonyViewBuildingViewMessage.class, ColonyViewBuildingViewMessage.class, ++id, Side.CLIENT);
        getNetwork().registerMessage(ColonyViewRemoveBuildingMessage.class, ColonyViewRemoveBuildingMessage.class, ++id, Side.CLIENT);
        getNetwork().registerMessage(PermissionsMessage.View.class, PermissionsMessage.View.class, ++id, Side.CLIENT);
        getNetwork().registerMessage(ColonyStylesMessage.class, ColonyStylesMessage.class, ++id, Side.CLIENT);
        getNetwork().registerMessage(ColonyViewWorkOrderMessage.class, ColonyViewWorkOrderMessage.class, ++id, Side.CLIENT);
        getNetwork().registerMessage(ColonyViewRemoveWorkOrderMessage.class, ColonyViewRemoveWorkOrderMessage.class, ++id, Side.CLIENT);

        //  Permission Request messages
        getNetwork().registerMessage(PermissionsMessage.Permission.class, PermissionsMessage.Permission.class, ++id, Side.SERVER);
        getNetwork().registerMessage(PermissionsMessage.AddPlayer.class, PermissionsMessage.AddPlayer.class, ++id, Side.SERVER);
        getNetwork().registerMessage(PermissionsMessage.RemovePlayer.class, PermissionsMessage.RemovePlayer.class, ++id, Side.SERVER);
        getNetwork().registerMessage(PermissionsMessage.ChangePlayerRank.class, PermissionsMessage.ChangePlayerRank.class, ++id, Side.SERVER);

        //  Colony Request messages
        getNetwork().registerMessage(BuildRequestMessage.class, BuildRequestMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(OpenInventoryMessage.class, OpenInventoryMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(TownHallRenameMessage.class, TownHallRenameMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(MinerSetLevelMessage.class, MinerSetLevelMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(RecallCitizenMessage.class, RecallCitizenMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(BuildToolPlaceMessage.class, BuildToolPlaceMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(ToggleJobMessage.class, ToggleJobMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(HireFireMessage.class, HireFireMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(WorkOrderChangeMessage.class, WorkOrderChangeMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(AssignFieldMessage.class, AssignFieldMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(AssignmentModeMessage.class, AssignmentModeMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(GuardTaskMessage.class, GuardTaskMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(GuardScepterMessage.class, GuardScepterMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(RecallTownhallMessage.class, RecallTownhallMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(TransferItemsRequestMessage.class, TransferItemsRequestMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(MarkBuildingDirtyMessage.class, MarkBuildingDirtyMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(ChangeFreeToInteractBlockMessage.class, ChangeFreeToInteractBlockMessage.class, ++id, Side.SERVER);


        // Schematic transfer messages
        getNetwork().registerMessage(SchematicRequestMessage.class, SchematicRequestMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(SchematicSaveMessage.class, SchematicSaveMessage.class, ++id, Side.CLIENT);
        getNetwork().registerMessage(SchematicSaveMessage.class, SchematicSaveMessage.class, ++id, Side.SERVER);

        //Client side only
        getNetwork().registerMessage(BlockParticleEffectMessage.class, BlockParticleEffectMessage.class, ++id, Side.CLIENT);
        getNetwork().registerMessage(SaveScanMessage.class, SaveScanMessage.class, ++id, Side.CLIENT);
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
