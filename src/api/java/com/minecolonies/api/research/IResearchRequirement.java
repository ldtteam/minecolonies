package com.minecolonies.api.research;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.research.registry.ResearchRequirementEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;

/**
 * Interface of research requirements.
 */
public interface IResearchRequirement
{
    /**
     * Check if this requirement is fulfilled for a certain colony.
     *
     * @param colony the colony to check.
     * @return true if so.
     */
    boolean isFulfilled(final IColony colony);

    /**
     * Get a human-readable description of the requirement, or a translation key.
     *
     * @return translation text component.
     */
    MutableComponent getDesc();

    /**
     * Get the {@link ResearchRequirementEntry} for this Research Requirement.
     *
     * @return a registry entry.
     */
    ResearchRequirementEntry getRegistryEntry();

    /**
     * Write the ResearchRequirement's traits to NBT, to simplify serialization for client-viewable data.
     *
     * @return an NBT file which must, at minimum, contain the necessary traits to reassemble the effect.
     */
    CompoundTag writeToNBT();
}
