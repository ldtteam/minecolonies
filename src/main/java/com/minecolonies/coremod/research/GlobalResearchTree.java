package com.minecolonies.coremod.research;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.research.IGlobalResearch;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.research.IResearchRequirement;
import com.minecolonies.api.research.effects.IResearchEffect;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.coremod.Network;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
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
     * The map containing all researches by ID and branch.
     */
    private final Map<String, Map<String, IGlobalResearch>> researchTree = new HashMap<>();

    /**
     * The map matching branch IDs to their UI-presented names, or IDs otherwise.
     */
    private final Map<String, String> branchNames = new HashMap<>();

    /**
     * The map matching branch IDs the base times, defaulting to 1.
     */
    private final Map<String, Double> branchTimes = new HashMap<>();

    /**
     * The map containing all researches by ResourceLocation and ResearchID.
     */
    private final Map<ResourceLocation, String> researchResourceLocations = new HashMap<>();

    /**
     * The list containing all resettable researches by ResourceLocation.
     */
    private final List<ResourceLocation> reloadableResearch = new ArrayList<>();

    /**
     * The list containing all autostart research.
     */
    private final HashSet<IGlobalResearch> autostartResearch = new HashSet<>();

    /**
     * The map containing loaded Research Effect IDs.
     */
    private final HashSet<String> researchEffectsIds = new HashSet<>();

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
            branchNames.put(branch, branch);
            branchTimes.put(branch, 1.0);
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
            reloadableResearch.add(research.getResourceLocation());
        }
        if (research.isAutostart())
        {
           autostartResearch.add(research);
        }
        for (IResearchEffect<?> effect : research.getEffects())
        {
            researchEffectsIds.add(effect.getId());
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
        for(ResourceLocation reset : reloadableResearch)
        {
            if(!researchResourceLocations.containsKey(reset))
            {
                continue;
            }
            for(Map.Entry<String, Map<String, IGlobalResearch>> branch : researchTree.entrySet())
            {
                branch.getValue().remove(researchResourceLocations.get(reset));
            }
            researchResourceLocations.remove(reset);
        }
        reloadableResearch.clear();
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
    public void setBranchName(final String branchId, final String branchName)
    {
        branchNames.put(branchId, branchName);
    }

    @Override
    public TranslationTextComponent getBranchName(final String branchId)
    {
        return new TranslationTextComponent(branchNames.get(branchId));
    }

    @Override
    public void setBranchTime(final String branchId, final double baseTime)
    {
        branchTimes.put(branchId, baseTime);
    }

    @Override
    public double getBranchTime(final String branchId)
    {
        return branchTimes.get(branchId);
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
          researchTagList = researchTree.values()
                             .stream()
                             .flatMap(map -> map.values().stream())
                             .map(research -> StandardFactoryController.getInstance().serialize(research))
                             .collect(NBTUtils.toListNBT());
        compound.put(TAG_RESEARCH_TREE, researchTagList);
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
    public void sendGlobalResearchTreePackets(final ServerPlayerEntity player)
    {
        final PacketBuffer researchTreePacketBuffer = new PacketBuffer(Unpooled.buffer());
        serializeNetworkData(researchTreePacketBuffer);

        Network.getNetwork().sendToPlayer(new GlobalResearchTreeMessage(researchTreePacketBuffer), player);
    }

    public void serializeNetworkData(final PacketBuffer buf)
    {
        CompoundNBT treeNBT = new CompoundNBT();
        writeToNBT(treeNBT);
        buf.writeCompoundTag(treeNBT);

        // Lastly, we'll send the branch identifiers.
        for(Map.Entry<String, Map<String, IGlobalResearch>> branch : researchTree.entrySet())
        {
            buf.writeString(branch.getKey());
            buf.writeString(branchNames.get(branch.getKey()));
            buf.writeDouble(branchTimes.get(branch.getKey()));
        }
    }

    @Override
    public IMessage handleGlobalResearchTreeMessage(final PacketBuffer buf)
    {
        try
        {
            readFromNBT(buf.readCompoundTag());
            for(int i = 0; i < researchTree.entrySet().size(); i++)
            {
                final String branchId = buf.readString();
                branchNames.put(branchId, buf.readString());
                branchTimes.put(branchId, buf.readDouble());
            }
        }
        catch(NullPointerException npe)
        {
            Log.getLogger().error("Global Research Error, please report : " + npe);
        }
        return null;
    }

    @Override
    public List<IResearchEffect<?>> getEffectsForResearch(@NotNull final String id)
    {
        for(final String branch: this.getBranches())
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
    public HashSet<IGlobalResearch> getAutostartResearches()
    {
        return autostartResearch;
    }

    @Override
    public List<String> getResearchResetCosts()
    {
        List<String> outputList = new ArrayList<>();
        for (final String cost : MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchResetCost.get())
        {
            final String[] costParts = cost.split(":");
            if (costParts.length == 1)
            {
                outputList.add("minecraft:" + costParts[0] + ":1");
            }
            if (costParts.length == 2)
            {
                try
                {
                    final int count = Integer.parseInt(costParts[1]);
                    outputList.add("minecraft:" + costParts[0] + ":" + count);
                }
                catch (final NumberFormatException err)
                {
                    outputList.add(costParts[0] + ":" + costParts[1] + ":1");
                }
            }
            if (costParts.length == 3)
            {
                try
                {
                    final int count = Integer.parseInt(costParts[2]);
                    outputList.add(costParts[0] + ":" + costParts[1] + ":" + count);
                }
                catch (final NumberFormatException err)
                {
                    Log.getLogger().error("Malformed count value in Research Reset Cost for" + costParts[0] + ":" + costParts[1] + " where " + costParts[2] + "is not a number.");
                    outputList.add(costParts[0] + ":" + costParts[1] + ":1");
                }
            }
            if (MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchDebugLog.get())
            {
                Log.getLogger().info("Validated research reset cost : " + outputList.get(outputList.size() - 1));
            }
        }
        return outputList;
    }
}
