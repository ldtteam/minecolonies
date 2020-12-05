package com.minecolonies.coremod.research;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.research.ILocalResearch;
import com.minecolonies.api.research.IResearchRequirement;
import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Certain building research requirements.
 */
public class ResearchResearchRequirement implements IResearchRequirement
{
    /**
     * The research id.
     */
    private final String researchId;
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
     * @return the building description
     */
    public String getResearchId()
    {
        return researchId;
    }

    @Override
    public boolean isFulfilled(final IColony colony)
    {
        int sum = 0;
        for(final ILocalResearch research : colony.getResearchManager().getResearchTree().getCompletedResearch())
        {
            if(researchId.contains(getResearchId()))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public TranslationTextComponent getDesc()
    {
        return new TranslationTextComponent(TranslationConstants.RESEARCH_REQUIRES, researchName);
    }
}
