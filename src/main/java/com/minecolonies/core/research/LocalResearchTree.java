package com.minecolonies.core.research;

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
import com.minecolonies.core.event.QuestObjectiveEventHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.research.util.ResearchConstants.MAX_DEPTH;
import static com.minecolonies.api.research.util.ResearchConstants.TAG_RESEARCH_TREE;
import static com.minecolonies.api.util.constant.TranslationConstants.MESSAGE_RESEARCH_STARTED;

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

    /**
     * The colony reference.
     */
    public final IColony colony;

    public LocalResearchTree(final IColony colony)
    {
        this.colony = colony;
    }

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
        if (IGlobalResearchTree.getInstance().hasResearch(researchId))
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

            QuestObjectiveEventHandler.onResearchComplete(colony, research.getId());
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

        QuestObjectiveEventHandler.onResearchComplete(colony, id);
    }

    @Override
    public void attemptBeginResearch(final Player player, final IColony colony, final IGlobalResearch research)
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
                colony.getResearchManager().markDirty();
                SoundUtils.playSuccessSound(player, player.blockPosition());
                colony.getResearchManager().markDirty();
                return;
            }
            final InvWrapper playerInv = new InvWrapper(player.getInventory());
            if (!research.hasEnoughResources(playerInv))
            {
                MessageUtils.format("com.minecolonies.coremod.research.costnotavailable", MutableComponent.create(research.getName())).sendTo(player);
                SoundUtils.playErrorSound(player, player.blockPosition());
                return;
            }
            if (!research.getResearchRequirement().isEmpty())
            {
                for (IResearchRequirement requirement : research.getResearchRequirement())
                {
                    if (!requirement.isFulfilled(colony))
                    {
                        MessageUtils.format("com.minecolonies.coremod.research.requirementnotmet").sendTo(player);
                        SoundUtils.playErrorSound(player, player.blockPosition());
                        return;
                    }
                }
            }
            // We know the player has the items, so now we can remove them safely.
            for (final SizedIngredient cost : research.getCostList())
            {
                int toRemoveLeft = cost.count();
                final List<Integer> slotsWithMaterial = InventoryUtils.findAllSlotsInItemHandlerWith(playerInv, cost.ingredient());
                for (Integer slotNum : slotsWithMaterial)
                {
                    toRemoveLeft = toRemoveLeft - playerInv.extractItem(slotNum, toRemoveLeft, false).getCount();
                    if (toRemoveLeft <= 0)
                    {
                        break;
                    }
                }
            }
            MessageUtils.format(MESSAGE_RESEARCH_STARTED, MutableComponent.create(research.getName())).sendTo(player);
            research.startResearch(colony.getResearchManager().getResearchTree());
            colony.getResearchManager().markDirty();
            SoundUtils.playSuccessSound(player, player.blockPosition());
        }
        else
        {
            if (player.isCreative())
            {
                if (MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchCreativeCompletion.get())
                {
                    colony.getResearchManager()
                      .getResearchTree()
                      .getResearch(research.getBranch(), research.getId())
                      .setProgress(IGlobalResearchTree.getInstance().getBranchData(research.getBranch()).getBaseTime(research.getDepth()));
                    colony.getResearchManager().markDirty();
                }
            }
            else
            {
                MessageUtils.format("com.minecolonies.coremod.research.alreadystarted").sendTo(player);
                SoundUtils.playErrorSound(player, player.blockPosition());
            }
        }
        colony.markDirty();
    }

    @Override
    public void attemptResetResearch(final Player player, final IColony colony, final ILocalResearch research)
    {
        // If in progress and get another request, cancel research, and remove it from the local tree.
        if (research.getState() == ResearchState.IN_PROGRESS)
        {
            MessageUtils.format("com.minecolonies.coremod.research.stopped",
                MutableComponent.create(IGlobalResearchTree.getInstance().getResearch(research.getBranch(), research.getId()).getName()))
              .sendTo(player);
            SoundUtils.playSuccessSound(player, player.blockPosition());
            removeResearch(research.getBranch(), research.getId());
            colony.getResearchManager().markDirty();
        }
        // If complete, it's a request to undo the research.
        else if (research.getState() == ResearchState.FINISHED)
        {
            for (ResourceLocation childIds : IGlobalResearchTree.getInstance().getResearch(research.getBranch(), research.getId()).getChildren())
            {
                if (researchTree.get(research.getBranch()).get(childIds) != null)
                {
                    MessageUtils.format("com.minecolonies.coremod.research.undo.haschilds").sendTo(player);
                    SoundUtils.playErrorSound(player, player.blockPosition());
                    return;
                }
            }

            if (!player.isCreative())
            {
                final List<ItemStorage> costList = IGlobalResearchTree.getInstance().getResearchResetCosts(colony.getWorld().registryAccess());
                final InvWrapper playerInv = new InvWrapper(player.getInventory());
                for (final ItemStorage cost : costList)
                {
                    final int count = InventoryUtils.getItemCountInItemHandler(playerInv,
                      stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, cost.getItemStack(), !cost.ignoreDamageValue(), !cost.ignoreNBT()));
                    if (count < cost.getAmount())
                    {
                        MessageUtils.format("com.minecolonies.coremod.research.costnotavailable",
                          MutableComponent.create(IGlobalResearchTree.getInstance().getResearch(research.getBranch(), research.getId()).getName())).sendTo(player);
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
            MessageUtils.format("com.minecolonies.coremod.research.undo",
                MutableComponent.create(IGlobalResearchTree.getInstance().getResearch(research.getBranch(), research.getId()).getName()))
              .sendTo(player);
            SoundUtils.playSuccessSound(player, player.blockPosition());
            removeResearch(research.getBranch(), research.getId());
            resetEffects(colony);
            colony.getResearchManager().markDirty();
        }
        colony.markDirty();
    }

    /**
     * Does the heavy lifting to remove research and, optionally, effects.
     *
     * @param branch The research branch id.
     * @param id     The research id.
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
     *
     * @param colony The colony to reset research effects.
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
                            colony.getResearchManager().markDirty();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void writeToNBT(@NotNull final HolderLookup.Provider provider, final CompoundTag compound)
    {
        final ListTag researchList = new ListTag();
        for (final Map<ResourceLocation, ILocalResearch> researchMap : researchTree.values())
        {
            for (final ILocalResearch research : researchMap.values())
            {
                researchList.add(StandardFactoryController.getInstance().serializeTag(provider, research));
            }
        }

        compound.put(TAG_RESEARCH_TREE, researchList);
    }

    @Override
    public void readFromNBT(@NotNull final HolderLookup.Provider provider, final CompoundTag compound, final IResearchEffectManager effects)
    {
        researchTree.clear();
        inProgress.clear();
        isComplete.clear();
        maxLevelResearchCompleted.clear();
        NBTUtils.streamCompound(compound.getList(TAG_RESEARCH_TREE, Tag.TAG_COMPOUND))
          .map(researchCompound -> (ILocalResearch) StandardFactoryController.getInstance().deserializeTag(provider, researchCompound))
          .forEach(research -> {
              /// region Updated ID helper.
              if (!MinecoloniesAPIProxy.getInstance().getGlobalResearchTree().hasResearch(research.getBranch(), research.getId()))
              {
                  if (research.getBranch().getNamespace().contains("minecraft"))
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
                      if (MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchDebugLog.get())
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
                      for (final IResearchEffect<?> effect : MinecoloniesAPIProxy.getInstance()
                        .getGlobalResearchTree()
                        .getResearch(research.getBranch(), research.getId())
                        .getEffects())
                      {
                          effects.applyEffect(effect);
                      }
                  }
                  else
                  {
                      if (MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchDebugLog.get())
                      {
                          Log.getLogger()
                            .warn("Research " + research.getId() + " was in colony save file, but not found as valid current research.  Progress on this research may be reset.");
                      }
                  }
              }
              addResearch(research.getBranch(), research);
          });
    }

    @Override
    public List<ResourceLocation> getCompletedList()
    {
        return new ArrayList<>(isComplete);
    }

    @Override
    public boolean isComplete(final ResourceLocation location)
    {
        return isComplete.contains(location);
    }
}
