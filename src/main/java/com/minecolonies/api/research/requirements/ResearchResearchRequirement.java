package com.minecolonies.api.research.requirements;

import com.google.gson.JsonObject;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.research.IResearchRequirement;
import com.minecolonies.api.research.ModResearchRequirements;
import com.minecolonies.core.util.GsonHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

/**
 * Certain building research requirements.
 */
public class ResearchResearchRequirement implements IResearchRequirement
{
    /**
     * The property name for a non-parent research requirement.
     */
    public static final String RESEARCH_REQUIRED_RESEARCH_PROP = "research";
    /**
     * The nbtProperty identifying the research resource location which must be unlocked.
     */
    private static final String TAG_ID = "id";
    /**
     * The research id.
     */
    private final ResourceLocation researchId;

    /**
     * Create research based research requirement.
     *
     * @param nbt the nbt containing the relevant data.
     */
    public ResearchResearchRequirement(final CompoundTag nbt)
    {
        this.researchId = new ResourceLocation(nbt.getString(TAG_ID));
    }

    /**
     * Create research based research requirement.
     *
     * @param json the json containing the relevant data.
     */
    public ResearchResearchRequirement(final JsonObject json)
    {
        this.researchId = GsonHelper.getAsResourceLocation(json, RESEARCH_REQUIRED_RESEARCH_PROP);
    }

    /**
     * @return the research identifier
     */
    public ResourceLocation getResearchId()
    {
        return researchId;
    }

    @Override
    public ModResearchRequirements.ResearchRequirementEntry getRegistryEntry()
    {
        return ModResearchRequirements.researchResearchRequirement.get();
    }

    @Override
    public MutableComponent getDesc()
    {
        return MutableComponent.create(IGlobalResearchTree.getInstance().getResearch(researchId).getName());
    }

    @Override
    public boolean isFulfilled(final IColony colony)
    {
        return colony.getResearchManager().getResearchTree().hasCompletedResearch(researchId);
    }

    @Override
    public CompoundTag writeToNBT()
    {
        CompoundTag nbt = new CompoundTag();
        nbt.putString(TAG_ID, this.researchId.toString());
        return nbt;
    }
}
