package com.minecolonies.coremod;

import com.minecolonies.api.configuration.ConfigurationHandler;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.colony.requestsystem.init.RequestSystemInitializer;
import com.minecolonies.coremod.colony.requestsystem.init.StandardFactoryControllerInitializer;
import com.minecolonies.coremod.commands.CommandEntryPoint;
import com.minecolonies.coremod.network.messages.*;
import com.minecolonies.coremod.proxy.IProxy;
import com.minecolonies.coremod.util.RecipeHandler;
import gigaherz.guidebook.client.BookRegistryEvent;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber
@Mod(modid = Constants.MOD_ID, name = Constants.MOD_NAME, version = Constants.VERSION,
  guiFactory = Constants.CONFIG_GUI_LOCATION, /*dependencies = Constants.FORGE_VERSION,*/
  acceptedMinecraftVersions = Constants.MC_VERSION)
public class MineColonies
{
    private static final Logger logger = LogManager.getLogger(Constants.MOD_ID);
    /**
     * Forge created instance of the Mod.
     */
    @Mod.Instance(Constants.MOD_ID)
    public static MineColonies instance;
    /**
     * Access to the proxy associated with your current side. Variable updated
     * by forge.
     */
    @SidedProxy(clientSide = Constants.CLIENT_PROXY_LOCATION, serverSide = Constants.SERVER_PROXY_LOCATION)

    public static IProxy       proxy;

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

    @Optional.Method(modid = "gbook")
    @SubscribeEvent
    public static void registerBook(final BookRegistryEvent event)
    {
        System.out.println("Hello " + Constants.MOD_ID + ":book/minecolonies.xml");
        System.out.println(new ResourceLocation(Constants.MOD_ID + ":book/minecolonies.xml"));
        event.register(new ResourceLocation(Constants.MOD_ID + ":book/minecolonies.xml"));
    }

    /**
     * Event handler for forge pre init event.
     *
     * @param event the forge pre init event.
     */
    @Mod.EventHandler
    public void preInit(@NotNull final FMLPreInitializationEvent event)
    {
        StandardFactoryControllerInitializer.onPreInit();
        proxy.registerSounds();
        proxy.registerEntities();

        proxy.registerEntityRendering();

        ConfigurationHandler.init(event.getSuggestedConfigurationFile());

        /*@NotNull final Configuration configuration = new Configuration(event.getSuggestedConfigurationFile());
        configuration.load();

        if (configuration.hasChanged())
        {
            configuration.save();
        }*/
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

        proxy.registerEvents();

        proxy.registerTileEntityRendering();

        proxy.registerRenderer();

        ModAchievements.init();

        RecipeHandler.init(Configurations.Gameplay.enableInDevelopmentFeatures, Configurations.Gameplay.supplyChests);
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
        getNetwork().registerMessage(LumberjackSaplingSelectorMessage.class, LumberjackSaplingSelectorMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(ToggleHousingMessage.class, ToggleHousingMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(AssignUnassignMessage.class, AssignUnassignMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(LumberjackSaplingSelectorMessage.class, LumberjackSaplingSelectorMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(UpgradeWarehouseMessage.class, UpgradeWarehouseMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(BuildToolPasteMessage.class, BuildToolPasteMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(TransferItemsToCitizenRequestMessage.class, TransferItemsToCitizenRequestMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(UpdateRequestStateMessage.class, UpdateRequestStateMessage.class, ++id, Side.SERVER);

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
        RequestSystemInitializer.onPostInit();
    }

    @Mod.EventHandler
    public void serverLoad(final FMLServerStartingEvent event)
    {
        // register server commands
        event.registerServerCommand(new CommandEntryPoint());
    }
}
