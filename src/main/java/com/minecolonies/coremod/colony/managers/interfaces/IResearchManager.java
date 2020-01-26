package com.minecolonies.coremod.colony.managers.interfaces;


import com.minecolonies.api.research.ResearchEffects;
import com.minecolonies.api.research.ResearchTree;
import net.minecraft.nbt.NBTTagCompound;
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
    void readFromNBT(@NotNull final NBTTagCompound compound);

    /**
     * Write all stats to nbt.
     * @param statsCompound the compound.
     */
    void writeToNBT(@NotNull final NBTTagCompound statsCompound);

    /**
     * Get the instance of the researchTree.
     * @return the ResearchTree object.
     */
    ResearchTree getResearchTree();

    /**
     * Get an instance of the research effects.
     * @return the ResearchEffects object.
     */
    ResearchEffects getResearchEffects();
}
