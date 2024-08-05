package com.minecolonies.core.research;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.research.ILocalResearch;
import com.minecolonies.api.research.factories.ILocalResearchFactory;
import com.minecolonies.api.research.util.ResearchState;
import com.minecolonies.api.util.constant.SerializationIdentifierConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.research.util.ResearchConstants.*;

/**
 * Factory implementation taking care of creating new instances, serializing and deserializing LocalResearch.
 */
public class LocalResearchFactory implements ILocalResearchFactory
{
    @NotNull
    @Override
    public TypeToken<LocalResearch> getFactoryOutputType()
    {
        return TypeToken.of(LocalResearch.class);
    }

    @NotNull
    @Override
    public TypeToken<FactoryVoidInput> getFactoryInputType()
    {
        return TypeConstants.FACTORYVOIDINPUT;
    }

    @NotNull
    @Override
    public ILocalResearch getNewInstance(final ResourceLocation id, final ResourceLocation branch, final int depth)
    {
        return new LocalResearch(id, branch, depth);
    }

    @NotNull
    @Override
    public CompoundTag serialize(@NotNull final HolderLookup.Provider provider, @NotNull final IFactoryController controller, @NotNull final ILocalResearch research)
    {
        final CompoundTag compound = new CompoundTag();
        compound.putInt(TAG_STATE, research.getState().ordinal());
        compound.putString(TAG_ID, research.getId().toString());
        compound.putString(TAG_BRANCH, research.getBranch().toString());
        compound.putInt(TAG_PROGRESS, research.getProgress());
        compound.putInt(TAG_RESEARCH_LVL, research.getDepth());

        return compound;
    }

    @NotNull
    @Override
    public ILocalResearch deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundTag nbt)
    {
        final int state = nbt.getInt(TAG_STATE);
        final ResourceLocation id = new ResourceLocation(nbt.getString(TAG_ID));
        final ResourceLocation branch = new ResourceLocation(nbt.getString(TAG_BRANCH));
        final int depth = nbt.getInt(TAG_RESEARCH_LVL);
        final int progress = nbt.getInt(TAG_PROGRESS);

        final ILocalResearch research = getNewInstance(id, branch, depth);
        research.setState(ResearchState.values()[state]);
        research.setProgress(progress);
        return research;
    }

    @Override
    public void serialize(IFactoryController controller, ILocalResearch input, FriendlyByteBuf packetBuffer)
    {
        packetBuffer.writeInt(input.getState().ordinal());
        packetBuffer.writeUtf(input.getId().toString());
        packetBuffer.writeResourceLocation(input.getBranch());
        packetBuffer.writeInt(input.getProgress());
        packetBuffer.writeInt(input.getDepth());
    }

    @Override
    public ILocalResearch deserialize(IFactoryController controller, FriendlyByteBuf buffer) throws Throwable
    {
        final int state = buffer.readInt();
        final ResourceLocation id = buffer.readResourceLocation();
        final ResourceLocation branch = buffer.readResourceLocation();
        final int progress = buffer.readInt();
        final int depth = buffer.readInt();

        final ILocalResearch research = getNewInstance(id, branch, depth);
        research.setState(ResearchState.values()[state]);
        research.setProgress(progress);
        return research;
    }

    @Override
    public short getSerializationId()
    {
        return SerializationIdentifierConstants.LOCAL_RESEARCH_ID;
    }
}
