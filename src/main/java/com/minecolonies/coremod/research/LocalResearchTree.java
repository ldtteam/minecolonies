package com.minecolonies.coremod.research;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.research.*;
import com.minecolonies.api.research.effects.IResearchEffect;
import com.minecolonies.api.research.effects.IResearchEffectManager;
import com.minecolonies.api.research.util.ResearchState;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.SoundUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.research.util.ResearchConstants.*;
import static com.minecolonies.api.research.util.ResearchConstants.BASE_RESEARCH_TIME;

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
    private final Set<String> isComplete = new HashSet<>();

    /**
     * Map containing all branches for which the max level research has been occupied already.
     */
    private final Set<String> maxLevelResearchCompleted = new HashSet<>();

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
            return isComplete.contains(researchId);
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
            isComplete.add(research.getId());
        }

        if (research.getDepth() == MAX_DEPTH)
        {
            maxLevelResearchCompleted.add(branch);
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
        inProgress.remove(id);
        isComplete.add(id);
    }

    @Override
    public void attemptBeginResearch(final PlayerEntity player, final IColony colony, final IGlobalResearch research)
    {
        if (colony.getResearchManager().getResearchTree().getResearch(research.getBranch(), research.getId()) == null)
        {
            if (player.isCreative())
            {
                research.startResearch(colony.getResearchManager().getResearchTree());
                if (MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchCreativeCompletion.get())
                {
                    colony.getResearchManager()
                      .getResearchTree()
                      .getResearch(research.getBranch(), research.getId())
                      .setProgress((int) (BASE_RESEARCH_TIME * IGlobalResearchTree.getInstance().getBranchTime(research.getBranch()) * Math.pow(2, research.getDepth() - 1)));
                }
                SoundUtils.playSuccessSound(player, player.getPosition());
                return;
            }
            if (research.hasEnoughResources(new InvWrapper(player.inventory)))
            {
                if (!research.getResearchRequirement().isEmpty())
                {
                    for (IResearchRequirement requirement : research.getResearchRequirement())
                    {
                        if (!requirement.isFulfilled(colony))
                        {
                            player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.requirementnotmet"), player.getUniqueID());
                            SoundUtils.playErrorSound(player, player.getPosition());
                            return;
                        }
                    }
                }
                // Remove items from player
                if (!InventoryUtils.tryRemoveStorageFromItemHandler(new InvWrapper(player.inventory), research.getCostList()))
                {
                    player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.costnotavailable", new TranslationTextComponent(research.getName())),
                      player.getUniqueID());
                    SoundUtils.playErrorSound(player, player.getPosition());
                    return;
                }
                player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.started", new TranslationTextComponent(research.getName())),
                  player.getUniqueID());
                research.startResearch(colony.getResearchManager().getResearchTree());
                SoundUtils.playSuccessSound(player, player.getPosition());
            }
        }
        else
        {
            if(player.isCreative())
            {
                if (MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchCreativeCompletion.get())
                {
                    colony.getResearchManager()
                      .getResearchTree()
                      .getResearch(research.getBranch(), research.getId())
                      .setProgress((int) (BASE_RESEARCH_TIME * IGlobalResearchTree.getInstance().getBranchTime(research.getBranch()) * Math.pow(2, research.getDepth() - 1)));
                }
            }
            else
            {
                player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.alreadystarted"), player.getUniqueID());
                SoundUtils.playErrorSound(player, player.getPosition());
            }
        }
        colony.markDirty();
    }

    @Override
    public void attemptResetResearch(final PlayerEntity player, final IColony colony, final ILocalResearch research)
    {
        // If in progress and get another request, cancel research, and remove it from the local tree.
        if(research.getState() == ResearchState.IN_PROGRESS)
        {
            player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.stopped",
                new TranslationTextComponent(IGlobalResearchTree.getInstance().getResearch(research.getBranch(), research.getId()).getName())), player.getUniqueID());
            SoundUtils.playSuccessSound(player, player.getPosition());
            removeResearch(research.getBranch(), research.getId());
        }
        // If complete, it's a request to undo the research.
        else if (research.getState() == ResearchState.FINISHED)
        {
            for(String childIds : IGlobalResearchTree.getInstance().getResearch(research.getBranch(), research.getId()).getChildren())
            {
                if(researchTree.get(research.getBranch()).get(childIds) != null)
                {
                    player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.undo.haschilds"), player.getUniqueID());
                    SoundUtils.playErrorSound(player, player.getPosition());
                    return;
                }
            }

            if(!player.isCreative())
            {
                final List<ItemStorage> costList = new ArrayList<>();
                for (final String cost : IGlobalResearchTree.getInstance().getResearchResetCosts())
                {
                    // Validated cost metrics during ResearchListener, so doesn't need to be redone here.
                    // Do, however, need to check against air, in case item type does not exist.
                    final String[] costParts = cost.split(":");
                    final Item costItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(costParts[0], costParts[1]));
                    if(costItem != null && !costItem.equals(Items.AIR))
                    {
                        costList.add(new ItemStorage(new ItemStack(costItem, Integer.parseInt(costParts[2])), false, true));
                    }
                }
                if (!InventoryUtils.tryRemoveStorageFromItemHandler(new InvWrapper(player.inventory), costList))
                {
                    player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.costnotavailable",
                        new TranslationTextComponent(IGlobalResearchTree.getInstance().getResearch(research.getBranch(), research.getId()).getName())), player.getUniqueID());
                    SoundUtils.playErrorSound(player, player.getPosition());
                    return;
                }
            }
            player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.undo",
              new TranslationTextComponent(IGlobalResearchTree.getInstance().getResearch(research.getBranch(), research.getId()).getName())), player.getUniqueID());
            SoundUtils.playSuccessSound(player, player.getPosition());
            removeResearch(research.getBranch(), research.getId());
            resetEffects(colony);
        }
        colony.markDirty();
    }

    /**
     * Does the heavy lifting to remove research and, optionally, effects.
     * @param branch            The research branch id.
     * @param id                The research id.
     */
    private void removeResearch(final String branch, final String id)
    {
        if (!researchTree.get(branch).containsKey(id))
        {
            Log.getLogger().warn("Something went wrong: player attempted to reset research that does not exist or is not started");
        }
        researchTree.get(branch).remove(id);
        inProgress.remove(id);
        isComplete.remove(id);
        if (IGlobalResearchTree.getInstance().getResearch(branch, id).getDepth() == MAX_DEPTH)
        {
            maxLevelResearchCompleted.remove(branch);
        }
    }

    /**
     * Resets effects for a specific colony.
     * @param colony        The colony to reset research effects.
     */
    private void resetEffects(IColony colony)
    {
        if (colony != null)
        {
            // There's no guarantee that undoing a research will only push its effects back one strength grade.
            // Instead, we have to apply every extant effect again.
            // Because effects may cross branches, must check all branches, not just the current one.
            colony.getResearchManager().getResearchEffects().clear();
            for (final Map.Entry<String, Map<String, ILocalResearch>> br : researchTree.entrySet())
            {
                for (final Map.Entry<String, ILocalResearch> research : br.getValue().entrySet())
                {
                    if (research.getValue().getState() == ResearchState.FINISHED)
                    {
                        for (final IResearchEffect<?> effect : IGlobalResearchTree.getInstance().getResearch(br.getKey(), research.getValue().getId()).getEffects())
                        {
                            colony.getResearchManager().getResearchEffects().applyEffect(effect);
                        }
                    }
                }
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
              /// region Updated ID helper.
              if (!MinecoloniesAPIProxy.getInstance().getGlobalResearchTree().hasResearch(research.getBranch(), research.getId()))
              {
                  if(ResearchCompatMap.updateMap.containsKey(research.getId()))
                  {
                      final ResearchState currentState = research.getState();
                      final int progress = research.getProgress();
                      research = new LocalResearch(ResearchCompatMap.updateMap.get(research.getId()), research.getBranch(), research.getDepth());
                      research.setState(currentState);
                      research.setProgress(progress);
                  }
              }
              /// endregion

              if (research.getState() == ResearchState.FINISHED)
              {


                  // Even after correction, we do still need to check for presence; it's possible for someone to have old save data and remove the research,
                  // or to have a different research that was in a now-removed data pack.  But those will get just thrown away.
                  if (MinecoloniesAPIProxy.getInstance().getGlobalResearchTree().hasResearch(research.getBranch(), research.getId()))
                  {
                      for (final IResearchEffect<?> effect : MinecoloniesAPIProxy.getInstance().getGlobalResearchTree().getResearch(research.getBranch(), research.getId()).getEffects())
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
