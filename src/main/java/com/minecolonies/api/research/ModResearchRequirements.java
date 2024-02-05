package com.minecolonies.api.research;

import com.minecolonies.api.research.registry.ResearchRequirementEntry;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegistryObject;

public class ModResearchRequirements
{
    public static final ResourceLocation ALTERNATE_BUILDING_RESEARCH_REQ_ID      = new ResourceLocation(Constants.MOD_ID,"alternatebuildingresearchreq");
    public static final ResourceLocation BUILDING_RESEARCH_REQ_ID                = new ResourceLocation(Constants.MOD_ID,"buildingresearchreq");
    public static final ResourceLocation RESEARCH_RESEARCH_REQ_ID                = new ResourceLocation(Constants.MOD_ID,"researchresearchrequirement");

    public static RegistryObject<ResearchRequirementEntry> alternateBuildingResearchRequirement;
    public static RegistryObject<ResearchRequirementEntry> buildingResearchRequirement;
    public static RegistryObject<ResearchRequirementEntry> researchResearchRequirement;

    private ModResearchRequirements()
    {
        throw new IllegalStateException("Tried to initialize: ModBuildings but this is a Utility class.");
    }
}
