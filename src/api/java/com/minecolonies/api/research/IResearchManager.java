package com.minecolonies.api.research;

import com.minecolonies.api.research.effects.IResearchEffectManager;
import net.minecraft.nbt.CompoundNBT;
import org.jetbrains.annotations.NotNull;

/**
 * Research manager of the colony holding the tree and effects.
 */
public interface IResearchManager
{
    /**
     * Reads all stats from nbt.
     * @param compound the compound.
     */
    void readFromNBT(@NotNull final CompoundNBT compound);

    /**
     * Write all stats to nbt.
     * @param statsCompound the compound.
     */
    void writeToNBT(@NotNull final CompoundNBT statsCompound);

    /**
     * Get the instance of the researchTree.
     * @return the ResearchTree object.
     */
    ILocalResearchTree getResearchTree();

    /**
     * Get an instance of the research effects.
     * @return the ResearchEffects object.
     */
    IResearchEffectManager getResearchEffects();
}
