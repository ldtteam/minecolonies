package com.minecolonies.apiimp;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import com.minecolonies.api.colony.ICitizenDataManager;
import com.minecolonies.api.colony.IColonyManager;
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
import com.minecolonies.api.crafting.registry.RecipeTypeEntry;
import com.minecolonies.api.entity.ai.registry.IMobAIRegistry;
import com.minecolonies.api.entity.pathfinding.registry.IPathNavigateRegistry;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.research.effects.registry.ResearchEffectEntry;
import com.minecolonies.api.research.registry.ResearchRequirementEntry;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.CitizenDataManager;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.registry.BuildingDataManager;
import com.minecolonies.coremod.colony.interactionhandling.registry.InteractionResponseHandlerManager;
import com.minecolonies.coremod.colony.jobs.registry.JobDataManager;
import com.minecolonies.coremod.entity.ai.registry.MobAIRegistry;
import com.minecolonies.coremod.entity.pathfinding.registry.PathNavigateRegistry;
import com.minecolonies.coremod.research.GlobalResearchTree;
import com.minecolonies.coremod.util.FurnaceRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.research.ModResearchRequirements.RESEARCH_RESEARCH_REQ_ID;
import static com.minecolonies.api.research.effects.ModResearchEffects.GLOBAL_EFFECT_ID;

public class CommonMinecoloniesAPIImpl implements IMinecoloniesAPI
{
    private final  IColonyManager                                          colonyManager          = new ColonyManager();
    private final  ICitizenDataManager                                     citizenDataManager     = new CitizenDataManager();
    private final  IMobAIRegistry                                          mobAIRegistry          = new MobAIRegistry();
    private final  IPathNavigateRegistry                                   pathNavigateRegistry   = new PathNavigateRegistry();
    private        IForgeRegistry<BuildingEntry>                           buildingRegistry;
    private final  IBuildingDataManager                                    buildingDataManager    = new BuildingDataManager();
    private final  IJobDataManager                                         jobDataManager         = new JobDataManager();
    private final  IGuardTypeDataManager                                   guardTypeDataManager   = new com.minecolonies.coremod.colony.buildings.registry.GuardTypeDataManager();
    private        IForgeRegistry<JobEntry>                                jobRegistry;
    private        IForgeRegistry<GuardType>                               guardTypeRegistry;
    private        IForgeRegistry<InteractionResponseHandlerEntry>         interactionHandlerRegistry;
    private final  IInteractionResponseHandlerDataManager                  interactionDataManager = new InteractionResponseHandlerManager();
    private        IForgeRegistry<ColonyEventTypeRegistryEntry>            colonyEventRegistry;
    private        IForgeRegistry<ColonyEventDescriptionTypeRegistryEntry> colonyEventDescriptionRegistry;
    private static IGlobalResearchTree                                     globalResearchTree     = new GlobalResearchTree();
    private        IForgeRegistry<ResearchRequirementEntry>                researchRequirementRegistry;
    private        IForgeRegistry<ResearchEffectEntry>                     researchEffectRegistry;
    private        IForgeRegistry<RecipeTypeEntry>                         recipeTypeEntryRegistry;

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
    public IForgeRegistry<ResearchRequirementEntry> getResearchRequirementRegistry() { return researchRequirementRegistry;}

    @Override
    public IForgeRegistry<ResearchEffectEntry> getResearchEffectRegistry() { return researchEffectRegistry;}

    public void onRegistryNewRegistry(final RegistryEvent.NewRegistry event)
    {
        buildingRegistry = new RegistryBuilder<BuildingEntry>()
                             .setName(new ResourceLocation(Constants.MOD_ID, "buildings"))
                             .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "null"))
                             .disableSaving()
                             .allowModification()
                             .setType(BuildingEntry.class)
                             .setIDRange(0, Integer.MAX_VALUE - 1)
                             .create();

        jobRegistry = new RegistryBuilder<JobEntry>()
                        .setName(new ResourceLocation(Constants.MOD_ID, "jobs"))
                        .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "null"))
                        .disableSaving()
                        .allowModification()
                        .setType(JobEntry.class)
                        .setIDRange(0, Integer.MAX_VALUE - 1)
                        .create();

        guardTypeRegistry = new RegistryBuilder<GuardType>()
                              .setName(new ResourceLocation(Constants.MOD_ID, "guardtypes"))
                              .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "null"))
                              .disableSaving()
                              .allowModification()
                              .setDefaultKey(ModGuardTypes.KNIGHT_ID)
                              .setType(GuardType.class)
                              .setIDRange(0, Integer.MAX_VALUE - 1)
                              .create();

        interactionHandlerRegistry = new RegistryBuilder<InteractionResponseHandlerEntry>()
                                       .setName(new ResourceLocation(Constants.MOD_ID, "interactionresponsehandlers"))
                                       .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "null"))
                                       .disableSaving()
                                       .allowModification()
                                       .setType(InteractionResponseHandlerEntry.class)
                                       .setIDRange(0, Integer.MAX_VALUE - 1)
                                       .create();

        colonyEventRegistry = new RegistryBuilder<ColonyEventTypeRegistryEntry>()
                                .setName(new ResourceLocation(Constants.MOD_ID, "colonyeventtypes"))
                                .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "null"))
                                .disableSaving().allowModification().setType(ColonyEventTypeRegistryEntry.class)
                                .setIDRange(0, Integer.MAX_VALUE - 1).create();

        colonyEventDescriptionRegistry = new RegistryBuilder<ColonyEventDescriptionTypeRegistryEntry>()
                .setName(new ResourceLocation(Constants.MOD_ID, "colonyeventdesctypes"))
                .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "null"))
                .disableSaving().allowModification().setType(ColonyEventDescriptionTypeRegistryEntry.class)
                .setIDRange(0, Integer.MAX_VALUE - 1).create();

        recipeTypeEntryRegistry = new RegistryBuilder<RecipeTypeEntry>()
                                    .setName(new ResourceLocation(Constants.MOD_ID, "recipetypeentries"))
                                    .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "classic"))
                                    .disableSaving().allowModification().setType(RecipeTypeEntry.class)
                                    .setIDRange(0, Integer.MAX_VALUE - 1).create();

        researchRequirementRegistry = new RegistryBuilder<ResearchRequirementEntry>()
                                        .setName(new ResourceLocation(Constants.MOD_ID, "researchrequirementtypes"))
                                        .setDefaultKey(RESEARCH_RESEARCH_REQ_ID)
                                        .disableSaving().allowModification().setType(ResearchRequirementEntry.class)
                                        .setIDRange(0, Integer.MAX_VALUE - 1).create();

        researchEffectRegistry = new RegistryBuilder<ResearchEffectEntry>()
                                        .setName(new ResourceLocation(Constants.MOD_ID, "researcheffecttypes"))
                                        .setDefaultKey(GLOBAL_EFFECT_ID)
                                        .disableSaving().allowModification().setType(ResearchEffectEntry.class)
                                        .setIDRange(0, Integer.MAX_VALUE - 1).create();
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
}

