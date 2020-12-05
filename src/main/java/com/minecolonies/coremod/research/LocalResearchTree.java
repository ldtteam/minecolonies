package com.minecolonies.coremod.research;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.research.IGlobalResearch;
import com.minecolonies.api.research.ILocalResearch;
import com.minecolonies.api.research.ILocalResearchTree;
import com.minecolonies.api.research.effects.IResearchEffect;
import com.minecolonies.api.research.effects.IResearchEffectManager;
import com.minecolonies.api.research.factories.ILocalResearchFactory;
import com.minecolonies.api.research.util.ResearchState;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * Map containing all branches for which the level 6 research has been occupied already.
     */
    private final Map<String, Boolean> levelSixResearchReached = new HashMap<>();

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
    public List<ILocalResearch> getCompletedResearch()
    {
        final List<ILocalResearch> allCompletedResearch = new ArrayList<>();
        for(Map<String, ILocalResearch> branchResearch : researchTree.values())
        {
            for(ILocalResearch research : branchResearch.values())
            {
                if(research.getState() == ResearchState.FINISHED)
                {
                    allCompletedResearch.add(research);
                }
            }
        }
        return allCompletedResearch;
    }

    @Override
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
        researchTree.put(branch, branchMap);

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

    @Override
    public boolean branchFinishedHighestLevel(final String branch)
    {
        return levelSixResearchReached.getOrDefault(branch, false);
    }

    @Override
    public List<ILocalResearch> getResearchInProgress()
    {
        return ImmutableList.copyOf(inProgress.values());
    }

    @Override
    public void finishResearch(final String id)
    {
        inProgress.remove(id);
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
        NBTUtils.streamCompound(compound.getList(TAG_RESEARCH_TREE, Constants.NBT.TAG_COMPOUND))
          .map(researchCompound -> (ILocalResearch) StandardFactoryController.getInstance().deserialize(researchCompound))
          .forEach(research -> {
              addResearch(research.getBranch(), research);
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
                      for (IResearchEffect effect : MinecoloniesAPIProxy.getInstance().getGlobalResearchTree().getResearch(research.getBranch(), research.getId()).getEffects())
                      {
                          effects.applyEffect(effect);
                      }
                  }
                  else
                  {
                      Log.getLogger().warn("Research " + research.getId() + " was in colony save file, but not found as valid current research.  Progress on this research may be reset.");
                  }
              }
          });
    }
}
