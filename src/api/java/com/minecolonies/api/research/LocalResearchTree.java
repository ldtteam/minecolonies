package com.minecolonies.api.research;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.research.interfaces.ILocalResearch;
import com.minecolonies.api.research.util.ResearchState;
import com.minecolonies.api.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.research.util.ResearchConstants.*;

/**
 * The class which contains all research.
 */
public class LocalResearchTree
{
    /**
     * The map containing all researches by ID.
     */
    private final Map<String, Map<String, ILocalResearch>> researchTree = new HashMap<>();

    /**
     * All research in progress.
     */
    private final Map<String, ILocalResearch> inProgress = new HashMap<>();

    /**
     * Map containing all branches for which the level 6 research has been occupied already.
     */
    private final Map<String, Boolean> levelSixResearchReached = new HashMap<>();

    /**
     * Get a research by id.
     * @param id the id of the research.
     * @param branch the branch of the research.
     * @return the IResearch object.
     */
    public ILocalResearch getResearch(final String branch, final String id)
    {
        if (!researchTree.containsKey(branch))
        {
            return null;
        }
        return researchTree.get(branch).get(id);
    }

    /**
     * Add a research to the tree.
     * @param research the research to add.
     */
    public void addResearch(final String branch, final ILocalResearch research)
    {
        final Map<String, ILocalResearch> branchMap;
        if (researchTree.containsKey(branch))
        {
            branchMap = researchTree.get(branch);
        }
        else
        {
            branchMap = new HashMap<>();
        }
        branchMap.put(research.getId(), research);
        researchTree.put(branch,branchMap);

        if (research.getState() == ResearchState.IN_PROGRESS)
        {
            inProgress.put(research.getId(), research);
        }
        else if (research.getState() == ResearchState.FINISHED)
        {
            inProgress.remove(research.getId());
        }

        if (research.getDepth() == 6)
        {
            levelSixResearchReached.put(research.getBranch(), true);
        }
    }

    /**
     * Check if a branch already researched a level 6 research.
     * @param branch the branch to check.
     * @return true if so.
     */
    public boolean branchAlreadyResearchedLevelSix(final String branch)
    {
        return levelSixResearchReached.getOrDefault(branch, false);
    }

    /**
     * Get a list of all research in progress.
     * @return the list.
     */
    public List<ILocalResearch> getResearchInProgress()
    {
        return ImmutableList.copyOf(inProgress.values());
    }

    /**
     * Finish a research and remove it from the inProgress list.
     * @param id the id of the research to remove.
     */
    public void finishResearch(final String id)
    {
        inProgress.remove(id);
    }

    /**
     * Write the research tree to NBT.
     * @param compound the compound.
     */
    public void writeToNBT(final CompoundNBT compound)
    {
        @NotNull final ListNBT
          researchList = researchTree.values().stream().flatMap(map -> map.values().stream()).map(research -> StandardFactoryController.getInstance().serialize(research)).collect(NBTUtils.toListNBT());
        compound.put(TAG_RESEARCH_TREE, researchList);
    }

    /**
     * Read the research tree from NBT.
     * @param compound the compound to read it from.
    +
     */
    public void readFromNBT(final CompoundNBT compound)
    {
        researchTree.clear();
        NBTUtils.streamCompound(compound.getList(TAG_RESEARCH_TREE, Constants.NBT.TAG_COMPOUND))
                              .map(researchCompound -> (ILocalResearch) StandardFactoryController.getInstance().deserialize(researchCompound))
                              .forEach(research -> addResearch(research.getBranch(), research));
    }
}
