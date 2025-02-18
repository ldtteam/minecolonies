package com.minecolonies.api;

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
import com.minecolonies.api.eventbus.EventBus;
import com.minecolonies.api.quests.registries.QuestRegistries;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.research.ModResearchCostTypes.ResearchCostType;
import com.minecolonies.api.research.effects.registry.ResearchEffectEntry;
import com.minecolonies.api.research.registry.ResearchRequirementEntry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;

public interface IMinecoloniesAPI
{

    static IMinecoloniesAPI getInstance()
    {
        return MinecoloniesAPIProxy.getInstance();
    }

    IColonyManager getColonyManager();

    ICitizenDataManager getCitizenDataManager();

    IMobAIRegistry getMobAIRegistry();

    IPathNavigateRegistry getPathNavigateRegistry();

    IBuildingDataManager getBuildingDataManager();

    IForgeRegistry<BuildingEntry> getBuildingRegistry();

    IForgeRegistry<FieldRegistries.FieldEntry> getFieldRegistry();

    IJobDataManager getJobDataManager();

    IForgeRegistry<JobEntry> getJobRegistry();

    IForgeRegistry<InteractionResponseHandlerEntry> getInteractionResponseHandlerRegistry();

    IGuardTypeDataManager getGuardTypeDataManager();

    IForgeRegistry<GuardType> getGuardTypeRegistry();

    IModelTypeRegistry getModelTypeRegistry();

    Configuration getConfig();

    IFurnaceRecipes getFurnaceRecipes();

    IInteractionResponseHandlerDataManager getInteractionResponseHandlerDataManager();

    IGlobalResearchTree getGlobalResearchTree();

    IForgeRegistry<ResearchRequirementEntry> getResearchRequirementRegistry();

    IForgeRegistry<ResearchEffectEntry> getResearchEffectRegistry();

    IForgeRegistry<ResearchCostType> getResearchCostRegistry();

    IForgeRegistry<ColonyEventTypeRegistryEntry> getColonyEventRegistry();

    IForgeRegistry<ColonyEventDescriptionTypeRegistryEntry> getColonyEventDescriptionRegistry();

    IForgeRegistry<RecipeTypeEntry> getRecipeTypeRegistry();

    IForgeRegistry<CraftingType> getCraftingTypeRegistry();

    IForgeRegistry<QuestRegistries.RewardEntry> getQuestRewardRegistry();

    IForgeRegistry<QuestRegistries.ObjectiveEntry> getQuestObjectiveRegistry();

    IForgeRegistry<QuestRegistries.TriggerEntry> getQuestTriggerRegistry();

    IForgeRegistry<QuestRegistries.DialogueAnswerEntry> getQuestDialogueAnswerRegistry();

    IForgeRegistry<HappinessRegistry.HappinessFactorTypeEntry> getHappinessTypeRegistry();

    IForgeRegistry<HappinessRegistry.HappinessFunctionEntry> getHappinessFunctionRegistry();

    void onRegistryNewRegistry(NewRegistryEvent event);

    IForgeRegistry<EquipmentTypeEntry> getEquipmentTypeRegistry();

    EventBus getEventBus();
}
