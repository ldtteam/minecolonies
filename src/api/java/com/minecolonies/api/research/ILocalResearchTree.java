package com.minecolonies.api.research;

import com.minecolonies.api.research.effects.IResearchEffectManager;
import net.minecraft.nbt.CompoundNBT;
import java.util.List;

/**
 * The class which contains all research.
 */
public interface ILocalResearchTree
{
    /**
     * Get a research by id.
     * @param id the id of the research.
     * @param branch the branch of the research.
     * @return the IResearch object.
     */
    ILocalResearch getResearch(final String branch, final String id);

    /**
     * Add a research to the tree.
     * @param research the research to add.
     */
    void addResearch(final String branch, final ILocalResearch research);

    /**
     * Check if a branch already researched a level 6 research.
     * This is important since only 1 of these can be researched for each branch.
     * @param branch the branch to check.
     * @return true if so.
     */
    boolean branchFinishedHighestLevel(final String branch);

    /**
     * Get a list of all research in progress.
     * @return the list.
     */
    List<ILocalResearch> getResearchInProgress();

    /**
     * Finish a research and remove it from the inProgress list.
     * @param id the id of the research to remove.
     */
    void finishResearch(final String id);

    /**
     * Write the research tree to NBT.
     * @param compound the compound.
     */
    void writeToNBT(final CompoundNBT compound);

    /**
     * Read the research tree from NBT.
     * @param compound the compound to read it from.
     * @param effects the effects.
     */
    void readFromNBT(final CompoundNBT compound, final IResearchEffectManager effects);
}
