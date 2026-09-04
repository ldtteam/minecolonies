package com.minecolonies.core.research;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.research.ILocalResearch;
import com.minecolonies.api.research.factories.ILocalResearchFactory;
import com.minecolonies.api.research.util.ResearchState;
import com.minecolonies.api.util.constant.SerializationIdentifierConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
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
    public ILocalResearch getNewInstance(final Identifier id, final Identifier branch, final int depth)
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
    public ILocalResearch deserialize(@NotNull final HolderLookup.Provider provider, @NotNull final IFactoryController controller, @NotNull final CompoundTag nbt)
    {
        final int state = nbt.getIntOr(TAG_STATE, 0);
        final Identifier id = Identifier.parse(nbt.getStringOr(TAG_ID, ""));
        final Identifier branch = Identifier.parse(nbt.getStringOr(TAG_BRANCH, ""));
        final int depth = nbt.getIntOr(TAG_RESEARCH_LVL, 0);
        final int progress = nbt.getIntOr(TAG_PROGRESS, 0);

        final ILocalResearch research = getNewInstance(id, branch, depth);
        research.setState(ResearchState.values()[state]);
        research.setProgress(progress);
        return research;
    }

    @Override
    public void serialize(IFactoryController controller, ILocalResearch input, RegistryFriendlyByteBuf packetBuffer)
    {
        packetBuffer.writeInt(input.getState().ordinal());
        packetBuffer.writeUtf(input.getId().toString());
        packetBuffer.writeIdentifier(input.getBranch());
        packetBuffer.writeInt(input.getProgress());
        packetBuffer.writeInt(input.getDepth());
    }

    @Override
    public ILocalResearch deserialize(IFactoryController controller, RegistryFriendlyByteBuf buffer) throws Throwable
    {
        final int state = buffer.readInt();
        final Identifier id = buffer.readIdentifier();
        final Identifier branch = buffer.readIdentifier();
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
