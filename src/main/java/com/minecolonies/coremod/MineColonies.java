package com.minecolonies.coremod;

import com.ldtteam.structurize.util.StructureLoadingUtils;
import com.minecolonies.api.colony.IChunkmanagerCapability;
import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.colony.IColonyManagerCapability;
import com.minecolonies.coremod.colony.requestsystem.init.RequestSystemInitializer;
import com.minecolonies.coremod.colony.requestsystem.init.StandardFactoryControllerInitializer;
import com.minecolonies.coremod.commands.CommandEntryPoint;
import com.minecolonies.coremod.commands.CommandEntryPointNew;
import com.minecolonies.coremod.event.BarbarianSpawnEventHandler;
import com.minecolonies.coremod.event.EventHandler;
import com.minecolonies.coremod.event.FMLEventHandler;
import com.minecolonies.coremod.fixers.TileEntityIdFixer;
import com.minecolonies.coremod.network.messages.*;
import com.minecolonies.coremod.placementhandlers.MinecoloniesPlacementHandlers;
import com.minecolonies.coremod.proxy.IProxy;
import com.minecolonies.coremod.util.RecipeHandler;
import net.minecraft.init.Items;
import net.minecraft.util.datafix.FixTypes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Configuration;
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
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber
@Mod(modid = Constants.MOD_ID, name = Constants.MOD_NAME, version = Constants.VERSION, dependencies="after:gbook;required-after:structurize@[0.10.79-ALPHA,);",
  /*dependencies = Constants.FORGE_VERSION,*/ acceptedMinecraftVersions = Constants.MC_VERSION)
public class MineColonies
{
    @CapabilityInject(IColonyTagCapability.class)
    public static Capability<IColonyTagCapability> CLOSE_COLONY_CAP;

    @CapabilityInject(IChunkmanagerCapability.class)
    public static Capability<IChunkmanagerCapability> CHUNK_STORAGE_UPDATE_CAP;

    @CapabilityInject(IColonyManagerCapability.class)
    public static Capability<IColonyManagerCapability> COLONY_MANAGER_CAP;

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

    static
    {
        MinecraftForge.EVENT_BUS.register(new BarbarianSpawnEventHandler());
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        MinecraftForge.EVENT_BUS.register(new FMLEventHandler());
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
     * Returns whether the side is server or not
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
        MinecraftForge.EVENT_BUS.register(proxy);

        proxy.setupApi();

        FMLCommonHandler.instance().getDataFixer().init(Constants.MOD_ID, TileEntityIdFixer.VERSION).registerFix(FixTypes.BLOCK_ENTITY, new TileEntityIdFixer());
        StructureLoadingUtils.originFolders.add(Constants.MOD_ID);
        CapabilityManager.INSTANCE.register(IColonyTagCapability.class, new IColonyTagCapability.Storage(), IColonyTagCapability.Impl::new);
        CapabilityManager.INSTANCE.register(IChunkmanagerCapability.class, new IChunkmanagerCapability.Storage(), IChunkmanagerCapability.Impl::new);
        CapabilityManager.INSTANCE.register(IColonyManagerCapability.class, new IColonyManagerCapability.Storage(), IColonyManagerCapability.Impl::new);

        StandardFactoryControllerInitializer.onPreInit();
        proxy.registerEntities();
        proxy.registerEntityRendering();
        proxy.registerEvents();

        @NotNull final Configuration configuration = new Configuration(event.getSuggestedConfigurationFile());
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

        proxy.registerEvents();

        proxy.registerTileEntityRendering();

        proxy.registerRenderer();

        ModAchievements.init();

        MinecoloniesPlacementHandlers.initHandlers();

        RecipeHandler.init(Configurations.gameplay.enableInDevelopmentFeatures, Configurations.gameplay.supplyChests);

        //Register Vanilla items with tags

        //FOOD
        OreDictionary.registerOre("food", Items.APPLE);
        OreDictionary.registerOre("food", Items.PORKCHOP);
        OreDictionary.registerOre("food", Items.BEEF);
        OreDictionary.registerOre("food", Items.BAKED_POTATO);
        OreDictionary.registerOre("food", Items.WHEAT);
        OreDictionary.registerOre("food", Items.BEETROOT);
        OreDictionary.registerOre("food", Items.BREAD);
        OreDictionary.registerOre("food", Items.CARROT);
        OreDictionary.registerOre("food", Items.CAKE);
        OreDictionary.registerOre("food", Items.CHICKEN);
        OreDictionary.registerOre("food", Items.COOKED_BEEF);
        OreDictionary.registerOre("food", Items.COOKED_CHICKEN);
        OreDictionary.registerOre("food", Items.COOKED_FISH);
        OreDictionary.registerOre("food", Items.COOKED_MUTTON);
        OreDictionary.registerOre("food", Items.COOKED_PORKCHOP);
        OreDictionary.registerOre("food", Items.COOKED_RABBIT);
        OreDictionary.registerOre("food", Items.COOKIE);
        OreDictionary.registerOre("food", Items.EGG);
        OreDictionary.registerOre("food", Items.FISH);
        OreDictionary.registerOre("food", Items.MELON);
        OreDictionary.registerOre("food", Items.MUTTON);
        OreDictionary.registerOre("food", Items.POTATO);
        OreDictionary.registerOre("food", Items.RABBIT);

        //SEEDS
        OreDictionary.registerOre("seed", Items.BEETROOT_SEEDS);
        OreDictionary.registerOre("seed", Items.MELON_SEEDS);
        OreDictionary.registerOre("seed", Items.PUMPKIN_SEEDS);
        OreDictionary.registerOre("seed", Items.WHEAT_SEEDS);
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
        getNetwork().registerMessage(UpdateChunkCapabilityMessage.class, UpdateChunkCapabilityMessage.class, ++id, Side.CLIENT);
        getNetwork().registerMessage(GuardMobAttackListMessage.class, GuardMobAttackListMessage.class, ++id, Side.CLIENT);
        getNetwork().registerMessage(HappinessDataMessage.class,HappinessDataMessage.class,++id,Side.CLIENT);

        //  Permission Request messages
        getNetwork().registerMessage(PermissionsMessage.Permission.class, PermissionsMessage.Permission.class, ++id, Side.SERVER);
        getNetwork().registerMessage(PermissionsMessage.AddPlayer.class, PermissionsMessage.AddPlayer.class, ++id, Side.SERVER);
        getNetwork().registerMessage(PermissionsMessage.RemovePlayer.class, PermissionsMessage.RemovePlayer.class, ++id, Side.SERVER);
        getNetwork().registerMessage(PermissionsMessage.ChangePlayerRank.class, PermissionsMessage.ChangePlayerRank.class, ++id, Side.SERVER);
        getNetwork().registerMessage(PermissionsMessage.AddPlayerOrFakePlayer.class, PermissionsMessage.AddPlayerOrFakePlayer.class, ++id, Side.SERVER);

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
        getNetwork().registerMessage(GuardRecalculateMessage.class, GuardRecalculateMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(MobEntryChangeMessage.class, MobEntryChangeMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(GuardScepterMessage.class, GuardScepterMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(LumberjackScepterMessage.class, LumberjackScepterMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(RecallTownhallMessage.class, RecallTownhallMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(TransferItemsRequestMessage.class, TransferItemsRequestMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(MarkBuildingDirtyMessage.class, MarkBuildingDirtyMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(ChangeFreeToInteractBlockMessage.class, ChangeFreeToInteractBlockMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(LumberjackReplantSaplingToggleMessage.class, LumberjackReplantSaplingToggleMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(LumberjackRestrictionToggleMessage.class, LumberjackRestrictionToggleMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(ToggleHousingMessage.class, ToggleHousingMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(ToggleMoveInMessage.class, ToggleMoveInMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(AssignUnassignMessage.class, AssignUnassignMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(OpenCraftingGUIMessage.class, OpenCraftingGUIMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(AddRemoveRecipeMessage.class, AddRemoveRecipeMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(ChangeRecipePriorityMessage.class, ChangeRecipePriorityMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(ChangeDeliveryPriorityMessage.class, ChangeDeliveryPriorityMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(ChangeDeliveryPriorityStateMessage.class, ChangeDeliveryPriorityStateMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(UpgradeWarehouseMessage.class, UpgradeWarehouseMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(BuildToolPasteMessage.class, BuildToolPasteMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(TransferItemsToCitizenRequestMessage.class, TransferItemsToCitizenRequestMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(UpdateRequestStateMessage.class, UpdateRequestStateMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(BuildingSetStyleMessage.class, BuildingSetStyleMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(CowboySetMilkCowsMessage.class, CowboySetMilkCowsMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(BuildingMoveMessage.class, BuildingMoveMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(RecallSingleCitizenMessage.class, RecallSingleCitizenMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(RemoveEntityMessage.class, RemoveEntityMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(AssignFilterableItemMessage.class, AssignFilterableItemMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(TeamColonyColorChangeMessage.class, TeamColonyColorChangeMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(ToggleHelpMessage.class, ToggleHelpMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(PauseCitizenMessage.class, PauseCitizenMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(RestartCitizenMessage.class, RestartCitizenMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(SortWarehouseMessage.class, SortWarehouseMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(PostBoxRequestMessage.class, PostBoxRequestMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(ComposterRetrievalMessage.class, ComposterRetrievalMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(CrusherSetModeMessage.class, CrusherSetModeMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(BuyCitizenMessage.class, BuyCitizenMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(HireMercenaryMessage.class, HireMercenaryMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(ShepherdSetDyeSheepsMessage.class, ShepherdSetDyeSheepsMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(SifterSettingsMessage.class, SifterSettingsMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(HutRenameMessage.class, HutRenameMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(BuildingHiringModeMessage.class, BuildingHiringModeMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(DecorationBuildRequestMessage.class, DecorationBuildRequestMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(DecorationControllUpdateMessage.class, DecorationControllUpdateMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(DirectPlaceMessage.class, DirectPlaceMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(EnchanterWorkerSetMessage.class, EnchanterWorkerSetMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(EnchanterQtySetMessage.class, EnchanterQtySetMessage.class, ++id, Side.SERVER);

        //Client side only
        getNetwork().registerMessage(BlockParticleEffectMessage.class, BlockParticleEffectMessage.class, ++id, Side.CLIENT);
        getNetwork().registerMessage(CompostParticleMessage.class, CompostParticleMessage.class, ++id, Side.CLIENT);
        getNetwork().registerMessage(ItemParticleEffectMessage.class, ItemParticleEffectMessage.class, ++id, Side.CLIENT);
        getNetwork().registerMessage(LocalizedParticleEffectMessage.class, LocalizedParticleEffectMessage.class, ++id, Side.CLIENT);
        getNetwork().registerMessage(UpdateChunkRangeCapabilityMessage.class, UpdateChunkRangeCapabilityMessage.class, ++id, Side.CLIENT);
        getNetwork().registerMessage(OpenSuggestionWindowMessage.class, OpenSuggestionWindowMessage.class, ++id, Side.CLIENT);
        getNetwork().registerMessage(StreamParticleEffectMessage.class, StreamParticleEffectMessage.class, ++id, Side.CLIENT);
        getNetwork().registerMessage(CircleParticleEffectMessage.class, CircleParticleEffectMessage.class, ++id, Side.CLIENT);

        //JEI Messages
        getNetwork().registerMessage(TransferRecipeCrafingTeachingMessage.class, TransferRecipeCrafingTeachingMessage.class, ++id, Side.SERVER);
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
        event.registerServerCommand(new CommandEntryPointNew());
    }
}