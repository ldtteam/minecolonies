package com.minecolonies.core.research;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.research.*;
import com.minecolonies.api.research.IResearchEffect;
import com.minecolonies.api.research.factories.IGlobalResearchFactory;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.Utils;
import com.minecolonies.api.util.constant.SerializationIdentifierConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
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
        final Identifier id,
        final Identifier parent,
        final Identifier branch,
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
    public CompoundTag serialize(final @NotNull HolderLookup.Provider provider, @NotNull final IFactoryController controller, @NotNull final IGlobalResearch research)
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
        compound.put(TAG_COSTS, Utils.serializeCodecMess(SizedIngredient.NESTED_CODEC.listOf(), provider, research.getCostList()));

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
    public IGlobalResearch deserialize(final @NotNull HolderLookup.Provider provider, @NotNull final IFactoryController controller, @NotNull final CompoundTag nbt)
    {
        final Identifier id = Identifier.parse(nbt.getStringOr(TAG_ID, ""));
        final Identifier parent = nbt.contains(TAG_PARENT) ? Identifier.parse(nbt.getStringOr(TAG_PARENT, "")) : null;
        final Identifier branch = Identifier.parse(nbt.getStringOr(TAG_BRANCH, ""));
        final TranslatableContents name = new TranslatableContents(nbt.getStringOr(TAG_NAME, ""), null, TranslatableContents.NO_ARGS);
        final TranslatableContents subtitle = new TranslatableContents(nbt.getStringOr(TAG_SUBTITLE_NAME, ""), null, TranslatableContents.NO_ARGS);
        final int depth = nbt.getIntOr(TAG_RESEARCH_LVL, 0);
        final int sortOrder =  nbt.getIntOr(TAG_RESEARCH_SORT, 0);
        final boolean onlyChild = nbt.getBooleanOr(TAG_ONLY_CHILD, false);
        final boolean instant = nbt.getBooleanOr(TAG_INSTANT, false);
        final boolean autostart = nbt.getBooleanOr(TAG_AUTOSTART, false);
        final boolean immutable = nbt.getBooleanOr(TAG_IMMUTABLE, false);
        final boolean hidden = nbt.getBooleanOr(TAG_HIDDEN, false);

        final IGlobalResearch research = getNewInstance(id, parent, branch, name, subtitle, depth, sortOrder, onlyChild, hidden, autostart, instant, immutable);

        Utils.deserializeCodecMess(SizedIngredient.NESTED_CODEC.listOf(), provider, nbt.get(TAG_COSTS)).forEach(research::addCost);

        NBTUtils.streamCompound(nbt.getListOrEmpty(TAG_REQS))
            .forEach(compound -> research.addRequirement(Objects.requireNonNull(IMinecoloniesAPI.getInstance()
                .getResearchRequirementRegistry()
                .getValue(Identifier.tryParse(compound.getStringOr(TAG_REQ_TYPE, "")))).readFromNBT(compound.getCompoundOrEmpty(TAG_REQ_ITEM))));

        NBTUtils.streamCompound(nbt.getListOrEmpty(TAG_EFFECTS))
            .forEach(compound -> research.addEffect(Objects.requireNonNull(IMinecoloniesAPI.getInstance()
                .getResearchEffectRegistry()
                .getValue(Identifier.tryParse(compound.getStringOr(TAG_EFFECT_TYPE, "")))).readFromNBT(compound.getCompoundOrEmpty(TAG_EFFECT_ITEM))));

        NBTUtils.streamCompound(nbt.getListOrEmpty(TAG_CHILDS)).forEach(compound -> research.addChild(Identifier.parse(compound.getStringOr(TAG_RESEARCH_CHILD, ""))));
        return research;
    }

    @Override
    public void serialize(final @NotNull IFactoryController controller, final @NotNull IGlobalResearch input, final RegistryFriendlyByteBuf packetBuffer)
    {
        packetBuffer.writeIdentifier(input.getId());
        packetBuffer.writeBoolean(input.getParent() != null);
        if (input.getParent() != null)
        {
            packetBuffer.writeIdentifier(input.getParent());
        }
        packetBuffer.writeIdentifier(input.getBranch());
        packetBuffer.writeUtf(input.getName().getKey());
        packetBuffer.writeUtf(input.getSubtitle().getKey());
        packetBuffer.writeVarInt(input.getDepth());
        packetBuffer.writeVarInt(input.getSortOrder());
        packetBuffer.writeBoolean(input.hasOnlyChild());
        packetBuffer.writeBoolean(input.isInstant());
        packetBuffer.writeBoolean(input.isAutostart());
        packetBuffer.writeBoolean(input.isImmutable());
        packetBuffer.writeBoolean(input.isHidden());
        Utils.serializeCodecMess(SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), packetBuffer, input.getCostList());
        packetBuffer.writeVarInt(input.getResearchRequirements().size());
        for (IResearchRequirement req : input.getResearchRequirements())
        {
            packetBuffer.writeById(IMinecoloniesAPI.getInstance().getResearchRequirementRegistry()::getIdOrThrow, req.getRegistryEntry());
            packetBuffer.writeNbt(req.writeToNBT());
        }
        packetBuffer.writeVarInt(input.getEffects().size());
        for (IResearchEffect effect : input.getEffects())
        {
            packetBuffer.writeById(IMinecoloniesAPI.getInstance().getResearchEffectRegistry()::getIdOrThrow, effect.getRegistryEntry());
            packetBuffer.writeNbt(effect.writeToNBT());
        }
        packetBuffer.writeVarInt(input.getChildren().size());
        for (Identifier child : input.getChildren())
        {
            packetBuffer.writeIdentifier(child);
        }
    }

    @NotNull
    @Override
    public IGlobalResearch deserialize(final @NotNull IFactoryController controller, final @NotNull RegistryFriendlyByteBuf buffer) throws Throwable
    {
        final Identifier id = buffer.readIdentifier();
        final Identifier parent = buffer.readBoolean() ? buffer.readIdentifier() : null;
        final Identifier branch = buffer.readIdentifier();
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

        Utils.deserializeCodecMess(SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), buffer).forEach(research::addCost);

        final int reqCount = buffer.readVarInt();
        for(int i = 0; i < reqCount; i++)
        {
            final ModResearchRequirements.ResearchRequirementEntry researchRequirementEntry = buffer.readById(IMinecoloniesAPI.getInstance().getResearchRequirementRegistry()::byIdOrThrow);
            research.addRequirement(researchRequirementEntry.readFromNBT(buffer.readNbt()));
        }

        final int effectCount = buffer.readVarInt();
        for(int i = 0; i < effectCount; i++)
        {
            final ModResearchEffects.ResearchEffectEntry researchEffectEntry = buffer.readById(IMinecoloniesAPI.getInstance().getResearchEffectRegistry()::byIdOrThrow);
            research.addEffect(researchEffectEntry.readFromNBT(buffer.readNbt()));
        }

        final int childCount = buffer.readVarInt();
        for(int i = 0; i < childCount; i++)
        {
            research.addChild(buffer.readIdentifier());
        }
        return research;
    }

    @Override
    public short getSerializationId()
    {
        return SerializationIdentifierConstants.GLOBAL_RESEARCH_ID;
    }
}
