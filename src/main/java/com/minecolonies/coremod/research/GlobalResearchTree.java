package com.minecolonies.coremod.research;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.research.IGlobalResearch;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.research.IResearchRequirement;
import com.minecolonies.api.research.effects.IResearchEffect;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
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
     * The map containing all researches by ID and branch.
     */
    private final Map<String, Map<String, IGlobalResearch>> researchTree = new HashMap<>();

    /**
     * The map containing all researches by ResourceLocation and ResearchID.
     */
    private final Map<ResourceLocation, String> researchResourceLocations = new HashMap<>();

    /**
     * The list containing all resettable researches by ResourceLocation.
     */
    private final List<ResourceLocation> resettableResearch = new ArrayList<>();

    /**
     * The list containing all autostart research.
     */
    private final List<IGlobalResearch> autostartResearch = new ArrayList<>();

    /**
     * The map containing loaded Research Effect IDs.
     */
    private final List<String> researchEffectsIds = new ArrayList<>();

    @Override
    public IGlobalResearch getResearch(final String branch, final String id) { return researchTree.get(branch).get(id); }

    @Override
    public ResourceLocation getResearchResourceLocation(final String branch, final String id) {  return researchTree.get(branch).get(id).getResourceLocation(); }

    @Override
    public boolean hasResearch(final String branch, final String id)
    {
        return (researchTree.containsKey(branch) && researchTree.get(branch).containsKey(id));
    }

    @Override
    public boolean hasResearch(final String id)
    {
        for(final Map.Entry<String, Map<String, IGlobalResearch>> branch: researchTree.entrySet())
        {
            if(branch.getValue().containsKey(id))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addResearch(final String branch, final IGlobalResearch research, final boolean isReloadedWithWorld)
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

        researchResourceLocations.put(research.getResourceLocation(), research.getId());

        if (isReloadedWithWorld)
        {
            resettableResearch.add(research.getResourceLocation());
        }
        if (research.isAutostart())
        {
           autostartResearch.add(research);
        }
        for (IResearchEffect effect : research.getEffects())
        {
            if(!researchEffectsIds.contains(effect.getId()))
            {
                researchEffectsIds.add(effect.getId());
            }
        }
    }

    @Override
    public boolean hasResearchEffect(final String id)
    {
        return researchEffectsIds.contains(id);
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
        return researchTree.get(branch).values().stream().filter(research -> research.getParent().isEmpty())
                 .sorted(Comparator.comparing(IGlobalResearch::getResourceLocation))
                 .map(IGlobalResearch::getId).collect(Collectors.toList());
    }

    @Override
    public void reset()
    {
        for(ResourceLocation reset : resettableResearch)
        {
            if(!researchResourceLocations.containsKey(reset))
            {
                continue;
            }
            for(Map.Entry<String, Map<String, IGlobalResearch>> branch : researchTree.entrySet())
            {
                if(branch.getValue().containsKey(researchResourceLocations.get(reset)));
                {
                    branch.getValue().remove(researchResourceLocations.get(reset));
                }
            }
            if(researchResourceLocations.containsKey(reset))
            {
                researchResourceLocations.remove(reset);
            }
        }
        resettableResearch.clear();
        // Autostart is only accessible as a dynamically-assigned trait, so we can reset all of it.
        autostartResearch.clear();
        final Iterator<Map.Entry<String, Map<String, IGlobalResearch>>> iterator = researchTree.entrySet().iterator();
        while (researchTree.entrySet().size() > 0 && iterator.hasNext())
        {
            if(iterator.next().getValue().size() == 0)
            {
                iterator.remove();
            }
        }
    }

    @Override
    public boolean isResearchRequirementsFulfilled(final List<IResearchRequirement> requirements, final IColony colony)
    {
        if (requirements == null || requirements.isEmpty())
        {
            return true;
        }
        for(final IResearchRequirement requirement : requirements)
        {
            if(!requirement.isFulfilled(colony))
            {
                return false;
            }
        }
        return true;
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

    @Override
    public List<IResearchEffect> getEffectsForResearch(final String id)
    {
        for(final String branch: this.getBranches())
        {
            final IGlobalResearch r = this.getResearch(branch, id);
            if (r != null)
            {
                return r.getEffects();
            }
        }
        return null; 
    }

    @Override
    public List<IGlobalResearch> getAutostartResearches()
    {
        return autostartResearch;
    }
}
