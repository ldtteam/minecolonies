package com.minecolonies.api;

import com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import com.minecolonies.api.colony.ICitizenDataManager;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildingextensions.registry.BuildingExtensionRegistries.BuildingExtensionEntry;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.registry.IBuildingDataManager;
import com.minecolonies.api.colony.colonyEvents.registry.ColonyEventDescriptionTypeRegistryEntry;
import com.minecolonies.api.colony.colonyEvents.registry.ColonyEventTypeRegistryEntry;
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
import com.minecolonies.api.entity.citizen.happiness.HappinessRegistry;
import com.minecolonies.api.entity.mobs.registry.IMobAIRegistry;
import com.minecolonies.api.entity.pathfinding.registry.IPathNavigateRegistry;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import com.minecolonies.api.eventbus.EventBus;
import com.minecolonies.api.quests.registries.QuestRegistries;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.research.ModResearchCosts;
import com.minecolonies.api.research.ModResearchEffects;
import com.minecolonies.api.research.ModResearchRequirements;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;

/**
 * The interface for our common APIs able to access all the registries and manager classes we provide.
 */
public interface IMinecoloniesAPI
{
    /**
     * Get the API instance.
     *
     * @return the singleton instance.
     */
    static IMinecoloniesAPI getInstance()
    {
        return MinecoloniesAPIProxy.getInstance();
    }

    /**
     * Handle the new registry event for making sure all registries are provided on the API instance.
     *
     * @param event the registry event.
     */
    void onRegistryNewRegistry(final NewRegistryEvent event);

    /**
     * Getter for the configuration instance.
     *
     * @return the mod configuration.
     */
    Configuration getConfig();

    /**
     * Getter for the mod event bus. Used for hooking into specific events of our API.
     *
     * @return the mod event bus instance.
     */
    EventBus getEventBus();

    /**
     * Get the colony manager. This is the primary container class required to obtain any colony data.
     *
     * @return the colony manager instance.
     */
    IColonyManager getColonyManager();

    // DATA MANAGERS

    /**
     * Get the building data manager.
     *
     * @return the data manager instance.
     */
    IBuildingDataManager getBuildingDataManager();

    /**
     * Get the citizen data manager.
     *
     * @return the data manager instance.
     */
    ICitizenDataManager getCitizenDataManager();

    /**
     * Get the job data manager.
     *
     * @return the data manager instance.
     */
    IJobDataManager getJobDataManager();

    /**
     * Get the guard type data manager.
     *
     * @return the data manager instance.
     */
    IGuardTypeDataManager getGuardTypeDataManager();

    /**
     * Get the interation response data manager.
     *
     * @return the data manager instance.
     */
    IInteractionResponseHandlerDataManager getInteractionResponseHandlerDataManager();

    // FORGE REGISTRIES

    /**
     * Get the registry for colony events.
     *
     * @return the registry instance.
     */
    IForgeRegistry<ColonyEventTypeRegistryEntry> getColonyEventRegistry();

    /**
     * Get the registry for colony event descriptions.
     *
     * @return the registry instance.
     */
    IForgeRegistry<ColonyEventDescriptionTypeRegistryEntry> getColonyEventDescriptionRegistry();

    /**
     * Get the registry for buildings.
     *
     * @return the registry instance.
     */
    IForgeRegistry<BuildingEntry> getBuildingRegistry();

    /**
     * Get the registry for building extensions.
     *
     * @return the registry instance.
     */
    IForgeRegistry<BuildingExtensionEntry> getBuildingExtensionRegistry();

    /**
     * Get the registry for jobs.
     *
     * @return the registry instance.
     */
    IForgeRegistry<JobEntry> getJobRegistry();

    /**
     * Get the registry for guard types.
     *
     * @return the registry instance.
     */
    IForgeRegistry<GuardType> getGuardTypeRegistry();

    /**
     * Get the registry for interaction response handlers.
     *
     * @return the registry instance.
     */
    IForgeRegistry<InteractionResponseHandlerEntry> getInteractionResponseHandlerRegistry();

    /**
     * Get the registry for research requirements.
     *
     * @return the registry instance.
     */
    IForgeRegistry<ModResearchRequirements.ResearchRequirementEntry> getResearchRequirementRegistry();

    /**
     * Get the registry for research costs.
     *
     * @return the registry instance.
     */
    IForgeRegistry<ModResearchCosts.ResearchCostEntry> getResearchCostRegistry();

    /**
     * Get the registry for research effects.
     *
     * @return the registry instance.
     */
    IForgeRegistry<ModResearchEffects.ResearchEffectEntry> getResearchEffectRegistry();

    /**
     * Get the registry for recipe types.
     *
     * @return the registry instance.
     */
    IForgeRegistry<RecipeTypeEntry> getRecipeTypeRegistry();

    /**
     * Get the registry for crafting types.
     *
     * @return the registry instance.
     */
    IForgeRegistry<CraftingType> getCraftingTypeRegistry();

    /**
     * Get the registry for happiness types.
     *
     * @return the registry instance.
     */
    IForgeRegistry<HappinessRegistry.HappinessFactorTypeEntry> getHappinessTypeRegistry();

    /**
     * Get the registry for happiness functions.
     *
     * @return the registry instance.
     */
    IForgeRegistry<HappinessRegistry.HappinessFunctionEntry> getHappinessFunctionRegistry();

    /**
     * Get the registry for quest rewards.
     *
     * @return the registry instance.
     */
    IForgeRegistry<QuestRegistries.RewardEntry> getQuestRewardRegistry();

    /**
     * Get the registry for quest objectives.
     *
     * @return the registry instance.
     */
    IForgeRegistry<QuestRegistries.ObjectiveEntry> getQuestObjectiveRegistry();

    /**
     * Get the registry for quest triggers.
     *
     * @return the registry instance.
     */
    IForgeRegistry<QuestRegistries.TriggerEntry> getQuestTriggerRegistry();

    /**
     * Get the registry for quest answers.
     *
     * @return the registry instance.
     */
    IForgeRegistry<QuestRegistries.DialogueAnswerEntry> getQuestDialogueAnswerRegistry();

    /**
     * Get the registry for equipment types.
     *
     * @return the registry instance.
     */
    IForgeRegistry<EquipmentTypeEntry> getEquipmentTypeRegistry();

    // CUSTOM REGISTRIES

    /**
     * Get the registry for mob AI.
     *
     * @return the registry instance.
     */
    IMobAIRegistry getMobAIRegistry();

    /**
     * Get the registry for path navigation.
     *
     * @return the registry instance.
     */
    IPathNavigateRegistry getPathNavigateRegistry();

    /**
     * Get the registry for model types.
     *
     * @return the registry instance.
     */
    IModelTypeRegistry getModelTypeRegistry();

    // OTHER

    /**
     * Get the global research tree instance.
     *
     * @return the research tree.
     */
    IGlobalResearchTree getGlobalResearchTree();

    /**
     * Get the furnace recipes.
     *
     * @return the furnace recipes instance.
     */
    IFurnaceRecipes getFurnaceRecipes();
}
