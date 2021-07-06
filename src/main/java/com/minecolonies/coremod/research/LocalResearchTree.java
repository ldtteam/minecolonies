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
import com.minecolonies.api.util.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.research.util.ResearchConstants.*;

/**
 * The class which contains all research.
 */
public class LocalResearchTree implements ILocalResearchTree
{
    /**
     * The map containing all researches by ID.
     */
    private final Map<ResourceLocation, Map<ResourceLocation, ILocalResearch>> researchTree = new HashMap<>();

    /**
     * All research in progress.
     */
    private final Map<ResourceLocation, ILocalResearch> inProgress = new HashMap<>();

    /**
     * All completed research.
     */
    private final Set<ResourceLocation> isComplete = new HashSet<>();

    /**
     * Map containing all branches for which the max level research has been occupied already.
     */
    private final Set<ResourceLocation> maxLevelResearchCompleted = new HashSet<>();

    @Override
    public ILocalResearch getResearch(final ResourceLocation branch, final ResourceLocation id)
    {
        if (!researchTree.containsKey(branch))
        {
            return null;
        }
        return researchTree.get(branch).get(id);
    }

    @Override
    public boolean hasCompletedResearch(final ResourceLocation researchId)
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
    public void addResearch(final ResourceLocation branch, final ILocalResearch research)
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
    public boolean branchFinishedHighestLevel(final ResourceLocation branch)
    {
        return maxLevelResearchCompleted.contains(branch);
    }

    @Override
    public List<ILocalResearch> getResearchInProgress()
    {
        return ImmutableList.copyOf(inProgress.values());
    }

    @Override
    public void finishResearch(final ResourceLocation id)
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
                      .setProgress(IGlobalResearchTree.getInstance().getBranchData(research.getBranch()).getBaseTime(research.getDepth()));
                }
                SoundUtils.playSuccessSound(player, player.blockPosition());
                return;
            }
            final InvWrapper playerInv = new InvWrapper(player.inventory);
            if (!research.hasEnoughResources(playerInv))
            {
                player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.costnotavailable", research.getName()),
                  player.getUUID());
                SoundUtils.playErrorSound(player, player.blockPosition());
                return;
            }
            if (!research.getResearchRequirement().isEmpty())
            {
                for (IResearchRequirement requirement : research.getResearchRequirement())
                {
                    if (!requirement.isFulfilled(colony))
                    {
                        player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.requirementnotmet"), player.getUUID());
                        SoundUtils.playErrorSound(player, player.blockPosition());
                        return;
                    }
                }
            }
            // We know the player has the items, so now we can remove them safely.
            for(ItemStorage cost : research.getCostList())
            {
                final List<Integer> slotsWithMaterial = InventoryUtils.findAllSlotsInItemHandlerWith(playerInv,
                  stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, cost.getItemStack(), !cost.ignoreDamageValue(), !cost.ignoreNBT()));
                int amount = cost.getAmount();
                for (Integer slotNum : slotsWithMaterial)
                {
                    amount = amount - playerInv.extractItem(slotNum, amount, false).getCount();
                    if(amount <= 0)
                    {
                        break;
                    }
                }
            }
            player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.started", research.getName()),
              player.getUUID());
            research.startResearch(colony.getResearchManager().getResearchTree());
            SoundUtils.playSuccessSound(player, player.blockPosition());
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
                      .setProgress(IGlobalResearchTree.getInstance().getBranchData(research.getBranch()).getBaseTime(research.getDepth()));
                }
            }
            else
            {
                player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.alreadystarted"), player.getUUID());
                SoundUtils.playErrorSound(player, player.blockPosition());
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
                IGlobalResearchTree.getInstance().getResearch(research.getBranch(), research.getId()).getName()), player.getUUID());
            SoundUtils.playSuccessSound(player, player.blockPosition());
            removeResearch(research.getBranch(), research.getId());
        }
        // If complete, it's a request to undo the research.
        else if (research.getState() == ResearchState.FINISHED)
        {
            for(ResourceLocation childIds : IGlobalResearchTree.getInstance().getResearch(research.getBranch(), research.getId()).getChildren())
            {
                if(researchTree.get(research.getBranch()).get(childIds) != null)
                {
                    player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.undo.haschilds"), player.getUUID());
                    SoundUtils.playErrorSound(player, player.blockPosition());
                    return;
                }
            }

            if(!player.isCreative())
            {
                final List<ItemStorage> costList = IGlobalResearchTree.getInstance().getResearchResetCosts();
                final InvWrapper playerInv = new InvWrapper(player.inventory);
                for (final ItemStorage cost : costList)
                {
                    final int count = InventoryUtils.getItemCountInItemHandler(playerInv,
                      stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, cost.getItemStack(), !cost.ignoreDamageValue(), !cost.ignoreNBT()));
                    if (count < cost.getAmount())
                    {
                        player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.costnotavailable",
                          IGlobalResearchTree.getInstance().getResearch(research.getBranch(), research.getId()).getName()), player.getUUID());
                        SoundUtils.playErrorSound(player, player.blockPosition());
                        return;
                    }
                }
                for (ItemStorage cost : costList)
                {
                    final List<Integer> slotsWithMaterial = InventoryUtils.findAllSlotsInItemHandlerWith(playerInv,
                      stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, cost.getItemStack(), !cost.ignoreDamageValue(), !cost.ignoreNBT()));
                    int amount = cost.getAmount();
                    for (Integer slotNum : slotsWithMaterial)
                    {
                        amount = amount - playerInv.extractItem(slotNum, amount, false).getCount();
                        if (amount <= 0)
                        {
                            break;
                        }
                    }
                }
            }
            player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.undo",
              IGlobalResearchTree.getInstance().getResearch(research.getBranch(), research.getId()).getName()), player.getUUID());
            SoundUtils.playSuccessSound(player, player.blockPosition());
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
    private void removeResearch(final ResourceLocation branch, final ResourceLocation id)
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
            colony.getResearchManager().getResearchEffects().removeAllEffects();
            for (final Map.Entry<ResourceLocation, Map<ResourceLocation, ILocalResearch>> branch : researchTree.entrySet())
            {
                for (final Map.Entry<ResourceLocation, ILocalResearch> research : branch.getValue().entrySet())
                {
                    if (research.getValue().getState() == ResearchState.FINISHED)
                    {
                        for (final IResearchEffect<?> effect : IGlobalResearchTree.getInstance().getResearch(branch.getKey(), research.getValue().getId()).getEffects())
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
                  if(ResearchCompatMap.updateMap.containsKey(research.getId().getPath()))
                  {
                      final ResearchState currentState = research.getState();
                      final int progress = research.getProgress();
                      research = new LocalResearch(ResearchCompatMap.updateMap.get(research.getId().getPath()),
                        new ResourceLocation(com.minecolonies.api.util.constant.Constants.MOD_ID, research.getBranch().getPath()), research.getDepth());
                      research.setState(currentState);
                      research.setProgress(progress);
                      Log.getLogger().warn("Research " + research.getId().getPath() + " was in colony save file, and was updated to " + research.getId());
                  }
                  else if(research.getBranch().getNamespace().contains("minecraft"))
                  {
                      final ResearchState currentState = research.getState();
                      final int progress = research.getProgress();
                      research = new LocalResearch(new ResourceLocation(com.minecolonies.api.util.constant.Constants.MOD_ID, research.getId().getPath()),
                        new ResourceLocation(com.minecolonies.api.util.constant.Constants.MOD_ID, research.getBranch().getPath()), research.getDepth());
                      research.setState(currentState);
                      research.setProgress(progress);
                  }
                  else
                  {
                      if(MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchDebugLog.get())
                      {
                          Log.getLogger().warn("Research " + research.getId() + " was in colony save file, but was not in CompatMap.");
                      }
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
                      if(MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchDebugLog.get())
                      {
                          Log.getLogger()
                            .warn("Research " + research.getId() + " was in colony save file, but not found as valid current research.  Progress on this research may be reset.");
                      }
                  }
              }
              addResearch(research.getBranch(), research);
          });
    }
}
