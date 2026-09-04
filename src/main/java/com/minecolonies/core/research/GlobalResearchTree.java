package com.minecolonies.core.research;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.research.IGlobalResearch;
import com.minecolonies.api.research.IGlobalResearchBranch;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.research.IResearchRequirement;
import com.minecolonies.api.research.IResearchEffect;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import io.netty.buffer.Unpooled;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The class which contains all research.
 */
public class GlobalResearchTree implements IGlobalResearchTree
{
    /**
     * The map containing all researches by ID and branch.
     */
    private final Map<Identifier, Map<Identifier, IGlobalResearch>> researchTree = new HashMap<>();

    /**
     * The map matching branch IDs to their branch data.
     */
    private final Map<Identifier, IGlobalResearchBranch> branchDatas = new HashMap<>();

    /**
     * The list containing all resettable researches by Identifier.
     */
    private final List<Identifier> reloadableResearch = new ArrayList<>();

    /**
     * The list containing all autostart research.
     */
    private final Set<IGlobalResearch> autostartResearch = new HashSet<>();

    /**
     * The map containing loaded Research Effect IDs.
     */
    private final Map<Identifier, Set<IGlobalResearch>> researchEffectsIds = new HashMap<>();

    @Override
    public IGlobalResearch getResearch(final Identifier branch, final Identifier id) { return researchTree.get(branch).get(id); }

    @Nullable
    @Override
    public IGlobalResearch getResearch(final Identifier id)
    {
        for(final Map.Entry<Identifier, Map<Identifier, IGlobalResearch>> branch: researchTree.entrySet())
        {
            if(branch.getValue().containsKey(id))
            {
                return branch.getValue().get(id);
            }
        }
        return null;
    }

    @Override
    public boolean hasResearch(final Identifier branch, final Identifier id)
    {
        return (researchTree.containsKey(branch) && researchTree.get(branch).containsKey(id));
    }

    @Override
    public boolean hasResearch(final Identifier id)
    {
        for(final Map.Entry<Identifier, Map<Identifier, IGlobalResearch>> branch: researchTree.entrySet())
        {
            if(branch.getValue().containsKey(id))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addResearch(final Identifier branch, final IGlobalResearch research, final boolean isReloadedWithWorld)
    {
        final Map<Identifier, IGlobalResearch> branchMap;
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

        if (isReloadedWithWorld)
        {
            reloadableResearch.add(research.getId());
        }
        for (IResearchEffect effect : research.getEffects())
        {
            researchEffectsIds.computeIfAbsent(effect.getId(), id -> new HashSet<>()).add(research);
        }
        if (research.isAutostart())
        {
           autostartResearch.add(research);
        }
    }

    @Override
    public void addBranchData(final Identifier branchId, final IGlobalResearchBranch branchData)
    {
        this.branchDatas.put(branchId, branchData);
    }

    @Override
    public Set<IGlobalResearch> getResearchForEffect(final Identifier id)
    {
        return researchEffectsIds.get(id);
    }

    @Override
    public boolean hasResearchEffect(final Identifier id)
    {
        return researchEffectsIds.get(id) != null;
    }

    @Override
    public List<Identifier> getBranches()
    {
        return new ArrayList<>(researchTree.keySet());
    }

    @Override
    public IGlobalResearchBranch getBranchData(final Identifier id)
    {
        if(branchDatas.containsKey(id))
        {
            return branchDatas.get(id);
        }
        else
        {
            return new GlobalResearchBranch(id);
        }
    }

    @Override
    public List<Identifier> getPrimaryResearch(final Identifier branch)
    {
        if (!researchTree.containsKey(branch))
        {
            return Collections.emptyList();
        }
        return researchTree.get(branch).values().stream().filter(research -> research.getParent() == null)
                 .sorted(Comparator.comparing(IGlobalResearch::getId))
                 .map(IGlobalResearch::getId).collect(Collectors.toList());
    }

    @Override
    public void reset()
    {
        for(Identifier reset : reloadableResearch)
        {
            for(Map.Entry<Identifier, Map<Identifier, IGlobalResearch>> branch : researchTree.entrySet())
            {
                branch.getValue().remove(reset);
            }
            for (final Set<IGlobalResearch> effectResearches : researchEffectsIds.values())
            {
                effectResearches.removeIf(r -> r.getId().equals(reset));
            }
        }
        reloadableResearch.clear();
        // Autostart is only accessible as a dynamically-assigned trait, so we can reset all of it.
        autostartResearch.clear();
        branchDatas.clear();
        final Iterator<Map.Entry<Identifier, Map<Identifier, IGlobalResearch>>> iterator = researchTree.entrySet().iterator();
        while (!researchTree.isEmpty() && iterator.hasNext())
        {
            if (iterator.next().getValue().isEmpty())
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
    public void sendGlobalResearchTreePackets(final ServerPlayer player)
    {
        final RegistryFriendlyByteBuf researchTreeFriendlyByteBuf = new RegistryFriendlyByteBuf(new FriendlyByteBuf(Unpooled.buffer()), player.registryAccess());
        serializeNetworkData(researchTreeFriendlyByteBuf);

        new GlobalResearchTreeMessage(researchTreeFriendlyByteBuf).sendToPlayer(player);
    }

    public void serializeNetworkData(final RegistryFriendlyByteBuf buf)
    {
        buf.writeVarInt(researchTree.size());
        for(final Map<Identifier, IGlobalResearch> branch : researchTree.values())
        {
            buf.writeVarInt(branch.size());
            for(final IGlobalResearch research : branch.values())
            {
                StandardFactoryController.getInstance().serialize(buf, research);
            }
        }
        // Lastly, we'll send the branch identifiers.
        for(Map.Entry<Identifier, IGlobalResearchBranch> branch : branchDatas.entrySet())
        {
            buf.writeIdentifier(branch.getKey());
            buf.writeNbt(branch.getValue().writeToNBT());
        }
    }

    @Override
    public void handleGlobalResearchTreeMessage(final RegistryFriendlyByteBuf buf)
    {
        researchTree.clear();
        branchDatas.clear();
        researchEffectsIds.clear();
        for (int branchNum = buf.readVarInt(); branchNum > 0; branchNum--)
        {
            for(int researchNum = buf.readVarInt(); researchNum > 0; researchNum--)
            {
                final IGlobalResearch newResearch = StandardFactoryController.getInstance().deserialize(buf);
                addResearch(newResearch.getBranch(), newResearch, true);
            }
        }
        for (int i = 0; i < researchTree.size(); i++)
        {
            Identifier branchId = buf.readIdentifier();
            branchDatas.put(branchId, new GlobalResearchBranch(buf.readNbt()));
        }
    }

    @Override
    public List<IResearchEffect> getEffectsForResearch(@NotNull final Identifier id)
    {
        for(final Identifier branch: this.getBranches())
        {
            final IGlobalResearch r = this.getResearch(branch, id);
            if (r != null)
            {
                return r.getEffects();
            }
        }
        return new ArrayList<>();
    }

    @Override
    public Set<IGlobalResearch> getAutostartResearches()
    {
        return autostartResearch;
    }

    @Override
    public List<ItemStorage> getResearchResetCosts(final HolderLookup.Provider provider)
    {
        List<ItemStorage> outputList = new ArrayList<>();
        for (String itemId : MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchResetCost.get())
        {
            int amount = 1;
            String[] split = itemId.split(":");
            if (split.length == 3)
            {
                try
                {
                    amount = Integer.parseInt(split[2]);
                }
                catch (Throwable t)
                {
                    Log.getLogger().error("Unable to parse item count: {}", itemId, t);
                }
                itemId = split[0] + ":" + split[1];
            }
            final ItemStack stack = ItemStackUtils.idToItemStack(itemId, provider);
            if (!stack.isEmpty())
            {
                stack.setCount(amount);
                outputList.add(new ItemStorage(stack, false, true));
            }
        }
        return outputList;
    }
}
