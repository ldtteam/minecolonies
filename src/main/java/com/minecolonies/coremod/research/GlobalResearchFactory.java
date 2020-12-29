package com.minecolonies.coremod.research;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.research.IGlobalResearch;
import com.minecolonies.api.research.IResearchRequirement;
import com.minecolonies.api.research.effects.IResearchEffect;
import com.minecolonies.api.research.factories.IGlobalResearchFactory;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
    public IGlobalResearch getNewInstance(final String id, final String resourcePath, final String branch, final String parent, final String desc, final int universityLevel,
      final String icon, final String subtitle, final boolean onlyChild, final boolean hidden, final boolean autostart, final boolean instant, final boolean immutable)
    {
        return new GlobalResearch(id, resourcePath, branch, parent, desc, universityLevel, icon, subtitle, onlyChild, hidden, autostart, instant, immutable);
    }

    @NotNull
    @Override
    public CompoundNBT serialize(@NotNull final IFactoryController controller, @NotNull final IGlobalResearch research)
    {
        final CompoundNBT compound = new CompoundNBT();
        compound.putString(TAG_PARENT, research.getParent());
        compound.putString(TAG_ID, research.getId());
        compound.putString(TAG_RESOURCE_PATH, research.getResourceLocation().getPath());
        compound.putString(TAG_BRANCH, research.getBranch());
        compound.putString(TAG_DESC, research.getDesc());
        compound.putInt(TAG_RESEARCH_LVL, research.getDepth());
        compound.putBoolean(TAG_ONLY_CHILD, research.hasOnlyChild());
        compound.putString(TAG_ICON, research.getIcon());
        compound.putString(TAG_SUBTITLE_NAME, research.getSubtitle());
        compound.putBoolean(TAG_INSTANT, research.isInstant());
        compound.putBoolean(TAG_AUTOSTART, research.isAutostart());
        compound.putBoolean(TAG_IMMUTABLE, research.isImmutable());
        compound.putBoolean(TAG_HIDDEN, research.isHidden());
        @NotNull final ListNBT costTagList = research.getCostList().stream().map(is ->
        {
            final CompoundNBT costCompound = new CompoundNBT();
            costCompound.putString(TAG_COST_ITEM, Objects.requireNonNull(is.getItem().getRegistryName()).toString() + ":" + is.getItemStack().getCount());
            return costCompound;
        }).collect(NBTUtils.toListNBT());
        compound.put(TAG_COSTS, costTagList);

        @NotNull final ListNBT reqTagList = research.getResearchRequirement().stream().map(req ->
        {
            final CompoundNBT reqCompound = new CompoundNBT();
            reqCompound.putString(TAG_REQ_ITEM, req.getAttributes());
            return reqCompound;
        }).collect(NBTUtils.toListNBT());
        compound.put(TAG_REQS, reqTagList);

        @NotNull final ListNBT effectTagList = research.getEffects().stream().map(eff ->
        {
            final CompoundNBT effectCompound = new CompoundNBT();
            effectCompound.putString(TAG_EFFECT_ITEM, eff.getAttributes());
            return effectCompound;
        }).collect(NBTUtils.toListNBT());
        compound.put(TAG_EFFECTS, effectTagList);

        @NotNull final ListNBT childTagList = research.getChildren().stream().map(child ->
        {
            final CompoundNBT childCompound = new CompoundNBT();
            childCompound.putString(TAG_RESEARCH_CHILD, child);
            return childCompound;
        }).collect(NBTUtils.toListNBT());
        compound.put(TAG_CHILDS, childTagList);

        return compound;
    }

    @NotNull
    @Override
    public IGlobalResearch deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt)
    {
        final String parent = nbt.getString(TAG_PARENT);
        final String id = nbt.getString(TAG_ID);
        final String resourcePath = nbt.getString(TAG_RESOURCE_PATH);
        final String branch = nbt.getString(TAG_BRANCH);
        final String desc = nbt.getString(TAG_DESC);
        final int depth = nbt.getInt(TAG_RESEARCH_LVL);
        final boolean onlyChild = nbt.getBoolean(TAG_ONLY_CHILD);
        final String icon = nbt.getString(TAG_ICON);
        final String subtitle = nbt.getString(TAG_SUBTITLE_NAME);
        final boolean instant = nbt.getBoolean(TAG_INSTANT);
        final boolean autostart = nbt.getBoolean(TAG_AUTOSTART);
        final boolean immutable = nbt.getBoolean(TAG_IMMUTABLE);
        final boolean hidden = nbt.getBoolean(TAG_HIDDEN);

        final IGlobalResearch research = getNewInstance(id, resourcePath, branch, parent, desc, depth, icon, subtitle, onlyChild, hidden, autostart, instant, immutable);

        NBTUtils.streamCompound(nbt.getList(TAG_COSTS, Constants.NBT.TAG_COMPOUND)).forEach(comp -> research.addCost(comp.getString(TAG_COST_ITEM)));
        NBTUtils.streamCompound(nbt.getList(TAG_EFFECTS, Constants.NBT.TAG_COMPOUND)).forEach(compound -> research.addEffect(compound.getString(TAG_EFFECT_ITEM)));
        NBTUtils.streamCompound(nbt.getList(TAG_REQS, Constants.NBT.TAG_COMPOUND)).forEach(compound -> research.addRequirement(compound.getString(TAG_REQ_ITEM)));
        NBTUtils.streamCompound(nbt.getList(TAG_CHILDS, Constants.NBT.TAG_COMPOUND)).forEach(compound -> research.addChild(compound.getString(TAG_RESEARCH_CHILD)));
        return research;
    }

    @Override
    public void serialize(@NotNull IFactoryController controller, IGlobalResearch input, PacketBuffer packetBuffer)
    {
        packetBuffer.writeString(input.getParent());
        packetBuffer.writeString(input.getId());
        packetBuffer.writeString(input.getResourceLocation().getPath());
        packetBuffer.writeString(input.getBranch());
        packetBuffer.writeString(input.getDesc());
        packetBuffer.writeInt(input.getDepth());
        packetBuffer.writeBoolean(input.hasOnlyChild());
        packetBuffer.writeString(input.getIcon());
        packetBuffer.writeString(input.getSubtitle());
        packetBuffer.writeBoolean(input.isInstant());
        packetBuffer.writeBoolean(input.isAutostart());
        packetBuffer.writeBoolean(input.isImmutable());
        packetBuffer.writeBoolean(input.isHidden());
        packetBuffer.writeInt(input.getCostList().size());
        for(ItemStorage is : input.getCostList())
        {
            final String itemString = Objects.requireNonNull(is.getItem().getRegistryName()).toString() + ":" + is.getItemStack().getCount();
            packetBuffer.writeString(itemString);
        }
        packetBuffer.writeInt(input.getResearchRequirement().size());
        for(IResearchRequirement req : input.getResearchRequirement())
        {
            packetBuffer.writeString(req.getAttributes());
        }
        packetBuffer.writeInt(input.getEffects().size());
        for(IResearchEffect<?> effect : input.getEffects())
        {
            packetBuffer.writeString(effect.getAttributes());
        }
        packetBuffer.writeInt(input.getChildren().size());
        for (String child : input.getChildren())
        {
            packetBuffer.writeString(child);
        }
    }

    @NotNull
    @Override
    public IGlobalResearch deserialize(@NotNull IFactoryController controller, PacketBuffer buffer) throws Throwable
    {
        final String parent = buffer.readString(32767);
        final String id = buffer.readString(32767);
        final String resourcePath = buffer.readString(32767);
        final String branch = buffer.readString(32767);
        final String desc = buffer.readString(32767);
        final int depth = buffer.readInt();
        final boolean hasOnlyChild = buffer.readBoolean();
        final String icon = buffer.readString(32767);
        final String subtitle = buffer.readString(32767);
        final boolean instant = buffer.readBoolean();
        final boolean autostart = buffer.readBoolean();
        final boolean immutable = buffer.readBoolean();
        final boolean hidden = buffer.readBoolean();

        final List<ItemStorage> costs = new ArrayList<>();
        final int costSize = buffer.readInt();
        for(int i = 0; i < costSize; i++)
        {
            final String[] costParts = buffer.readString(32767).split(":");
            final ItemStack is = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(costParts[0], costParts[1])));
            is.setCount(Integer.parseInt(costParts[2]));
            costs.add(new ItemStorage(is));
        }

        final int reqCount = buffer.readInt();
        final List<IResearchRequirement> reqs = new ArrayList<>();
        for(int i = 0; i < reqCount; i++)
        {
            String[] reqParts = buffer.readString(32767).split(":");
            switch(reqParts[0])
            {
                case ResearchResearchRequirement.type:
                    reqs.add(new ResearchResearchRequirement(reqParts));
                    break;
                case BuildingResearchRequirement.type:
                    reqs.add(new BuildingResearchRequirement(reqParts));
                    break;
                case AlternateBuildingResearchRequirement.type:
                    reqs.add(new AlternateBuildingResearchRequirement(reqParts));
                    break;
            }
        }

        final List<IResearchEffect<?>> effects = new ArrayList<>();
        final int effectCount = buffer.readInt();
        for(int i = 0; i < effectCount; i++)
        {
            effects.add(controller.deserialize(buffer));
        }

        final IGlobalResearch research = getNewInstance(id, resourcePath, branch, parent, desc, depth, icon, subtitle, hasOnlyChild, hidden, autostart, instant, immutable);
        research.setCosts(costs);
        research.setRequirement(reqs);
        research.setEffects(effects);

        final int childCount = buffer.readInt();
        for(int i = 0; i < childCount; i++)
        {
            research.addChild(buffer.readString(32767));
        }
        return research;
    }

    @Override
    public short getSerializationId()
    {
        return 28;
    }
}
