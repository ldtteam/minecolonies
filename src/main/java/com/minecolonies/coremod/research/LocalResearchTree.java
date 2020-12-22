package com.minecolonies.coremod.research;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.research.IGlobalResearch;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.research.ILocalResearch;
import com.minecolonies.api.research.ILocalResearchTree;
import com.minecolonies.api.research.effects.IResearchEffect;
import com.minecolonies.api.research.effects.IResearchEffectManager;
import com.minecolonies.api.research.util.ResearchState;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.research.util.ResearchConstants.MAX_DEPTH;
import static com.minecolonies.api.research.util.ResearchConstants.TAG_RESEARCH_TREE;

/**
 * The class which contains all research.
 */
public class LocalResearchTree implements ILocalResearchTree
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
     * All completed research.
     */
    private final List<String> isComplete = new ArrayList<>();

    /**
     * Map containing all branches for which the max level research has been occupied already.
     */
    private final List<String> maxLevelResearchCompleted = new ArrayList<>();

    @Override
    public ILocalResearch getResearch(final String branch, final String id)
    {
        if (!researchTree.containsKey(branch))
        {
            return null;
        }
        return researchTree.get(branch).get(id);
    }

    @Override
    public boolean hasCompletedResearch(final String researchId)
    {
        if(IGlobalResearchTree.getInstance().hasResearch(researchId))
        {
            if(isComplete.contains(researchId))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        // For now, if a research requirement doesn't exist, we'll act as though it was completed.
        else
        {
            return true;
        }
    }

    @Override
    public void addResearch(final String branch, final ILocalResearch research)
    {
        if (!researchTree.containsKey(branch))
        {
            researchTree.put(branch, new HashMap<>());
        }

        if (!researchTree.get(branch).containsKey(research.getId()))
        {
            researchTree.get(branch).put(research.getId(), research);
        }

        if (research.getState() == ResearchState.IN_PROGRESS)
        {
            inProgress.put(research.getId(), research);
        }
        else if (research.getState() == ResearchState.FINISHED)
        {
            inProgress.remove(research.getId());
            if (!isComplete.contains(research.getId()))
            {
                isComplete.add(research.getId());
            }
        }

        if (research.getDepth() == MAX_DEPTH)
        {
            if (!maxLevelResearchCompleted.contains(branch))
            {
                maxLevelResearchCompleted.add(branch);
            }
        }
    }

    @Override
    public boolean branchFinishedHighestLevel(final String branch)
    {
        return maxLevelResearchCompleted.contains(branch);
    }

    @Override
    public List<ILocalResearch> getResearchInProgress()
    {
        return ImmutableList.copyOf(inProgress.values());
    }

    @Override
    public void finishResearch(final String id)
    {
        inProgress.remove(id); isComplete.add(id);
    }

    @Override
    public void cancelResearch(final String branch, final String id, final IColony colony)
    {
        checkAndResetDescendants(branch, Arrays.asList(id));

        if(colony != null)
        {
            // There's no guarantee that undoing a research will only push its effects back one strength grade.
            // Instead, we have to apply every extant effect again.
            // Because effects may cross branches, must check all branches, not just the current one.
            colony.getResearchManager().getResearchEffects().clear();
            for (final Map.Entry<String, Map<String, ILocalResearch>> br : researchTree.entrySet())
            {
                for(final Map.Entry<String, ILocalResearch> research : br.getValue().entrySet())
                {
                    if(research.getValue().getState() == ResearchState.FINISHED)
                    {
                        for(final IResearchEffect effect : IGlobalResearchTree.getInstance().getResearch(br.getKey(), research.getValue().getId()).getEffects())
                        {
                            colony.getResearchManager().getResearchEffects().applyEffect(effect);
                        }
                    }
                }
            }
        }

    }

    /**
     *  Recursively checks descendant researches, and resets their status, if complete.
     * @param branch      branch from which to remove research.
     * @param ids          identifier of the specific research.
     */
    private void checkAndResetDescendants(final String branch, final List<String> ids)
    {
        for(String id : ids)
        {
            final ILocalResearch localResearch = getResearch(branch, id);
            if(localResearch == null)
            {
                continue;
            }
            checkAndResetDescendants(branch, IGlobalResearchTree.getInstance().getResearch(branch, id).getChildren());

            researchTree.get(branch).remove(id);
            if(inProgress.containsKey(id))
            {
                inProgress.remove(id);
            }
            if(isComplete.contains(id))
            {
                isComplete.remove(id);
            }
            if (IGlobalResearchTree.getInstance().getResearch(branch, id).getDepth() == MAX_DEPTH
                  && maxLevelResearchCompleted.contains(branch))
            {
                maxLevelResearchCompleted.remove(branch);
            }
        }
    }

    @Override
    public void writeToNBT(final CompoundNBT compound)
    {
        @NotNull final ListNBT
          researchList = researchTree.values()
                           .stream()
                           .flatMap(map -> map.values().stream())
                           .map(research -> StandardFactoryController.getInstance().serialize(research))
                           .collect(NBTUtils.toListNBT());
        compound.put(TAG_RESEARCH_TREE, researchList);
    }

    @Override
    public void readFromNBT(final CompoundNBT compound, final IResearchEffectManager effects)
    {
        researchTree.clear();
        inProgress.clear();
        isComplete.clear();
        maxLevelResearchCompleted.clear();
        NBTUtils.streamCompound(compound.getList(TAG_RESEARCH_TREE, Constants.NBT.TAG_COMPOUND))
          .map(researchCompound -> (ILocalResearch) StandardFactoryController.getInstance().deserialize(researchCompound))
          .forEach(research -> {
              if (research.getState() == ResearchState.FINISHED)
              {
                  /// region Updated ID helper.  TODO: Remove for 1.17+, or after sufficient update time.
                  if (!MinecoloniesAPIProxy.getInstance().getGlobalResearchTree().hasResearch(research.getBranch(), research.getId()))
                  {
                      switch (research.getId())
                      {
                          case "tickshot":
                              research = new LocalResearch("trickshot", "combat", 3);
                              break;
                          case "whirldwind":
                              research = new LocalResearch("whirlwind", "combat", 6);
                              break;
                          case "repost":
                              research = new LocalResearch("riposte", "combat", 3);
                              break;
                          case "ironarmour":
                              research = new LocalResearch("ironarmor", "combat", 4);
                              break;
                          case "steelarmour":
                              research = new LocalResearch("steelarmor", "combat", 5);
                              break;
                          case "livesaver":
                              research = new LocalResearch("lifesaver", "civilian", 3);
                              break;
                          case "livesaver2":
                              research = new LocalResearch("lifesaver2", "civilian", 4);
                              break;
                      }
                  }
                  /// endregion


                  // Even after correction, we do still need to check; it's possible for someone to have old save data and remove the research,
                  // or to have a different research that was in a now-removed datapack.
                  if (MinecoloniesAPIProxy.getInstance().getGlobalResearchTree().hasResearch(research.getBranch(), research.getId()))
                  {
                      for (final IResearchEffect effect : MinecoloniesAPIProxy.getInstance().getGlobalResearchTree().getResearch(research.getBranch(), research.getId()).getEffects())
                      {
                          effects.applyEffect(effect);
                      }
                  }
                  else
                  {
                      Log.getLogger().warn("Research " + research.getId() + " was in colony save file, but not found as valid current research.  Progress on this research may be reset.");
                  }
              }
              addResearch(research.getBranch(), research);
          });
    }
}
