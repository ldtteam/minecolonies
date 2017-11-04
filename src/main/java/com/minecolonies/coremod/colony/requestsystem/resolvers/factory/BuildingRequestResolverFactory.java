package com.minecolonies.coremod.colony.requestsystem.resolvers.factory;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolverFactory;
import com.minecolonies.coremod.colony.requestsystem.resolvers.BuildingRequestResolver;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

/**
 * ------------ Class not Documented ------------
 */
public class BuildingRequestResolverFactory implements IRequestResolverFactory<BuildingRequestResolver>
{
    @NotNull
    @Override
    public TypeToken<? extends BuildingRequestResolver> getFactoryOutputType()
    {
        return null;
    }

    @NotNull
    @Override
    public TypeToken<? extends ILocation> getFactoryInputType()
    {
        return null;
    }

    @NotNull
    @Override
    public BuildingRequestResolver getNewInstance(
                                                   @NotNull final IFactoryController factoryController, @NotNull final ILocation iLocation, @NotNull final Object... context)
      throws IllegalArgumentException
    {
        return null;
    }

    @NotNull
    @Override
    public NBTTagCompound serialize(
                                     @NotNull final IFactoryController controller, @NotNull final BuildingRequestResolver buildingRequestResolver)
    {
        return null;
    }

    @NotNull
    @Override
    public BuildingRequestResolver deserialize(@NotNull final IFactoryController controller, @NotNull final NBTTagCompound nbt)
    {
        return null;
    }
}
