package com.minecolonies.coremod.research;

import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.research.interfaces.IGlobalResearchTree;
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
public class GlobalResearchTree implements IGlobalResearchTree
{
    /**
     * The map containing all researches by ID.
     */
    private final Map<String, Map<String, IGlobalResearch>> researchTree = new HashMap<>();

    @Override
    public IGlobalResearch getResearch(final String branch, final String id)
    {
        return researchTree.get(branch).get(id);
    }

    @Override
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

    @Override
    public List<String> getBranches()
    {
        return new ArrayList<>(researchTree.keySet());
    }

    @Override
    public List<String> getPrimaryResearch(final String branch)
    {
        if (!researchTree.containsKey(branch))
        {
            return Collections.emptyList();
        }
        return researchTree.get(branch).values().stream().filter(research -> research.getParent().isEmpty()).map(IGlobalResearch::getId).collect(Collectors.toList());
    }

    @Override
    public void writeToNBT(final CompoundNBT compound)
    {
        @NotNull final ListNBT
          citizenTagList = researchTree.values().stream().flatMap(map -> map.values().stream()).map(research -> StandardFactoryController.getInstance().serialize(research)).collect(NBTUtils.toListNBT());
        compound.put(TAG_RESEARCH_TREE, citizenTagList);
    }

    @Override
    public void readFromNBT(final CompoundNBT compound)
    {
        researchTree.clear();
        NBTUtils.streamCompound(compound.getList(TAG_RESEARCH_TREE, Constants.NBT.TAG_COMPOUND))
                              .map(researchCompound -> (IGlobalResearch) StandardFactoryController.getInstance().deserialize(researchCompound))
                              .forEach(research -> addResearch(research.getBranch(), research));
    }

    @Override
    public void loadCost()
    {
        researchTree.values().forEach(b -> b.values().forEach(IGlobalResearch::loadCostFromConfig));
    }
}
