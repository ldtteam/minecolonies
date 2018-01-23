package com.minecolonies.api.colony.requestsystem.factory.standard;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

public class IntegerFactory implements IFactory<FactoryVoidInput,Integer>
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
        return new Integer(0);
    }

    @NotNull
    @Override
    public NBTTagCompound serialize(@NotNull final IFactoryController controller, @NotNull final Integer integer)
    {
        NBTTagCompound compound = new NBTTagCompound();

        compound.setInteger(NbtTagConstants.TAG_VALUE, integer);

        return compound;
    }

    @NotNull
    @Override
    public Integer deserialize(@NotNull final IFactoryController controller, @NotNull final NBTTagCompound nbt)
    {
        return nbt.getInteger(NbtTagConstants.TAG_VALUE);
    }
}
