package com.minecolonies.core.research;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.research.IGlobalResearch;
import com.minecolonies.api.research.IResearchRequirement;
import com.minecolonies.api.research.effects.IResearchEffect;
import com.minecolonies.api.research.effects.registry.IResearchEffectRegistry;
import com.minecolonies.api.research.factories.IGlobalResearchFactory;
import com.minecolonies.api.research.registry.IResearchRequirementRegistry;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.Utils;
import com.minecolonies.api.util.constant.SerializationIdentifierConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.List;
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
    public IGlobalResearch getNewInstance(final ResourceLocation id, final ResourceLocation branch, final ResourceLocation parent, final TranslatableContents desc, final int universityLevel, final int sortOrder,
      final ResourceLocation iconTexture, final ItemStack iconStack, final TranslatableContents subtitle, final boolean onlyChild, final boolean hidden, final boolean autostart, final boolean instant, final boolean immutable)
    {
        return new GlobalResearch(id, branch, parent, desc, universityLevel, sortOrder, iconTexture, iconStack, subtitle, onlyChild, hidden, autostart, instant, immutable);
    }

    @NotNull
    @Override
    public CompoundTag serialize(@NotNull final HolderLookup.Provider provider, @NotNull final IFactoryController controller, @NotNull final IGlobalResearch research)
    {
        final CompoundTag compound = new CompoundTag();
        compound.putString(TAG_PARENT, research.getParent().toString());
        compound.putString(TAG_ID, research.getId().toString());
        compound.putString(TAG_BRANCH, research.getBranch().toString());
        compound.putString(TAG_NAME, research.getName().getKey());
        compound.putInt(TAG_RESEARCH_LVL, research.getDepth());
        compound.putInt(TAG_RESEARCH_SORT, research.getSortOrder());
        compound.putBoolean(TAG_ONLY_CHILD, research.hasOnlyChild());
        compound.putString(TAG_ICON_TEXTURE, research.getIconTextureResourceLocation().toString());
        compound.putString(TAG_ICON_ITEM_STACK, BuiltInRegistries.ITEM.getKey(research.getIconItemStack().getItem()) + ":" + research.getIconItemStack().getCount());
        compound.putString(TAG_SUBTITLE_NAME, research.getSubtitle().getKey());
        compound.putBoolean(TAG_INSTANT, research.isInstant());
        compound.putBoolean(TAG_AUTOSTART, research.isAutostart());
        compound.putBoolean(TAG_IMMUTABLE, research.isImmutable());
        compound.putBoolean(TAG_HIDDEN, research.isHidden());
        compound.put(TAG_COSTS, Utils.serializeCodecMess(SizedIngredient.FLAT_CODEC.listOf(), provider, research.getCostList()));

        @NotNull final ListTag reqTagList = research.getResearchRequirement().stream().map(req ->
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
    public IGlobalResearch deserialize(@NotNull final HolderLookup.Provider provider, @NotNull final IFactoryController controller, @NotNull final CompoundTag nbt)
    {
        final ResourceLocation parent = ResourceLocation.parse(nbt.getString(TAG_PARENT));
        final ResourceLocation id = ResourceLocation.parse(nbt.getString(TAG_ID));
        final ResourceLocation branch = ResourceLocation.parse(nbt.getString(TAG_BRANCH));
        final TranslatableContents desc = new TranslatableContents(nbt.getString(TAG_NAME), null, TranslatableContents.NO_ARGS);
        final int depth = nbt.getInt(TAG_RESEARCH_LVL);
        final int sortOrder =  nbt.getInt(TAG_RESEARCH_SORT);
        final boolean onlyChild = nbt.getBoolean(TAG_ONLY_CHILD);
        final ResourceLocation iconTexture = ResourceLocation.parse(nbt.getString(TAG_ICON_TEXTURE));
        final String[] iconStackParts =  nbt.getString(TAG_ICON_ITEM_STACK).split(":");
        final ItemStack iconStack = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(iconStackParts[0], iconStackParts[1])));
        iconStack.setCount(Integer.parseInt(iconStackParts[2]));
        final TranslatableContents subtitle = new TranslatableContents(nbt.getString(TAG_SUBTITLE_NAME), null, TranslatableContents.NO_ARGS);
        final boolean instant = nbt.getBoolean(TAG_INSTANT);
        final boolean autostart = nbt.getBoolean(TAG_AUTOSTART);
        final boolean immutable = nbt.getBoolean(TAG_IMMUTABLE);
        final boolean hidden = nbt.getBoolean(TAG_HIDDEN);

        final IGlobalResearch research = getNewInstance(id, branch, parent, desc, depth, sortOrder, iconTexture, iconStack, subtitle, onlyChild, hidden, autostart, instant, immutable);

        final List<SizedIngredient> costs = Utils.deserializeCodecMess(SizedIngredient.FLAT_CODEC.listOf(), provider, nbt.get(TAG_COSTS));
        research.addCosts(costs);
        NBTUtils.streamCompound(nbt.getList(TAG_REQS, Tag.TAG_COMPOUND)).
             forEach(compound ->
                     research.addRequirement(Objects.requireNonNull(IResearchRequirementRegistry.getInstance()
                                                                      .get(ResourceLocation.tryParse(compound.getString(TAG_REQ_TYPE)))).readFromNBT(compound.getCompound(TAG_REQ_ITEM))));

        NBTUtils.streamCompound(nbt.getList(TAG_EFFECTS, Tag.TAG_COMPOUND)).forEach(compound ->
            research.addEffect(Objects.requireNonNull(IResearchEffectRegistry.getInstance()
                                                        .get(ResourceLocation.tryParse(compound.getString(TAG_EFFECT_TYPE)))).readFromNBT(compound.getCompound(TAG_EFFECT_ITEM))));

        NBTUtils.streamCompound(nbt.getList(TAG_CHILDS, Tag.TAG_COMPOUND)).forEach(compound -> research.addChild(ResourceLocation.parse(compound.getString(TAG_RESEARCH_CHILD))));
        return research;
    }

    @Override
    public void serialize(@NotNull IFactoryController controller, IGlobalResearch input, RegistryFriendlyByteBuf packetBuffer)
    {
        packetBuffer.writeResourceLocation(input.getParent());
        packetBuffer.writeResourceLocation(input.getId());
        packetBuffer.writeResourceLocation(input.getBranch());
        packetBuffer.writeUtf(input.getName().getKey());
        packetBuffer.writeVarInt(input.getDepth());
        packetBuffer.writeVarInt(input.getSortOrder());
        packetBuffer.writeBoolean(input.hasOnlyChild());
        Utils.serializeCodecMess(packetBuffer, input.getIconItemStack());
        packetBuffer.writeResourceLocation(input.getIconTextureResourceLocation());
        packetBuffer.writeUtf(input.getSubtitle().getKey());
        packetBuffer.writeBoolean(input.isInstant());
        packetBuffer.writeBoolean(input.isAutostart());
        packetBuffer.writeBoolean(input.isImmutable());
        packetBuffer.writeBoolean(input.isHidden());
        Utils.serializeCodecMess(SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), packetBuffer, input.getCostList());
        packetBuffer.writeVarInt(input.getResearchRequirement().size());
        for (IResearchRequirement req : input.getResearchRequirement())
        {
            packetBuffer.writeResourceLocation(req.getRegistryEntry().getRegistryName());
            packetBuffer.writeNbt(req.writeToNBT());
        }
        packetBuffer.writeVarInt(input.getEffects().size());
        for (IResearchEffect<?> effect : input.getEffects())
        {
            packetBuffer.writeResourceLocation(effect.getRegistryEntry().getRegistryName());
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
    public IGlobalResearch deserialize(@NotNull IFactoryController controller, RegistryFriendlyByteBuf buffer) throws Throwable
    {
        final ResourceLocation parent = buffer.readResourceLocation();
        final ResourceLocation id = buffer.readResourceLocation();
        final ResourceLocation branch = buffer.readResourceLocation();
        final TranslatableContents desc = new TranslatableContents(buffer.readUtf(), null, TranslatableContents.NO_ARGS);
        final int depth = buffer.readVarInt();
        final int sortOrder = buffer.readVarInt();
        final boolean hasOnlyChild = buffer.readBoolean();
        final ItemStack iconStack = Utils.deserializeCodecMess(buffer);
        final ResourceLocation iconTexture = buffer.readResourceLocation();
        final TranslatableContents subtitle = new TranslatableContents(buffer.readUtf(), null, TranslatableContents.NO_ARGS);
        final boolean instant = buffer.readBoolean();
        final boolean autostart = buffer.readBoolean();
        final boolean immutable = buffer.readBoolean();
        final boolean hidden = buffer.readBoolean();

        final IGlobalResearch research = getNewInstance(id, branch, parent, desc, depth, sortOrder,iconTexture, iconStack, subtitle, hasOnlyChild, hidden, autostart, instant, immutable);

        final List<SizedIngredient> costs = Utils.deserializeCodecMess(SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), buffer);
        research.addCosts(costs);

        final int reqCount = buffer.readVarInt();
        for(int i = 0; i < reqCount; i++)
        {
            final ResourceLocation reqId = buffer.readResourceLocation();
            research.addRequirement(Objects.requireNonNull(IResearchRequirementRegistry.getInstance().get(reqId)).readFromNBT(buffer.readNbt()));
        }

        final int effectCount = buffer.readVarInt();
        for(int i = 0; i < effectCount; i++)
        {
            final ResourceLocation effectId = buffer.readResourceLocation();
            research.addEffect(Objects.requireNonNull(IResearchEffectRegistry.getInstance().get(effectId)).readFromNBT(buffer.readNbt()));
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
