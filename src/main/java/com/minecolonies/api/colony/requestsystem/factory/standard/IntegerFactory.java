package com.minecolonies.api.colony.requestsystem.factory.standard;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.api.util.constant.SerializationIdentifierConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class IntegerFactory implements IFactory<FactoryVoidInput, Integer>
{
    @NotNull
    @Override
    public TypeToken<? extends Integer> getFactoryOutputType()
    {
        return TypeConstants.INTEGER;
    }

    @NotNull
    @Override
    public TypeToken<? extends FactoryVoidInput> getFactoryInputType()
    {
        return TypeConstants.FACTORYVOIDINPUT;
    }

    @NotNull
    @Override
    public Integer getNewInstance(
      @NotNull final IFactoryController factoryController, @NotNull final FactoryVoidInput factoryVoidInput, @NotNull final Object... context) throws IllegalArgumentException
    {
        return Integer.valueOf(0);
    }

    @NotNull
    @Override
    public CompoundTag serialize(@NotNull final HolderLookup.Provider provider, @NotNull final IFactoryController controller, @NotNull final Integer integer)
    {
        CompoundTag compound = new CompoundTag();

        compound.putInt(NbtTagConstants.TAG_VALUE, integer);

        return compound;
    }

    @NotNull
    @Override
    public Integer deserialize(@NotNull final HolderLookup.Provider provider, @NotNull final IFactoryController controller, @NotNull final CompoundTag nbt)
    {
        return nbt.getInt(NbtTagConstants.TAG_VALUE);
    }

    @Override
    public void serialize(IFactoryController controller, Integer input, RegistryFriendlyByteBuf packetBuffer)
    {
        packetBuffer.writeInt(input);
    }

    @Override
    public Integer deserialize(IFactoryController controller, RegistryFriendlyByteBuf buffer) throws Throwable
    {
        return buffer.readInt();
    }

    @Override
    public short getSerializationId()
    {
        return SerializationIdentifierConstants.INTEGER_FACTORY_ID;
    }
}
