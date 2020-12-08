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

    private final Map<ResourceLocation, String> researchResourceLocations = new HashMap<>();

    private final List<ResourceLocation> resettableResearch = new ArrayList<>();

    private final List<IGlobalResearch> autostartResearch = new ArrayList<>();

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
    public boolean hasUnlockAbilityEffect(String id)
    {
        return unlockAbilityEffect.containsKey(id);
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
        // and the reason we're not using BiMaps or identifying static research by resourceLocation.
        for(ResourceLocation reset : resettableResearch)
        {
            for(IResearchEffect effect : getEffectsForResearch(researchResourceLocations.get(reset)))
            {
                if(unlockAbilityEffect.containsKey(effect.getId()))
                {
                    unlockAbilityEffect.remove(effect.getId());
                }
                if(unlockBuildingEffect.containsKey(effect.getId()))
                {
                    unlockBuildingEffect.remove(effect.getId());
                }
            }
        }
        for(ResourceLocation reset : resettableResearch)
        {
            if(researchResourceLocations.containsKey(reset))
            {
                researchResourceLocations.remove(reset);
            }
        }
        resettableResearch.clear();
        // Autostart is only accessible as a dynamically-assigned trait, so we can reset all of it.
        autostartResearch.clear();
    }

    @Override
    public boolean isResearchRequirementsFulfilled(final List<IResearchRequirement> requirements, IColony colony)
    {
        if (requirements == null || requirements.isEmpty())
        {
            return true;
        }
        for(IResearchRequirement requirement : requirements)
        {
            if(requirement instanceof BuildingResearchRequirement)
            {
                int levels = 0;
                if(colony instanceof IColonyView)
                {
                    for (IBuildingView building : ((IColonyView)colony).getBuildings())
                    {
                        if (building.getSchematicName().equals(((BuildingResearchRequirement) requirement).getBuilding()))
                        {
                            levels += building.getBuildingLevel();
                        }
                    }
                }
                else if(colony instanceof IColony)
                {
                    for (Map.Entry<BlockPos, IBuilding> building : colony.getBuildingManager().getBuildings().entrySet())
                    {
                        if (building.getValue().getSchematicName().equals(((BuildingResearchRequirement) requirement).getBuilding()))
                        {
                            levels += building.getValue().getBuildingLevel();
                        }
                    }
                }

                if(levels < ((BuildingResearchRequirement)requirement).getBuildingLevel())
                {
                    return false;
                }
            }
            if(requirement instanceof ResearchResearchRequirement)
            {
                if(!Boolean.TRUE.equals(colony.getResearchManager().getResearchTree().hasCompletedResearch(((ResearchResearchRequirement) requirement).getResearchId())))
                {
                    return false;
                }
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
    public List<IResearchEffect<?>> getEffectsForResearch(final String id)
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
