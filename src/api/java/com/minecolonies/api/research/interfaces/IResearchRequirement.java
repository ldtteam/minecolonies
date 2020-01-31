package com.minecolonies.api.research.interfaces;

import com.minecolonies.api.colony.IColony;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Interface of research requirements.
 */
public interface IResearchRequirement
{
    /**
     * Check if this requirement is fulfilled for a certain colony.
     * @param colony the colony to check.
     * @return true if so.
     */
    boolean isFulfilled(final IColony colony);

    /**
     * Get a description of the requirement.
     * @return translation text component.
     */
    TranslationTextComponent getDesc();
}
