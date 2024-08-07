package com.minecolonies.core.research;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.research.IResearchRequirement;
import com.minecolonies.api.research.ModResearchRequirements;
import com.minecolonies.api.research.registry.ResearchRequirementEntry;
import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

/**
 * Certain building research requirements.
 */
public class ResearchResearchRequirement implements IResearchRequirement
{
    /**
     * The nbtProperty identifying the research resource location which must be unlocked.
     */
    private static final String TAG_ID = "id";

    /**
     * The nbtProperty identifying the human-readable description or translation key for the research.
     */
    private static final String TAG_NAME = "name";

    /**
     * The research id.
     */
    private final ResourceLocation researchId;

    /**
     * The research name.
     */
    private final MutableComponent researchName;

    /**
     * Create a research-based research requirement, assigning an auto-generation key.
     *
     * @param researchId the required precursor research.
     */
    public ResearchResearchRequirement(final ResourceLocation researchId)
    {
        this.researchId = researchId;
        this.researchName = Component.translatableEscape("com." + researchId.getNamespace() + ".research." + researchId.getPath().replaceAll("[ /]",".") + ".name");
    }

    /**
     * Create a research-based research requirement.
     *
     * @param researchId the required precursor research.
     * @param researchName the override name for the required research.
     */
    public ResearchResearchRequirement(final ResourceLocation researchId, final MutableComponent researchName)
    {
        this.researchId = researchId;
        this.researchName = researchName;
    }

    /**
     * Create a research-based research requirement from a CompoundTag.
     * @param nbt       the nbt containing the research information.
     */
    public ResearchResearchRequirement(final CompoundTag nbt)
    {
        this.researchId = ResourceLocation.parse(nbt.getString(TAG_ID));
        this.researchName = Component.translatableEscape(nbt.getString(TAG_NAME));
    }

    /**
     * @return the research identifier
     */
    public ResourceLocation getResearchId()
    {
        return researchId;
    }

    @Override
    public boolean isFulfilled(final IColony colony)
    {
        return colony.getResearchManager().getResearchTree().hasCompletedResearch(researchId);
    }

    @Override
    public MutableComponent getDesc()
    {
        return Component.translatableEscape(TranslationConstants.RESEARCH_REQUIRES, researchName);
    }

    @Override
    public ResearchRequirementEntry getRegistryEntry() {return ModResearchRequirements.researchResearchRequirement.get();}

    @Override
    public CompoundTag writeToNBT()
    {
        CompoundTag nbt = new CompoundTag();
        nbt.putString(TAG_ID, this.researchId.toString());
        nbt.putString(TAG_NAME, this.researchName.getContents() instanceof TranslatableContents ? ((TranslatableContents) this.researchName.getContents()).getKey() : "");
        return nbt;
    }
}
