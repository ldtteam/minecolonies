package com.minecolonies.coremod.research;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.research.IGlobalResearch;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.research.IResearchRequirement;
import com.minecolonies.api.research.effects.IResearchEffect;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.coremod.Network;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;
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
    private final Map<ResourceLocation, Map<ResourceLocation, IGlobalResearch>> researchTree = new HashMap<>();

    /**
     * The map matching branch IDs to their UI-presented names, or IDs otherwise.
     */
    private final Map<ResourceLocation, TranslationTextComponent> branchNames = new HashMap<>();

    /**
     * The map matching branch IDs the base times, defaulting to 1.
     */
    private final Map<ResourceLocation, Double> branchTimes = new HashMap<>();

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
    private final HashSet<ResourceLocation> researchEffectsIds = new HashSet<>();

    @Override
    public IGlobalResearch getResearch(final ResourceLocation branch, final ResourceLocation id) { return researchTree.get(branch).get(id); }

    @Override
    public boolean hasResearch(final ResourceLocation branch, final ResourceLocation id)
    {
        return (researchTree.containsKey(branch) && researchTree.get(branch).containsKey(id));
    }

    @Override
    public boolean hasResearch(final ResourceLocation id)
    {
        for(final Map.Entry<ResourceLocation, Map<ResourceLocation, IGlobalResearch>> branch: researchTree.entrySet())
        {
            if(branch.getValue().containsKey(id))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addResearch(final ResourceLocation branch, final IGlobalResearch research, final boolean isReloadedWithWorld)
    {
        final Map<ResourceLocation, IGlobalResearch> branchMap;
        if (researchTree.containsKey(branch))
        {
            branchMap = researchTree.get(branch);
        }
        else
        {
            branchMap = new HashMap<>();
            branchNames.put(branch, new TranslationTextComponent(branch.getPath()));
            branchTimes.put(branch, 1.0);
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
        for (IResearchEffect<?> effect : research.getEffects())
        {
            researchEffectsIds.add(effect.getId());
        }
        if (research.isAutostart())
        {
           autostartResearch.add(research);
        }
    }

    @Override
    public boolean hasResearchEffect(final ResourceLocation id)
    {
        return researchEffectsIds.contains(id);
    }

    @Override
    public List<ResourceLocation> getBranches()
    {
        return new ArrayList<>(researchTree.keySet());
    }

    @Override
    public List<ResourceLocation> getPrimaryResearch(final ResourceLocation branch)
    {
        if (!researchTree.containsKey(branch))
        {
            return Collections.emptyList();
        }
        return researchTree.get(branch).values().stream().filter(research -> research.getParent().getPath().isEmpty())
                 .sorted(Comparator.comparing(IGlobalResearch::getId))
                 .map(IGlobalResearch::getId).collect(Collectors.toList());
    }

    @Override
    public void reset()
    {
        for(ResourceLocation reset : reloadableResearch)
        {
            for(Map.Entry<ResourceLocation, Map<ResourceLocation, IGlobalResearch>> branch : researchTree.entrySet())
            {
                branch.getValue().remove(reset);
            }
        }
        reloadableResearch.clear();
        // Autostart is only accessible as a dynamically-assigned trait, so we can reset all of it.
        autostartResearch.clear();
        final Iterator<Map.Entry<ResourceLocation, Map<ResourceLocation, IGlobalResearch>>> iterator = researchTree.entrySet().iterator();
        while (researchTree.entrySet().size() > 0 && iterator.hasNext())
        {
            if(iterator.next().getValue().size() == 0)
            {
                iterator.remove();
            }
        }
    }

    @Override
    public void setBranchName(final ResourceLocation branchId, final TranslationTextComponent branchName)
    {
        branchNames.put(branchId, branchName);
    }

    @Override
    public TranslationTextComponent getBranchName(final ResourceLocation branchId)
    {
        return branchNames.get(branchId);
    }

    @Override
    public void setBranchTime(final ResourceLocation branchId, final double baseTime)
    {
        branchTimes.put(branchId, baseTime);
    }

    @Override
    public double getBranchTime(final ResourceLocation branchId)
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
        for(Map.Entry<ResourceLocation, Map<ResourceLocation, IGlobalResearch>> branch : researchTree.entrySet())
        {
            buf.writeResourceLocation(branch.getKey());
            buf.writeString(branchNames.get(branch.getKey()).getKey());
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
                ResourceLocation branchId = buf.readResourceLocation();
                branchNames.put(branchId, new TranslationTextComponent(buf.readString()));
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
    public List<IResearchEffect<?>> getEffectsForResearch(@NotNull final ResourceLocation id)
    {
        for(final ResourceLocation branch: this.getBranches())
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
    public List<ItemStorage> getResearchResetCosts()
    {
        List<ItemStorage> outputList = new ArrayList<>();
        for (String itemId : MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchResetCost.get())
        {
            final int tagIndex = itemId.indexOf("{");
            final String tag = tagIndex > 0 ? itemId.substring(tagIndex) : null;
            itemId = tagIndex > 0 ? itemId.substring(0, tagIndex) : itemId;
            String[] split = itemId.split(":");
            if(split.length != 2)
            {
                if(split.length == 1)
                {
                    final String[] tempArray ={"minecraft", split[0]};
                    split = tempArray;
                }
                else if(split.length > 3)
                {
                    Log.getLogger().error("Unable to parse Research Reset Cost definition: " + itemId);
                }
            }
            final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(split[0], split[1]));
            final ItemStack stack = new ItemStack(item);
            if (stack.isEmpty())
            {
                Log.getLogger().warn("Unable to parse Research Reset Cost definition: " + itemId);
                continue;
            }
            if (tag != null)
            {
                try
                {
                    stack.setTag(JsonToNBT.getTagFromJson(tag));
                    outputList.add(new ItemStorage(stack, false, false));
                }
                catch (CommandSyntaxException parseException)
                {
                    //Unable to parse tags, drop them.
                    Log.getLogger().error("Unable to parse Research Reset Cost definition: " + itemId);
                }
            }
            else
            {
                outputList.add(new ItemStorage(stack, false, true));
            }
        }
        return outputList;
    }
}
