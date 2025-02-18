package com.minecolonies.apiimp;

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
import com.minecolonies.api.configuration.Configuration;
import com.minecolonies.api.crafting.registry.CraftingType;
import com.minecolonies.api.crafting.registry.RecipeTypeEntry;
import com.minecolonies.api.entity.mobs.registry.IMobAIRegistry;
import com.minecolonies.api.entity.citizen.happiness.HappinessRegistry;
import com.minecolonies.api.entity.pathfinding.registry.IPathNavigateRegistry;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import com.minecolonies.api.eventbus.DefaultEventBus;
import com.minecolonies.api.eventbus.EventBus;
import com.minecolonies.api.quests.registries.QuestRegistries;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.research.ModResearchCostTypes.ResearchCostType;
import com.minecolonies.api.research.effects.registry.ResearchEffectEntry;
import com.minecolonies.api.research.registry.ResearchRequirementEntry;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.colony.CitizenDataManager;
import com.minecolonies.core.colony.ColonyManager;
import com.minecolonies.core.colony.buildings.registry.BuildingDataManager;
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

import static com.minecolonies.api.research.ModResearchCostTypes.LIST_ITEM_COST_ID;
import static com.minecolonies.api.research.ModResearchRequirements.RESEARCH_RESEARCH_REQ_ID;
import static com.minecolonies.api.research.effects.ModResearchEffects.GLOBAL_EFFECT_ID;

public class CommonMinecoloniesAPIImpl implements IMinecoloniesAPI
{
    private final  IColonyManager                                          colonyManager          = new ColonyManager();
    private final  ICitizenDataManager                                     citizenDataManager     = new CitizenDataManager();
    private final  IMobAIRegistry                                          mobAIRegistry          = new MobAIRegistry();
    private final  IPathNavigateRegistry              pathNavigateRegistry   = new PathNavigateRegistry();
    private        IForgeRegistry<EquipmentTypeEntry> equipmentTypeRegistry;
    private        IForgeRegistry<BuildingEntry>      buildingRegistry;
    private        IForgeRegistry<FieldRegistries.FieldEntry>              fieldRegistry;
    private final  IBuildingDataManager                                    buildingDataManager    = new BuildingDataManager();
    private final  IJobDataManager                                         jobDataManager         = new JobDataManager();
    private final  IGuardTypeDataManager                                   guardTypeDataManager   = new com.minecolonies.core.colony.buildings.registry.GuardTypeDataManager();
    private        IForgeRegistry<JobEntry>                                jobRegistry;
    private        IForgeRegistry<GuardType>                               guardTypeRegistry;
    private        IForgeRegistry<InteractionResponseHandlerEntry>         interactionHandlerRegistry;
    private final  IInteractionResponseHandlerDataManager                  interactionDataManager = new InteractionResponseHandlerManager();
    private        IForgeRegistry<ColonyEventTypeRegistryEntry>            colonyEventRegistry;
    private        IForgeRegistry<ColonyEventDescriptionTypeRegistryEntry> colonyEventDescriptionRegistry;
    private static IGlobalResearchTree                                     globalResearchTree     = new GlobalResearchTree();
    private        IForgeRegistry<ResearchRequirementEntry>                researchRequirementRegistry;
    private        IForgeRegistry<ResearchEffectEntry>                     researchEffectRegistry;
    private        IForgeRegistry<ResearchCostType>                        researchCostRegistry;
    private        IForgeRegistry<RecipeTypeEntry>                         recipeTypeEntryRegistry;
    private        IForgeRegistry<CraftingType>                            craftingTypeRegistry;
    private        IForgeRegistry<QuestRegistries.ObjectiveEntry>          questObjectiveRegistry;
    private        IForgeRegistry<QuestRegistries.RewardEntry>             questRewardRegistry;
    private        IForgeRegistry<QuestRegistries.TriggerEntry>            questTriggerRegistry;
    private        IForgeRegistry<QuestRegistries.DialogueAnswerEntry>     questDialogueAnswerRegistry;
    private        IForgeRegistry<HappinessRegistry.HappinessFactorTypeEntry> happinessFactorTypeRegistry;
    private        IForgeRegistry<HappinessRegistry.HappinessFunctionEntry> happinessFunctionRegistry;

    private EventBus eventBus = new DefaultEventBus();

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
    public IForgeRegistry<BuildingEntry> getBuildingRegistry()
    {
        return buildingRegistry;
    }

    @Override
    @NotNull
    public IForgeRegistry<FieldRegistries.FieldEntry> getFieldRegistry()
    {
        return fieldRegistry;
    }

    @Override
    public IJobDataManager getJobDataManager()
    {
        return jobDataManager;
    }

    @Override
    public IForgeRegistry<JobEntry> getJobRegistry()
    {
        return jobRegistry;
    }

    @Override
    public IForgeRegistry<InteractionResponseHandlerEntry> getInteractionResponseHandlerRegistry()
    {
        return interactionHandlerRegistry;
    }

    @Override
    public IGuardTypeDataManager getGuardTypeDataManager()
    {
        return guardTypeDataManager;
    }

    @Override
    public IForgeRegistry<GuardType> getGuardTypeRegistry()
    {
        return guardTypeRegistry;
    }

    @Override
    public IModelTypeRegistry getModelTypeRegistry()
    {
        return null;
    }

    @Override
    public Configuration getConfig()
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
    public IForgeRegistry<ResearchRequirementEntry> getResearchRequirementRegistry() {return researchRequirementRegistry;}

    @Override
    public IForgeRegistry<ResearchEffectEntry> getResearchEffectRegistry() {return researchEffectRegistry;}

    @Override
    public IForgeRegistry<ResearchCostType> getResearchCostRegistry()
    {
        return researchCostRegistry;
    }

    @Override
    public void onRegistryNewRegistry(final NewRegistryEvent event)
    {
        event.create(new RegistryBuilder<EquipmentTypeEntry>()
                        .setName(new ResourceLocation(Constants.MOD_ID, "equipmenttypes"))
                        .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "null"))
                        .disableSaving()
                        .allowModification()
                        .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> equipmentTypeRegistry = b);

        event.create(new RegistryBuilder<BuildingEntry>()
                       .setName(new ResourceLocation(Constants.MOD_ID, "buildings"))
                       .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "null"))
                       .disableSaving()
                       .allowModification()
                       .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> buildingRegistry = b);

        event.create(new RegistryBuilder<FieldRegistries.FieldEntry>()
                       .setName(new ResourceLocation(Constants.MOD_ID, "fields"))
                       .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "null"))
                       .disableSaving()
                       .allowModification()
                       .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> fieldRegistry = b);

        event.create(new RegistryBuilder<JobEntry>()
                       .setName(new ResourceLocation(Constants.MOD_ID, "jobs"))
                       .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "null"))
                       .disableSaving()
                       .allowModification()
                       .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> jobRegistry = b);

        event.create(new RegistryBuilder<GuardType>()
                       .setName(new ResourceLocation(Constants.MOD_ID, "guardtypes"))
                       .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "null"))
                       .disableSaving()
                       .allowModification()
                       .setDefaultKey(ModGuardTypes.KNIGHT_ID)
                       .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> guardTypeRegistry = b);

        event.create(new RegistryBuilder<InteractionResponseHandlerEntry>()
                       .setName(new ResourceLocation(Constants.MOD_ID, "interactionresponsehandlers"))
                       .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "null"))
                       .disableSaving()
                       .allowModification()
                       .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> interactionHandlerRegistry = b);

        event.create(new RegistryBuilder<ColonyEventTypeRegistryEntry>()
                       .setName(new ResourceLocation(Constants.MOD_ID, "colonyeventtypes"))
                       .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "null"))
                       .disableSaving().allowModification()
                       .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> colonyEventRegistry = b);

        event.create(new RegistryBuilder<ColonyEventDescriptionTypeRegistryEntry>()
                       .setName(new ResourceLocation(Constants.MOD_ID, "colonyeventdesctypes"))
                       .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "null"))
                       .disableSaving().allowModification()
                       .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> colonyEventDescriptionRegistry = b);


        event.create(new RegistryBuilder<CraftingType>()
                       .setName(new ResourceLocation(Constants.MOD_ID, "craftingtypes"))
                       .disableSaving().allowModification()
                       .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> craftingTypeRegistry = b);

        event.create(new RegistryBuilder<RecipeTypeEntry>()
                       .setName(new ResourceLocation(Constants.MOD_ID, "recipetypeentries"))
                       .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "classic"))
                       .disableSaving().allowModification()
                       .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> recipeTypeEntryRegistry = b);

        event.create(new RegistryBuilder<ResearchRequirementEntry>()
                       .setName(new ResourceLocation(Constants.MOD_ID, "researchrequirementtypes"))
                       .setDefaultKey(RESEARCH_RESEARCH_REQ_ID)
                       .disableSaving().allowModification()
                       .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> researchRequirementRegistry = b);

        event.create(new RegistryBuilder<ResearchEffectEntry>()
                       .setName(new ResourceLocation(Constants.MOD_ID, "researcheffecttypes"))
                       .setDefaultKey(GLOBAL_EFFECT_ID)
                       .disableSaving().allowModification()
                       .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> researchEffectRegistry = b);

        event.create(new RegistryBuilder<ResearchCostType>()
                       .setName(new ResourceLocation(Constants.MOD_ID, "researchcosttypes"))
                       .setDefaultKey(LIST_ITEM_COST_ID)
                       .disableSaving().allowModification()
                       .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> researchCostRegistry = b);


        event.create(new RegistryBuilder<QuestRegistries.ObjectiveEntry>()
                       .setName(new ResourceLocation(Constants.MOD_ID, "questobjectives"))
                       .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "null"))
                       .disableSaving().allowModification()
                       .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> questObjectiveRegistry = b);

        event.create(new RegistryBuilder<QuestRegistries.RewardEntry>()
                       .setName(new ResourceLocation(Constants.MOD_ID, "questrewards"))
                       .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "null"))
                       .disableSaving().allowModification()
                       .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> questRewardRegistry = b);

        event.create(new RegistryBuilder<QuestRegistries.TriggerEntry>()
                       .setName(new ResourceLocation(Constants.MOD_ID, "questtriggers"))
                       .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "null"))
                       .disableSaving().allowModification()
                       .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> questTriggerRegistry = b);

        event.create(new RegistryBuilder<QuestRegistries.DialogueAnswerEntry>()
                       .setName(new ResourceLocation(Constants.MOD_ID, "questanswerresults"))
                       .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "null"))
                       .disableSaving().allowModification()
                       .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> questDialogueAnswerRegistry = b);

        event.create(new RegistryBuilder<HappinessRegistry.HappinessFactorTypeEntry>()
                       .setName(new ResourceLocation(Constants.MOD_ID, "happinessfactortypes"))
                       .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "null"))
                       .disableSaving().allowModification()
                       .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> happinessFactorTypeRegistry = b);

        event.create(new RegistryBuilder<HappinessRegistry.HappinessFunctionEntry>()
                       .setName(new ResourceLocation(Constants.MOD_ID, "happinessfunction"))
                       .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "null"))
                       .disableSaving().allowModification()
                       .setIDRange(0, Integer.MAX_VALUE - 1), (b) -> happinessFunctionRegistry = b);
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
    public IForgeRegistry<EquipmentTypeEntry> getEquipmentTypeRegistry()
    {
        return equipmentTypeRegistry;
    }

    @Override
    public EventBus getEventBus()
    {
        return eventBus;
    }
}

