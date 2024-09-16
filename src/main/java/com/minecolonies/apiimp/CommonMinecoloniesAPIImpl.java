package com.minecolonies.apiimp;

import com.ldtteam.common.config.Configurations;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import com.minecolonies.api.colony.ICitizenDataManager;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.registry.IBuildingDataManager;
import com.minecolonies.api.colony.colonyEvents.registry.ColonyEventDescriptionTypeRegistryEntry;
import com.minecolonies.api.colony.colonyEvents.registry.ColonyEventTypeRegistryEntry;
import com.minecolonies.api.colony.fields.registry.FieldRegistries;
import com.minecolonies.api.colony.guardtype.GuardType;
import com.minecolonies.api.colony.guardtype.registry.IGuardTypeDataManager;
import com.minecolonies.api.colony.guardtype.registry.ModGuardTypes;
import com.minecolonies.api.colony.interactionhandling.registry.IInteractionResponseHandlerDataManager;
import com.minecolonies.api.colony.interactionhandling.registry.InteractionResponseHandlerEntry;
import com.minecolonies.api.colony.jobs.registry.IJobDataManager;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.compatibility.IFurnaceRecipes;
import com.minecolonies.api.configuration.ClientConfiguration;
import com.minecolonies.api.configuration.CommonConfiguration;
import com.minecolonies.api.configuration.ServerConfiguration;
import com.minecolonies.api.crafting.registry.CraftingType;
import com.minecolonies.api.crafting.registry.RecipeTypeEntry;
import com.minecolonies.api.entity.mobs.registry.IMobAIRegistry;
import com.minecolonies.api.entity.citizen.happiness.HappinessRegistry;
import com.minecolonies.api.entity.pathfinding.registry.IPathNavigateRegistry;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import com.minecolonies.api.quests.registries.QuestRegistries;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.research.effects.registry.ResearchEffectEntry;
import com.minecolonies.api.research.registry.ResearchRequirementEntry;
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
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.research.ModResearchRequirements.RESEARCH_RESEARCH_REQ_ID;
import static com.minecolonies.api.research.effects.ModResearchEffects.GLOBAL_EFFECT_ID;

public class CommonMinecoloniesAPIImpl implements IMinecoloniesAPI
{
    public static final ResourceKey<Registry<BuildingEntry>> BUILDINGS = key("buildings");
    public static final ResourceKey<Registry<FieldRegistries.FieldEntry>> FIELDS = key("fields");
    public static final ResourceKey<Registry<JobEntry>> JOBS = key("jobs");
    public static final ResourceKey<Registry<GuardType>> GUARD_TYPES = key("guardtypes");
    public static final ResourceKey<Registry<InteractionResponseHandlerEntry>> INTERACTION_RESPONSE_HANDLERS = key("interactionresponsehandlers");
    public static final ResourceKey<Registry<ColonyEventTypeRegistryEntry>> COLONY_EVENT_TYPES = key("colonyeventtypes");
    public static final ResourceKey<Registry<ColonyEventDescriptionTypeRegistryEntry>> COLONY_EVENT_DESC_TYPES = key("colonyeventdesctypes");
    public static final ResourceKey<Registry<CraftingType>> CRAFTING_TYPES = key("craftingtypes");
    public static final ResourceKey<Registry<RecipeTypeEntry>> RECIPE_TYPE_ENTRIES = key("recipetypeentries");
    public static final ResourceKey<Registry<ResearchRequirementEntry>> RESEARCH_REQUIREMENT_TYPES = key("researchrequirementtypes");
    public static final ResourceKey<Registry<ResearchEffectEntry>> RESEARCH_EFFECT_TYPES = key("researcheffecttypes");
    public static final ResourceKey<Registry<QuestRegistries.ObjectiveEntry>> QUEST_OBJECTIVES = key("questobjectives");
    public static final ResourceKey<Registry<QuestRegistries.RewardEntry>> QUEST_REWARDS = key("questrewards");
    public static final ResourceKey<Registry<QuestRegistries.TriggerEntry>> QUEST_TRIGGERS = key("questtriggers");
    public static final ResourceKey<Registry<QuestRegistries.DialogueAnswerEntry>> QUEST_ANSWER_RESULTS = key("questanswerresults");
    public static final ResourceKey<Registry<HappinessRegistry.HappinessFactorTypeEntry>> HAPPINESS_FACTOR_TYPES = key("happinessfactortypes");
    public static final ResourceKey<Registry<HappinessRegistry.HappinessFunctionEntry>> HAPPINESS_FUNCTION = key("happinessfunction");
    public static final ResourceKey<Registry<EquipmentTypeEntry>> EQUIPMENT_TYPES = key("equipmenttypes");

    private final IColonyManager                         colonyManager          = new ColonyManager();
    private final ICitizenDataManager                    citizenDataManager     = new CitizenDataManager();
    private final IMobAIRegistry                         mobAIRegistry          = new MobAIRegistry();
    private final IPathNavigateRegistry                  pathNavigateRegistry   = new PathNavigateRegistry();
    private final IBuildingDataManager                   buildingDataManager    = new BuildingDataManager();
    private final IJobDataManager                        jobDataManager         = new JobDataManager();
    private final IGuardTypeDataManager                  guardTypeDataManager   = new GuardTypeDataManager();
    private final IInteractionResponseHandlerDataManager interactionDataManager = new InteractionResponseHandlerManager();
    private final IGlobalResearchTree                    globalResearchTree     = new GlobalResearchTree();

    private Registry<BuildingEntry>                              buildingRegistry;
    private Registry<FieldRegistries.FieldEntry>                 fieldRegistry;
    private Registry<JobEntry>                                   jobRegistry;
    private Registry<GuardType>                                  guardTypeRegistry;
    private Registry<InteractionResponseHandlerEntry>            interactionHandlerRegistry;
    private Registry<ColonyEventTypeRegistryEntry>               colonyEventRegistry;
    private Registry<ColonyEventDescriptionTypeRegistryEntry>    colonyEventDescriptionRegistry;
    private Registry<ResearchRequirementEntry>                   researchRequirementRegistry;
    private Registry<ResearchEffectEntry>                        researchEffectRegistry;
    private Registry<RecipeTypeEntry>                            recipeTypeEntryRegistry;
    private Registry<CraftingType>                               craftingTypeRegistry;
    private Registry<QuestRegistries.ObjectiveEntry>             questObjectiveRegistry;
    private Registry<QuestRegistries.RewardEntry>                questRewardRegistry;
    private Registry<QuestRegistries.TriggerEntry>               questTriggerRegistry;
    private Registry<QuestRegistries.DialogueAnswerEntry>        questDialogueAnswerRegistry;
    private Registry<HappinessRegistry.HappinessFactorTypeEntry> happinessFactorTypeRegistry;
    private Registry<HappinessRegistry.HappinessFunctionEntry>   happinessFunctionRegistry;
    private Registry<EquipmentTypeEntry>                         equipmentTypeRegistry;

    @Override
    @NotNull
    public IColonyManager getColonyManager()
    {
        return colonyManager;
    }

    @Override
    @NotNull
    public ICitizenDataManager getCitizenDataManager()
    {
        return citizenDataManager;
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
    @NotNull
    public IBuildingDataManager getBuildingDataManager()
    {
        return buildingDataManager;
    }

    @Override
    @NotNull
    public Registry<BuildingEntry> getBuildingRegistry()
    {
        return buildingRegistry;
    }

    @Override
    @NotNull
    public Registry<FieldRegistries.FieldEntry> getFieldRegistry()
    {
        return fieldRegistry;
    }

    @Override
    public IJobDataManager getJobDataManager()
    {
        return jobDataManager;
    }

    @Override
    public Registry<JobEntry> getJobRegistry()
    {
        return jobRegistry;
    }

    @Override
    public Registry<InteractionResponseHandlerEntry> getInteractionResponseHandlerRegistry()
    {
        return interactionHandlerRegistry;
    }

    @Override
    public IGuardTypeDataManager getGuardTypeDataManager()
    {
        return guardTypeDataManager;
    }

    @Override
    public Registry<GuardType> getGuardTypeRegistry()
    {
        return guardTypeRegistry;
    }

    @Override
    public IModelTypeRegistry getModelTypeRegistry()
    {
        return null;
    }

    @Override
    public Configurations<ClientConfiguration, ServerConfiguration, CommonConfiguration> getConfig()
    {
        return MineColonies.getConfig();
    }

    @Override
    public IFurnaceRecipes getFurnaceRecipes()
    {
        return FurnaceRecipes.getInstance();
    }

    @Override
    public IInteractionResponseHandlerDataManager getInteractionResponseHandlerDataManager()
    {
        return interactionDataManager;
    }

    @Override
    public IGlobalResearchTree getGlobalResearchTree()
    {
        return globalResearchTree;
    }

    @Override
    public Registry<ResearchRequirementEntry> getResearchRequirementRegistry() {return researchRequirementRegistry;}

    @Override
    public Registry<ResearchEffectEntry> getResearchEffectRegistry() {return researchEffectRegistry;}

    @Override
    public void onRegistryNewRegistry(final NewRegistryEvent event)
    {
        buildingRegistry = event.create(syncedRegistry(BUILDINGS));
        fieldRegistry = event.create(syncedRegistry(FIELDS));
        jobRegistry = event.create(syncedRegistry(JOBS));
        guardTypeRegistry = event.create(syncedRegistry(GUARD_TYPES, ModGuardTypes.KNIGHT_ID));
        interactionHandlerRegistry = event.create(syncedRegistry(INTERACTION_RESPONSE_HANDLERS));
        colonyEventRegistry = event.create(syncedRegistry(COLONY_EVENT_TYPES));
        colonyEventDescriptionRegistry = event.create(syncedRegistry(COLONY_EVENT_DESC_TYPES));
        craftingTypeRegistry = event.create(syncedRegistry(CRAFTING_TYPES));
        recipeTypeEntryRegistry = event.create(syncedRegistry(RECIPE_TYPE_ENTRIES, new ResourceLocation(Constants.MOD_ID, "classic")));
        researchRequirementRegistry = event.create(syncedRegistry(RESEARCH_REQUIREMENT_TYPES, RESEARCH_RESEARCH_REQ_ID));
        researchEffectRegistry = event.create(syncedRegistry(RESEARCH_EFFECT_TYPES, GLOBAL_EFFECT_ID));
        questObjectiveRegistry = event.create(syncedRegistry(QUEST_OBJECTIVES));
        questRewardRegistry = event.create(syncedRegistry(QUEST_REWARDS));
        questTriggerRegistry = event.create(syncedRegistry(QUEST_TRIGGERS));
        questDialogueAnswerRegistry = event.create(syncedRegistry(QUEST_ANSWER_RESULTS));
        happinessFactorTypeRegistry = event.create(syncedRegistry(HAPPINESS_FACTOR_TYPES));
        happinessFunctionRegistry = event.create(syncedRegistry(HAPPINESS_FUNCTION));
        equipmentTypeRegistry = event.create(syncedRegistry(EQUIPMENT_TYPES));
    }

    private static <T> ResourceKey<Registry<T>> key(final String registryName)
    {
        return ResourceKey.createRegistryKey(new ResourceLocation(Constants.MOD_ID, registryName));
    }


    private static <T> RegistryBuilder<T> syncedRegistry(final ResourceKey<Registry<T>> registryKey)
    {
        return syncedRegistry(registryKey, new ResourceLocation(Constants.MOD_ID, "null"));
    }

    private static <T> RegistryBuilder<T> syncedRegistry(final ResourceKey<Registry<T>> registryKey, final ResourceLocation defaultKey)
    {
        return new RegistryBuilder<T>(registryKey).sync(true).defaultKey(defaultKey);
    }

    @Override
    public Registry<ColonyEventTypeRegistryEntry> getColonyEventRegistry()
    {
        return colonyEventRegistry;
    }

    @Override
    public Registry<ColonyEventDescriptionTypeRegistryEntry> getColonyEventDescriptionRegistry()
    {
        return colonyEventDescriptionRegistry;
    }

    @Override
    public Registry<RecipeTypeEntry> getRecipeTypeRegistry()
    {
        return recipeTypeEntryRegistry;
    }

    @Override
    public Registry<CraftingType> getCraftingTypeRegistry()
    {
        return craftingTypeRegistry;
    }

    @Override
    public Registry<QuestRegistries.RewardEntry> getQuestRewardRegistry()
    {
        return questRewardRegistry;
    }

    @Override
    public Registry<QuestRegistries.ObjectiveEntry> getQuestObjectiveRegistry()
    {
        return questObjectiveRegistry;
    }

    @Override
    public Registry<QuestRegistries.TriggerEntry> getQuestTriggerRegistry()
    {
        return questTriggerRegistry;
    }

    @Override
    public Registry<QuestRegistries.DialogueAnswerEntry> getQuestDialogueAnswerRegistry()
    {
        return questDialogueAnswerRegistry;
    }

    @Override
    public Registry<HappinessRegistry.HappinessFactorTypeEntry> getHappinessTypeRegistry()
    {
        return happinessFactorTypeRegistry;
    }

    @Override
    public Registry<HappinessRegistry.HappinessFunctionEntry> getHappinessFunctionRegistry()
    {
        return happinessFunctionRegistry;
    }

    @Override
    public Registry<EquipmentTypeEntry> getEquipmentTypeRegistry()
    {
        return equipmentTypeRegistry;
    }
}

