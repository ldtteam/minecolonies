package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.research.ModResearchRequirements;
import com.minecolonies.api.research.requirements.BuildingAlternatesResearchRequirement;
import com.minecolonies.api.research.requirements.BuildingMandatoryResearchRequirement;
import com.minecolonies.api.research.requirements.BuildingResearchRequirement;
import com.minecolonies.api.research.requirements.ResearchResearchRequirement;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.apiimp.CommonMinecoloniesAPIImpl;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.minecolonies.api.research.ModResearchRequirements.*;

/**
 * Registry initializer for the {@link ModResearchRequirements}.
 */
public class ModResearchRequirementInitializer
{
    public final static DeferredRegister<ResearchRequirementEntry> DEFERRED_REGISTER = DeferredRegister.create(CommonMinecoloniesAPIImpl.RESEARCH_REQUIREMENT_TYPES, Constants.MOD_ID);
    static
    {
        buildingResearchRequirement = create(BUILDING_RESEARCH_REQ_ID, BuildingResearchRequirement::new, BuildingResearchRequirement::new);
        buildingAlternatesResearchRequirement = create(BUILDING_ALTERNATES_RESEARCH_REQ_ID, BuildingAlternatesResearchRequirement::new, BuildingAlternatesResearchRequirement::new);
        buildingMandatoryResearchRequirement = create(BUILDING_MANDATORY_RESEARCH_REQ_ID, BuildingMandatoryResearchRequirement::new, BuildingMandatoryResearchRequirement::new);

        researchResearchRequirement = create(RESEARCH_RESEARCH_REQ_ID, ResearchResearchRequirement::new, ResearchResearchRequirement::new);
    }
    private ModResearchRequirementInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModResearchRequirementInitializer but this is a Utility class.");
    }

    /**
     * Utility method to aid in the creation of a research requirement.
     *
     * @param registryName the registry name for this entry.
     * @param readFromNBT  function to read this item from json.
     * @param readFromJson function to read this item from NBT.
     * @return the finalized registry object.
     */
    private static DeferredHolder<ResearchRequirementEntry, ResearchRequirementEntry> create(
        final ResourceLocation registryName,
        final ReadFromNBTFunction readFromNBT,
        final ReadFromJsonFunction readFromJson)
    {
        return DEFERRED_REGISTER.register(registryName.getPath(), () -> new ResearchRequirementEntry(registryName, readFromNBT, readFromJson));
    }
}
