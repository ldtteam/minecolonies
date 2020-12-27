package com.minecolonies.coremod.research;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.research.IResearchRequirement;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Certain building research requirements.
 */
public class ResearchResearchRequirement implements IResearchRequirement
{
    /**
     * The identifier tag for this type of requirement.
     */
    public static final String type = "research";

    /**
     * The research id.
     */
    private final String researchId;

    /**
     * The research name.
     */
    private final String researchName;

    /**
     * Create a building based research requirement.
     *
     * @param researchId the required precursor research.
     */
    public ResearchResearchRequirement(final String researchId, final String researchName)
    {
        this.researchId = researchId;
        this.researchName = researchName;
    }

    /**
     * Creates an building requirement from an attributes string array.
     * See getAttributes for the format.
     * @param attributes        An attributes array describing the research requirement.
     */
    public ResearchResearchRequirement(String[] attributes)
    {
        if(!attributes[0].equals(type) || attributes.length < 3)
        {
            Log.getLogger().error("Error parsing received ResearchResearchRequirement.");
            researchId = "";
            researchName = "";
        }
        else
        {
            researchId = attributes[1];
            researchName = attributes[2];
        }
    }

    /**
     * @return the building description
     */
    public String getResearchId()
    {
        return researchId;
    }

    @Override
    public boolean isFulfilled(final IColony colony)
    {
        return colony.getResearchManager().getResearchTree().hasCompletedResearch(researchId);
    }

    @Override
    public String getAttributes()
    {
        return type + ":" + researchId + ":" + researchName;
    }

    @Override
    public TranslationTextComponent getDesc()
    {
        return new TranslationTextComponent(TranslationConstants.RESEARCH_REQUIRES, researchName);
    }
}
