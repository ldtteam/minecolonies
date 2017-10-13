package com.minecolonies.coremod.colony.requestsystem.requesters.factories;

import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.coremod.colony.requestsystem.requesters.BuildingBasedRequester;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

public class BuildingBasedRequesterFactory implements IFactory<ILocation, BuildingBasedRequester>
{
    @NotNull
    @Override
    public Class<? extends BuildingBasedRequester> getFactoryOutputType()
    {
        return BuildingBasedRequester.class;
    }

    @NotNull
    @Override
    public Class<? extends ILocation> getFactoryInputType()
    {
        return ILocation.class;
    }

    @NotNull
    @Override
    public BuildingBasedRequester getNewInstance(@NotNull final ILocation iLocation, @NotNull final Object... context) throws IllegalArgumentException
    {
        if (context.length != 1)
            throw new IllegalArgumentException("To many context elements. Only 1 supported. The id of the requester.");

        if (!(context[0] instanceof IToken))
            throw new IllegalArgumentException("Wrong context element type. Supported is: IToken");

        IToken token = (IToken) context[0];
        return new BuildingBasedRequester(iLocation, token);
    }

    @NotNull
    @Override
    public NBTTagCompound serialize(
                                     @NotNull final IFactoryController controller, @NotNull final BuildingBasedRequester output)
    {
        return output.serialize(controller);
    }

    @NotNull
    @Override
    public BuildingBasedRequester deserialize(@NotNull final IFactoryController controller, @NotNull final NBTTagCompound nbt)
    {
        return BuildingBasedRequester.deserialize(controller, nbt);
    }
}
