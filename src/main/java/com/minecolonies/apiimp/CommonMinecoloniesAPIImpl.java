package com.minecolonies.apiimp;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import com.minecolonies.api.colony.ICitizenDataManager;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildingextensions.registry.BuildingExtensionRegistries;
import com.minecolonies.api.colony.buildingextensions.registry.BuildingExtensionRegistries.BuildingExtensionEntry;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.registry.IBuildingDataManager;
import com.minecolonies.api.colony.colonyEvents.registry.ColonyEventDescriptionTypeRegistryEntry;
import com.minecolonies.api.colony.colonyEvents.registry.ColonyEventTypeRegistryEntry;
import com.minecolonies.api.colony.guardtype.GuardType;
import com.minecolonies.api.colony.guardtype.registry.IGuardTypeDataManager;
import com.minecolonies.api.colony.guardtype.registry.ModGuardTypes;
import com.minecolonies.api.colony.interactionhandling.registry.IInteractionResponseHandlerDataManager;
import com.minecolonies.api.colony.interactionhandling.registry.InteractionResponseHandlerEntry;
import com.minecolonies.api.colony.jobs.registry.IJobDataManager;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.compatibility.IFurnaceRecipes;
import com.minecolonies.api.configuration.Configuration;
import com.minecolonies.api.crafting.registry.CraftingType;
import com.minecolonies.api.crafting.registry.RecipeTypeEntry;
import com.minecolonies.api.entity.citizen.happiness.HappinessRegistry;
import com.minecolonies.api.entity.mobs.registry.IMobAIRegistry;
import com.minecolonies.api.entity.pathfinding.registry.IPathNavigateRegistry;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import com.minecolonies.api.eventbus.DefaultEventBus;
import com.minecolonies.api.eventbus.EventBus;
import com.minecolonies.api.quests.registries.QuestRegistries;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.research.ModResearchCosts;
import com.minecolonies.api.research.ModResearchEffects;
import com.minecolonies.api.research.ModResearchRequirements;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.colony.CitizenDataManager;
import com.minecolonies.core.colony.ColonyManager;
import com.minecolonies.core.colony.buildings.registry.BuildingDataManager;
import com.minecolonies.core.colony.buildings.registry.GuardTypeDataManager;
import com.minecolonies.core.colony.interactionhandling.registry.InteractionResponseHandlerManager;
import com.minecolonies.core.colony.jobs.registry.JobDataManager;
import com.minecolonies.core.entity.mobs.registry.MobAIRegistry;
import com.minecolonies.core.entity.pathfinding.registry.PathNavigateRegistry;
import com.minecolonies.core.research.GlobalResearchTree;
import com.minecolonies.core.util.FurnaceRecipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.research.ModResearchCosts.LIST_ITEM_COST_ID;
import static com.minecolonies.api.research.ModResearchEffects.GLOBAL_EFFECT_ID;
import static com.minecolonies.api.research.ModResearchRequirements.RESEARCH_RESEARCH_REQ_ID;

/**
 * Server side implementation for the {@link IMinecoloniesAPI}.
 */
public class CommonMinecoloniesAPIImpl implements IMinecoloniesAPI
{
    /**
     * Registry keys
     */
    public static final ResourceLocation REGISTRY_KEY_COLONY_EVENTS                 = new ResourceLocation(Constants.MOD_ID, "colonyeventtypes");
    public static final ResourceLocation REGISTRY_KEY_COLONY_EVENT_DESCRIPTIONS     = new ResourceLocation(Constants.MOD_ID, "colonyeventdesctypes");
    public static final ResourceLocation REGISTRY_KEY_BUILDINGS                     = new ResourceLocation(Constants.MOD_ID, "buildings");
    public static final ResourceLocation REGISTRY_KEY_BUILDING_EXTENSIONS           = new ResourceLocation(Constants.MOD_ID, "buildingextensions");
    public static final ResourceLocation REGISTRY_KEY_JOBS                          = new ResourceLocation(Constants.MOD_ID, "jobs");
    public static final ResourceLocation REGISTRY_KEY_GUARD_TYPES                   = new ResourceLocation(Constants.MOD_ID, "guardtypes");
    public static final ResourceLocation REGISTRY_KEY_INTERACTION_RESPONSE_HANDLERS = new ResourceLocation(Constants.MOD_ID, "interactionresponsehandlers");
    public static final ResourceLocation REGISTRY_KEY_RECIPE_TYPES                  = new ResourceLocation(Constants.MOD_ID, "recipetypeentries");
    public static final ResourceLocation REGISTRY_KEY_CRAFTING_TYPES                = new ResourceLocation(Constants.MOD_ID, "craftingtypes");
    public static final ResourceLocation REGISTRY_KEY_RESEARCH_REQUIREMENT_TYPES    = new ResourceLocation(Constants.MOD_ID, "researchrequirementtypes");
    public static final ResourceLocation REGISTRY_KEY_RESEARCH_COST_TYPES           = new ResourceLocation(Constants.MOD_ID, "researchrequirementtypes");
    public static final ResourceLocation REGISTRY_KEY_RESEARCH_EFFECT_TYPES         = new ResourceLocation(Constants.MOD_ID, "researcheffecttypes");
    public static final ResourceLocation REGISTRY_KEY_HAPPINESS_FACTOR_TYPES        = new ResourceLocation(Constants.MOD_ID, "happinessfactortypes");
    public static final ResourceLocation REGISTRY_KEY_HAPPINESS_FUNCTIONS           = new ResourceLocation(Constants.MOD_ID, "happinessfunction");
    public static final ResourceLocation REGISTRY_KEY_QUEST_OBJECTIVES              = new ResourceLocation(Constants.MOD_ID, "questobjectives");
    public static final ResourceLocation REGISTRY_KEY_QUEST_REWARDS                 = new ResourceLocation(Constants.MOD_ID, "questrewards");
    public static final ResourceLocation REGISTRY_KEY_QUEST_TRIGGERS                = new ResourceLocation(Constants.MOD_ID, "questtriggers");
    public static final ResourceLocation REGISTRY_KEY_QUEST_ANSWER_RESULTS          = new ResourceLocation(Constants.MOD_ID, "questanswerresults");
    public static final ResourceLocation REGISTRY_KEY_EQUIPMENT_TYPES               = new ResourceLocation(Constants.MOD_ID, "equipmenttypes");

    private static final ResourceLocation REGISTRY_VALUE_EMPTY = new ResourceLocation(Constants.MOD_ID, "null");

    /**
     * The event bus instance.
     */
    private final EventBus eventBus = new DefaultEventBus();

    /**
     * Data manager instances.
     */
    private final IColonyManager                         colonyManager          = new ColonyManager();
    private final IBuildingDataManager                   buildingDataManager    = new BuildingDataManager();
    private final ICitizenDataManager                    citizenDataManager     = new CitizenDataManager();
    private final IJobDataManager                        jobDataManager         = new JobDataManager();
    private final IGuardTypeDataManager                  guardTypeDataManager   = new GuardTypeDataManager();
    private final IInteractionResponseHandlerDataManager interactionDataManager = new InteractionResponseHandlerManager();

    /**
     * Custom registries.
     */
    private final IMobAIRegistry        mobAIRegistry        = new MobAIRegistry();
    private final IPathNavigateRegistry pathNavigateRegistry = new PathNavigateRegistry();

    /**
     * Other instances.
     */
    private final IGlobalResearchTree globalResearchTree = new GlobalResearchTree();
    private final IFurnaceRecipes     furnaceRecipes     = new FurnaceRecipes();

    /**
     * The forge registries.
     */
    private IForgeRegistry<ColonyEventTypeRegistryEntry>                       colonyEventRegistry;
    private IForgeRegistry<ColonyEventDescriptionTypeRegistryEntry>            colonyEventDescriptionRegistry;
    private IForgeRegistry<BuildingEntry>                                      buildingRegistry;
    private IForgeRegistry<BuildingExtensionRegistries.BuildingExtensionEntry> buildingExtensionRegistry;
    private IForgeRegistry<JobEntry>                                           jobRegistry;
    private IForgeRegistry<GuardType>                                          guardTypeRegistry;
    private IForgeRegistry<InteractionResponseHandlerEntry>                    interactionHandlerRegistry;
    private IForgeRegistry<ModResearchRequirements.ResearchRequirementEntry>   researchRequirementRegistry;
    private IForgeRegistry<ModResearchCosts.ResearchCostEntry>                 researchCostRegistry;
    private IForgeRegistry<ModResearchEffects.ResearchEffectEntry>             researchEffectRegistry;
    private IForgeRegistry<RecipeTypeEntry>                                    recipeTypeEntryRegistry;
    private IForgeRegistry<CraftingType>                                       craftingTypeRegistry;
    private IForgeRegistry<HappinessRegistry.HappinessFactorTypeEntry>         happinessFactorTypeRegistry;
    private IForgeRegistry<HappinessRegistry.HappinessFunctionEntry>           happinessFunctionRegistry;
    private IForgeRegistry<QuestRegistries.ObjectiveEntry>                     questObjectiveRegistry;
    private IForgeRegistry<QuestRegistries.RewardEntry>                        questRewardRegistry;
    private IForgeRegistry<QuestRegistries.TriggerEntry>                       questTriggerRegistry;
    private IForgeRegistry<QuestRegistries.DialogueAnswerEntry>                questDialogueAnswerRegistry;
    private IForgeRegistry<EquipmentTypeEntry>                                 equipmentTypeRegistry;

    @Override
    public void onRegistryNewRegistry(final NewRegistryEvent event)
    {
        event.create(new RegistryBuilder<ColonyEventTypeRegistryEntry>().setName(REGISTRY_KEY_COLONY_EVENTS)
            .setDefaultKey(REGISTRY_VALUE_EMPTY)
            .disableSaving()
            .allowModification()
            .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> colonyEventRegistry = b);

        event.create(new RegistryBuilder<ColonyEventDescriptionTypeRegistryEntry>().setName(REGISTRY_KEY_COLONY_EVENT_DESCRIPTIONS)
            .setDefaultKey(REGISTRY_VALUE_EMPTY)
            .disableSaving()
            .allowModification()
            .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> colonyEventDescriptionRegistry = b);

        event.create(new RegistryBuilder<BuildingEntry>().setName(REGISTRY_KEY_BUILDINGS)
            .setDefaultKey(REGISTRY_VALUE_EMPTY)
            .disableSaving()
            .allowModification()
            .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> buildingRegistry = b);

        event.create(new RegistryBuilder<BuildingExtensionEntry>().setName(REGISTRY_KEY_BUILDING_EXTENSIONS)
            .setDefaultKey(REGISTRY_VALUE_EMPTY)
            .disableSaving()
            .allowModification()
            .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> buildingExtensionRegistry = b);

        event.create(new RegistryBuilder<JobEntry>().setName(REGISTRY_KEY_JOBS)
            .setDefaultKey(REGISTRY_VALUE_EMPTY)
            .disableSaving()
            .allowModification()
            .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> jobRegistry = b);

        event.create(new RegistryBuilder<GuardType>().setName(REGISTRY_KEY_GUARD_TYPES)
            .setDefaultKey(REGISTRY_VALUE_EMPTY)
            .disableSaving()
            .allowModification()
            .setDefaultKey(ModGuardTypes.KNIGHT_ID)
            .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> guardTypeRegistry = b);

        event.create(new RegistryBuilder<InteractionResponseHandlerEntry>().setName(REGISTRY_KEY_INTERACTION_RESPONSE_HANDLERS)
            .setDefaultKey(REGISTRY_VALUE_EMPTY)
            .disableSaving()
            .allowModification()
            .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> interactionHandlerRegistry = b);

        event.create(new RegistryBuilder<RecipeTypeEntry>().setName(REGISTRY_KEY_RECIPE_TYPES)
            .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "classic"))
            .disableSaving()
            .allowModification()
            .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> recipeTypeEntryRegistry = b);

        event.create(new RegistryBuilder<CraftingType>().setName(REGISTRY_KEY_CRAFTING_TYPES)
            .disableSaving()
            .allowModification()
            .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> craftingTypeRegistry = b);

        event.create(new RegistryBuilder<ModResearchRequirements.ResearchRequirementEntry>().setName(REGISTRY_KEY_RESEARCH_REQUIREMENT_TYPES)
            .setDefaultKey(RESEARCH_RESEARCH_REQ_ID)
            .disableSaving()
            .allowModification()
            .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> researchRequirementRegistry = b);

        event.create(new RegistryBuilder<ModResearchCosts.ResearchCostEntry>().setName(REGISTRY_KEY_RESEARCH_COST_TYPES)
            .setDefaultKey(LIST_ITEM_COST_ID)
            .disableSaving()
            .allowModification()
            .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> researchCostRegistry = b);

        event.create(new RegistryBuilder<ModResearchEffects.ResearchEffectEntry>().setName(REGISTRY_KEY_RESEARCH_EFFECT_TYPES)
            .setDefaultKey(GLOBAL_EFFECT_ID)
            .disableSaving()
            .allowModification()
            .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> researchEffectRegistry = b);

        event.create(new RegistryBuilder<HappinessRegistry.HappinessFactorTypeEntry>().setName(REGISTRY_KEY_HAPPINESS_FACTOR_TYPES)
            .setDefaultKey(REGISTRY_VALUE_EMPTY)
            .disableSaving()
            .allowModification()
            .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> happinessFactorTypeRegistry = b);

        event.create(new RegistryBuilder<HappinessRegistry.HappinessFunctionEntry>().setName(REGISTRY_KEY_HAPPINESS_FUNCTIONS)
            .setDefaultKey(REGISTRY_VALUE_EMPTY)
            .disableSaving()
            .allowModification()
            .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> happinessFunctionRegistry = b);

        event.create(new RegistryBuilder<QuestRegistries.ObjectiveEntry>().setName(REGISTRY_KEY_QUEST_OBJECTIVES)
            .setDefaultKey(REGISTRY_VALUE_EMPTY)
            .disableSaving()
            .allowModification()
            .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> questObjectiveRegistry = b);

        event.create(new RegistryBuilder<QuestRegistries.RewardEntry>().setName(REGISTRY_KEY_QUEST_REWARDS)
            .setDefaultKey(REGISTRY_VALUE_EMPTY)
            .disableSaving()
            .allowModification()
            .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> questRewardRegistry = b);

        event.create(new RegistryBuilder<QuestRegistries.TriggerEntry>().setName(REGISTRY_KEY_QUEST_TRIGGERS)
            .setDefaultKey(REGISTRY_VALUE_EMPTY)
            .disableSaving()
            .allowModification()
            .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> questTriggerRegistry = b);

        event.create(new RegistryBuilder<QuestRegistries.DialogueAnswerEntry>().setName(REGISTRY_KEY_QUEST_ANSWER_RESULTS)
            .setDefaultKey(REGISTRY_VALUE_EMPTY)
            .disableSaving()
            .allowModification()
            .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> questDialogueAnswerRegistry = b);

        event.create(new RegistryBuilder<EquipmentTypeEntry>().setName(REGISTRY_KEY_EQUIPMENT_TYPES)
            .setDefaultKey(REGISTRY_VALUE_EMPTY)
            .disableSaving()
            .allowModification()
            .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> equipmentTypeRegistry = b);
    }

    @Override
    public Configuration getConfig()
    {
        return MineColonies.getConfig();
    }

    @Override
    public EventBus getEventBus()
    {
        return eventBus;
    }

    @Override
    @NotNull
    public IColonyManager getColonyManager()
    {
        return colonyManager;
    }

    @Override
    @NotNull
    public IBuildingDataManager getBuildingDataManager()
    {
        return buildingDataManager;
    }

    @Override
    @NotNull
    public ICitizenDataManager getCitizenDataManager()
    {
        return citizenDataManager;
    }

    @Override
    public IJobDataManager getJobDataManager()
    {
        return jobDataManager;
    }

    @Override
    public IGuardTypeDataManager getGuardTypeDataManager()
    {
        return guardTypeDataManager;
    }

    @Override
    public IInteractionResponseHandlerDataManager getInteractionResponseHandlerDataManager()
    {
        return interactionDataManager;
    }

    @Override
    public IForgeRegistry<ColonyEventTypeRegistryEntry> getColonyEventRegistry()
    {
        return colonyEventRegistry;
    }

    @Override
    public IForgeRegistry<ColonyEventDescriptionTypeRegistryEntry> getColonyEventDescriptionRegistry()
    {
        return colonyEventDescriptionRegistry;
    }

    @Override
    @NotNull
    public IForgeRegistry<BuildingEntry> getBuildingRegistry()
    {
        return buildingRegistry;
    }

    @Override
    @NotNull
    public IForgeRegistry<BuildingExtensionEntry> getBuildingExtensionRegistry()
    {
        return buildingExtensionRegistry;
    }

    @Override
    public IForgeRegistry<JobEntry> getJobRegistry()
    {
        return jobRegistry;
    }

    @Override
    public IForgeRegistry<GuardType> getGuardTypeRegistry()
    {
        return guardTypeRegistry;
    }

    @Override
    public IForgeRegistry<InteractionResponseHandlerEntry> getInteractionResponseHandlerRegistry()
    {
        return interactionHandlerRegistry;
    }

    @Override
    public IForgeRegistry<ModResearchRequirements.ResearchRequirementEntry> getResearchRequirementRegistry() {return researchRequirementRegistry;}

    @Override
    public IForgeRegistry<ModResearchCosts.ResearchCostEntry> getResearchCostRegistry()
    {
        return researchCostRegistry;
    }

    @Override
    public IForgeRegistry<ModResearchEffects.ResearchEffectEntry> getResearchEffectRegistry() {return researchEffectRegistry;}

    @Override
    public IForgeRegistry<RecipeTypeEntry> getRecipeTypeRegistry()
    {
        return recipeTypeEntryRegistry;
    }

    @Override
    public IForgeRegistry<CraftingType> getCraftingTypeRegistry()
    {
        return craftingTypeRegistry;
    }

    @Override
    public IForgeRegistry<HappinessRegistry.HappinessFactorTypeEntry> getHappinessTypeRegistry()
    {
        return happinessFactorTypeRegistry;
    }

    @Override
    public IForgeRegistry<HappinessRegistry.HappinessFunctionEntry> getHappinessFunctionRegistry()
    {
        return happinessFunctionRegistry;
    }

    @Override
    public IForgeRegistry<QuestRegistries.RewardEntry> getQuestRewardRegistry()
    {
        return questRewardRegistry;
    }

    @Override
    public IForgeRegistry<QuestRegistries.ObjectiveEntry> getQuestObjectiveRegistry()
    {
        return questObjectiveRegistry;
    }

    @Override
    public IForgeRegistry<QuestRegistries.TriggerEntry> getQuestTriggerRegistry()
    {
        return questTriggerRegistry;
    }

    @Override
    public IForgeRegistry<QuestRegistries.DialogueAnswerEntry> getQuestDialogueAnswerRegistry()
    {
        return questDialogueAnswerRegistry;
    }

    @Override
    public IForgeRegistry<EquipmentTypeEntry> getEquipmentTypeRegistry()
    {
        return equipmentTypeRegistry;
    }

    @Override
    @NotNull
    public IMobAIRegistry getMobAIRegistry()
    {
        return mobAIRegistry;
    }

    @Override
    @NotNull
    public IPathNavigateRegistry getPathNavigateRegistry()
    {
        return pathNavigateRegistry;
    }

    @Override
    public IModelTypeRegistry getModelTypeRegistry()
    {
        return null;
    }

    @Override
    public IGlobalResearchTree getGlobalResearchTree()
    {
        return globalResearchTree;
    }

    @Override
    public IFurnaceRecipes getFurnaceRecipes()
    {
        return furnaceRecipes;
    }
}

