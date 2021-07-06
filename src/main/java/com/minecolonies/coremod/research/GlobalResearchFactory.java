package com.minecolonies.coremod.research;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.research.IGlobalResearch;
import com.minecolonies.api.research.IResearchRequirement;
import com.minecolonies.api.research.effects.IResearchEffect;
import com.minecolonies.api.research.effects.registry.IResearchEffectRegistry;
import com.minecolonies.api.research.factories.IGlobalResearchFactory;
import com.minecolonies.api.research.registry.IResearchRequirementRegistry;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;
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
    public IGlobalResearch getNewInstance(final ResourceLocation id, final ResourceLocation branch, final ResourceLocation parent, final TranslationTextComponent desc, final int universityLevel, final int sortOrder,
      final ResourceLocation iconTexture, final ItemStack iconStack, final TranslationTextComponent subtitle, final boolean onlyChild, final boolean hidden, final boolean autostart, final boolean instant, final boolean immutable)
    {
        return new GlobalResearch(id, branch, parent, desc, universityLevel, sortOrder, iconTexture, iconStack, subtitle, onlyChild, hidden, autostart, instant, immutable);
    }

    @NotNull
    @Override
    public CompoundNBT serialize(@NotNull final IFactoryController controller, @NotNull final IGlobalResearch research)
    {
        final CompoundNBT compound = new CompoundNBT();
        compound.putString(TAG_PARENT, research.getParent().toString());
        compound.putString(TAG_ID, research.getId().toString());
        compound.putString(TAG_BRANCH, research.getBranch().toString());
        compound.putString(TAG_NAME, research.getName().getKey());
        compound.putInt(TAG_RESEARCH_LVL, research.getDepth());
        compound.putInt(TAG_RESEARCH_SORT, research.getSortOrder());
        compound.putBoolean(TAG_ONLY_CHILD, research.hasOnlyChild());
        compound.putString(TAG_ICON_TEXTURE, research.getIconTextureResourceLocation().toString());
        compound.putString(TAG_ICON_ITEM_STACK, research.getIconItemStack().getItem().getRegistryName() + ":" + research.getIconItemStack().getCount());
        compound.putString(TAG_SUBTITLE_NAME, research.getSubtitle().getKey());
        compound.putBoolean(TAG_INSTANT, research.isInstant());
        compound.putBoolean(TAG_AUTOSTART, research.isAutostart());
        compound.putBoolean(TAG_IMMUTABLE, research.isImmutable());
        compound.putBoolean(TAG_HIDDEN, research.isHidden());
        @NotNull final ListNBT costTagList = research.getCostList().stream().map(is ->
        {
            final CompoundNBT costCompound = new CompoundNBT();
            costCompound.putString(TAG_COST_ITEM, Objects.requireNonNull(is.getItem().getRegistryName()).toString() + ":" + is.getItemStack().getCount());
            if(is.getItemStack().getTag() != null)
            {
                costCompound.put(TAG_COST_NBT, is.getItemStack().getTag());
            }
            return costCompound;
        }).collect(NBTUtils.toListNBT());
        compound.put(TAG_COSTS, costTagList);

        @NotNull final ListNBT reqTagList = research.getResearchRequirement().stream().map(req ->
        {
            final CompoundNBT reqCompound = new CompoundNBT();
            reqCompound.putString(TAG_REQ_TYPE, req.getRegistryEntry().getRegistryName().toString());
            reqCompound.put(TAG_REQ_ITEM, req.writeToNBT());
            return reqCompound;
        }).collect(NBTUtils.toListNBT());
        compound.put(TAG_REQS, reqTagList);

        @NotNull final ListNBT effectTagList = research.getEffects().stream().map(eff ->
        {
            final CompoundNBT effectCompound = new CompoundNBT();
            effectCompound.putString(TAG_EFFECT_TYPE, eff.getRegistryEntry().getRegistryName().toString());
            effectCompound.put(TAG_EFFECT_ITEM, eff.writeToNBT());
            return effectCompound;
        }).collect(NBTUtils.toListNBT());
        compound.put(TAG_EFFECTS, effectTagList);

        @NotNull final ListNBT childTagList = research.getChildren().stream().map(child ->
        {
            final CompoundNBT childCompound = new CompoundNBT();
            childCompound.putString(TAG_RESEARCH_CHILD, child.toString());
            return childCompound;
        }).collect(NBTUtils.toListNBT());
        compound.put(TAG_CHILDS, childTagList);

        return compound;
    }

    @NotNull
    @Override
    public IGlobalResearch deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt)
    {
        final ResourceLocation parent = new ResourceLocation(nbt.getString(TAG_PARENT));
        final ResourceLocation id = new ResourceLocation(nbt.getString(TAG_ID));
        final ResourceLocation branch = new ResourceLocation(nbt.getString(TAG_BRANCH));
        final TranslationTextComponent desc = new TranslationTextComponent(nbt.getString(TAG_NAME));
        final int depth = nbt.getInt(TAG_RESEARCH_LVL);
        final int sortOrder =  nbt.getInt(TAG_RESEARCH_SORT);
        final boolean onlyChild = nbt.getBoolean(TAG_ONLY_CHILD);
        final ResourceLocation iconTexture = new ResourceLocation(nbt.getString(TAG_ICON_TEXTURE));
        final String[] iconStackParts =  nbt.getString(TAG_ICON_ITEM_STACK).split(":");
        final ItemStack iconStack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(iconStackParts[0], iconStackParts[1])));
        iconStack.setCount(Integer.parseInt(iconStackParts[2]));
        final TranslationTextComponent subtitle = new TranslationTextComponent(nbt.getString(TAG_SUBTITLE_NAME));
        final boolean instant = nbt.getBoolean(TAG_INSTANT);
        final boolean autostart = nbt.getBoolean(TAG_AUTOSTART);
        final boolean immutable = nbt.getBoolean(TAG_IMMUTABLE);
        final boolean hidden = nbt.getBoolean(TAG_HIDDEN);

        final IGlobalResearch research = getNewInstance(id, branch, parent, desc, depth, sortOrder, iconTexture, iconStack, subtitle, onlyChild, hidden, autostart, instant, immutable);

        NBTUtils.streamCompound(nbt.getList(TAG_COSTS, Constants.NBT.TAG_COMPOUND)).forEach(compound ->
        {
            String[] costParts = compound.getString(TAG_COST_ITEM).split(":");
            if(costParts.length == 3)
            {
                final ItemStack is = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(costParts[0], costParts[1])));
                is.setCount(Integer.parseInt(costParts[2]));
                if (compound.hasUUID(TAG_COST_NBT))
                {
                    is.setTag(compound.getCompound(TAG_COST_NBT));
                }
                research.addCost(new ItemStorage(is, false, !is.hasTag()));
            }
        });
        NBTUtils.streamCompound(nbt.getList(TAG_REQS, Constants.NBT.TAG_COMPOUND)).
             forEach(compound ->
                     research.addRequirement(Objects.requireNonNull(IResearchRequirementRegistry.getInstance()
                                                                      .getValue(ResourceLocation.tryParse(compound.getString(TAG_REQ_TYPE)))).readFromNBT(compound.getCompound(TAG_REQ_ITEM))));

        NBTUtils.streamCompound(nbt.getList(TAG_EFFECTS, Constants.NBT.TAG_COMPOUND)).forEach(compound ->
            research.addEffect(Objects.requireNonNull(IResearchEffectRegistry.getInstance()
                                                        .getValue(ResourceLocation.tryParse(compound.getString(TAG_EFFECT_TYPE)))).readFromNBT(compound.getCompound(TAG_EFFECT_ITEM))));

        NBTUtils.streamCompound(nbt.getList(TAG_CHILDS, Constants.NBT.TAG_COMPOUND)).forEach(compound -> research.addChild(new ResourceLocation(compound.getString(TAG_RESEARCH_CHILD))));
        return research;
    }

    @Override
    public void serialize(@NotNull IFactoryController controller, IGlobalResearch input, PacketBuffer packetBuffer)
    {
        packetBuffer.writeResourceLocation(input.getParent());
        packetBuffer.writeResourceLocation(input.getId());
        packetBuffer.writeResourceLocation(input.getBranch());
        packetBuffer.writeUtf(input.getName().getKey());
        packetBuffer.writeVarInt(input.getDepth());
        packetBuffer.writeVarInt(input.getSortOrder());
        packetBuffer.writeBoolean(input.hasOnlyChild());
        packetBuffer.writeItem(input.getIconItemStack());
        packetBuffer.writeResourceLocation(input.getIconTextureResourceLocation());
        packetBuffer.writeUtf(input.getSubtitle().getKey());
        packetBuffer.writeBoolean(input.isInstant());
        packetBuffer.writeBoolean(input.isAutostart());
        packetBuffer.writeBoolean(input.isImmutable());
        packetBuffer.writeBoolean(input.isHidden());
        packetBuffer.writeVarInt(input.getCostList().size());
        for(ItemStorage is : input.getCostList())
        {
            controller.serialize(packetBuffer, is);
        }
        packetBuffer.writeVarInt(input.getResearchRequirement().size());
        for(IResearchRequirement req : input.getResearchRequirement())
        {
            packetBuffer.writeResourceLocation(req.getRegistryEntry().getRegistryName());
            packetBuffer.writeNbt(req.writeToNBT());
        }
        packetBuffer.writeVarInt(input.getEffects().size());
        for(IResearchEffect<?> effect : input.getEffects())
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
    public IGlobalResearch deserialize(@NotNull IFactoryController controller, PacketBuffer buffer) throws Throwable
    {
        final ResourceLocation parent = buffer.readResourceLocation();
        final ResourceLocation id = buffer.readResourceLocation();
        final ResourceLocation branch = buffer.readResourceLocation();
        final TranslationTextComponent desc = new TranslationTextComponent(buffer.readUtf());
        final int depth = buffer.readVarInt();
        final int sortOrder = buffer.readVarInt();
        final boolean hasOnlyChild = buffer.readBoolean();
        final ItemStack iconStack = buffer.readItem();
        final ResourceLocation iconTexture = buffer.readResourceLocation();
        final TranslationTextComponent subtitle = new TranslationTextComponent(buffer.readUtf());
        final boolean instant = buffer.readBoolean();
        final boolean autostart = buffer.readBoolean();
        final boolean immutable = buffer.readBoolean();
        final boolean hidden = buffer.readBoolean();

        final IGlobalResearch research = getNewInstance(id, branch, parent, desc, depth, sortOrder,iconTexture, iconStack, subtitle, hasOnlyChild, hidden, autostart, instant, immutable);

        final int costSize = buffer.readVarInt();
        for(int i = 0; i < costSize; i++)
        {
            research.addCost(controller.deserialize(buffer));
        }

        final int reqCount = buffer.readVarInt();
        for(int i = 0; i < reqCount; i++)
        {
            final ResourceLocation reqId = buffer.readResourceLocation();
            research.addRequirement(Objects.requireNonNull(IResearchRequirementRegistry.getInstance().getValue(reqId)).readFromNBT(buffer.readNbt()));
        }

        final int effectCount = buffer.readVarInt();
        for(int i = 0; i < effectCount; i++)
        {
            final ResourceLocation effectId = buffer.readResourceLocation();
            research.addEffect(Objects.requireNonNull(IResearchEffectRegistry.getInstance().getValue(effectId)).readFromNBT(buffer.readNbt()));
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
        return 28;
    }
}
