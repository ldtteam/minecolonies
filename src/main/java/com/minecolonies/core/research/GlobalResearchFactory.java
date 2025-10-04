package com.minecolonies.core.research;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.research.*;
import com.minecolonies.api.research.IResearchEffect;
import com.minecolonies.api.research.factories.IGlobalResearchFactory;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.SerializationIdentifierConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.minecolonies.api.research.util.ResearchConstants.*;

/**
 * Factory implementation taking care of creating new instances, serializing and deserializing GlobalResearch.
 */
public class GlobalResearchFactory implements IGlobalResearchFactory
{
    @NotNull
    @Override
    public TypeToken<GlobalResearch> getFactoryOutputType()
    {
        return TypeToken.of(GlobalResearch.class);
    }

    @NotNull
    @Override
    public TypeToken<FactoryVoidInput> getFactoryInputType()
    {
        return TypeConstants.FACTORYVOIDINPUT;
    }

    @NotNull
    @Override
    public IGlobalResearch getNewInstance(
        final ResourceLocation id,
        final ResourceLocation parent,
        final ResourceLocation branch,
        final TranslatableContents name,
        final TranslatableContents subtitle,
        final int depth,
        final int sortOrder,
        final boolean onlyChild,
        final boolean hidden,
        final boolean autostart,
        final boolean instant,
        final boolean immutable)
    {
        return new GlobalResearch(id, parent, branch, name, subtitle, depth, sortOrder, onlyChild, hidden, autostart, instant, immutable);
    }

    @NotNull
    @Override
    public CompoundTag serialize(@NotNull final IFactoryController controller, @NotNull final IGlobalResearch research)
    {
        final CompoundTag compound = new CompoundTag();
        if (research.getParent() != null)
        {
            compound.putString(TAG_PARENT, research.getParent().toString());
        }
        compound.putString(TAG_ID, research.getId().toString());
        compound.putString(TAG_BRANCH, research.getBranch().toString());
        compound.putString(TAG_NAME, research.getName().getKey());
        compound.putInt(TAG_RESEARCH_LVL, research.getDepth());
        compound.putInt(TAG_RESEARCH_SORT, research.getSortOrder());
        compound.putBoolean(TAG_ONLY_CHILD, research.hasOnlyChild());
        compound.putString(TAG_SUBTITLE_NAME, research.getSubtitle().getKey());
        compound.putBoolean(TAG_INSTANT, research.isInstant());
        compound.putBoolean(TAG_AUTOSTART, research.isAutostart());
        compound.putBoolean(TAG_IMMUTABLE, research.isImmutable());
        compound.putBoolean(TAG_HIDDEN, research.isHidden());
        @NotNull final ListTag costTagList = research.getCostList().stream().map(cost ->
        {
            final CompoundTag costCompound = new CompoundTag();
            costCompound.putString(TAG_COST_TYPE, cost.getType().getRegistryName().toString());
            costCompound.put(TAG_COST_NBT, cost.writeToNBT());
            return compound;
        }).collect(NBTUtils.toListNBT());
        compound.put(TAG_COSTS, costTagList);

        @NotNull final ListTag reqTagList = research.getResearchRequirements().stream().map(req ->
        {
            final CompoundTag reqCompound = new CompoundTag();
            reqCompound.putString(TAG_REQ_TYPE, req.getRegistryEntry().getRegistryName().toString());
            reqCompound.put(TAG_REQ_ITEM, req.writeToNBT());
            return reqCompound;
        }).collect(NBTUtils.toListNBT());
        compound.put(TAG_REQS, reqTagList);

        @NotNull final ListTag effectTagList = research.getEffects().stream().map(eff ->
        {
            final CompoundTag effectCompound = new CompoundTag();
            effectCompound.putString(TAG_EFFECT_TYPE, eff.getRegistryEntry().getRegistryName().toString());
            effectCompound.put(TAG_EFFECT_ITEM, eff.writeToNBT());
            return effectCompound;
        }).collect(NBTUtils.toListNBT());
        compound.put(TAG_EFFECTS, effectTagList);

        @NotNull final ListTag childTagList = research.getChildren().stream().map(child ->
        {
            final CompoundTag childCompound = new CompoundTag();
            childCompound.putString(TAG_RESEARCH_CHILD, child.toString());
            return childCompound;
        }).collect(NBTUtils.toListNBT());
        compound.put(TAG_CHILDS, childTagList);

        return compound;
    }

    @NotNull
    @Override
    public IGlobalResearch deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundTag nbt)
    {
        final ResourceLocation id = new ResourceLocation(nbt.getString(TAG_ID));
        final ResourceLocation parent = nbt.contains(TAG_PARENT) ? new ResourceLocation(nbt.getString(TAG_PARENT)) : null;
        final ResourceLocation branch = new ResourceLocation(nbt.getString(TAG_BRANCH));
        final TranslatableContents name = new TranslatableContents(nbt.getString(TAG_NAME), null, TranslatableContents.NO_ARGS);
        final TranslatableContents subtitle = new TranslatableContents(nbt.getString(TAG_SUBTITLE_NAME), null, TranslatableContents.NO_ARGS);
        final int depth = nbt.getInt(TAG_RESEARCH_LVL);
        final int sortOrder =  nbt.getInt(TAG_RESEARCH_SORT);
        final boolean onlyChild = nbt.getBoolean(TAG_ONLY_CHILD);
        final boolean instant = nbt.getBoolean(TAG_INSTANT);
        final boolean autostart = nbt.getBoolean(TAG_AUTOSTART);
        final boolean immutable = nbt.getBoolean(TAG_IMMUTABLE);
        final boolean hidden = nbt.getBoolean(TAG_HIDDEN);

        final IGlobalResearch research = getNewInstance(id, parent, branch, name, subtitle, depth, sortOrder, onlyChild, hidden, autostart, instant, immutable);

        NBTUtils.streamCompound(nbt.getList(TAG_COSTS, Tag.TAG_COMPOUND)).forEach(compound -> {
            final ModResearchCosts.ResearchCostEntry researchCostType = IMinecoloniesAPI.getInstance().getResearchCostRegistry().getValue(new ResourceLocation(compound.getString(TAG_COST_TYPE)));
            research.addCost(researchCostType.readFromNBT(compound.getCompound(TAG_COST_NBT)));
        });
        NBTUtils.streamCompound(nbt.getList(TAG_REQS, Tag.TAG_COMPOUND))
            .forEach(compound -> research.addRequirement(Objects.requireNonNull(IMinecoloniesAPI.getInstance()
                .getResearchRequirementRegistry()
                .getValue(ResourceLocation.tryParse(compound.getString(TAG_REQ_TYPE)))).readFromNBT(compound.getCompound(TAG_REQ_ITEM))));

        NBTUtils.streamCompound(nbt.getList(TAG_EFFECTS, Tag.TAG_COMPOUND))
            .forEach(compound -> research.addEffect(Objects.requireNonNull(IMinecoloniesAPI.getInstance().getResearchEffectRegistry()
                .getValue(ResourceLocation.tryParse(compound.getString(TAG_EFFECT_TYPE)))).readFromNBT(compound.getCompound(TAG_EFFECT_ITEM))));

        NBTUtils.streamCompound(nbt.getList(TAG_CHILDS, Tag.TAG_COMPOUND)).forEach(compound -> research.addChild(new ResourceLocation(compound.getString(TAG_RESEARCH_CHILD))));
        return research;
    }

    @Override
    public void serialize(@NotNull IFactoryController controller, IGlobalResearch input, FriendlyByteBuf packetBuffer)
    {
        packetBuffer.writeResourceLocation(input.getId());
        packetBuffer.writeBoolean(input.getParent() != null);
        if (input.getParent() != null)
        {
            packetBuffer.writeResourceLocation(input.getParent());
        }
        packetBuffer.writeResourceLocation(input.getBranch());
        packetBuffer.writeUtf(input.getName().getKey());
        packetBuffer.writeUtf(input.getSubtitle().getKey());
        packetBuffer.writeVarInt(input.getDepth());
        packetBuffer.writeVarInt(input.getSortOrder());
        packetBuffer.writeBoolean(input.hasOnlyChild());
        packetBuffer.writeBoolean(input.isInstant());
        packetBuffer.writeBoolean(input.isAutostart());
        packetBuffer.writeBoolean(input.isImmutable());
        packetBuffer.writeBoolean(input.isHidden());
        packetBuffer.writeVarInt(input.getCostList().size());
        for (IResearchCost cost : input.getCostList())
        {
            packetBuffer.writeRegistryId(IMinecoloniesAPI.getInstance().getResearchCostRegistry(), cost.getType());
            packetBuffer.writeNbt(cost.writeToNBT());
        }
        packetBuffer.writeVarInt(input.getResearchRequirements().size());
        for (IResearchRequirement req : input.getResearchRequirements())
        {
            packetBuffer.writeRegistryId(IMinecoloniesAPI.getInstance().getResearchRequirementRegistry(), req.getRegistryEntry());
            packetBuffer.writeNbt(req.writeToNBT());
        }
        packetBuffer.writeVarInt(input.getEffects().size());
        for (IResearchEffect effect : input.getEffects())
        {
            packetBuffer.writeRegistryId(IMinecoloniesAPI.getInstance().getResearchEffectRegistry(), effect.getRegistryEntry());
            packetBuffer.writeNbt(effect.writeToNBT());
        }
        packetBuffer.writeVarInt(input.getChildren().size());
        for (ResourceLocation child : input.getChildren())
        {
            packetBuffer.writeResourceLocation(child);
        }
    }

    @NotNull
    @Override
    public IGlobalResearch deserialize(@NotNull IFactoryController controller, FriendlyByteBuf buffer) throws Throwable
    {
        final ResourceLocation id = buffer.readResourceLocation();
        final ResourceLocation parent = buffer.readBoolean() ? buffer.readResourceLocation() : null;
        final ResourceLocation branch = buffer.readResourceLocation();
        final TranslatableContents name = new TranslatableContents(buffer.readUtf(), null, TranslatableContents.NO_ARGS);
        final TranslatableContents subtitle = new TranslatableContents(buffer.readUtf(), null, TranslatableContents.NO_ARGS);
        final int depth = buffer.readVarInt();
        final int sortOrder = buffer.readVarInt();
        final boolean hasOnlyChild = buffer.readBoolean();
        final boolean instant = buffer.readBoolean();
        final boolean autostart = buffer.readBoolean();
        final boolean immutable = buffer.readBoolean();
        final boolean hidden = buffer.readBoolean();

        final IGlobalResearch research = getNewInstance(id, parent, branch, name, subtitle, depth, sortOrder, hasOnlyChild, hidden, autostart, instant, immutable);

        final int costSize = buffer.readVarInt();
        for(int i = 0; i < costSize; i++)
        {
            final ModResearchCosts.ResearchCostEntry researchCostEntry = buffer.readRegistryIdSafe(ModResearchCosts.ResearchCostEntry.class);
            research.addCost(researchCostEntry.readFromNBT(buffer.readNbt()));
        }

        final int reqCount = buffer.readVarInt();
        for(int i = 0; i < reqCount; i++)
        {
            final ModResearchRequirements.ResearchRequirementEntry researchRequirementEntry = buffer.readRegistryIdSafe(ModResearchRequirements.ResearchRequirementEntry.class);
            research.addRequirement(researchRequirementEntry.readFromNBT(buffer.readNbt()));
        }

        final int effectCount = buffer.readVarInt();
        for(int i = 0; i < effectCount; i++)
        {
            final ModResearchEffects.ResearchEffectEntry researchEffectEntry = buffer.readRegistryIdSafe(ModResearchEffects.ResearchEffectEntry.class);
            research.addEffect(researchEffectEntry.readFromNBT(buffer.readNbt()));
        }

        final int childCount = buffer.readVarInt();
        for(int i = 0; i < childCount; i++)
        {
            research.addChild(buffer.readResourceLocation());
        }
        return research;
    }

    @Override
    public short getSerializationId()
    {
        return SerializationIdentifierConstants.GLOBAL_RESEARCH_ID;
    }
}
