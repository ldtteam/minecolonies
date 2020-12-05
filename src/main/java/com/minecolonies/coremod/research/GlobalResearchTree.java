package com.minecolonies.coremod.research;

import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.research.IGlobalResearch;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.research.effects.IResearchEffect;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Resource;
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

    private final Map<ResourceLocation, String> researchResourceLocations = new HashMap<>();

    private final List<ResourceLocation> resettableResearch = new ArrayList<>();

    private final Map<String, IResearchEffect> unlockBuildingEffect = new HashMap<>();

    private final Map<String, IResearchEffect> unlockAbilityEffect = new HashMap<>();

    @Override
    public IGlobalResearch getResearch(final String branch, final String id)
    {
        return researchTree.get(branch).get(id);
    }

    @Override
    public ResourceLocation getResearchResourceLocation(final String branch, final String id) {  return researchTree.get(branch).get(id).getResourceLocation(); }

    @Override
    public boolean hasResearch(final String branch, final String id)
    {
        return (researchTree.containsKey(branch) && researchTree.get(branch).containsKey(id));
    }

    @Override
    public void addResearch(final String branch, final IGlobalResearch research, Boolean isReloadedWithWorld)
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
        if (branchMap.containsKey(research.getId()))
        {
            Log.getLogger().error("Duplicate research key:" + research.getId());
        }

        branchMap.put(research.getId(), research);
        researchTree.put(branch, branchMap);
        if(isReloadedWithWorld)
        {
            resettableResearch.add(research.getResourceLocation());
        }
        for (IResearchEffect effect : research.getEffects())
        {
            if (effect instanceof UnlockBuildingResearchEffect)
            {
                if(!unlockBuildingEffect.containsKey(effect.getId()))
                {
                    unlockBuildingEffect.put(effect.getId(), effect);
                }
            }
            else if (effect instanceof UnlockAbilityResearchEffect)
            {
                if(!unlockAbilityEffect.containsKey(effect.getId()))
                {
                    unlockAbilityEffect.put(effect.getId(), effect);
                }
            }
        }
    }

    @Override
    public boolean hasUnlockBuildingEffect(String id)
    {
        return unlockBuildingEffect.containsKey(id);
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
    public void reset()
    {
        // and the reason we're not using BiMaps or identifying static research by resourceLocation.
        for(ResourceLocation reset : resettableResearch)
        {
            if(unlockAbilityEffect.containsValue(reset))
            {
                unlockAbilityEffect.remove(researchResourceLocations.get(reset));
            }
            if(unlockBuildingEffect.containsValue(reset))
            {
                unlockBuildingEffect.remove(researchResourceLocations.get(reset));
            }
        }
        for(ResourceLocation reset : resettableResearch)
        {
            if(researchResourceLocations.containsValue(reset))
            {
                researchResourceLocations.remove(reset);
            }
        }
        resettableResearch.clear();
    }

    @Override
    public void writeToNBT(final CompoundNBT compound)
    {
        @NotNull final ListNBT
          citizenTagList = researchTree.values()
                             .stream()
                             .flatMap(map -> map.values().stream())
                             .map(research -> StandardFactoryController.getInstance().serialize(research))
                             .collect(NBTUtils.toListNBT());
        compound.put(TAG_RESEARCH_TREE, citizenTagList);
    }

    @Override
    public void readFromNBT(final CompoundNBT compound)
    {
        researchTree.clear();
        NBTUtils.streamCompound(compound.getList(TAG_RESEARCH_TREE, Constants.NBT.TAG_COMPOUND))
          .map(researchCompound -> (IGlobalResearch) StandardFactoryController.getInstance().deserialize(researchCompound))
          .forEach(research -> addResearch(research.getBranch(), research, true));
    }
}
