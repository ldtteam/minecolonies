package com.minecolonies.api;

import com.ldtteam.common.config.Configurations;
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
import net.minecraft.core.Registry;
import net.neoforged.neoforge.registries.NewRegistryEvent;

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

    Registry<BuildingEntry> getBuildingRegistry();

    Registry<FieldRegistries.FieldEntry> getFieldRegistry();

    IJobDataManager getJobDataManager();

    Registry<JobEntry> getJobRegistry();

    Registry<InteractionResponseHandlerEntry> getInteractionResponseHandlerRegistry();

    IGuardTypeDataManager getGuardTypeDataManager();

    Registry<GuardType> getGuardTypeRegistry();

    IModelTypeRegistry getModelTypeRegistry();

    Configurations<ClientConfiguration, ServerConfiguration, CommonConfiguration> getConfig();

    IFurnaceRecipes getFurnaceRecipes();

    IInteractionResponseHandlerDataManager getInteractionResponseHandlerDataManager();

    IGlobalResearchTree getGlobalResearchTree();

    Registry<ResearchRequirementEntry> getResearchRequirementRegistry();

    Registry<ResearchEffectEntry> getResearchEffectRegistry();

    Registry<ColonyEventTypeRegistryEntry> getColonyEventRegistry();

    Registry<ColonyEventDescriptionTypeRegistryEntry> getColonyEventDescriptionRegistry();

    Registry<RecipeTypeEntry> getRecipeTypeRegistry();

    Registry<CraftingType> getCraftingTypeRegistry();

    Registry<QuestRegistries.RewardEntry> getQuestRewardRegistry();

    Registry<QuestRegistries.ObjectiveEntry> getQuestObjectiveRegistry();

    Registry<QuestRegistries.TriggerEntry> getQuestTriggerRegistry();

    Registry<QuestRegistries.DialogueAnswerEntry> getQuestDialogueAnswerRegistry();

    Registry<HappinessRegistry.HappinessFactorTypeEntry> getHappinessTypeRegistry();

    Registry<HappinessRegistry.HappinessFunctionEntry> getHappinessFunctionRegistry();

    void onRegistryNewRegistry(NewRegistryEvent event);

    Registry<EquipmentTypeEntry> getEquipmentTypeRegistry();
}
