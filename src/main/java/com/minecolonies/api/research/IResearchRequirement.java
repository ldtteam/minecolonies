package com.minecolonies.api.research;

import com.minecolonies.api.colony.IColony;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;

/**
 * Interface of research requirements.
 */
public interface IResearchRequirement
{
    /**
     * Get the {@link ModResearchRequirements.ResearchRequirementEntry} for this Research Requirement.
     *
     * @return a registry entry.
     */
    ModResearchRequirements.ResearchRequirementEntry getRegistryEntry();

    /**
     * Get a human-readable description of the requirement, or a translation key.
     *
     * @return translation text component.
     */
    MutableComponent getDesc();

    /**
     * Check if this requirement is fulfilled for a certain colony.
     *
     * @param colony the colony to check.
     * @return true if so.
     */
    boolean isFulfilled(final IColony colony);

    /**
     * Write the ResearchRequirement's traits to NBT, to simplify serialization for client-viewable data.
     *
     * @return an NBT file which must, at minimum, contain the necessary traits to reassemble the effect.
     */
    CompoundTag writeToNBT();
}
