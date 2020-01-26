package com.minecolonies.api.research;

import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.util.NBTUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.minecolonies.api.research.ResearchConstants.TAG_RESEARCH_TREE;

/**
 * The class which contains all research.
 */
public class ResearchTree
{
    /**
     * The map containing all researches by ID.
     */
    private final Map<String, Map<String, IResearch>> researchTree = new HashMap<>();

    /**
     * Get a research by id.
     * @param id the id of the research.
     * @param branch the branch of the research.
     * @return the IResearch object.
     */
    public IResearch getResearch(final String branch, final String id)
    {
        return researchTree.get(branch).get(id).copy();
    }

    /**
     * Add a research to the tree.
     * @param research the research to add.
     */
    public void addResearch(final String branch, final IResearch research)
    {
        final Map<String, IResearch> branchMap;
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
     * Write the research tree to NBT.
     * @param compound the compound.
     */
    public void writeToNBT(final NBTTagCompound compound)
    {
        @NotNull final NBTTagList citizenTagList = researchTree.values().stream().flatMap(map -> map.values().stream()).map(research -> StandardFactoryController.getInstance().serialize(research)).collect(NBTUtils.toNBTTagList());
        compound.setTag(TAG_RESEARCH_TREE, citizenTagList);
    }

    /**
     * Read the research tree from NBT.
     * @param compound the compound to read it from.
    +
     */
    public void readFromNBT(final NBTTagCompound compound)
    {
        researchTree.clear();
        NBTUtils.streamCompound(compound.getTagList(TAG_RESEARCH_TREE, Constants.NBT.TAG_COMPOUND))
                              .map(researchCompound -> (IResearch) StandardFactoryController.getInstance().deserialize(researchCompound))
                              .forEach(research -> addResearch(research.getBranch(), research));
    }
}
