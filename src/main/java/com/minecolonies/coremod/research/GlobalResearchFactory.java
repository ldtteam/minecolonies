package com.minecolonies.coremod.research;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.research.IGlobalResearch;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.research.effects.IResearchEffect;
import com.minecolonies.api.research.factories.IGlobalResearchFactory;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.research.util.ResearchConstants.*;

/**
 * Factory implementation taking care of creating new instances, serializing and deserializing GloblaResearch.
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
    public IGlobalResearch getNewInstance(final String id, final String parent, final String branch, @NotNull final String desc, final int depth, final IResearchEffect<?> effect)
    {
        return new GlobalResearch(id, branch, desc, depth, effect);
    }

    @NotNull
    @Override
    public CompoundNBT serialize(@NotNull final IFactoryController controller, @NotNull final IGlobalResearch effect)
    {
        final CompoundNBT compound = new CompoundNBT();
        compound.putString(TAG_PARENT, effect.getParent());
        compound.putString(TAG_ID, effect.getId());
        compound.putString(TAG_BRANCH, effect.getBranch());
        compound.putString(TAG_DESC, effect.getDesc());
        compound.put(TAG_EFFECT, StandardFactoryController.getInstance().serialize(effect));
        compound.putInt(TAG_DEPTH, effect.getDepth());
        compound.putBoolean(TAG_ONLY_CHILD, effect.hasOnlyChild());

        @NotNull final ListNBT childTagList = effect.getChilds().stream().map(child ->
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
        final String branch = nbt.getString(TAG_BRANCH);
        final String desc = nbt.getString(TAG_DESC);
        final CompoundNBT effect = nbt.getCompound(TAG_EFFECT);
        final int depth = nbt.getInt(TAG_DEPTH);
        final boolean onlyChild = nbt.getBoolean(TAG_ONLY_CHILD);

        final IGlobalResearch research = getNewInstance(id, parent, branch, desc, depth, StandardFactoryController.getInstance().deserialize(effect));
        research.loadCostFromConfig();
        research.setOnlyChild(onlyChild);

        NBTUtils.streamCompound(nbt.getList(TAG_CHILDS, Constants.NBT.TAG_COMPOUND)).forEach(compound -> IGlobalResearchTree.getInstance().getResearch(branch, compound.getString(
          TAG_RESEARCH_CHILD)));
        return research;
    }

    @Override
    public void serialize(IFactoryController controller, IGlobalResearch input, PacketBuffer packetBuffer)
    {
        packetBuffer.writeString(input.getParent());
        packetBuffer.writeString(input.getId());
        packetBuffer.writeString(input.getBranch());
        packetBuffer.writeString(input.getDesc());
        controller.serialize(packetBuffer, input);
        packetBuffer.writeInt(input.getDepth());
        packetBuffer.writeBoolean(input.hasOnlyChild());
        packetBuffer.writeInt(input.getChilds().size());
        input.getChilds().forEach(child -> packetBuffer.writeString(child));
    }

    @Override
    public IGlobalResearch deserialize(IFactoryController controller, PacketBuffer buffer) throws Throwable
    {
        final String parent = buffer.readString();
        final String id = buffer.readString();
        final String branch = buffer.readString();
        final String desc = buffer.readString();
        // This is a IGlobalResearch before serialization
        final IResearchEffect<?> effect = controller.deserialize(buffer);
        final int depth = buffer.readInt();
        final boolean onlyChild = buffer.readBoolean();

        final IGlobalResearch research = getNewInstance(id, parent, branch, desc, depth, effect);
        research.loadCostFromConfig();
        research.setOnlyChild(onlyChild);

        final int childsSize = buffer.readInt();
        for (int i = 0; i < childsSize; ++i)
        {
            IGlobalResearchTree.getInstance().getResearch(branch, buffer.readString());
        }

        return research;
    }
}
