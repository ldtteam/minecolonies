package com.minecolonies.coremod.colony.requestsystem.resolvers.factory;

import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolverFactory;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.coremod.colony.requestsystem.resolvers.DeliveryRequestResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.WarehouseRequestResolver;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

public class WarehouseRequestResolverFactory implements IRequestResolverFactory<WarehouseRequestResolver>
{
    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_TOKEN  = "Token";
    private static final String NBT_LOCATION = "Location";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    @NotNull
    @Override
    public Class<? extends WarehouseRequestResolver> getFactoryOutputType()
    {
        return WarehouseRequestResolver.class;
    }

    @NotNull
    @Override
    public Class<? extends ILocation> getFactoryInputType()
    {
        return ILocation.class;
    }

    @NotNull
    @Override
    public WarehouseRequestResolver getNewInstance(
                                                   @NotNull final IFactoryController factoryController,
                                                   @NotNull final ILocation iLocation,
                                                   @NotNull final Object... context)
      throws IllegalArgumentException
    {
        return new WarehouseRequestResolver(iLocation, factoryController.getNewInstance());
    }

    @NotNull
    @Override
    public NBTTagCompound serialize(
                                     @NotNull final IFactoryController controller, @NotNull final WarehouseRequestResolver warehouseRequestResolver)
    {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag(NBT_TOKEN, controller.serialize(warehouseRequestResolver.getRequesterId()));
        compound.setTag(NBT_LOCATION, controller.serialize(warehouseRequestResolver.getRequesterLocation()));
        return compound;
    }

    @NotNull
    @Override
    public WarehouseRequestResolver deserialize(@NotNull final IFactoryController controller, @NotNull final NBTTagCompound nbt)
    {
        IToken token = controller.deserialize(nbt.getCompoundTag(NBT_TOKEN));
        ILocation location = controller.deserialize(nbt.getCompoundTag(NBT_LOCATION));

        return new WarehouseRequestResolver(location, token);
    }
}
