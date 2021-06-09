package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.research.ModResearchRequirements;
import com.minecolonies.api.research.registry.ResearchRequirementEntry;
import com.minecolonies.coremod.research.AlternateBuildingResearchRequirement;
import com.minecolonies.coremod.research.BuildingResearchRequirement;
import com.minecolonies.coremod.research.ResearchResearchRequirement;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

import static com.minecolonies.api.research.ModResearchRequirements.*;

public class ModResearchRequirementInitializer
{
    private ModResearchRequirementInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModResearchRequirementInitializer but this is a Utility class.");
    }

    public static void init(final RegistryEvent.Register<ResearchRequirementEntry> event)
    {
        final IForgeRegistry<ResearchRequirementEntry> reg = event.getRegistry();

        ModResearchRequirements.alternateBuildingResearchRequirement = new ResearchRequirementEntry.Builder()
                                                                         .setReadFromNBT(AlternateBuildingResearchRequirement::new)
                                                                         .setRegistryName(ALTERNATE_BUILDING_RESEARCH_REQ_ID)
                                                                         .createResearchRequirementEntry();
        ModResearchRequirements.buildingResearchRequirement = new ResearchRequirementEntry.Builder()
                                                                         .setReadFromNBT(BuildingResearchRequirement::new)
                                                                         .setRegistryName(BUILDING_RESEARCH_REQ_ID)
                                                                         .createResearchRequirementEntry();
        ModResearchRequirements.researchResearchRequirement = new ResearchRequirementEntry.Builder()
                                                                         .setReadFromNBT(ResearchResearchRequirement::new)
                                                                         .setRegistryName(RESEARCH_RESEARCH_REQ_ID)
                                                                         .createResearchRequirementEntry();

        reg.register(ModResearchRequirements.alternateBuildingResearchRequirement);
        reg.register(ModResearchRequirements.buildingResearchRequirement);
        reg.register(ModResearchRequirements.researchResearchRequirement);
    }
}
