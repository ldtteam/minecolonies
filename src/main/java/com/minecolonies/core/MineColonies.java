package com.minecolonies.core;

import com.ldtteam.common.config.Configurations;
import com.ldtteam.common.language.LanguageHandler;
import com.ldtteam.structurize.storage.SurvivalBlueprintHandlers;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.configuration.ClientConfiguration;
import com.minecolonies.api.configuration.CommonConfiguration;
import com.minecolonies.api.configuration.ServerConfiguration;
import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesRaider;
import com.minecolonies.api.entity.mobs.RaiderMobUtils;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.items.component.ModDataComponents;
import com.minecolonies.api.loot.ModLootConditions;
import com.minecolonies.api.sounds.ModSoundEvents;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.util.IItemHandlerCapProvider;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.apiimp.ClientMinecoloniesAPIImpl;
import com.minecolonies.apiimp.CommonMinecoloniesAPIImpl;
import com.minecolonies.apiimp.initializer.*;
import com.minecolonies.core.client.render.SpearItemTileEntityRenderer;
import com.minecolonies.core.colony.crafting.CustomRecipeManagerMessage;
import com.minecolonies.core.colony.requestsystem.init.RequestSystemInitializer;
import com.minecolonies.core.colony.requestsystem.init.StandardFactoryControllerInitializer;
import com.minecolonies.core.entity.mobs.EntityMercenary;
import com.minecolonies.core.event.*;
import com.minecolonies.core.network.messages.PermissionsMessage;
import com.minecolonies.core.network.messages.client.*;
import com.minecolonies.core.network.messages.client.colony.*;
import com.minecolonies.core.network.messages.server.*;
import com.minecolonies.core.network.messages.server.colony.*;
import com.minecolonies.core.network.messages.server.colony.building.*;
import com.minecolonies.core.network.messages.server.colony.building.builder.BuilderSelectWorkOrderMessage;
import com.minecolonies.core.network.messages.server.colony.building.enchanter.EnchanterWorkerSetMessage;
import com.minecolonies.core.network.messages.server.colony.building.fields.AssignFieldMessage;
import com.minecolonies.core.network.messages.server.colony.building.fields.AssignmentModeMessage;
import com.minecolonies.core.network.messages.server.colony.building.fields.FarmFieldPlotResizeMessage;
import com.minecolonies.core.network.messages.server.colony.building.fields.FarmFieldUpdateSeedMessage;
import com.minecolonies.core.network.messages.server.colony.building.guard.GuardSetMinePosMessage;
import com.minecolonies.core.network.messages.server.colony.building.home.AssignUnassignMessage;
import com.minecolonies.core.network.messages.server.colony.building.miner.MinerRepairLevelMessage;
import com.minecolonies.core.network.messages.server.colony.building.miner.MinerSetLevelMessage;
import com.minecolonies.core.network.messages.server.colony.building.postbox.PostBoxRequestMessage;
import com.minecolonies.core.network.messages.server.colony.building.university.TryResearchMessage;
import com.minecolonies.core.network.messages.server.colony.building.warehouse.SortWarehouseMessage;
import com.minecolonies.core.network.messages.server.colony.building.warehouse.UpgradeWarehouseMessage;
import com.minecolonies.core.network.messages.server.colony.building.worker.*;
import com.minecolonies.core.network.messages.server.colony.citizen.*;
import com.minecolonies.core.placementhandlers.PlacementHandlerInitializer;
import com.minecolonies.core.placementhandlers.main.SuppliesHandler;
import com.minecolonies.core.placementhandlers.main.SurvivalHandler;
import com.minecolonies.core.research.GlobalResearchTreeMessage;
import com.minecolonies.core.structures.MineColoniesStructures;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@Mod(Constants.MOD_ID)
public class MineColonies
{
    /**
     * The config instance.
     */
    private static Configurations<ClientConfiguration, ServerConfiguration, CommonConfiguration> config;

    public MineColonies(final FMLModContainer modContainer, final Dist dist)
    {
        final IEventBus modBus = modContainer.getEventBus();
        final IEventBus forgeBus = NeoForge.EVENT_BUS;
    
        LanguageHandler.loadLangPath("assets/minecolonies/lang/%s.json");
        config = new Configurations<>(modContainer, modBus, ClientConfiguration::new, ServerConfiguration::new, CommonConfiguration::new);

        TileEntityInitializer.BLOCK_ENTITIES.register(modBus);
        AdvancementTriggers.DEFERRED_REGISTER.register(modBus);
        ModIngredientTypeInitializer.DEFERRED_REGISTER.register(modBus);
        ModContainerInitializers.CONTAINERS.register(modBus);
        ModBuildingsInitializer.DEFERRED_REGISTER.register(modBus);
        ModBuildingExtensionsInitializer.DEFERRED_REGISTER.register(modBus);
        ModGuardTypesInitializer.DEFERRED_REGISTER.register(modBus);
        ModColonyEventDescriptionTypeInitializer.DEFERRED_REGISTER.register(modBus);
        ModResearchRequirementInitializer.DEFERRED_REGISTER.register(modBus);
        ModRecipeSerializerInitializer.RECIPE_SERIALIZER.register(modBus);
        ModRecipeSerializerInitializer.RECIPE_TYPES.register(modBus);
        ModColonyEventTypeInitializer.DEFERRED_REGISTER.register(modBus);
        ModCraftingTypesInitializer.DEFERRED_REGISTER.register(modBus);
        ModJobsInitializer.DEFERRED_REGISTER.register(modBus);
        ModRecipeTypesInitializer.DEFERRED_REGISTER.register(modBus);
        RaiderMobUtils.ATTRIBUTES.register(modBus);
        ModSoundEvents.SOUND_EVENTS.register(modBus);
        ModInteractionsInitializer.DEFERRED_REGISTER.register(modBus);
        ModResearchEffectInitializer.DEFERRED_REGISTER.register(modBus);
        ModLootConditions.DEFERRED_REGISTER.register(modBus);
        ModItemsInitializer.DEFERRED_REGISTER.register(modBus);
        ModEquipmentTypes.DEFERRED_REGISTER.register(modBus);

        ModQuestInitializer.DEFERRED_REGISTER_OBJECTIVE.register(modBus);
        ModQuestInitializer.DEFERRED_REGISTER_TRIGGER.register(modBus);
        ModQuestInitializer.DEFERRED_REGISTER_REWARD.register(modBus);
        ModQuestInitializer.DEFERRED_REGISTER_ANSWER_RESULT.register(modBus);
        ModHappinessFactorTypeInitializer.DEFERRED_REGISTER_HAPPINESS_FACTOR.register(modBus);
        ModHappinessFactorTypeInitializer.DEFERRED_REGISTER_HAPPINESS_FUNCTION.register(modBus);
        ModDataComponents.REGISTRY.register(modBus);

        ModCreativeTabs.TAB_REG.register(modBus);

        Consumer<TagsUpdatedEvent> onTagsLoaded = (event) -> ModTags.tagsLoaded = true;
        NeoForge.EVENT_BUS.addListener(onTagsLoaded);

        forgeBus.register(EventHandler.class);
        forgeBus.register(FMLEventHandler.class);
        forgeBus.register(DataPackSyncEventHandler.ServerEvents.class);
        if (dist.isClient()) 
        {
            forgeBus.register(ClientEventHandler.class);
            forgeBus.register(DataPackSyncEventHandler.ClientEvents.class);
            modBus.register(ClientRegistryHandler.class);
        }

        modBus.addListener(GatherDataHandler::dataGeneratorSetup);

        modBus.register(this.getClass());

        InteractionValidatorInitializer.init();
        switch (dist)
        {
            case CLIENT -> MinecoloniesAPIProxy.getInstance().setApiInstance(new ClientMinecoloniesAPIImpl());
            case DEDICATED_SERVER -> MinecoloniesAPIProxy.getInstance().setApiInstance(new CommonMinecoloniesAPIImpl());
        }

        MineColoniesStructures.DEFERRED_REGISTRY_STRUCTURE.register(modBus);

        SurvivalBlueprintHandlers.registerHandler(new SurvivalHandler());
        SurvivalBlueprintHandlers.registerHandler(new SuppliesHandler());

        logIncompatibilities();
    }

    @SubscribeEvent
    public static void registerNewRegistries(final NewRegistryEvent event)
    {
        MinecoloniesAPIProxy.getInstance().onRegistryNewRegistry(event);
    }

    /**
     * Event handler for forge pre init event.
     *
     * @param event the forge pre init event.
     */
    @SubscribeEvent
    public static void preInit(@NotNull final FMLCommonSetupEvent event)
    {
        StandardFactoryControllerInitializer.onPreInit();

        event.enqueueWork(ModLootConditions::init);
        event.enqueueWork(ModTags::init);
    }

    /**
     * High event priority so we register before forge defaults.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void registerCaps(final RegisterCapabilitiesEvent event)
    {
        // only register LivingEntities that have our own capability provider
        // barbs (pirates, etc.) have cap registered automatically by forge
        event.registerEntity(ItemHandler.ENTITY, ModEntities.CITIZEN, IItemHandlerCapProvider::getItemHandlerCap);
        event.registerEntity(ItemHandler.ENTITY, ModEntities.VISITOR, IItemHandlerCapProvider::getItemHandlerCap);

        // BUILDING includes types: WAREHOUSE, ENCHANTER, STASH
        event.registerBlockEntity(ItemHandler.BLOCK, MinecoloniesTileEntities.BUILDING.get(), IItemHandlerCapProvider::getItemHandlerCap);
        event.registerBlockEntity(ItemHandler.BLOCK, MinecoloniesTileEntities.ENCHANTER.get(), IItemHandlerCapProvider::getItemHandlerCap);
        event.registerBlockEntity(ItemHandler.BLOCK, MinecoloniesTileEntities.RACK.get(), IItemHandlerCapProvider::getItemHandlerCap);
        event.registerBlockEntity(ItemHandler.BLOCK, MinecoloniesTileEntities.GRAVE.get(), IItemHandlerCapProvider::getItemHandlerCap);
        event.registerBlockEntity(ItemHandler.BLOCK, MinecoloniesTileEntities.WAREHOUSE.get(), IItemHandlerCapProvider::getItemHandlerCap);
        event.registerBlockEntity(ItemHandler.BLOCK, MinecoloniesTileEntities.STASH.get(), IItemHandlerCapProvider::getItemHandlerCap);
    }

    @SubscribeEvent
    public static void createEntityAttribute(final EntityAttributeCreationEvent event)
    {
        event.put(ModEntities.CITIZEN, AbstractEntityCitizen.getDefaultAttributes().build());
        event.put(ModEntities.VISITOR, AbstractEntityCitizen.getDefaultAttributes().build());
        event.put(ModEntities.MERCENARY, EntityMercenary.getDefaultAttributes().build());
        event.put(ModEntities.BARBARIAN, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.ARCHERBARBARIAN, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CHIEFBARBARIAN, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.PHARAO, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.MUMMY, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.ARCHERMUMMY, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.PIRATE, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.ARCHERPIRATE, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CHIEFPIRATE, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.AMAZON, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.AMAZONSPEARMAN, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.AMAZONCHIEF, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.NORSEMEN_ARCHER, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.NORSEMEN_CHIEF, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.SHIELDMAIDEN, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.DROWNED_PIRATE, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.DROWNED_ARCHERPIRATE, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.DROWNED_CHIEFPIRATE, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());

        event.put(ModEntities.CAMP_BARBARIAN, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAMP_ARCHERBARBARIAN, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAMP_CHIEFBARBARIAN, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAMP_PIRATE, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAMP_ARCHERPIRATE, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAMP_CHIEFPIRATE, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAMP_PHARAO, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAMP_MUMMY, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAMP_ARCHERMUMMY, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAMP_AMAZON, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAMP_AMAZONSPEARMAN, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAMP_AMAZONCHIEF, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAMP_NORSEMEN_ARCHER, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAMP_NORSEMEN_CHIEF, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAMP_SHIELDMAIDEN, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAMP_DROWNED_PIRATE, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAMP_DROWNED_ARCHERPIRATE, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
        event.put(ModEntities.CAMP_DROWNED_CHIEFPIRATE, AbstractEntityMinecoloniesRaider.getDefaultAttributes().build());
    }

    /**
     * Called when MC loading is about to finish.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void onLoadComplete(final FMLLoadCompleteEvent event)
    {
        PlacementHandlerInitializer.initHandlers();
        RequestSystemInitializer.onPostInit();
    }

    /**
     * Get the config handler.
     *
     * @return the config handler.
     */
    public static Configurations<ClientConfiguration, ServerConfiguration, CommonConfiguration> getConfig()
    {
        return config;
    }

    @SubscribeEvent
    public static void onNetworkRegistry(final RegisterPayloadHandlersEvent event)
    {
        final String modVersion = ModList.get().getModContainerById(Constants.MOD_ID).get().getModInfo().getVersion().toString();
        final PayloadRegistrar registry = event.registrar(Constants.MOD_ID).versioned(modVersion);
        
        //  ColonyView messages
        ColonyViewMessage.TYPE.register(registry);
        ColonyViewCitizenViewMessage.TYPE.register(registry);
        ColonyViewRemoveCitizenMessage.TYPE.register(registry);
        ColonyViewBuildingViewMessage.TYPE.register(registry);
        ColonyViewRemoveBuildingMessage.TYPE.register(registry);
        ColonyViewBuildingExtensionsUpdateMessage.TYPE.register(registry);
        PermissionsMessage.View.TYPE.register(registry);
        ColonyViewWorkOrderMessage.TYPE.register(registry);
        ColonyViewRemoveWorkOrderMessage.TYPE.register(registry);
        ColonyViewResearchManagerViewMessage.TYPE.register(registry);

        //  Permission Request messages
        PermissionsMessage.Permission.TYPE.register(registry);
        PermissionsMessage.AddPlayer.TYPE.register(registry);
        PermissionsMessage.RemovePlayer.TYPE.register(registry);
        PermissionsMessage.ChangePlayerRank.TYPE.register(registry);
        PermissionsMessage.AddPlayerOrFakePlayer.TYPE.register(registry);
        PermissionsMessage.AddRank.TYPE.register(registry);
        PermissionsMessage.RemoveRank.TYPE.register(registry);
        PermissionsMessage.EditRankType.TYPE.register(registry);
        PermissionsMessage.SetSubscriber.TYPE.register(registry);

        //  Colony Request messages
        BuildRequestMessage.TYPE.register(registry);
        OpenInventoryMessage.TYPE.register(registry);
        TownHallRenameMessage.TYPE.register(registry);
        MinerSetLevelMessage.TYPE.register(registry);
        RecallCitizenMessage.TYPE.register(registry);
        HireFireMessage.TYPE.register(registry);
        WorkOrderChangeMessage.TYPE.register(registry);
        AssignFieldMessage.TYPE.register(registry);
        AssignmentModeMessage.TYPE.register(registry);
        GuardSetMinePosMessage.TYPE.register(registry);
        RecallCitizenHutMessage.TYPE.register(registry);
        TransferItemsRequestMessage.TYPE.register(registry);
        MarkBuildingDirtyMessage.TYPE.register(registry);
        ChangeFreeToInteractBlockMessage.TYPE.register(registry);
        CreateColonyMessage.TYPE.register(registry);
        ColonyDeleteOwnMessage.TYPE.register(registry);
        ColonyViewRemoveMessage.TYPE.register(registry);
        GiveToolMessage.TYPE.register(registry);
        GetColonyInfoMessage.TYPE.register(registry);
        MarkStoryReadOnItem.TYPE.register(registry);
        PickupBlockMessage.TYPE.register(registry);
        ColonyAbandonOwnMessage.TYPE.register(registry);

        AssignUnassignMessage.TYPE.register(registry);
        OpenCraftingGUIMessage.TYPE.register(registry);
        AddRemoveRecipeMessage.TYPE.register(registry);
        ChangeRecipePriorityMessage.TYPE.register(registry);
        ChangeDeliveryPriorityMessage.TYPE.register(registry);
        ForcePickupMessage.TYPE.register(registry);
        UpgradeWarehouseMessage.TYPE.register(registry);
        TransferItemsToCitizenRequestMessage.TYPE.register(registry);
        UpdateRequestStateMessage.TYPE.register(registry);
        BuildingSetStyleMessage.TYPE.register(registry);
        RecallSingleCitizenMessage.TYPE.register(registry);
        AssignFilterableItemMessage.TYPE.register(registry);
        TeamColonyColorChangeMessage.TYPE.register(registry);
        ColonyFlagChangeMessage.TYPE.register(registry);
        ColonyStructureStyleMessage.TYPE.register(registry);
        PauseCitizenMessage.TYPE.register(registry);
        RestartCitizenMessage.TYPE.register(registry);
        SortWarehouseMessage.TYPE.register(registry);
        PostBoxRequestMessage.TYPE.register(registry);
        HireMercenaryMessage.TYPE.register(registry);
        HutRenameMessage.TYPE.register(registry);
        BuildingHiringModeMessage.TYPE.register(registry);
        DecorationBuildRequestMessage.TYPE.register(registry);
        DirectPlaceMessage.TYPE.register(registry);
        TeleportToColonyMessage.TYPE.register(registry);
        EnchanterWorkerSetMessage.TYPE.register(registry);
        InteractionResponse.TYPE.register(registry);
        TryResearchMessage.TYPE.register(registry);
        HireSpiesMessage.TYPE.register(registry);
        AddMinimumStockToBuildingModuleMessage.TYPE.register(registry);
        RemoveMinimumStockFromBuildingModuleMessage.TYPE.register(registry);
        FarmFieldPlotResizeMessage.TYPE.register(registry);
        FarmFieldUpdateSeedMessage.TYPE.register(registry);
        AdjustSkillCitizenMessage.TYPE.register(registry);
        BuilderSelectWorkOrderMessage.TYPE.register(registry);
        TriggerSettingMessage.TYPE.register(registry);
        AssignFilterableEntityMessage.TYPE.register(registry);
        BuildPickUpMessage.TYPE.register(registry);
        SwitchBuildingWithToolMessage.TYPE.register(registry);
        ColonyTextureStyleMessage.TYPE.register(registry);
        MinerRepairLevelMessage.TYPE.register(registry);
        PlantationFieldBuildRequestMessage.TYPE.register(registry);
        ResetFilterableItemMessage.TYPE.register(registry);
        CourierHiringModeMessage.TYPE.register(registry);
        QuarryHiringModeMessage.TYPE.register(registry);
        ToggleRecipeMessage.TYPE.register(registry);
        ColonyNameStyleMessage.TYPE.register(registry);
        InteractionClose.TYPE.register(registry);
        AlterRestaurantMenuItemMessage.TYPE.register(registry);

        //Client side only
        BlockParticleEffectMessage.TYPE.register(registry);
        CompostParticleMessage.TYPE.register(registry);
        ItemParticleEffectMessage.TYPE.register(registry);
        LocalizedParticleEffectMessage.TYPE.register(registry);
        OpenSuggestionWindowMessage.TYPE.register(registry);
        UpdateClientWithCompatibilityMessage.TYPE.register(registry);
        CircleParticleEffectMessage.TYPE.register(registry);
        StreamParticleEffectMessage.TYPE.register(registry);
        SleepingParticleMessage.TYPE.register(registry);
        VanillaParticleMessage.TYPE.register(registry);
        StopMusicMessage.TYPE.register(registry);
        PlayAudioMessage.TYPE.register(registry);
        PlayMusicAtPosMessage.TYPE.register(registry);
        ColonyVisitorViewDataMessage.TYPE.register(registry);
        SyncPathMessage.TYPE.register(registry);
        SyncPathReachedMessage.TYPE.register(registry);
        ReactivateBuildingMessage.TYPE.register(registry);
        PlaySoundForCitizenMessage.TYPE.register(registry);
        OpenDecoBuildWindowMessage.TYPE.register(registry);
        OpenPlantationFieldBuildWindowMessage.TYPE.register(registry);
        SaveStructureNBTMessage.TYPE.register(registry);
        GlobalQuestSyncMessage.TYPE.register(registry);
        GlobalDiseaseSyncMessage.TYPE.register(registry);

        OpenBuildingUIMessage.TYPE.register(registry);
        OpenCantFoundColonyWarningMessage.TYPE.register(registry);
        OpenColonyFoundingCovenantMessage.TYPE.register(registry);
        OpenDeleteAbandonColonyMessage.TYPE.register(registry);
        OpenReactivateColonyMessage.TYPE.register(registry);

        //JEI Messages
        TransferRecipeCraftingTeachingMessage.TYPE.register(registry);

        //Advancement Messages
        OpenGuiWindowTriggerMessage.TYPE.register(registry);
        ClickGuiButtonTriggerMessage.TYPE.register(registry);

        // Colony-Independent items
        RemoveFromRallyingListMessage.TYPE.register(registry);
        ToggleBannerRallyGuardsMessage.TYPE.register(registry);

        // Research-related messages.
        GlobalResearchTreeMessage.TYPE.register(registry);

        // Crafter Recipe-related messages
        CustomRecipeManagerMessage.TYPE.register(registry);
        SwitchRecipeCraftingTeachingMessage.TYPE.register(registry);

        ColonyListMessage.TYPE.register(registry);

        // Resource scroll NBT share message
        ResourceScrollSaveWarehouseSnapshotMessage.TYPE.register(registry);
    }

    @SubscribeEvent
    static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @NotNull
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer()
            {
                return new SpearItemTileEntityRenderer();
            }
        }, ModItems.spear);
    }

    /**
     * Report known incompatibilities to the log.
     */
    private void logIncompatibilities()
    {
        if (ModList.get().getModContainerById("minecolonies_tweaks").isPresent())
        {
            Log.getLogger().warn("|======================================================================================================================================|");
            Log.getLogger().warn("|                                                                                                                                      |");
            Log.getLogger().warn("| Minecolonies has detected an addon mod that alters Minecolonies core code recklessly: 'Tweaks/Compatibility addon for Minecolonies'. |");
            Log.getLogger().warn("|          Please report any bugs or issues you find directly to the authors of this addon, as the Official Minecolonies Team          |");
            Log.getLogger().warn("|               will not be able to provide you any support with potential issues that will arise when using this addon.               |");
            Log.getLogger().warn("|                                                                                                                                      |");
            Log.getLogger().warn("|======================================================================================================================================|");
        }
    }
}
