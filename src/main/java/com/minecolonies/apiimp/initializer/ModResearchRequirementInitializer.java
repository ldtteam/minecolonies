package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.research.ModResearchRequirements;
import com.minecolonies.api.research.registry.ResearchRequirementEntry;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.apiimp.CommonMinecoloniesAPIImpl;
import com.minecolonies.core.research.AlternateBuildingResearchRequirement;
import com.minecolonies.core.research.BuildingResearchRequirement;
import com.minecolonies.core.research.ResearchResearchRequirement;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.minecolonies.api.research.ModResearchRequirements.*;

public class ModResearchRequirementInitializer
{
    public final static DeferredRegister<ResearchRequirementEntry> DEFERRED_REGISTER = DeferredRegister.create(CommonMinecoloniesAPIImpl.RESEARCH_REQUIREMENT_TYPES, Constants.MOD_ID);

    private ModResearchRequirementInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModResearchRequirementInitializer but this is a Utility class.");
    }

    static
    {
        ModResearchRequirements.alternateBuildingResearchRequirement = DEFERRED_REGISTER.register(ALTERNATE_BUILDING_RESEARCH_REQ_ID.getPath(), () -> new ResearchRequirementEntry.Builder()
                                                                         .setReadFromNBT(AlternateBuildingResearchRequirement::new)
                                                                         .setRegistryName(ALTERNATE_BUILDING_RESEARCH_REQ_ID)
                                                                         .createResearchRequirementEntry());
        ModResearchRequirements.buildingResearchRequirement = DEFERRED_REGISTER.register(BUILDING_RESEARCH_REQ_ID.getPath(), () -> new ResearchRequirementEntry.Builder()
                                                                         .setReadFromNBT(BuildingResearchRequirement::new)
                                                                         .setRegistryName(BUILDING_RESEARCH_REQ_ID)
                                                                         .createResearchRequirementEntry());
        ModResearchRequirements.researchResearchRequirement = DEFERRED_REGISTER.register(RESEARCH_RESEARCH_REQ_ID.getPath(), () -> new ResearchRequirementEntry.Builder()
                                                                         .setReadFromNBT(ResearchResearchRequirement::new)
                                                                         .setRegistryName(RESEARCH_RESEARCH_REQ_ID)
                                                                         .createResearchRequirementEntry());
    }
}
