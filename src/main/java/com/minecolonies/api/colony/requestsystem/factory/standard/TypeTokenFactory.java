package com.minecolonies.api.colony.requestsystem.factory.standard;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.factory.ITypeOverrideHandler;
import com.minecolonies.api.util.ReflectionUtils;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.api.util.constant.SerializationIdentifierConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class TypeTokenFactory implements IFactory<Class<?>, TypeToken<?>>
{
    @NotNull
    @Override
    public TypeToken<? extends TypeToken<?>> getFactoryOutputType()
    {
        return TypeConstants.TYPETOKEN;
    }

    @NotNull
    @Override
    public TypeToken<? extends Class<?>> getFactoryInputType()
    {
        return TypeConstants.CLASS;
    }

    @NotNull
    @Override
    public TypeToken<?> getNewInstance(
      @NotNull final IFactoryController factoryController, @NotNull final Class<?> aClass, @NotNull final Object... context) throws IllegalArgumentException
    {
        return TypeToken.of(aClass);
    }

    @NotNull
    @Override
    public CompoundTag serialize(@NotNull final HolderLookup.Provider provider, @NotNull final IFactoryController controller, @NotNull final TypeToken<?> typeToken)
    {
        CompoundTag compound = new CompoundTag();

        compound.putString(NbtTagConstants.TAG_VALUE, typeToken.getRawType().getName());

        return compound;
    }

    @NotNull
    @Override
    public TypeToken<?> deserialize(@NotNull final HolderLookup.Provider provider, @NotNull final IFactoryController controller, @NotNull final CompoundTag nbt) throws Throwable
    {
        try
        {
            return TypeToken.of(Class.forName(nbt.getString(NbtTagConstants.TAG_VALUE).replace("coremod", "core")));
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalStateException("Failed to create TypeToken", e);
        }
    }

    public static class TypeTokenSubTypeOverrideHandler implements ITypeOverrideHandler<TypeToken<?>>
    {

        @Override
        public boolean matches(final TypeToken<?> inputType)
        {
            return ReflectionUtils.getSuperClasses(inputType).contains(TypeConstants.TYPETOKEN);
        }

        @Override
        public TypeToken<TypeToken<?>> getOutputType()
        {
            return TypeConstants.TYPETOKEN;
        }
    }

    @Override
    public void serialize(IFactoryController controller, TypeToken<?> input, RegistryFriendlyByteBuf packetBuffer)
    {
        packetBuffer.writeUtf(input.getRawType().getName());
    }

    @Override
    public TypeToken<?> deserialize(IFactoryController controller, RegistryFriendlyByteBuf buffer) throws Throwable
    {
        try
        {
            return TypeToken.of(Class.forName(buffer.readUtf(32767).replace("coremod", "core")));
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalStateException("Failed to create TypeToken", e);
        }
    }

    @Override
    public short getSerializationId()
    {
        return SerializationIdentifierConstants.TYPE_TOKEN_ID;
    }
}
