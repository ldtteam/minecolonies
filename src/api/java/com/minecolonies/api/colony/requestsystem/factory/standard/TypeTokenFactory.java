package com.minecolonies.api.colony.requestsystem.factory.standard;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.NBTTagCompound;
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
    public NBTTagCompound serialize(@NotNull final IFactoryController controller, @NotNull final TypeToken typeToken)
    {
        NBTTagCompound compound = new NBTTagCompound();

        compound.setString(NbtTagConstants.TAG_VALUE, typeToken.getRawType().getName());

        return compound;
    }

    @NotNull
    @Override
    public TypeToken deserialize(@NotNull final IFactoryController controller, @NotNull final NBTTagCompound nbt) throws Throwable
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
}
