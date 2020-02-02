package com.minecolonies.api.research;

import com.minecolonies.api.MinecoloniesAPIProxy;
import net.minecraft.nbt.CompoundNBT;

import java.util.*;

/**
 * The class which contains all research.
 */
public interface IGlobalResearchTree
{
    /**
     * Get an instance of this Tree.
     * @return the instance.
     */
    static IGlobalResearchTree getInstance()
    {
        return MinecoloniesAPIProxy.getInstance().getGlobalResearchTree();
    }

    /**
     * Get a research by id.
     * @param id the id of the research.
     * @param branch the branch of the research.
     * @return the IResearch object.
     */
    IGlobalResearch getResearch(final String branch, final String id);

    /**
     * Add a research to the tree.
     * @param research the research to add.
     */
    void addResearch(final String branch, final IGlobalResearch research);

    /**
     * Get the list of all branches.
     * @return the list of branches.
     */
    List<String> getBranches();

    /**
     * Get the primary research of a certain branch.
     * @param branch the branch it belongs to.
     * @return the list of research without parent.
     */
    List<String> getPrimaryResearch(final String branch);

    /**
     * Write the research tree to NBT.
     * @param compound the compound.
     */
    void writeToNBT(final CompoundNBT compound);

    /**
     * Read the research tree from NBT.
     * @param compound the compound to read it from.
    +
     */
    void readFromNBT(final CompoundNBT compound);

    /**
     * Load cost for all research.
     */
    void loadCost();
}
