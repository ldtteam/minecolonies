package com.minecolonies.api.research;

import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.research.interfaces.IGlobalResearch;
import com.minecolonies.api.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.research.util.ResearchConstants.TAG_RESEARCH_TREE;

/**
 * The class which contains all research.
 */
public class ResearchTree
{
    /**
     * The map containing all researches by ID.
     */
    private final Map<String, Map<String, IGlobalResearch>> researchTree = new HashMap<>();

    /**
     * Get a research by id.
     * @param id the id of the research.
     * @param branch the branch of the research.
     * @return the IResearch object.
     */
    public IGlobalResearch getResearch(final String branch, final String id)
    {
        return researchTree.get(branch).get(id);
    }

    /**
     * Add a research to the tree.
     * @param research the research to add.
     */
    public void addResearch(final String branch, final IGlobalResearch research)
    {
        final Map<String, IGlobalResearch> branchMap;
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
    }

    /**
     * Get the list of all branches.
     * @return the list of branches.
     */
    public List<String> getBranches()
    {
        return new ArrayList<>(researchTree.keySet());
    }

    /**
     * Get the primary research of a certain branch.
     * @param branch the branch it belongs to.
     * @return the list of research without parent.
     */
    public List<String> getPrimaryResearch(final String branch)
    {
        if (!researchTree.containsKey(branch))
        {
            return Collections.emptyList();
        }
        return researchTree.get(branch).values().stream().filter(research -> research.getParent().isEmpty()).map(IGlobalResearch::getId).collect(Collectors.toList());
    }

    /**
     * Write the research tree to NBT.
     * @param compound the compound.
     */
    public void writeToNBT(final CompoundNBT compound)
    {
        @NotNull final ListNBT
          citizenTagList = researchTree.values().stream().flatMap(map -> map.values().stream()).map(research -> StandardFactoryController.getInstance().serialize(research)).collect(NBTUtils.toListNBT());
        compound.put(TAG_RESEARCH_TREE, citizenTagList);
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
                              .map(researchCompound -> (IGlobalResearch) StandardFactoryController.getInstance().deserialize(researchCompound))
                              .forEach(research -> addResearch(research.getBranch(), research));
    }

    /**
     * Load cost for all research.
     */
    public void loadCost()
    {
        researchTree.values().forEach(b -> b.values().forEach(IGlobalResearch::loadCostFromConfig));
    }
}
