package com.minecolonies.api.colony.requestsystem.factory.standard;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.factory.ITypeOverrideHandler;
import com.minecolonies.api.util.ReflectionUtils;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.CompoundNBT;
import org.jetbrains.annotations.NotNull;

public class TypeTokenFactory implements IFactory<Class, TypeToken>
{
    @NotNull
    @Override
    public TypeToken<? extends TypeToken> getFactoryOutputType()
    {
        return TypeConstants.TYPETOKEN;
    }

    @NotNull
    @Override
    public TypeToken<? extends Class> getFactoryInputType()
    {
        return TypeConstants.CLASS;
    }

    @NotNull
    @Override
    public TypeToken getNewInstance(
      @NotNull final IFactoryController factoryController, @NotNull final Class aClass, @NotNull final Object... context) throws IllegalArgumentException
    {
        return TypeToken.of(aClass);
    }

    @NotNull
    @Override
    public CompoundNBT serialize(@NotNull final IFactoryController controller, @NotNull final TypeToken typeToken)
    {
        CompoundNBT compound = new CompoundNBT();

        compound.putString(NbtTagConstants.TAG_VALUE, typeToken.getRawType().getName());

        return compound;
    }

    @NotNull
    @Override
    public TypeToken deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt) throws Throwable
    {
        try
        {
            return TypeToken.of(Class.forName(nbt.getString(NbtTagConstants.TAG_VALUE)));
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalStateException("Failed to create TypeToken", e);
        }
    }

    public static class TypeTokenSubTypeOverrideHandler implements ITypeOverrideHandler<TypeToken>
    {

        @Override
        public boolean matches(final TypeToken<?> inputType)
        {
            return ReflectionUtils.getSuperClasses(inputType).contains(TypeConstants.TYPETOKEN);
        }

        @Override
        public TypeToken<TypeToken> getOutputType()
        {
            return TypeConstants.TYPETOKEN;
        }
    }
}
